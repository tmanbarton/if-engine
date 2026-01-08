package com.ifengine.command;

import com.ifengine.game.Player;
import com.ifengine.parser.ParsedCommand;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Routes commands to appropriate handlers.
 *
 * This class serves as the central command routing mechanism, taking parsed commands
 * and finding the correct handler to process them. It replaces the large switch
 * statement that was in GameEngine with a more maintainable and extensible approach.
 *
 * <h3>How Command Dispatching Works</h3>
 * <pre>
 * 1. GameEngine receives command: "take key"
 * 2. CommandParser creates ParsedCommand with verb="take", object="key"
 * 3. GameEngine calls dispatcher.handle(player, parsedCommand)
 * 4. Dispatcher looks up "take" in verb-to-handler map
 * 5. Dispatcher calls TakeHandler.handle(player, parsedCommand)
 * 6. TakeHandler processes command and returns response
 * 7. Dispatcher returns response to GameEngine
 * 8. GameEngine sends response to player
 * </pre>
 *
 * <h3>Handler Registration</h3>
 * Handlers register themselves by providing a list of verbs they support:
 * <ul>
 *   <li><b>TakeHandler</b>: ["take", "get", "grab", "pick up"]</li>
 *   <li><b>LookHandler</b>: ["look", "l"]</li>
 *   <li><b>InventoryHandler</b>: ["inventory", "i"]</li>
 * </ul>
 *
 * <h3>Performance Characteristics</h3>
 * <ul>
 *   <li><b>Lookup Time</b>: O(1) - uses ConcurrentHashMap for verb-to-handler mapping</li>
 *   <li><b>Memory Usage</b>: Small overhead - one map entry per supported verb</li>
 *   <li><b>Thread Safety</b>: ConcurrentHashMap allows safe concurrent access</li>
 *   <li><b>Registration Time</b>: O(n) where n is number of verbs per handler</li>
 * </ul>
 *
 * <h3>Error Handling Strategy</h3>
 * The dispatcher itself doesn't handle command errors - it delegates that to handlers.
 * If no handler is found for a verb, it returns Optional.empty(), letting GameEngine
 * decide how to respond.
 *
 * @see BaseCommandHandler
 * @see com.ifengine.game.GameEngine
 * @see com.ifengine.parser.ParsedCommand
 */
public class CommandDispatcher {

  /**
   * Thread-safe map from verb strings to their corresponding handlers.
   *
   * Using ConcurrentHashMap because:
   * <ul>
   *   <li>O(1) lookup performance for command routing</li>
   *   <li>Thread-safe for concurrent player sessions</li>
   *   <li>No locking overhead for reads (which are 99% of operations)</li>
   * </ul>
   *
   * Key: lowercase verb string (e.g., "take", "look", "north")
   * Value: handler instance that can process that verb
   */
  private final Map<String, BaseCommandHandler> verbToHandlerMap = new ConcurrentHashMap<>();

  /**
   * Registry of all registered handlers for management operations.
   *
   * Maintains a separate list for operations that need to work with all handlers
   * (like clearing registry, getting statistics, etc.) without iterating through
   * the verb map.
   */
  private final Set<BaseCommandHandler> registeredHandlers = ConcurrentHashMap.newKeySet();

  /**
   * Creates a new CommandDispatcher with no registered handlers.
   *
   * Handlers must be registered using registerHandler() before they can process commands.
   * This allows for flexible configuration and testing with different handler sets.
   */
  public CommandDispatcher() {
  }

  /**
   * Registers a command handler for a specific verb.
   *
   * This method registers a handler for a single verb. If the verb is already
   * registered to another handler, this method will overwrite the previous
   * registration (last registration wins).
   *
   * <h4>Registration Process:</h4>
   * <ol>
   *   <li>Validate verb and handler are not null/empty</li>
   *   <li>Convert verb to lowercase for consistent matching</li>
   *   <li>Add verb → handler mapping to internal map</li>
   *   <li>Add handler to registered handlers set</li>
   * </ol>
   *
   * <h4>Verb Conflicts:</h4>
   * If a verb is already registered to another handler, the new handler wins.
   * This is intentional to allow overriding default handlers with custom ones.
   *
   * <h4>Example Usage:</h4>
   * <pre>
   * CommandDispatcher dispatcher = new CommandDispatcher();
   * dispatcher.registerHandler("take", new TakeHandler());
   * dispatcher.registerHandler("get", new TakeHandler());  // same handler, different verb
   * dispatcher.registerHandler("look", new LookHandler());
   * </pre>
   *
   * @param verb the verb to register, must not be null or empty
   * @param handler the handler to register, must not be null
   * @throws IllegalArgumentException if verb is null/empty or handler is null
   */
  public void registerHandler(@Nonnull final String verb, @Nonnull final BaseCommandHandler handler) {
    if (verb == null || verb.trim().isEmpty()) {
      throw new IllegalArgumentException("Verb cannot be null or empty");
    }
    if (handler == null) {
      throw new IllegalArgumentException("Handler cannot be null");
    }

    verbToHandlerMap.put(verb.toLowerCase().trim(), handler);
    registeredHandlers.add(handler);
  }

  /**
   * Registers a command handler for all its supported verbs.
   *
   * This method takes a handler and registers it for every verb in its getSupportedVerbs()
   * list. If any verb is already registered to another handler, this method will
   * overwrite the previous registration (last registration wins).
   *
   * <h4>Registration Process:</h4>
   * <ol>
   *   <li>Get list of supported verbs from handler</li>
   *   <li>Convert each verb to lowercase for consistent matching</li>
   *   <li>Add verb → handler mapping to internal map</li>
   *   <li>Add handler to registered handlers set</li>
   * </ol>
   *
   * <h4>Verb Conflicts:</h4>
   * If two handlers support the same verb, the last one registered wins.
   * This is intentional to allow overriding default handlers with custom ones.
   *
   * <h4>Example Usage:</h4>
   * <pre>
   * CommandDispatcher dispatcher = new CommandDispatcher();
   * dispatcher.registerHandler(new TakeHandler());  // registers: take, get, grab
   * dispatcher.registerHandler(new LookHandler());  // registers: look, examine, l
   * </pre>
   *
   * @param handler the handler to register, must not be null
   * @throws IllegalArgumentException if handler is null or returns null/empty verb list
   */
  public void registerHandler(@Nonnull final BaseCommandHandler handler) {
    if (handler == null) {
      throw new IllegalArgumentException("Handler cannot be null");
    }

    final List<String> supportedVerbs = handler.getSupportedVerbs();
    if (supportedVerbs == null || supportedVerbs.isEmpty()) {
      throw new IllegalArgumentException("Handler must support at least one verb");
    }

    for (final String verb : supportedVerbs) {
      if (verb != null && !verb.trim().isEmpty()) {
        registerHandler(verb, handler);
      }
    }
  }

  /**
   * Routes a command to the appropriate handler and returns the response.
   *
   * This is the main entry point for command processing. It takes a parsed command,
   * finds the appropriate handler based on the verb, and delegates processing to
   * that handler.
   *
   * <h4>Processing Steps:</h4>
   * <ol>
   *   <li>Extract verb from parsed command</li>
   *   <li>Convert verb to lowercase for case-insensitive matching</li>
   *   <li>Look up handler in verb-to-handler map</li>
   *   <li>If handler found, call handler.handle() and return result</li>
   *   <li>If no handler found, return Optional.empty()</li>
   * </ol>
   *
   * <h4>Error Handling:</h4>
   * <ul>
   *   <li><b>Null Parameters</b>: Returns Optional.empty() rather than throwing</li>
   *   <li><b>Unknown Verbs</b>: Returns Optional.empty() to let caller decide response</li>
   *   <li><b>Handler Errors</b>: Passes through handler's Optional result</li>
   * </ul>
   *
   * <h4>Performance Notes:</h4>
   * <ul>
   *   <li>O(1) lookup time using ConcurrentHashMap</li>
   *   <li>No synchronization overhead for reads</li>
   *   <li>Thread-safe for concurrent player sessions</li>
   * </ul>
   *
   * @param player the player executing the command, may be null
   * @param command the parsed command to process, may be null
   * @return Optional containing response if handler found and processed command, empty otherwise
   */
  @Nonnull
  public Optional<String> handle(@Nullable final Player player, @Nullable final ParsedCommand command) {
    if (player == null || command == null) {
      return Optional.empty();
    }

    final String verb = command.getVerb();
    if (verb == null || verb.trim().isEmpty()) {
      return Optional.empty();
    }

    final BaseCommandHandler handler = verbToHandlerMap.get(verb.toLowerCase().trim());
    if (handler == null) {
      return Optional.empty();
    }

    final String result = handler.handle(player, command);
    return Optional.ofNullable(result);
  }

  /**
   * Dispatches a command to the appropriate handler and returns the response.
   *
   * This method is an alias for handle() that returns the response directly
   * rather than wrapped in Optional. Used by tests that expect direct String returns.
   *
   * @param player the player executing the command, may be null
   * @param command the parsed command to process, may be null
   * @return response string if handler found and processed command, null otherwise
   */
  public String dispatch(@Nullable final Player player, @Nullable final ParsedCommand command) {
    final Optional<String> result = handle(player, command);
    return result.orElse(null);
  }

  /**
   * Gets all verbs currently registered in the dispatcher.
   *
   * Returns an immutable set of all verb strings that have registered handlers.
   * Useful for debugging, testing, and displaying available commands to players.
   *
   * <h4>Use Cases:</h4>
   * <ul>
   *   <li>Help system showing available commands</li>
   *   <li>Testing to verify all expected verbs are registered</li>
   *   <li>Debugging handler registration issues</li>
   *   <li>Command auto-completion features</li>
   * </ul>
   *
   * @return immutable set of all registered verb strings, never null
   */
  @Nonnull
  public Set<String> getRegisteredVerbs() {
    return Collections.unmodifiableSet(verbToHandlerMap.keySet());
  }

  /**
   * Gets all handlers currently registered in the dispatcher.
   *
   * Returns an immutable set of all handler instances that have been registered.
   * Useful for testing and debugging handler registration.
   *
   * @return immutable set of all registered handlers, never null
   */
  @Nonnull
  public Set<BaseCommandHandler> getRegisteredHandlers() {
    return Collections.unmodifiableSet(registeredHandlers);
  }

  /**
   * Removes a specific verb mapping from the dispatcher.
   *
   * This method removes the handler mapping for a specific verb. If the handler
   * for this verb has no other verb mappings, it will also be removed from the
   * registered handlers set.
   *
   * <h4>Use Cases:</h4>
   * <ul>
   *   <li>Testing with different handler configurations</li>
   *   <li>Dynamic verb replacement at runtime</li>
   *   <li>Disabling specific commands temporarily</li>
   * </ul>
   *
   * @param verb the verb to unregister, may be null (no-op if null)
   * @return true if verb was found and removed, false otherwise
   */
  public boolean unregisterHandler(final String verb) {
    if (verb == null || verb.trim().isEmpty()) {
      return false;
    }

    final String normalizedVerb = verb.toLowerCase().trim();
    final BaseCommandHandler removedHandler = verbToHandlerMap.remove(normalizedVerb);

    if (removedHandler == null) {
      return false;
    }

    final boolean handlerStillUsed = verbToHandlerMap.containsValue(removedHandler);
    if (!handlerStillUsed) {
      registeredHandlers.remove(removedHandler);
    }

    return true;
  }

  /**
   * Removes a specific handler and all its verb mappings.
   *
   * This method removes all verb → handler mappings for the specified handler
   * and removes the handler from the registered handlers set.
   *
   * <h4>Use Cases:</h4>
   * <ul>
   *   <li>Testing with different handler configurations</li>
   *   <li>Dynamic handler replacement at runtime</li>
   *   <li>Disabling specific commands temporarily</li>
   * </ul>
   *
   * @param handler the handler to remove, may be null (no-op if null)
   * @return true if handler was found and removed, false otherwise
   */
  public boolean unregisterHandler(final BaseCommandHandler handler) {
    if (handler == null) {
      return false;
    }

    final boolean wasRegistered = registeredHandlers.remove(handler);
    verbToHandlerMap.entrySet().removeIf(entry -> entry.getValue() == handler);

    return wasRegistered;
  }

  /**
   * Removes all registered handlers and clears all verb mappings.
   *
   * After calling this method, the dispatcher will not route any commands until
   * new handlers are registered. Useful for testing and reconfiguration.
   */
  public void clearAllHandlers() {
    verbToHandlerMap.clear();
    registeredHandlers.clear();
  }

  /**
   * Gets the number of registered verb mappings.
   *
   * Note that this may be larger than the number of registered handlers since
   * each handler can register multiple verbs.
   *
   * @return number of verb → handler mappings
   */
  public int getVerbCount() {
    return verbToHandlerMap.size();
  }

  /**
   * Gets the number of registered handlers.
   *
   * @return number of unique handlers registered
   */
  public int getHandlerCount() {
    return registeredHandlers.size();
  }

  /**
   * Checks if a specific verb has a registered handler.
   *
   * @param verb the verb to check, may be null
   * @return true if verb has a registered handler, false otherwise
   */
  public boolean hasHandlerForVerb(final String verb) {
    if (verb == null) {
      return false;
    }
    return verbToHandlerMap.containsKey(verb.toLowerCase().trim());
  }

  /**
   * Gets the handler registered for a specific verb.
   *
   * @param verb the verb to look up, may be null
   * @return the handler for this verb, or null if no handler registered
   */
  public BaseCommandHandler getHandlerForVerb(final String verb) {
    if (verb == null) {
      return null;
    }
    return verbToHandlerMap.get(verb.toLowerCase().trim());
  }

  /**
   * Gets the handler registered for a specific verb.
   * Alias for getHandlerForVerb() used by tests.
   *
   * @param verb the verb to look up, may be null
   * @return the handler for this verb, or null if no handler registered
   */
  public BaseCommandHandler getHandler(final String verb) {
    return getHandlerForVerb(verb);
  }

  /**
   * Checks if a specific verb has a registered handler.
   * Alias for hasHandlerForVerb() used by tests.
   *
   * @param verb the verb to check, may be null
   * @return true if verb has a registered handler, false otherwise
   */
  public boolean isHandlerRegistered(final String verb) {
    return hasHandlerForVerb(verb);
  }
}
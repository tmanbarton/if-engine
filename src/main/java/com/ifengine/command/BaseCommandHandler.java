package com.ifengine.command;

import com.ifengine.game.Player;
import com.ifengine.parser.ParsedCommand;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Base interface for all command handlers in the unified command framework.
 *
 * This interface replaces the large switch statement in GameEngine with a more
 * maintainable and extensible command handling system. Instead of a single monolithic
 * method handling all commands, each command type gets its own handler that implements
 * this interface.
 *
 * <h3>Why This Pattern?</h3>
 * The previous switch statement approach had several problems:
 * <ul>
 *   <li>200+ lines of tightly coupled command logic in one method</li>
 *   <li>Difficult to add new commands without modifying core GameEngine</li>
 *   <li>Hard to test individual command behaviors in isolation</li>
 *   <li>Inconsistent object resolution and error handling patterns</li>
 * </ul>
 *
 * This handler-based approach provides:
 * <ul>
 *   <li><b>Separation of Concerns</b>: Each command has its own focused handler</li>
 *   <li><b>Easy Extensibility</b>: New commands = new handler + registration</li>
 *   <li><b>Better Testability</b>: Each handler can be tested independently</li>
 *   <li><b>Consistent Patterns</b>: Unified object resolution and error handling</li>
 * </ul>
 *
 * <h3>How Command Routing Works</h3>
 * <pre>
 * 1. Player types command: "take key"
 * 2. CommandParser creates ParsedCommand object
 * 3. CommandDispatcher looks up handler for verb "take"
 * 4. TakeHandler.handle() method is called
 * 5. Handler processes command and returns response
 * </pre>
 *
 * <h3>Handler Categories</h3>
 * Different types of commands inherit from specialized base classes:
 * <ul>
 *   <li><b>ObjectCommandHandler</b>: Commands that work on items/scenery (take, drop, look)</li>
 *   <li><b>MovementCommandHandler</b>: Direction commands (go, north, south)</li>
 *   <li><b>SystemCommandHandler</b>: System commands (inventory, status, quit)</li>
 *   <li><b>SceneryCommandHandler</b>: Scenery-only commands (climb, break)</li>
 * </ul>
 *
 * <h3>Implementation Requirements</h3>
 * All handlers must:
 * <ul>
 *   <li>Return meaningful responses for both success and failure cases</li>
 *   <li>Handle null parameters gracefully (return error responses, not exceptions)</li>
 *   <li>Be stateless (no instance variables that change between calls)</li>
 *   <li>Integrate properly with existing object resolution and error handling</li>
 * </ul>
 *
 * @see com.ifengine.command.CommandDispatcher
 * @see com.ifengine.parser.ParsedCommand
 * @see com.ifengine.game.Player
 */
public interface BaseCommandHandler {

  /**
   * Returns the list of verbs that this handler can process.
   *
   * This method is used by the CommandDispatcher during registration to build
   * the verb-to-handler mapping. Each verb should only be handled by one handler
   * to avoid conflicts.
   *
   * <h4>Examples:</h4>
   * <ul>
   *   <li>TakeHandler: ["take", "get", "grab", "pick up"]</li>
   *   <li>LookHandler: ["look", "l"]</li>
   *   <li>InventoryHandler: ["inventory", "i"]</li>
   *   <li>NorthHandler: ["north", "n"]</li>
   * </ul>
   *
   * <h4>Important Notes:</h4>
   * <ul>
   *   <li>All verbs should be lowercase for consistent matching</li>
   *   <li>Include common aliases and abbreviations users expect</li>
   *   <li>Don't include verbs that conflict with other handlers</li>
   *   <li>Return an immutable list to prevent external modification</li>
   * </ul>
   *
   * @return immutable list of verbs this handler supports, never null or empty
   */
  @Nonnull
  List<String> getSupportedVerbs();

  /**
   * Processes a command and returns the appropriate response.
   *
   * This is the core method where command-specific logic is implemented. The handler
   * receives a parsed command and player state, then returns a response string that
   * will be displayed to the player.
   *
   * <h4>Parameter Handling:</h4>
   * <ul>
   *   <li><b>Null Parameters</b>: Should return error messages, never throw exceptions</li>
   *   <li><b>Invalid Commands</b>: Return helpful error messages explaining the problem</li>
   *   <li><b>Player State</b>: Use player.getCurrentLocation(), player.getInventory(), etc.</li>
   *   <li><b>Command Objects</b>: Use command.getDirectObjects(), command.getPreposition(), etc.</li>
   * </ul>
   *
   * <h4>Response Guidelines:</h4>
   * <ul>
   *   <li>Use ResponseProvider for consistent messaging</li>
   *   <li>Include context about what went wrong and how to fix it for actual errors</li>
   * </ul>
   *
   * <h4>State Management:</h4>
   * <ul>
   *   <li>Handlers should be stateless - no instance variables that change</li>
   *   <li>All state changes go through the Player object (location, inventory, etc.)</li>
   *   <li>Use immutable data where possible</li>
   *   <li>Don't cache references to mutable objects</li>
   * </ul>
   *
   * @param player the player executing the command, never null
   * @param command the parsed command to process, never null
   * @return String containing response message
   */
  @Nonnull
  String handle(@Nonnull Player player, @Nonnull ParsedCommand command);

  /**
   * Determines whether this handler can process the given verb.
   *
   * This is a convenience method that checks if the verb is in the list of
   * supported verbs. The CommandDispatcher uses this during command routing
   * to find the appropriate handler for each command.
   *
   * <h4>Implementation Note:</h4>
   * Most handlers can use the default implementation that just checks if the verb
   * is in getSupportedVerbs(). Override this method only if you need custom logic
   * for verb matching (e.g., pattern matching, dynamic verb support).
   *
   * <h4>Performance Consideration:</h4>
   * This method may be called frequently during command processing, so it should
   * be fast. If you override with complex logic, consider caching results.
   *
   * @param verb the command verb to check, may be null
   * @return true if this handler can process the verb, false otherwise
   */
  default boolean canHandle(final String verb) {
    if (verb == null) {
      return false;
    }
    return getSupportedVerbs().contains(verb.toLowerCase());
  }
}
package com.ifengine.game;

import com.ifengine.Direction;
import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.OpenResult;
import com.ifengine.Openable;
import com.ifengine.UnlockResult;
import com.ifengine.command.CommandDispatcher;
import com.ifengine.command.handlers.ClimbHandler;
import com.ifengine.command.handlers.DrinkHandler;
import com.ifengine.command.handlers.DropHandler;
import com.ifengine.command.handlers.EatHandler;
import com.ifengine.command.handlers.HintHandler;
import com.ifengine.command.handlers.KickHandler;
import com.ifengine.command.handlers.LookHandler;
import com.ifengine.command.handlers.OpenHandler;
import com.ifengine.command.handlers.PunchHandler;
import com.ifengine.command.handlers.PutHandler;
import com.ifengine.command.handlers.ReadHandler;
import com.ifengine.command.handlers.SwimHandler;
import com.ifengine.command.handlers.SystemCommandHandler;
import com.ifengine.command.handlers.TakeHandler;
import com.ifengine.command.handlers.UnlockHandler;
import com.ifengine.constants.GameConstants;
import com.ifengine.parser.CommandParser;
import com.ifengine.parser.CommandType;
import com.ifengine.parser.ContextManager;
import com.ifengine.parser.ObjectResolver;
import com.ifengine.parser.ParsedCommand;
import com.ifengine.parser.VocabularyManager;
import com.ifengine.response.DefaultResponses;
import com.ifengine.response.ResponseProvider;
import com.ifengine.util.DirectionHelper;
import com.ifengine.util.LocationItemFormatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core game engine handling commands, player sessions, and game flow.
 * <p>
 * The GameEngine orchestrates all game functionality:
 * <ul>
 *   <li>Session management for multiple concurrent players</li>
 *   <li>Command parsing and routing to handlers</li>
 *   <li>Movement between locations</li>
 *   <li>Game state management (intro, playing, confirmations)</li>
 *   <li>JSON response generation</li>
 * </ul>
 */
public class GameEngine {

  private GameMapInterface gameMap;
  private final ConcurrentHashMap<String, Player> players = new ConcurrentHashMap<>();

  // Tracks boldable text (location descriptions) per session for JSON response
  private final ConcurrentHashMap<String, String> sessionBoldableText = new ConcurrentHashMap<>();

  private final CommandParser commandParser;
  private final ObjectResolver objectResolver;
  private final VocabularyManager vocabularyManager;
  private final ContextManager contextManager;
  private final SceneryInteractionHandler sceneryInteractionHandler;
  private final CommandDispatcher commandDispatcher;
  private final ResponseProvider responseProvider;

  /**
   * Creates a GameEngine with a custom game map and default responses.
   *
   * @param gameMap the game map to use
   */
  public GameEngine(@Nonnull final GameMapInterface gameMap) {
    this(gameMap, new DefaultResponses());
  }

  /**
   * Creates a GameEngine with a custom game map and custom responses.
   *
   * @param gameMap the game map to use
   * @param responseProvider the response provider for customized text
   */
  public GameEngine(@Nonnull final GameMapInterface gameMap, @Nonnull final ResponseProvider responseProvider) {
    Objects.requireNonNull(gameMap, "gameMap cannot be null");
    Objects.requireNonNull(responseProvider, "responseProvider cannot be null");

    this.gameMap = gameMap;
    this.responseProvider = responseProvider;

    // Initialize parser components
    this.commandParser = new CommandParser();
    this.objectResolver = commandParser.getObjectResolver();
    this.vocabularyManager = commandParser.getVocabularyManager();
    this.contextManager = new ContextManager();
    this.sceneryInteractionHandler = new SceneryInteractionHandler();

    // Initialize command dispatcher and register handlers
    this.commandDispatcher = new CommandDispatcher();
    registerCommandHandlers();
  }

  /**
   * Registers all command handlers with the dispatcher.
   * <p>
   * Handlers are registered here to centralize command routing configuration.
   * Dependencies are injected into each handler for loose coupling.
   */
  private void registerCommandHandlers() {
    commandDispatcher.registerHandler(new TakeHandler(objectResolver, sceneryInteractionHandler, contextManager, responseProvider));
    commandDispatcher.registerHandler(new DropHandler(objectResolver, contextManager, responseProvider));
    commandDispatcher.registerHandler(new PutHandler(objectResolver, responseProvider));
    commandDispatcher.registerHandler(new LookHandler(objectResolver, sceneryInteractionHandler, contextManager, responseProvider));
    commandDispatcher.registerHandler(new ReadHandler(responseProvider, objectResolver, contextManager));
    commandDispatcher.registerHandler(new UnlockHandler(objectResolver, contextManager, gameMap, responseProvider));
    commandDispatcher.registerHandler(new OpenHandler(objectResolver, contextManager, gameMap, responseProvider));
    commandDispatcher.registerHandler(new ClimbHandler(responseProvider));
    commandDispatcher.registerHandler(new PunchHandler(responseProvider));
    commandDispatcher.registerHandler(new KickHandler(responseProvider));
    commandDispatcher.registerHandler(new DrinkHandler(responseProvider));
    commandDispatcher.registerHandler(new SwimHandler(responseProvider));
    commandDispatcher.registerHandler(new EatHandler(contextManager, sceneryInteractionHandler, responseProvider));
    commandDispatcher.registerHandler(createHintHandler());
    commandDispatcher.registerHandler(new SystemCommandHandler(responseProvider));
  }

  /**
   * Creates a HintHandler with configuration from the GameMap, if available.
   *
   * @return a configured HintHandler
   */
  @Nonnull
  private HintHandler createHintHandler() {
    if (gameMap instanceof GameMap map) {
      final HintConfiguration hintConfig = map.getHintConfiguration();
      return new HintHandler(hintConfig, gameMap);
    }
    return new HintHandler(null, gameMap);
  }

  /**
   * Processes a command from a player session.
   *
   * @param sessionId the unique session identifier
   * @param command the raw command string from the player
   * @return JSON response with message and game state
   */
  @Nonnull
  public String processCommand(@Nonnull final String sessionId, @Nonnull final String command) {
    // Clear boldable text at start of each command
    sessionBoldableText.remove(sessionId);

    // Get or create player for this session
    final Player player = players.computeIfAbsent(sessionId, id -> {
      final Player newPlayer = new Player(gameMap.getStartingLocation());
      // Mark starting location as visited for new players
      newPlayer.getCurrentLocation().setVisited(true);
      // Check if intro should be skipped
      if (gameMap instanceof GameMap map && map.shouldSkipIntro()) {
        newPlayer.setGameState(GameState.PLAYING);
      }
      return newPlayer;
    });

    // Set sessionId on player so downstream methods can access it
    player.setSessionId(sessionId);

    final String responseMessage;

    // Handle state-specific command processing
    if (player.getGameState() == GameState.WAITING_FOR_START_ANSWER) {
      responseMessage = handleStartAnswer(player, command.trim());
    } else if (player.getGameState() == GameState.WAITING_FOR_RESTART_CONFIRMATION) {
      responseMessage = handleRestartConfirmation(player, command.trim());
    } else if (player.getGameState() == GameState.WAITING_FOR_QUIT_CONFIRMATION) {
      responseMessage = handleQuitConfirmation(player, command.trim());
    } else if (player.getGameState() == GameState.WAITING_FOR_UNLOCK_CODE) {
      responseMessage = handleUnlockCodeInput(player, command.trim());
    } else if (player.getGameState() == GameState.WAITING_FOR_OPEN_CODE) {
      responseMessage = handleOpenCodeInput(player, command.trim());
    } else {
      // Parse command using the parser
      final ParsedCommand parsedCommand = commandParser.parseCommand(command, sessionId, player);

      // Handle empty or unrecognized commands
      if (parsedCommand.getVerb().isEmpty()) {
        responseMessage = responseProvider.getCommandNotUnderstood(command.trim());
      } else {
        // Process commands using the parsed structure
        responseMessage = processGameCommand(player, parsedCommand);
      }
    }

    // Add consistent blank line to all responses for better visual spacing
    final String formattedResponse = responseMessage.isEmpty() ? responseMessage : responseMessage + "\n\n";

    // Create structured JSON response with game state and valid directions
    return createJsonResponse(sessionId, formattedResponse, player);
  }

  @Nonnull
  private String createJsonResponse(@Nonnull final String sessionId, @Nonnull final String message, @Nonnull final Player player) {
    // Get valid directions from player's current location
    final List<String> validDirections = DirectionHelper.getDirectionWordsFromSet(
        player.getCurrentLocation().getAvailableDirections());

    // Get boldable text (location description) for this session, if set
    final String boldableText = sessionBoldableText.get(sessionId);
    final String boldableTextJson = boldableText != null
        ? "\"" + escapeJsonString(boldableText) + "\""
        : "null";

    // Create a JSON response with message, boldable text, game state, and valid directions
    return String.format("""
        {
          "type": "game_response",
          "message": "%s",
          "boldableText": %s,
          "gameState": "%s",
          "validDirections": %s
        }""",
        escapeJsonString(message),
        boldableTextJson,
        player.getGameState().name(),
        toJsonArray(validDirections)
    );
  }

  @Nonnull
  private String toJsonArray(@Nonnull final List<String> elements) {
    if (elements.isEmpty()) {
      return "[]";
    }
    return "[\"" + String.join("\", \"", elements) + "\"]";
  }

  @Nonnull
  private String escapeJsonString(@Nonnull final String input) {
    return input
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }

  @Nonnull
  private String processGameCommand(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    // Handle sequence commands by executing each command in order
    if (command.getType() == CommandType.SEQUENCE && command.hasSequenceCommands()) {
      final StringBuilder result = new StringBuilder();

      // Execute first command
      final String firstResult = executeSingleCommand(player, command);
      result.append(firstResult);

      // Execute remaining commands in sequence
      for (final String remainingCommand : command.getSequenceCommands()) {
        final ParsedCommand nextCommand = commandParser.parseCommand(remainingCommand, player.getSessionId(), player);
        final String nextResult = executeSingleCommand(player, nextCommand);

        // Add separator and next result
        result.append("\n\n").append(nextResult);
      }

      return result.toString();
    }

    // Regular single command processing
    return executeSingleCommand(player, command);
  }

  /**
   * Executes a single command for the given player.
   * <p>
   * This method performs semantic validation of verb-preposition combinations before
   * executing the command. Commands are routed through the CommandDispatcher to handlers,
   * or handled directly for movement and stateful commands.
   *
   * @param player the player executing the command
   * @param command the parsed command to execute
   * @return the result message to display to the player
   */
  private String executeSingleCommand(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    final String verb = command.getVerb();

    // Validate verb-preposition combinations semantically
    if (command.hasPreposition()) {
      final String preposition = command.getPreposition();
      if (!vocabularyManager.isValidVerbPrepositionCombination(verb, preposition)) {
        return responseProvider.getVerbPrepositionInvalid();
      }
    }

    // Check if this is a bare "look" command - set boldable text
    if (isLookAtLocationCommand(verb, command)) {
      final String description = getLocationDescription(player.getCurrentLocation(), true);
      sessionBoldableText.put(player.getSessionId(), description);
    }

    // Try command dispatcher first (for refactored commands)
    final java.util.Optional<String> dispatcherResult = commandDispatcher.handle(player, command);
    if (dispatcherResult.isPresent()) {
      return dispatcherResult.get();
    }

    // Switch statement for stateful commands and movement
    return switch (verb) {
      case "go", "north", "n", "south", "s", "east", "e", "west", "w", "up", "u", "down", "d",
          "northeast", "ne", "northwest", "nw", "southeast", "se", "southwest", "sw", "in", "out" ->
          handleMovement(player, command);
      case "restart", "reset" -> {
        player.setGameState(GameState.WAITING_FOR_RESTART_CONFIRMATION);
        yield responseProvider.getRestartConfirmation();
      }
      case "quit", "exit" -> {
        player.setGameState(GameState.WAITING_FOR_QUIT_CONFIRMATION);
        yield responseProvider.getQuitConfirmation();
      }
      default -> handleUnknownCommand(player, command);
    };
  }

  /**
   * Checks if this is a bare location look command (look without targeting an item/scenery).
   * Bare look commands include: "look", "l", "look around"
   */
  private boolean isLookAtLocationCommand(@Nonnull final String verb, @Nonnull final ParsedCommand command) {
    if (!verb.equals("look") && !verb.equals("l")) {
      return false;
    }
    // Check for indirect objects
    if (!command.getIndirectObjects().isEmpty()) {
      return false;
    }
    // No direct objects means "look" command for location
    if (command.getDirectObjects().isEmpty()) {
      return true;
    }
    // "look around" is also looking at location
    final String firstObject = command.getFirstDirectObject();
    return "around".equalsIgnoreCase(firstObject);
  }

  @Nonnull
  private String handleMovement(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    final String verb = command.getVerb();

    // Handle direct direction commands
    if (isDirectionVerb(verb)) {
      return move(player, verb, "");
    }

    // Handle "go <direction>" format
    if (Objects.equals("go", verb) && !command.getDirectObjects().isEmpty()) {
      final String direction = vocabularyManager.normalizeDirection(command.getFirstDirectObject());
      return move(player, direction, "");
    }

    return responseProvider.getGoNoDirectionSpecified();
  }

  private boolean isDirectionVerb(@Nonnull final String verb) {
    return GameConstants.DIRECTIONS.contains(verb);
  }

  /**
   * Checks if a word is a pronoun that should trigger context resolution.
   */
  public boolean isPronoun(@Nonnull final String word) {
    return contextManager.isPronoun(word);
  }

  private boolean isYesAnswer(@Nonnull final String answer) {
    final String lowerAnswer = answer.toLowerCase();
    return lowerAnswer.equals("yes") || lowerAnswer.equals("y") || lowerAnswer.equals("yeah") ||
        lowerAnswer.equals("yep") || lowerAnswer.equals("sure");
  }

  private boolean isNoAnswer(@Nonnull final String answer) {
    final String lowerAnswer = answer.toLowerCase();
    return lowerAnswer.equals("no") || lowerAnswer.equals("n") || lowerAnswer.equals("nah") ||
        lowerAnswer.equals("nope") || lowerAnswer.equals("no thanks");
  }

  /**
   * Handles the initial "have you played before?" question response.
   * Shows appropriate intro based on answer and transitions to gameplay.
   * If a custom intro handler is configured, delegates to it instead.
   */
  @Nonnull
  private String handleStartAnswer(@Nonnull final Player player, @Nonnull final String answer) {
    // Check for custom intro handler
    if (gameMap instanceof GameMap map && map.hasCustomIntroHandler()) {
      final IntroHandler handler = map.getIntroHandler();
      final IntroResult result = handler.handle(player, answer, gameMap);
      if (result.transitionToPlaying()) {
        player.setGameState(GameState.PLAYING);
      }
      return result.message();
    }

    // Check for custom intro message with simple yes/no responses
    if (gameMap instanceof GameMap map && map.hasCustomIntroMessage()) {
      if (isYesAnswer(answer)) {
        player.setGameState(GameState.PLAYING);
        return map.getCustomYesResponse();
      } else if (isNoAnswer(answer)) {
        player.setGameState(GameState.PLAYING);
        return map.getCustomNoResponse();
      } else {
        return responseProvider.getPleaseAnswerQuestion();
      }
    }

    // Default yes/no handling
    if (isYesAnswer(answer)) {
      // Yes - they have played before
      player.setExperiencedPlayer(true);
      player.setGameState(GameState.PLAYING);
      return responseProvider.getExperiencedPlayerIntro() + "\n\n" + lookAtLocation(player, true);
    } else if (isNoAnswer(answer)) {
      // No - they haven't played before
      player.setExperiencedPlayer(false);
      player.setGameState(GameState.PLAYING);
      return responseProvider.getNewPlayerIntro() + "\n\n" + lookAtLocation(player, true);
    } else {
      return responseProvider.getPleaseAnswerQuestion();
    }
  }

  @Nonnull
  private String handleRestartConfirmation(@Nonnull final Player player, @Nonnull final String answer) {
    if (isYesAnswer(answer)) {
      player.setGameState(GameState.PLAYING);
      return restartGame(player.getSessionId());
    } else if (isNoAnswer(answer)) {
      player.setGameState(GameState.PLAYING);
      return responseProvider.getRestartCancelled();
    } else {
      return responseProvider.getPleaseAnswerQuestion();
    }
  }

  @Nonnull
  private String handleQuitConfirmation(@Nonnull final Player player, @Nonnull final String answer) {
    if (isYesAnswer(answer)) {
      // Reset game state to initial conditions
      resetGameState(player.getSessionId());

      // Set state to waiting for start answer to restart the intro flow
      player.setGameState(GameState.WAITING_FOR_START_ANSWER);
      // Return the intro question to restart the game
      return responseProvider.getHaveYouPlayedBeforeQuestion();
    } else if (isNoAnswer(answer)) {
      player.setGameState(GameState.PLAYING);
      return responseProvider.getQuitCancelled();
    } else {
      return responseProvider.getPleaseAnswerQuestion();
    }
  }

  /**
   * Handles player input when waiting for an unlock code.
   * Passes the input to the pending openable's tryUnlock method.
   *
   * @param player the player providing the code
   * @param input the code/word input from the player
   * @return the result message from the unlock attempt
   */
  @Nonnull
  private String handleUnlockCodeInput(@Nonnull final Player player, @Nonnull final String input) {
    final Openable pendingOpenable = player.getPendingOpenable();
    if (pendingOpenable == null) {
      player.setGameState(GameState.PLAYING);
      return responseProvider.getCommandNotUnderstood(input);
    }

    player.clearPendingOpenable();
    player.setGameState(GameState.PLAYING);

    final UnlockResult result = pendingOpenable.tryUnlock(player, input, gameMap);
    return result.message();
  }

  /**
   * Handles player input when waiting for an open code.
   * Passes the input to the pending openable's tryOpen method.
   *
   * @param player the player providing the code
   * @param input the code/word input from the player
   * @return the result message from the open attempt
   */
  @Nonnull
  private String handleOpenCodeInput(@Nonnull final Player player, @Nonnull final String input) {
    final Openable pendingOpenable = player.getPendingOpenable();
    if (pendingOpenable == null) {
      player.setGameState(GameState.PLAYING);
      return responseProvider.getCommandNotUnderstood(input);
    }

    player.clearPendingOpenable();
    player.setGameState(GameState.PLAYING);

    final OpenResult result = pendingOpenable.tryOpen(player, input, gameMap);
    return result.message();
  }

  @Nonnull
  private String look(@Nonnull final Player player, @Nonnull final String object) {
    if (object.isEmpty()) {
      // Look at current location - always use long description for explicit look command
      return lookAtLocation(player, true);
    } else {
      // Look at specific object
      // First check inventory
      final Item item = player.getInventoryItemByName(object);
      if (item != null) {
        return item.getDetailedDescription();
      }

      // Then check location items
      final Location location = player.getCurrentLocation();
      final Item locationItem = location.getItems().stream()
          .filter(i -> i.matchesName(object))
          .findFirst()
          .orElse(null);

      if (locationItem != null) {
        return locationItem.getDetailedDescription();
      }

      return responseProvider.getLookAtObjectNotPresent(object);
    }
  }

  @Nonnull
  private String getLocationDescription(@Nonnull final Location location, final boolean useLongDescription) {
    return useLongDescription ? location.getLongDescription() : location.getShortDescription();
  }

  @Nonnull
  private String move(@Nonnull final Player player, @Nonnull final String verb, @Nonnull final String object) {
    Direction direction;

    // Handle direct direction commands
    if (object.isEmpty()) {
      direction = switch (verb.toLowerCase()) {
        case "north", "n" -> Direction.NORTH;
        case "south", "s" -> Direction.SOUTH;
        case "east", "e" -> Direction.EAST;
        case "west", "w" -> Direction.WEST;
        case "up", "u" -> Direction.UP;
        case "down", "d" -> Direction.DOWN;
        case "northeast", "ne" -> Direction.NORTHEAST;
        case "northwest", "nw" -> Direction.NORTHWEST;
        case "southeast", "se" -> Direction.SOUTHEAST;
        case "southwest", "sw" -> Direction.SOUTHWEST;
        case "in" -> Direction.IN;
        case "out" -> Direction.OUT;
        default -> null;
      };
    } else {
      // Handle "go <direction>" format
      direction = switch (object.toLowerCase()) {
        case "north", "n" -> Direction.NORTH;
        case "south", "s" -> Direction.SOUTH;
        case "east", "e" -> Direction.EAST;
        case "west", "w" -> Direction.WEST;
        case "up", "u" -> Direction.UP;
        case "down", "d" -> Direction.DOWN;
        case "northeast", "ne" -> Direction.NORTHEAST;
        case "northwest", "nw" -> Direction.NORTHWEST;
        case "southeast", "se" -> Direction.SOUTHEAST;
        case "southwest", "sw" -> Direction.SOUTHWEST;
        default -> null;
      };
    }

    if (direction == null) {
      return responseProvider.getDirectionNotUnderstood(verb);
    }

    final Location currentLocation = player.getCurrentLocation();
    final Location newLocation = currentLocation.getConnection(direction);

    if (newLocation == null) {
      return responseProvider.getCantGoThatWay();
    }

    // Check if this is the first visit to determine description type
    final boolean firstVisit = !newLocation.isVisited();

    // Mark location as visited and move player
    newLocation.setVisited(true);
    player.setCurrentLocation(newLocation);

    // Show description based on visit status
    return lookAtLocation(player, firstVisit);
  }

  @Nonnull
  private String lookAtLocation(@Nonnull final Player player, final boolean useLongDescription) {
    final Location location = player.getCurrentLocation();
    final StringBuilder sb = new StringBuilder();

    // Use appropriate description based on visit status
    final String description = getLocationDescription(location, useLongDescription);
    sb.append(description);

    // Set boldable text to ONLY the location description (not item listing)
    sessionBoldableText.put(player.getSessionId(), description);

    // Always show items when moving to a location
    final List<Item> items = location.getItems();
    if (!items.isEmpty()) {
      sb.append("\n\n");
      // Use LocationItemFormatter to show container status
      sb.append(LocationItemFormatter.formatItems(items, player, true));
    }

    return sb.toString();
  }

  private void resetGameState(@Nonnull final String sessionId) {
    // Reset the game map to initial state
    gameMap.resetMap();

    // Reset player or create new one with visited starting location
    final Location startingLocation = gameMap.getStartingLocation();
    startingLocation.setVisited(true);
    final Player player = players.get(sessionId);
    if (player != null) {
      player.reset(startingLocation);
    } else {
      players.put(sessionId, new Player(startingLocation));
    }

    // Clear parser context and history
    commandParser.clearContext(sessionId);
  }

  /**
   * Restarts the game for the given session.
   *
   * @param sessionId the session to restart
   * @return the restart message with current location description
   */
  @Nonnull
  public String restartGame(@Nonnull final String sessionId) {
    resetGameState(sessionId);
    return String.format(responseProvider.getRestartMessage(), look(players.get(sessionId), ""));
  }

  /**
   * Gets the player for a given session.
   *
   * @param sessionId the session ID
   * @return the player, or null if no player exists for that session
   */
  @Nullable
  public Player getPlayer(@Nonnull final String sessionId) {
    return players.get(sessionId);
  }

  /**
   * Cleans up resources for a session when it ends.
   *
   * @param sessionId the session to clean up
   */
  public void cleanupSession(@Nonnull final String sessionId) {
    players.remove(sessionId);
    commandParser.clearContext(sessionId);
  }

  @Nonnull
  private String handleUnknownCommand(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    return responseProvider.getCommandNotUnderstood(command.getVerb());
  }

  /**
   * Gets the response provider used by this engine.
   * Useful for handlers that need to access response text.
   *
   * @return the response provider
   */
  @Nonnull
  public ResponseProvider getResponseProvider() {
    return responseProvider;
  }

  /**
   * Gets the game map used by this engine.
   *
   * @return the game map
   */
  @Nonnull
  public GameMapInterface getGameMap() {
    return gameMap;
  }
}
package com.ifengine.test;

import com.ifengine.game.GameEngine;
import com.ifengine.game.GameState;
import com.ifengine.game.Player;
import com.ifengine.response.DefaultResponses;
import com.ifengine.response.ResponseProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test wrapper around GameEngine that provides additional test utilities.
 * <p>
 * TestGameEngine wraps a production GameEngine with a TestGameMap, providing:
 * <ul>
 *   <li>Initial GameState control via reflection</li>
 *   <li>Direct player access for assertions</li>
 *   <li>Session tracking for test cleanup</li>
 *   <li>Test map access for assertions</li>
 * </ul>
 * <p>
 * Use TestGameEngineBuilder or TestFixtures to create instances.
 */
public class TestGameEngine {

  private final GameEngine gameEngine;
  private final TestGameMap testGameMap;
  private final GameState initialState;
  private final Set<String> createdSessions;

  /**
   * Creates a TestGameEngine with a test map and default responses.
   *
   * @param testGameMap the test map to use
   */
  public TestGameEngine(@Nonnull final TestGameMap testGameMap) {
    this(testGameMap, new DefaultResponses(), GameState.PLAYING);
  }

  /**
   * Creates a TestGameEngine with a test map and initial game state.
   *
   * @param testGameMap the test map to use
   * @param initialState the initial game state for new players
   */
  public TestGameEngine(@Nonnull final TestGameMap testGameMap,
                        @Nonnull final GameState initialState) {
    this(testGameMap, new DefaultResponses(), initialState);
  }

  /**
   * Creates a TestGameEngine with full configuration.
   *
   * @param testGameMap the test map to use
   * @param responseProvider the response provider
   * @param initialState the initial game state for new players
   */
  public TestGameEngine(@Nonnull final TestGameMap testGameMap,
                        @Nonnull final ResponseProvider responseProvider,
                        @Nonnull final GameState initialState) {
    this.testGameMap = testGameMap;
    this.gameEngine = new GameEngine(testGameMap, responseProvider);
    this.initialState = initialState;
    this.createdSessions = new HashSet<>();
  }

  /**
   * Processes a command for a session.
   * On first command for a session, sets the initial game state.
   *
   * @param sessionId the session ID
   * @param command the command to process
   * @return the JSON response
   */
  @Nonnull
  public String processCommand(@Nonnull final String sessionId, @Nonnull final String command) {
    // Set initial state on first command for this session
    if (!createdSessions.contains(sessionId)) {
      setPlayerState(sessionId, initialState);
      createdSessions.add(sessionId);
    }
    return gameEngine.processCommand(sessionId, command);
  }

  /**
   * Gets the player for a session.
   *
   * @param sessionId the session ID
   * @return the player, or null if no player exists
   */
  @Nullable
  public Player getPlayer(@Nonnull final String sessionId) {
    return gameEngine.getPlayer(sessionId);
  }

  /**
   * Pre-creates a player for a session with the initial game state.
   * Useful for setting up test state before processing commands.
   *
   * @param sessionId the session ID
   */
  public void createPlayer(@Nonnull final String sessionId) {
    setPlayerState(sessionId, initialState);
    createdSessions.add(sessionId);
  }

  /**
   * Creates a test player at the starting location, marked as visited.
   * Useful for direct player manipulation in tests.
   *
   * @return a new Player at the starting location
   */
  @Nonnull
  public Player createTestPlayer() {
    final Player player = new Player(testGameMap.getStartingLocation());
    player.getCurrentLocation().setVisited(true);
    player.setGameState(initialState);
    return player;
  }

  /**
   * Sets the game state for a player using reflection.
   * Creates the player if it doesn't exist.
   *
   * @param sessionId the session ID
   * @param state the game state to set
   */
  @SuppressWarnings("unchecked")
  public void setPlayerState(@Nonnull final String sessionId, @Nonnull final GameState state) {
    try {
      // Access the players map via reflection
      final Field playersField = GameEngine.class.getDeclaredField("players");
      playersField.setAccessible(true);
      final ConcurrentHashMap<String, Player> players =
          (ConcurrentHashMap<String, Player>) playersField.get(gameEngine);

      // Get or create player
      Player player = players.get(sessionId);
      if (player == null) {
        player = new Player(testGameMap.getStartingLocation());
        player.getCurrentLocation().setVisited(true);
        players.put(sessionId, player);
      }

      // Set game state
      player.setGameState(state);
    } catch (final NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Failed to set player state via reflection", e);
    }
  }

  /**
   * Gets the test game map.
   *
   * @return the test game map
   */
  @Nonnull
  public TestGameMap getTestGameMap() {
    return testGameMap;
  }

  /**
   * Gets the underlying game engine.
   *
   * @return the game engine
   */
  @Nonnull
  public GameEngine getGameEngine() {
    return gameEngine;
  }

  /**
   * Cleans up a session.
   *
   * @param sessionId the session to clean up
   */
  public void cleanupSession(@Nonnull final String sessionId) {
    gameEngine.cleanupSession(sessionId);
    createdSessions.remove(sessionId);
  }

  /**
   * Cleans up all created sessions.
   */
  public void cleanupAllSessions() {
    for (final String sessionId : new HashSet<>(createdSessions)) {
      cleanupSession(sessionId);
    }
  }
}
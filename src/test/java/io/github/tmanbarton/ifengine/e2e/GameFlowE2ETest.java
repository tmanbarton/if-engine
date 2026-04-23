package io.github.tmanbarton.ifengine.e2e;

import io.github.tmanbarton.ifengine.Direction;
import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.game.GameEngine;
import io.github.tmanbarton.ifengine.game.GameMap;
import io.github.tmanbarton.ifengine.game.GameState;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.response.DefaultResponses;
import io.github.tmanbarton.ifengine.response.ResponseProvider;
import io.github.tmanbarton.ifengine.test.JsonTestUtils;
import io.github.tmanbarton.ifengine.test.TestFixtures;
import io.github.tmanbarton.ifengine.test.TestGameEngine;
import io.github.tmanbarton.ifengine.test.TestItemFactory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end tests for game flow.
 * <p>
 * Tests the complete game flow from start to various endpoints:
 * - Intro sequence with custom responses
 * - Restart flow
 * - Quit flow
 * - Full game session (intro → play → restart)
 */
@DisplayName("Game Flow E2E Tests")
class GameFlowE2ETest {

  private static final String SESSION_ID = "test-session";
  private static final ResponseProvider RESPONSES = new DefaultResponses();

  private GameEngine createIntroEngine() {
    final GameMap map = new GameMap.Builder()
        .addLocation(new Location("start", "Start location.", "Start"))
        .setStartingLocation("start")
        .withIntroResponses("Welcome back!", "Let me show you around.")
        .build();
    return new GameEngine(map);
  }

  private GameEngine createAdventureIntroEngine() {
    final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
    final Location forest = new Location("forest", "A dark forest.", "In the forest.");
    final Item key = TestItemFactory.createTestKey();
    final GameMap map = new GameMap.Builder()
        .addLocation(cottage)
        .addLocation(forest)
        .connectBidirectional("cottage", Direction.NORTH, "forest")
        .placeItem(key, "cottage")
        .setStartingLocation("cottage")
        .withIntroResponses("Let's go!", "No worries, let's begin.")
        .build();
    return new GameEngine(map);
  }

  @Nested
  class IntroStateTransitions {

    @Test
    @DisplayName("no answer transitions from WAITING_FOR_START_ANSWER to PLAYING")
    void testIntro_noTransitionsToPlaying() {
      // Given
      final GameEngine engine = createIntroEngine();

      // When
      engine.processCommand(SESSION_ID, "no");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.PLAYING, player.getGameState(),
          "no answer should transition to PLAYING state");
    }

    @Test
    @DisplayName("yes answer transitions from WAITING_FOR_START_ANSWER to PLAYING")
    void testIntro_yesTransitionsToPlaying() {
      // Given
      final GameEngine engine = createIntroEngine();

      // When
      engine.processCommand(SESSION_ID, "yes");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.PLAYING, player.getGameState(),
          "yes answer should transition to PLAYING state");
    }

    @Test
    @DisplayName("player can issue commands after completing intro")
    void testIntro_canPlayAfterIntro() {
      // Given
      final GameEngine engine = createAdventureIntroEngine();

      // When
      engine.processCommand(SESSION_ID, "no");
      engine.processCommand(SESSION_ID, "take key");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.PLAYING, player.getGameState(),
          "player should remain in PLAYING state after issuing commands");
      assertTrue(player.hasItem("key"),
          "player should be able to take items after completing intro");
    }
  }

  @Nested
  class RestartFlow {

    @Test
    @DisplayName("restart command triggers confirmation")
    void testRestart_triggersConfirmation() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "restart");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.WAITING_FOR_RESTART_CONFIRMATION, player.getGameState());
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getRestartConfirmation() + "\n\n", message);
    }

    @Test
    @DisplayName("confirming restart resets player to starting location")
    void testRestart_confirmedResetsPlayer() {
      // Given
      final TestGameEngine engine = TestFixtures.adventureScenario();
      engine.createPlayer(SESSION_ID);

      // Move and take item
      engine.processCommand(SESSION_ID, "take key");
      engine.processCommand(SESSION_ID, "north");

      final Player player = engine.getPlayer(SESSION_ID);
      assertTrue(player.hasItem("key"));
      assertEquals("forest", player.getCurrentLocation().getName());

      // When
      engine.processCommand(SESSION_ID, "restart");
      engine.processCommand(SESSION_ID, "yes");

      // Then
      assertFalse(player.hasItem("key"));
      assertEquals("minimal-location", player.getCurrentLocation().getName());
      assertEquals(GameState.PLAYING, player.getGameState());
    }

    @Test
    @DisplayName("cancelling restart returns to playing")
    void testRestart_cancelledReturnsToPlaying() {
      // Given
      final TestGameEngine engine = TestFixtures.adventureScenario();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "take key");

      // When
      engine.processCommand(SESSION_ID, "restart");
      engine.processCommand(SESSION_ID, "no");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.PLAYING, player.getGameState());
      assertTrue(player.hasItem("key"));
    }
  }

  @Nested
  class QuitFlow {

    @Test
    @DisplayName("quit command triggers confirmation")
    void testQuit_triggersConfirmation() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "quit");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.WAITING_FOR_QUIT_CONFIRMATION, player.getGameState());
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getQuitConfirmation() + "\n\n", message);
    }

    @Test
    @DisplayName("confirming quit returns to intro question")
    void testQuit_confirmedReturnsToIntro() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "quit");
      engine.processCommand(SESSION_ID, "yes");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.WAITING_FOR_START_ANSWER, player.getGameState());
    }

    @Test
    @DisplayName("cancelling quit returns to playing")
    void testQuit_cancelledReturnsToPlaying() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "quit");
      engine.processCommand(SESSION_ID, "no");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.PLAYING, player.getGameState());
    }
  }

  @Nested
  class FullGameSession {

    @Test
    @DisplayName("complete game session: intro → play → restart → continue playing with fresh state")
    void testFullSession_introPlayRestartPlay() {
      // Given
      final GameEngine engine = createAdventureIntroEngine();

      // Intro
      engine.processCommand(SESSION_ID, "yes");
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.PLAYING, player.getGameState(),
          "player should be in PLAYING state after answering intro");

      // Play the game
      engine.processCommand(SESSION_ID, "take key");
      engine.processCommand(SESSION_ID, "north");
      assertTrue(player.hasItem("key"),
          "player should have key after taking it");

      // Restart
      engine.processCommand(SESSION_ID, "restart");
      engine.processCommand(SESSION_ID, "yes");

      // Then - game is reset, still in PLAYING state
      assertEquals(GameState.PLAYING, player.getGameState(),
          "player should be in PLAYING state after restart");
      assertEquals("cottage", player.getCurrentLocation().getName(),
          "player should be back at starting location after restart");
      assertFalse(player.hasItem("key"),
          "player inventory should be cleared after restart");

      // Can take key again
      engine.processCommand(SESSION_ID, "take key");
      assertTrue(player.hasItem("key"),
          "player should be able to take key again after restart");
    }
  }
}
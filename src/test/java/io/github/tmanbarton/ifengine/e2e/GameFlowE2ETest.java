package io.github.tmanbarton.ifengine.e2e;

import io.github.tmanbarton.ifengine.game.GameState;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.response.DefaultResponses;
import io.github.tmanbarton.ifengine.response.ResponseProvider;
import io.github.tmanbarton.ifengine.test.JsonTestUtils;
import io.github.tmanbarton.ifengine.test.TestFixtures;
import io.github.tmanbarton.ifengine.test.TestGameEngine;

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
 * - New player intro sequence
 * - Experienced player intro sequence
 * - Restart flow
 * - Quit flow
 */
@DisplayName("Game Flow E2E Tests")
class GameFlowE2ETest {

  private static final String SESSION_ID = "test-session";
  private static final ResponseProvider RESPONSES = new DefaultResponses();

  @Nested
  class NewPlayerFlow {

    @Test
    @DisplayName("new player answers 'no' - transitions to PLAYING state")
    void testNewPlayer_answersNo_transitionsToPlaying() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationInstructionScenario();
      engine.createPlayer(SESSION_ID);

      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.WAITING_FOR_START_ANSWER, player.getGameState());

      // When
      engine.processCommand(SESSION_ID, "no");

      // Then
      assertEquals(GameState.PLAYING, player.getGameState());
      assertFalse(player.isExperiencedPlayer());
    }

    @Test
    @DisplayName("new player can play commands after answering 'no'")
    void testNewPlayer_canPlayAfterIntro() {
      // Given
      final TestGameEngine engine = TestFixtures.adventureScenario();

      // Force initial state
      engine.createPlayer(SESSION_ID);
      engine.getPlayer(SESSION_ID).setGameState(GameState.WAITING_FOR_START_ANSWER);

      // When - answer no, then play
      engine.processCommand(SESSION_ID, "no");
      engine.processCommand(SESSION_ID, "take key");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.PLAYING, player.getGameState());
      assertTrue(player.hasItem("key"));
    }
  }

  @Nested
  class ExperiencedPlayerFlow {

    @Test
    @DisplayName("experienced player answers 'yes' - transitions to PLAYING state")
    void testExperiencedPlayer_answersYes_transitionsToPlaying() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationInstructionScenario();
      engine.createPlayer(SESSION_ID);

      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.WAITING_FOR_START_ANSWER, player.getGameState());

      // When
      engine.processCommand(SESSION_ID, "yes");

      // Then
      assertEquals(GameState.PLAYING, player.getGameState());
      assertTrue(player.isExperiencedPlayer());
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
      final TestGameEngine engine = TestFixtures.adventureScenario();
      engine.createPlayer(SESSION_ID);
      engine.getPlayer(SESSION_ID).setGameState(GameState.WAITING_FOR_START_ANSWER);

      // When - answer intro
      engine.processCommand(SESSION_ID, "yes");
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.PLAYING, player.getGameState());

      // Play the game
      engine.processCommand(SESSION_ID, "take key");
      engine.processCommand(SESSION_ID, "north");
      assertTrue(player.hasItem("key"));

      // Restart
      engine.processCommand(SESSION_ID, "restart");
      engine.processCommand(SESSION_ID, "yes");

      // Then - game is reset, still in PLAYING GameState
      assertEquals(GameState.PLAYING, player.getGameState());
      assertEquals("minimal-location", player.getCurrentLocation().getName());
      assertFalse(player.hasItem("key"));

      // Can take key again
      engine.processCommand(SESSION_ID, "take key");
      assertTrue(player.hasItem("key"));
    }
  }
}
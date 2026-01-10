package com.ifengine.game;

import com.ifengine.Direction;
import com.ifengine.Location;
import com.ifengine.response.DefaultResponses;
import com.ifengine.response.ResponseProvider;
import com.ifengine.test.JsonTestUtils;
import com.ifengine.test.TestFixtures;
import com.ifengine.test.TestGameEngine;
import com.ifengine.test.TestOpenableItem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for GameEngine.
 * <p>
 * Tests cover:
 * - Session management (creating, maintaining, cleaning up player sessions)
 * - Intro flow (new vs experienced players)
 * - State transitions (restart/quit confirmations)
 * - Movement handling (navigation, visit tracking)
 * - Command routing (dispatching to handlers)
 * - Semantic validation (verb-preposition combinations)
 */
@DisplayName("GameEngine Tests")
class GameEngineTest {

  private static final String SESSION_ID = "test-session";
  private static final ResponseProvider RESPONSES = new DefaultResponses();

  @Nested
  @DisplayName("Session Management")
  class SessionManagement {

    @Test
    @DisplayName("Test processCommand - creates player on first command")
    void testProcessCommand_createsPlayerOnFirstCommand() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationInstructionScenario();

      // When
      engine.processCommand(SESSION_ID, "yes");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertNotNull(player);
    }

    @Test
    @DisplayName("Test processCommand - maintains session between commands")
    void testProcessCommand_maintainsSessionBetweenCommands() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.processCommand(SESSION_ID, "look");
      final Player playerAfterFirst = engine.getPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "look");

      // Then
      final Player playerAfterSecond = engine.getPlayer(SESSION_ID);
      assertEquals(playerAfterFirst, playerAfterSecond);
    }

    @Test
    @DisplayName("Test processCommand - different sessions have different players")
    void testProcessCommand_differentSessionsDifferentPlayers() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();

      // When
      engine.processCommand("session-1", "look");
      engine.processCommand("session-2", "look");

      // Then
      final Player player1 = engine.getPlayer("session-1");
      final Player player2 = engine.getPlayer("session-2");
      assertNotNull(player1);
      assertNotNull(player2);
      assertNotSame(player1, player2);
    }

    @Test
    @DisplayName("Test cleanupSession - removes player")
    void testCleanupSession_removesPlayer() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.processCommand(SESSION_ID, "look");
      assertNotNull(engine.getPlayer(SESSION_ID));

      // When
      engine.cleanupSession(SESSION_ID);

      // Then
      assertNull(engine.getPlayer(SESSION_ID));
    }
  }

  @Nested
  @DisplayName("Intro Flow")
  class IntroFlow {

    @Test
    @DisplayName("Test processCommand - 'yes' answer sets experienced player flag")
    void testProcessCommand_yesAnswerSetsExperiencedFlag() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationInstructionScenario();

      // When
      engine.processCommand(SESSION_ID, "yes");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertTrue(player.isExperiencedPlayer());
    }

    @Test
    @DisplayName("Test processCommand - 'no' answer does not set experienced player flag")
    void testProcessCommand_noAnswerDoesNotSetExperiencedFlag() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationInstructionScenario();

      // When
      engine.processCommand(SESSION_ID, "no");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertFalse(player.isExperiencedPlayer());
    }

    @Test
    @DisplayName("Test processCommand - intro answer transitions to PLAYING state")
    void testProcessCommand_introAnswerTransitionsToPlaying() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationInstructionScenario();

      // When
      engine.processCommand(SESSION_ID, "yes");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.PLAYING, player.getGameState());
    }

    @Test
    @DisplayName("Test processCommand - 'yes' answer shows experienced player intro")
    void testProcessCommand_yesAnswerShowsExperiencedIntro() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationInstructionScenario();

      // When
      final String response = engine.processCommand(SESSION_ID, "yes");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertTrue(message.startsWith(RESPONSES.getExperiencedPlayerIntro()));
    }

    @Test
    @DisplayName("Test processCommand - 'no' answer shows new player intro")
    void testProcessCommand_noAnswerShowsNewPlayerIntro() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationInstructionScenario();

      // When
      final String response = engine.processCommand(SESSION_ID, "no");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertTrue(message.startsWith(RESPONSES.getNewPlayerIntro()));
    }

    @Test
    @DisplayName("Test processCommand - invalid intro answer keeps state unchanged")
    void testProcessCommand_invalidIntroAnswerKeepsState() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationInstructionScenario();

      // When
      engine.processCommand(SESSION_ID, "maybe");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.WAITING_FOR_START_ANSWER, player.getGameState());
    }

    @Test
    @DisplayName("Test processCommand - invalid intro answer shows please answer message")
    void testProcessCommand_invalidIntroAnswerShowsMessage() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationInstructionScenario();

      // When
      final String response = engine.processCommand(SESSION_ID, "maybe");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPleaseAnswerQuestion() + "\n\n", message);
    }

    @Test
    @DisplayName("Test processCommand - 'y' accepted as yes")
    void testProcessCommand_yAcceptedAsYes() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationInstructionScenario();

      // When
      engine.processCommand(SESSION_ID, "y");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.PLAYING, player.getGameState());
      assertTrue(player.isExperiencedPlayer());
    }

    @Test
    @DisplayName("Test processCommand - 'n' accepted as no")
    void testProcessCommand_nAcceptedAsNo() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationInstructionScenario();

      // When
      engine.processCommand(SESSION_ID, "n");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.PLAYING, player.getGameState());
      assertFalse(player.isExperiencedPlayer());
    }
  }

  @Nested
  @DisplayName("Restart Flow")
  class RestartFlow {

    @Test
    @DisplayName("Test processCommand - restart command transitions to confirmation state")
    void testProcessCommand_restartTransitionsToConfirmation() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "restart");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.WAITING_FOR_RESTART_CONFIRMATION, player.getGameState());
    }

    @Test
    @DisplayName("Test processCommand - restart command shows confirmation message")
    void testProcessCommand_restartShowsConfirmation() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "restart");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getRestartConfirmation() + "\n\n", message);
    }

    @Test
    @DisplayName("Test processCommand - confirming restart clears inventory")
    void testProcessCommand_confirmingRestartClearsInventory() {
      // Given
      final TestGameEngine engine = TestFixtures.itemInteractionScenario();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "take key");
      assertTrue(engine.getPlayer(SESSION_ID).hasItem("key"));
      engine.processCommand(SESSION_ID, "restart");

      // When
      engine.processCommand(SESSION_ID, "yes");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertFalse(player.hasItem("key"));
    }

    @Test
    @DisplayName("Test processCommand - confirming restart returns player to starting location")
    void testProcessCommand_confirmingRestartReturnsToStart() {
      // Given
      final TestGameEngine engine = TestFixtures.twoLocationScenario();
      engine.createPlayer(SESSION_ID);
      final Location startingLocation = engine.getPlayer(SESSION_ID).getCurrentLocation();
      engine.processCommand(SESSION_ID, "north");
      final Location movedLocation = engine.getPlayer(SESSION_ID).getCurrentLocation();
      assertNotSame(startingLocation, movedLocation);
      engine.processCommand(SESSION_ID, "restart");

      // When
      engine.processCommand(SESSION_ID, "yes");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(startingLocation.getName(), player.getCurrentLocation().getName());
    }

    @Test
    @DisplayName("Test processCommand - confirming restart transitions to PLAYING state")
    void testProcessCommand_confirmingRestartTransitionsToPlaying() {
      // Given
      final TestGameEngine engine = TestFixtures.restartConfirmationScenario();
      engine.createPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "yes");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.PLAYING, player.getGameState());
    }

    @Test
    @DisplayName("Test processCommand - canceling restart returns to playing state")
    void testProcessCommand_cancelingRestartReturnsToPlaying() {
      // Given
      final TestGameEngine engine = TestFixtures.restartConfirmationScenario();
      engine.createPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "no");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.PLAYING, player.getGameState());
    }

    @Test
    @DisplayName("Test processCommand - canceling restart shows cancelled message")
    void testProcessCommand_cancelingRestartShowsMessage() {
      // Given
      final TestGameEngine engine = TestFixtures.restartConfirmationScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "no");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getRestartCancelled() + "\n\n", message);
    }

    @Test
    @DisplayName("Test processCommand - invalid restart answer keeps confirmation state")
    void testProcessCommand_invalidRestartAnswerKeepsState() {
      // Given
      final TestGameEngine engine = TestFixtures.restartConfirmationScenario();
      engine.createPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "maybe");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.WAITING_FOR_RESTART_CONFIRMATION, player.getGameState());
    }
  }

  @Nested
  @DisplayName("Quit Flow")
  class QuitFlow {

    @Test
    @DisplayName("Test processCommand - quit command transitions to confirmation state")
    void testProcessCommand_quitTransitionsToConfirmation() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "quit");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.WAITING_FOR_QUIT_CONFIRMATION, player.getGameState());
    }

    @Test
    @DisplayName("Test processCommand - quit command shows confirmation message")
    void testProcessCommand_quitShowsConfirmation() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "quit");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getQuitConfirmation() + "\n\n", message);
    }

    @Test
    @DisplayName("Test processCommand - confirming quit resets game and goes to intro question")
    void testProcessCommand_confirmingQuitResetsAndGoesToIntro() {
      // Given
      final TestGameEngine engine = TestFixtures.quitConfirmationScenario();
      engine.createPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "yes");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.WAITING_FOR_START_ANSWER, player.getGameState());
    }

    @Test
    @DisplayName("Test processCommand - confirming quit shows intro question")
    void testProcessCommand_confirmingQuitShowsIntroQuestion() {
      // Given
      final TestGameEngine engine = TestFixtures.quitConfirmationScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "yes");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getHaveYouPlayedBeforeQuestion() + "\n\n", message);
    }

    @Test
    @DisplayName("Test processCommand - confirming quit clears inventory")
    void testProcessCommand_confirmingQuitClearsInventory() {
      // Given
      final TestGameEngine engine = TestFixtures.itemInteractionScenario();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "take key");
      assertTrue(engine.getPlayer(SESSION_ID).hasItem("key"));
      engine.processCommand(SESSION_ID, "quit");

      // When
      engine.processCommand(SESSION_ID, "yes");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertFalse(player.hasItem("key"));
    }

    @Test
    @DisplayName("Test processCommand - canceling quit returns to playing state")
    void testProcessCommand_cancelingQuitReturnsToPlaying() {
      // Given
      final TestGameEngine engine = TestFixtures.quitConfirmationScenario();
      engine.createPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "no");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.PLAYING, player.getGameState());
    }

    @Test
    @DisplayName("Test processCommand - canceling quit shows cancelled message")
    void testProcessCommand_cancelingQuitShowsMessage() {
      // Given
      final TestGameEngine engine = TestFixtures.quitConfirmationScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "no");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getQuitCancelled() + "\n\n", message);
    }
  }

  @Nested
  @DisplayName("Movement Handling")
  class MovementHandling {

    @Test
    @DisplayName("Test processCommand - valid movement changes location")
    void testProcessCommand_validMovementChangesLocation() {
      // Given
      final TestGameEngine engine = TestFixtures.twoLocationScenario();
      engine.createPlayer(SESSION_ID);
      final Location startLocation = engine.getPlayer(SESSION_ID).getCurrentLocation();

      // When
      engine.processCommand(SESSION_ID, "north");

      // Then
      final Location newLocation = engine.getPlayer(SESSION_ID).getCurrentLocation();
      assertNotSame(startLocation, newLocation);
    }

    @Test
    @DisplayName("Test processCommand - blocked direction keeps location unchanged")
    void testProcessCommand_blockedDirectionKeepsLocation() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);
      final Location startLocation = engine.getPlayer(SESSION_ID).getCurrentLocation();

      // When
      engine.processCommand(SESSION_ID, "north");

      // Then
      final Location currentLocation = engine.getPlayer(SESSION_ID).getCurrentLocation();
      assertEquals(startLocation, currentLocation);
    }

    @Test
    @DisplayName("Test processCommand - blocked direction shows error message")
    void testProcessCommand_blockedDirectionShowsError() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "north");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getCantGoThatWay() + "\n\n", message);
    }

    @Test
    @DisplayName("Test processCommand - direction abbreviation works")
    void testProcessCommand_directionAbbreviationWorks() {
      // Given
      final TestGameEngine engine = TestFixtures.twoLocationScenario();
      engine.createPlayer(SESSION_ID);
      final Location startLocation = engine.getPlayer(SESSION_ID).getCurrentLocation();

      // When
      engine.processCommand(SESSION_ID, "n");

      // Then
      final Location newLocation = engine.getPlayer(SESSION_ID).getCurrentLocation();
      assertNotSame(startLocation, newLocation);
    }

    @Test
    @DisplayName("Test processCommand - 'go direction' format works")
    void testProcessCommand_goDirectionFormatWorks() {
      // Given
      final TestGameEngine engine = TestFixtures.twoLocationScenario();
      engine.createPlayer(SESSION_ID);
      final Location startLocation = engine.getPlayer(SESSION_ID).getCurrentLocation();

      // When
      engine.processCommand(SESSION_ID, "go north");

      // Then
      final Location newLocation = engine.getPlayer(SESSION_ID).getCurrentLocation();
      assertNotSame(startLocation, newLocation);
    }

    @Test
    @DisplayName("Test processCommand - first visit marks location as visited")
    void testProcessCommand_firstVisitMarksLocationVisited() {
      // Given
      final TestGameEngine engine = TestFixtures.twoLocationScenario();
      engine.createPlayer(SESSION_ID);

      // Get the destination location (north of starting location)
      final Location startLocation = engine.getPlayer(SESSION_ID).getCurrentLocation();
      final Location northLocation = startLocation.getConnection(Direction.NORTH);
      assertFalse(northLocation.isVisited());

      // When
      engine.processCommand(SESSION_ID, "north");

      // Then
      assertTrue(northLocation.isVisited());
    }

    @Test
    @DisplayName("Test processCommand - 'go' without direction shows error")
    void testProcessCommand_goWithoutDirectionShowsError() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "go");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getGoNoDirectionSpecified() + "\n\n", message);
    }

    @Test
    @DisplayName("Test processCommand - response includes valid directions")
    void testProcessCommand_responseIncludesValidDirections() {
      // Given
      final TestGameEngine engine = TestFixtures.twoLocationScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "look");

      // Then
      final List<String> validDirections = JsonTestUtils.extractValidDirections(response);
      assertFalse(validDirections.isEmpty());
    }
  }

  @Nested
  @DisplayName("Command Routing")
  class CommandRouting {

    @Test
    @DisplayName("Test processCommand - routes take command to handler")
    void testProcessCommand_routesTakeCommand() {
      // Given
      final TestGameEngine engine = TestFixtures.itemInteractionScenario();
      engine.createPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "take key");

      // Then
      assertTrue(engine.getPlayer(SESSION_ID).hasItem("key"));
    }

    @Test
    @DisplayName("Test processCommand - routes drop command to handler")
    void testProcessCommand_routesDropCommand() {
      // Given
      final TestGameEngine engine = TestFixtures.itemInteractionScenario();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "take key");
      assertTrue(engine.getPlayer(SESSION_ID).hasItem("key"));

      // When
      engine.processCommand(SESSION_ID, "drop key");

      // Then
      assertFalse(engine.getPlayer(SESSION_ID).hasItem("key"));
    }

    @Test
    @DisplayName("Test processCommand - routes look command to handler")
    void testProcessCommand_routesLookCommand() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);
      final Location location = engine.getPlayer(SESSION_ID).getCurrentLocation();

      // When
      final String response = engine.processCommand(SESSION_ID, "look");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertTrue(message.startsWith(location.getLongDescription()));
    }

    @Test
    @DisplayName("Test processCommand - unknown command shows error")
    void testProcessCommand_unknownCommandShowsError() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "xyzzy");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getCommandNotUnderstood("xyzzy") + "\n\n", message);
    }

    @Test
    @DisplayName("Test processCommand - empty command shows error")
    void testProcessCommand_emptyCommandShowsError() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getCommandNotUnderstood("") + "\n\n", message);
    }
  }

  @Nested
  @DisplayName("Semantic Validation")
  class SemanticValidation {

    @Test
    @DisplayName("Test processCommand - rejects invalid verb-preposition combination")
    void testProcessCommand_rejectsInvalidVerbPreposition() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "take key on table");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getVerbPrepositionInvalid() + "\n\n", message);
    }
  }

  @Nested
  @DisplayName("JSON Response Format")
  class JsonResponseFormat {

    @Test
    @DisplayName("Test processCommand - response includes game state in JSON")
    void testProcessCommand_responseIncludesGameState() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "look");

      // Then
      final String gameState = JsonTestUtils.extractGameState(response);
      assertEquals("PLAYING", gameState);
    }

    @Test
    @DisplayName("Test processCommand - look command sets boldable text")
    void testProcessCommand_lookSetsBoldableText() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);
      final Location location = engine.getPlayer(SESSION_ID).getCurrentLocation();

      // When
      final String response = engine.processCommand(SESSION_ID, "look");

      // Then
      final String boldableText = JsonTestUtils.extractBoldableText(response);
      assertEquals(location.getLongDescription(), boldableText);
    }
  }

  @Nested
  @DisplayName("Unlock Code Input Flow")
  class UnlockCodeInputFlow {

    @Test
    @DisplayName("Test processCommand - correct code unlocks pending openable")
    void testProcessCommand_correctCodeUnlocksPendingOpenable() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);
      final Player player = engine.getPlayer(SESSION_ID);
      final TestOpenableItem chest = (TestOpenableItem) engine.getTestGameMap()
          .getStartingLocation().getItems().get(0);
      player.setPendingOpenable(chest);
      player.setGameState(GameState.WAITING_FOR_UNLOCK_CODE);
      assertFalse(chest.isUnlocked());

      // When
      engine.processCommand(SESSION_ID, "1, 2, 3, 4");

      // Then
      assertTrue(chest.isUnlocked());
    }

    @Test
    @DisplayName("Test processCommand - correct code returns success message")
    void testProcessCommand_correctCodeReturnsSuccessMessage() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);
      final Player player = engine.getPlayer(SESSION_ID);
      final TestOpenableItem chest = (TestOpenableItem) engine.getTestGameMap()
          .getStartingLocation().getItems().get(0);
      player.setPendingOpenable(chest);
      player.setGameState(GameState.WAITING_FOR_UNLOCK_CODE);

      // When
      final String response = engine.processCommand(SESSION_ID, "1, 2, 3, 4");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_MESSAGE + "\n\n", message);
    }

    @Test
    @DisplayName("Test processCommand - wrong code shows error message")
    void testProcessCommand_wrongCodeShowsErrorMessage() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);
      final Player player = engine.getPlayer(SESSION_ID);
      final TestOpenableItem chest = (TestOpenableItem) engine.getTestGameMap()
          .getStartingLocation().getItems().get(0);
      player.setPendingOpenable(chest);
      player.setGameState(GameState.WAITING_FOR_UNLOCK_CODE);

      // When
      final String response = engine.processCommand(SESSION_ID, "9, 9, 9, 9");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(TestOpenableItem.DEFAULT_WRONG_CODE_MESSAGE + "\n\n", message);
    }

    @Test
    @DisplayName("Test processCommand - wrong code keeps item locked")
    void testProcessCommand_wrongCodeKeepsItemLocked() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);
      final Player player = engine.getPlayer(SESSION_ID);
      final TestOpenableItem chest = (TestOpenableItem) engine.getTestGameMap()
          .getStartingLocation().getItems().get(0);
      player.setPendingOpenable(chest);
      player.setGameState(GameState.WAITING_FOR_UNLOCK_CODE);

      // When
      engine.processCommand(SESSION_ID, "9, 9, 9, 9");

      // Then
      assertFalse(chest.isUnlocked());
    }

    @Test
    @DisplayName("Test processCommand - code input clears pending openable")
    void testProcessCommand_codeInputClearsPendingOpenable() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);
      final Player player = engine.getPlayer(SESSION_ID);
      final TestOpenableItem chest = (TestOpenableItem) engine.getTestGameMap()
          .getStartingLocation().getItems().get(0);
      player.setPendingOpenable(chest);
      player.setGameState(GameState.WAITING_FOR_UNLOCK_CODE);

      // When
      engine.processCommand(SESSION_ID, "1, 2, 3, 4");

      // Then
      assertNull(player.getPendingOpenable());
    }

    @Test
    @DisplayName("Test processCommand - code input transitions to PLAYING state")
    void testProcessCommand_codeInputTransitionsToPlaying() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);
      final Player player = engine.getPlayer(SESSION_ID);
      final TestOpenableItem chest = (TestOpenableItem) engine.getTestGameMap()
          .getStartingLocation().getItems().get(0);
      player.setPendingOpenable(chest);
      player.setGameState(GameState.WAITING_FOR_UNLOCK_CODE);

      // When
      engine.processCommand(SESSION_ID, "1, 2, 3, 4");

      // Then
      assertEquals(GameState.PLAYING, player.getGameState());
    }

    @Test
    @DisplayName("Test processCommand - null pending openable returns error")
    void testProcessCommand_nullPendingOpenableReturnsError() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);
      final Player player = engine.getPlayer(SESSION_ID);
      player.setGameState(GameState.WAITING_FOR_UNLOCK_CODE);
      // pendingOpenable is null

      // When
      final String response = engine.processCommand(SESSION_ID, "1, 2, 3, 4");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getCommandNotUnderstood("1, 2, 3, 4") + "\n\n", message);
    }

    @Test
    @DisplayName("Test processCommand - null pending openable transitions to PLAYING")
    void testProcessCommand_nullPendingOpenableTransitionsToPlaying() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);
      final Player player = engine.getPlayer(SESSION_ID);
      player.setGameState(GameState.WAITING_FOR_UNLOCK_CODE);

      // When
      engine.processCommand(SESSION_ID, "1, 2, 3, 4");

      // Then
      assertEquals(GameState.PLAYING, player.getGameState());
    }

    @Test
    @DisplayName("Test processCommand - code with spaces works")
    void testProcessCommand_codeWithSpacesWorks() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);
      final Player player = engine.getPlayer(SESSION_ID);
      final TestOpenableItem chest = (TestOpenableItem) engine.getTestGameMap()
          .getStartingLocation().getItems().get(0);
      player.setPendingOpenable(chest);
      player.setGameState(GameState.WAITING_FOR_UNLOCK_CODE);

      // When
      engine.processCommand(SESSION_ID, "1 2 3 4");

      // Then
      assertTrue(chest.isUnlocked());
    }
  }

  @Nested
  @DisplayName("Open Code Input Flow")
  class OpenCodeInputFlow {

    @Test
    @DisplayName("Test processCommand - correct code opens pending openable")
    void testProcessCommand_correctCodeOpensPendingOpenable() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);
      final Player player = engine.getPlayer(SESSION_ID);
      final TestOpenableItem chest = (TestOpenableItem) engine.getTestGameMap()
          .getStartingLocation().getItems().get(0);
      player.setPendingOpenable(chest);
      player.setGameState(GameState.WAITING_FOR_OPEN_CODE);
      assertFalse(chest.isOpen());

      // When
      engine.processCommand(SESSION_ID, "1, 2, 3, 4");

      // Then
      assertTrue(chest.isOpen());
    }

    @Test
    @DisplayName("Test processCommand - correct code returns success message")
    void testProcessCommand_correctCodeReturnsSuccessMessage() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);
      final Player player = engine.getPlayer(SESSION_ID);
      final TestOpenableItem chest = (TestOpenableItem) engine.getTestGameMap()
          .getStartingLocation().getItems().get(0);
      player.setPendingOpenable(chest);
      player.setGameState(GameState.WAITING_FOR_OPEN_CODE);

      // When
      final String response = engine.processCommand(SESSION_ID, "1, 2, 3, 4");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      // Opens with auto-unlock, so both messages
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_MESSAGE + " " +
          TestOpenableItem.DEFAULT_OPEN_MESSAGE + "\n\n", message);
    }

    @Test
    @DisplayName("Test processCommand - wrong code shows error message")
    void testProcessCommand_wrongCodeShowsErrorMessage() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);
      final Player player = engine.getPlayer(SESSION_ID);
      final TestOpenableItem chest = (TestOpenableItem) engine.getTestGameMap()
          .getStartingLocation().getItems().get(0);
      player.setPendingOpenable(chest);
      player.setGameState(GameState.WAITING_FOR_OPEN_CODE);

      // When
      final String response = engine.processCommand(SESSION_ID, "9, 9, 9, 9");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(TestOpenableItem.DEFAULT_WRONG_CODE_MESSAGE + "\n\n", message);
    }

    @Test
    @DisplayName("Test processCommand - wrong code keeps item closed")
    void testProcessCommand_wrongCodeKeepsItemClosed() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);
      final Player player = engine.getPlayer(SESSION_ID);
      final TestOpenableItem chest = (TestOpenableItem) engine.getTestGameMap()
          .getStartingLocation().getItems().get(0);
      player.setPendingOpenable(chest);
      player.setGameState(GameState.WAITING_FOR_OPEN_CODE);

      // When
      engine.processCommand(SESSION_ID, "9, 9, 9, 9");

      // Then
      assertFalse(chest.isOpen());
    }

    @Test
    @DisplayName("Test processCommand - open code input clears pending openable")
    void testProcessCommand_openCodeInputClearsPendingOpenable() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);
      final Player player = engine.getPlayer(SESSION_ID);
      final TestOpenableItem chest = (TestOpenableItem) engine.getTestGameMap()
          .getStartingLocation().getItems().get(0);
      player.setPendingOpenable(chest);
      player.setGameState(GameState.WAITING_FOR_OPEN_CODE);

      // When
      engine.processCommand(SESSION_ID, "1, 2, 3, 4");

      // Then
      assertNull(player.getPendingOpenable());
    }

    @Test
    @DisplayName("Test processCommand - open code input transitions to PLAYING state")
    void testProcessCommand_openCodeInputTransitionsToPlaying() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);
      final Player player = engine.getPlayer(SESSION_ID);
      final TestOpenableItem chest = (TestOpenableItem) engine.getTestGameMap()
          .getStartingLocation().getItems().get(0);
      player.setPendingOpenable(chest);
      player.setGameState(GameState.WAITING_FOR_OPEN_CODE);

      // When
      engine.processCommand(SESSION_ID, "1, 2, 3, 4");

      // Then
      assertEquals(GameState.PLAYING, player.getGameState());
    }

    @Test
    @DisplayName("Test processCommand - null pending openable returns error for open code")
    void testProcessCommand_nullPendingOpenableReturnsErrorForOpenCode() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);
      final Player player = engine.getPlayer(SESSION_ID);
      player.setGameState(GameState.WAITING_FOR_OPEN_CODE);
      // pendingOpenable is null

      // When
      final String response = engine.processCommand(SESSION_ID, "1, 2, 3, 4");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getCommandNotUnderstood("1, 2, 3, 4") + "\n\n", message);
    }

    @Test
    @DisplayName("Test processCommand - null pending openable transitions to PLAYING for open code")
    void testProcessCommand_nullPendingOpenableTransitionsToPlayingForOpenCode() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);
      final Player player = engine.getPlayer(SESSION_ID);
      player.setGameState(GameState.WAITING_FOR_OPEN_CODE);

      // When
      engine.processCommand(SESSION_ID, "1, 2, 3, 4");

      // Then
      assertEquals(GameState.PLAYING, player.getGameState());
    }
  }

  @Nested
  @DisplayName("Custom Intro Flow")
  class CustomIntroFlow {

    @Test
    @DisplayName("skipIntro - player starts in PLAYING state and commands work")
    void testSkipIntro_playerStartsInPlayingState() {
      // Given
      final GameMap map = new GameMap.Builder()
          .addLocation(new Location("start", "Start location.", "Start"))
          .setStartingLocation("start")
          .skipIntro()
          .build();
      final GameEngine engine = new GameEngine(map);

      // When
      final String response = engine.processCommand(SESSION_ID, "look");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.PLAYING, player.getGameState());
      assertTrue(JsonTestUtils.extractMessage(response).contains("Start location."));
    }

    @Test
    @DisplayName("withIntroHandler - handler controls state transition")
    void testWithIntroHandler_handlerControlsStateTransition() {
      // Given
      final GameMap map = new GameMap.Builder()
          .addLocation(new Location("start", "Start location.", "Start"))
          .setStartingLocation("start")
          .withIntroHandler((player, response, gameMap) -> {
            if ("begin".equalsIgnoreCase(response)) {
              return IntroResult.playing("Starting!");
            }
            return IntroResult.waiting("Please type 'begin'.");
          })
          .build();
      final GameEngine engine = new GameEngine(map);

      // When - wrong answer keeps waiting
      engine.processCommand(SESSION_ID, "hello");
      assertEquals(GameState.WAITING_FOR_START_ANSWER, engine.getPlayer(SESSION_ID).getGameState());

      // When - correct answer transitions
      engine.processCommand(SESSION_ID, "begin");
      assertEquals(GameState.PLAYING, engine.getPlayer(SESSION_ID).getGameState());
    }

    @Test
    @DisplayName("withIntroResponses - uses custom yes/no responses")
    void testWithIntroResponses_usesCustomYesNoResponses() {
      // Given
      final GameMap map = new GameMap.Builder()
          .addLocation(new Location("start", "Start location.", "Start"))
          .setStartingLocation("start")
          .withIntroResponses("Let's go!", "Come back later.")
          .build();
      final GameEngine engine = new GameEngine(map);

      // When - yes answer
      final String yesResponse = engine.processCommand(SESSION_ID, "yes");

      // Then (note: engine adds \n\n to all responses for visual spacing)
      assertEquals("Let's go!\n\n", JsonTestUtils.extractMessage(yesResponse));
      assertEquals(GameState.PLAYING, engine.getPlayer(SESSION_ID).getGameState());
    }

    @Test
    @DisplayName("withIntroResponses - no answer returns custom response and transitions to playing")
    void testWithIntroResponses_noAnswerTransitionsToPlaying() {
      // Given
      final GameMap map = new GameMap.Builder()
          .addLocation(new Location("start", "Start location.", "Start"))
          .setStartingLocation("start")
          .withIntroResponses("Let's go!", "Come back later.")
          .build();
      final GameEngine engine = new GameEngine(map);

      // When - no answer
      final String noResponse = engine.processCommand(SESSION_ID, "no");

      // Then (note: engine adds \n\n to all responses for visual spacing)
      assertEquals("Come back later.\n\n", JsonTestUtils.extractMessage(noResponse));
      assertEquals(GameState.PLAYING, engine.getPlayer(SESSION_ID).getGameState());
    }

    @Test
    @DisplayName("default - uses standard yes/no logic when no intro configured")
    void testDefault_usesStandardYesNoLogic() {
      // Given
      final GameMap map = new GameMap.Builder()
          .addLocation(new Location("start", "Start location.", "Start"))
          .setStartingLocation("start")
          .build();
      final GameEngine engine = new GameEngine(map);

      // When
      engine.processCommand(SESSION_ID, "yes");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(GameState.PLAYING, player.getGameState());
      assertTrue(player.isExperiencedPlayer());
    }
  }
}
package io.github.tmanbarton.ifengine.integration;

import io.github.tmanbarton.ifengine.game.GameState;
import io.github.tmanbarton.ifengine.test.JsonTestUtils;
import io.github.tmanbarton.ifengine.test.TestFixtures;
import io.github.tmanbarton.ifengine.test.TestGameEngine;
import io.github.tmanbarton.ifengine.test.TestGameEngineBuilder;
import io.github.tmanbarton.ifengine.test.TestGameMap;
import io.github.tmanbarton.ifengine.test.TestGameMapBuilder;
import io.github.tmanbarton.ifengine.test.TestOpenableItem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for code/word-based unlock functionality.
 * <p>
 * Tests the full flow from command input through state transitions:
 * - Immediate unlock with correct/wrong code via "with" preposition
 * - Prompt flow: command without code → enter code → result
 * - Word-based unlocking (multi-word answers)
 */
@DisplayName("Code-Based Unlock Integration Tests")
class CodeBasedUnlockIntegrationTest {

  private static final String SESSION_ID = "test-session";

  @Nested
  class ImmediateUnlockWithCode {

    @Test
    @DisplayName("unlock lockbox with correct code succeeds immediately")
    void testUnlock_correctCodeImmediate() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "unlock lockbox with 1, 2, 3, 4");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_MESSAGE + "\n\n", message);
    }

    @Test
    @DisplayName("unlock lockbox with wrong code fails")
    void testUnlock_wrongCodeImmediate() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "unlock lockbox with 9, 9, 9, 9");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(TestOpenableItem.DEFAULT_WRONG_CODE_MESSAGE + "\n\n", message);
    }

    @Test
    @DisplayName("unlock with alternate code format (spaces only) succeeds")
    void testUnlock_alternateCodeFormat() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "unlock lockbox with 1 2 3 4");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_MESSAGE + "\n\n", message);
    }
  }

  @Nested
  class PromptFlow {

    @Test
    @DisplayName("unlock without code prompts, then correct code succeeds")
    void testUnlock_promptFlowCorrectCode() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);

      // When - first command prompts for code
      final String promptResponse = engine.processCommand(SESSION_ID, "unlock lockbox");
      final String promptMessage = JsonTestUtils.extractMessage(promptResponse);
      assertEquals(TestOpenableItem.DEFAULT_PROMPT_MESSAGE + "\n\n", promptMessage);

      // Verify state is WAITING_FOR_UNLOCK_CODE
      final GameState stateAfterPrompt = engine.getPlayer(SESSION_ID).getGameState();
      assertEquals(GameState.WAITING_FOR_UNLOCK_CODE, stateAfterPrompt);

      // When - enter correct code
      final String unlockResponse = engine.processCommand(SESSION_ID, "1, 2, 3, 4");

      // Then
      final String unlockMessage = JsonTestUtils.extractMessage(unlockResponse);
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_MESSAGE + "\n\n", unlockMessage);

      // Verify state returns to PLAYING
      final GameState stateAfterUnlock = engine.getPlayer(SESSION_ID).getGameState();
      assertEquals(GameState.PLAYING, stateAfterUnlock);
    }

    @Test
    @DisplayName("unlock without code prompts, then wrong code fails")
    void testUnlock_promptFlowWrongCode() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);

      // When - first command prompts for code
      engine.processCommand(SESSION_ID, "unlock lockbox");

      // When - enter wrong code
      final String response = engine.processCommand(SESSION_ID, "wrong");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(TestOpenableItem.DEFAULT_WRONG_CODE_MESSAGE + "\n\n", message);

      // Verify state returns to PLAYING (not stuck in waiting)
      final GameState state = engine.getPlayer(SESSION_ID).getGameState();
      assertEquals(GameState.PLAYING, state);
    }
  }

  @Nested
  class OpenWithCode {

    @Test
    @DisplayName("open lockbox with correct code unlocks and opens")
    void testOpen_correctCodeImmediate() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "open lockbox with 1, 2, 3, 4");

      // Then - should unlock and open in one step
      final String message = JsonTestUtils.extractMessage(response);
      final String expected = TestOpenableItem.DEFAULT_UNLOCK_MESSAGE + " "
          + TestOpenableItem.DEFAULT_OPEN_MESSAGE + "\n\n";
      assertEquals(expected, message);
    }

    @Test
    @DisplayName("open without code prompts, then correct code succeeds")
    void testOpen_promptFlowCorrectCode() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);

      // When - first command prompts for code
      final String promptResponse = engine.processCommand(SESSION_ID, "open lockbox");
      final String promptMessage = JsonTestUtils.extractMessage(promptResponse);
      assertEquals(TestOpenableItem.DEFAULT_PROMPT_MESSAGE + "\n\n", promptMessage);

      // Verify state is WAITING_FOR_OPEN_CODE
      final GameState stateAfterPrompt = engine.getPlayer(SESSION_ID).getGameState();
      assertEquals(GameState.WAITING_FOR_OPEN_CODE, stateAfterPrompt);

      // When - enter correct code
      final String openResponse = engine.processCommand(SESSION_ID, "1, 2, 3, 4");

      // Then - should unlock and open
      final String openMessage = JsonTestUtils.extractMessage(openResponse);
      final String expected = TestOpenableItem.DEFAULT_UNLOCK_MESSAGE + " "
          + TestOpenableItem.DEFAULT_OPEN_MESSAGE + "\n\n";
      assertEquals(expected, openMessage);
    }
  }

  @Nested
  class WordBasedUnlock {

    @Test
    @DisplayName("unlock cryptex with secret word succeeds")
    void testUnlock_wordBasedImmediate() {
      // Given - create a word-based lockable item
      final TestOpenableItem cryptex = TestOpenableItem.builder("cryptex")
          .withUnlockTargets("cryptex")
          .withOpenTargets("cryptex")
          .withInferredTargetNames("cryptex")
          .withExpectedCode("plugh")
          .build();

      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      map.getStartingLocation().addItem(cryptex);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "unlock cryptex with plugh");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_MESSAGE + "\n\n", message);
    }

    @Test
    @DisplayName("unlock with multi-word answer succeeds")
    void testUnlock_multiWordAnswer() {
      // Given - create item expecting multi-word answer
      final TestOpenableItem chest = TestOpenableItem.builder("chest")
          .withUnlockTargets("chest")
          .withOpenTargets("chest")
          .withInferredTargetNames("chest")
          .withExpectedCode("secret phrase")
          .build();

      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      map.getStartingLocation().addItem(chest);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "unlock chest with secret phrase");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_MESSAGE + "\n\n", message);
    }
  }

  @Nested
  class ImpliedObjectWithCode {

    @Test
    @DisplayName("unlock with code (no object name) uses implied object")
    void testUnlock_impliedObjectWithCode() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);

      // When - only one unlockable present, should infer it
      final String response = engine.processCommand(SESSION_ID, "unlock with 1, 2, 3, 4");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_MESSAGE + "\n\n", message);
    }

    @Test
    @DisplayName("open with code (no object name) uses implied object")
    void testOpen_impliedObjectWithCode() {
      // Given
      final TestGameEngine engine = TestFixtures.codeBasedItemScenario();
      engine.createPlayer(SESSION_ID);

      // When - only one openable present, should infer it
      final String response = engine.processCommand(SESSION_ID, "open with 1, 2, 3, 4");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      final String expected = TestOpenableItem.DEFAULT_UNLOCK_MESSAGE + " "
          + TestOpenableItem.DEFAULT_OPEN_MESSAGE + "\n\n";
      assertEquals(expected, message);
    }
  }
}
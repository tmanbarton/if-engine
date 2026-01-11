package io.github.tmanbarton.ifengine.integration;

import io.github.tmanbarton.ifengine.Direction;
import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.game.GameState;
import io.github.tmanbarton.ifengine.test.JsonTestUtils;
import io.github.tmanbarton.ifengine.test.TestGameEngine;
import io.github.tmanbarton.ifengine.test.TestGameEngineBuilder;
import io.github.tmanbarton.ifengine.test.TestGameMap;
import io.github.tmanbarton.ifengine.test.TestItemFactory;
import io.github.tmanbarton.ifengine.test.TestLocationFactory;
import io.github.tmanbarton.ifengine.test.TestOpenableLocation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for unlock/open functionality.
 * <p>
 * These tests focus on cross-location scenarios that unit tests don't cover:
 * - Getting key from one location, using it at another
 * - Error recovery flows (fail → get key → retry)
 * <p>
 * Basic unlock/open behavior is covered by UnlockHandlerTest and OpenHandlerTest.
 */
@DisplayName("Unlock/Open Integration Tests")
class UnlockOpenIntegrationTest {

  private static final String SESSION_ID = "test-session";

  @Nested
  class CrossLocationKeyUsage {

    @Test
    @DisplayName("get key from one location, unlock door at another location")
    void testUnlock_keyFromDifferentLocation() {
      // Given
      final TestGameMap map = new TestGameMap();

      final Location keyRoom = TestLocationFactory.createSimpleLocation("key-room");
      final TestOpenableLocation vaultRoom = TestOpenableLocation.builder("vault-room")
          .withUnlockTargets("door", "vault")
          .withOpenTargets("door", "vault")
          .withRequiredKey("key")
          .build();

      map.addLocation(keyRoom);
      map.addLocation(vaultRoom);
      TestLocationFactory.addBidirectionalConnection(keyRoom, Direction.NORTH, vaultRoom);

      final Item key = TestItemFactory.createTestKey();
      keyRoom.addItem(key);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "take key");
      engine.processCommand(SESSION_ID, "north");
      final String unlockResponse = engine.processCommand(SESSION_ID, "unlock door");

      // Then
      final String message = JsonTestUtils.extractMessage(unlockResponse);
      assertEquals(TestOpenableLocation.DEFAULT_UNLOCK_MESSAGE + "\n\n", message);
    }

    @Test
    @DisplayName("get key, unlock, open - full sequence across locations")
    void testUnlockOpen_fullSequenceAcrossLocations() {
      // Given
      final TestGameMap map = new TestGameMap();

      final Location keyRoom = TestLocationFactory.createSimpleLocation("key-room");
      final TestOpenableLocation vaultRoom = TestOpenableLocation.builder("vault-room")
          .withUnlockTargets("door")
          .withOpenTargets("door")
          .withRequiredKey("key")
          .build();

      map.addLocation(keyRoom);
      map.addLocation(vaultRoom);
      TestLocationFactory.addBidirectionalConnection(keyRoom, Direction.NORTH, vaultRoom);

      final Item key = TestItemFactory.createTestKey();
      keyRoom.addItem(key);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "take key");
      engine.processCommand(SESSION_ID, "north");
      engine.processCommand(SESSION_ID, "unlock door");
      final String openResponse = engine.processCommand(SESSION_ID, "open door");

      // Then
      final String message = JsonTestUtils.extractMessage(openResponse);
      assertEquals(TestOpenableLocation.DEFAULT_OPEN_MESSAGE + "\n\n", message);
    }
  }

  @Nested
  class ErrorRecoveryFlow {

    @Test
    @DisplayName("fail to unlock, go get key, return and succeed")
    void testUnlock_errorRecoveryFlow() {
      // Given
      final TestGameMap map = new TestGameMap();

      final Location keyRoom = TestLocationFactory.createSimpleLocation("key-room");
      final TestOpenableLocation vaultRoom = TestOpenableLocation.builder("vault-room")
          .withUnlockTargets("door")
          .withOpenTargets("door")
          .withRequiredKey("key")
          .build();

      map.addLocation(keyRoom);
      map.addLocation(vaultRoom);
      TestLocationFactory.addBidirectionalConnection(keyRoom, Direction.NORTH, vaultRoom);

      final Item key = TestItemFactory.createTestKey();
      keyRoom.addItem(key);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      // When - try to unlock without key (start at key-room, go north first)
      engine.processCommand(SESSION_ID, "north");
      final String failResponse = engine.processCommand(SESSION_ID, "unlock door");

      // Then - fails
      final String failMessage = JsonTestUtils.extractMessage(failResponse);
      assertEquals(TestOpenableLocation.DEFAULT_UNLOCK_NO_KEY_MESSAGE + "\n\n", failMessage);

      // When - go back, get key, return and unlock
      engine.processCommand(SESSION_ID, "south");
      engine.processCommand(SESSION_ID, "take key");
      engine.processCommand(SESSION_ID, "north");
      final String successResponse = engine.processCommand(SESSION_ID, "unlock door");

      // Then - succeeds
      final String successMessage = JsonTestUtils.extractMessage(successResponse);
      assertEquals(TestOpenableLocation.DEFAULT_UNLOCK_MESSAGE + "\n\n", successMessage);
    }
  }
}
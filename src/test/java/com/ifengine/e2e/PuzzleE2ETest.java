package com.ifengine.e2e;

import com.ifengine.Direction;
import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.game.GameState;
import com.ifengine.game.Player;
import com.ifengine.test.TestGameEngine;
import com.ifengine.test.TestGameEngineBuilder;
import com.ifengine.test.TestGameMap;
import com.ifengine.test.TestItemFactory;
import com.ifengine.test.TestLocationFactory;
import com.ifengine.test.TestOpenableLocation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end tests for puzzle gameplay scenarios.
 * <p>
 * Tests complete puzzle scenarios as a player would experience them:
 * - Find key → unlock door → open door → enter new area
 * - Multi-step puzzles across locations
 */
@DisplayName("Puzzle E2E Tests")
class PuzzleE2ETest {

  private static final String SESSION_ID = "test-session";

  @Nested
  @DisplayName("Unlock Door Puzzle")
  class UnlockDoorPuzzle {

    @Test
    @DisplayName("full puzzle: find key → travel → unlock → open → verify state")
    void testPuzzle_findKeyUnlockOpenSequence() {
      // Given - create a multi-location puzzle scenario
      final TestGameMap map = new TestGameMap();

      // Start room (no key)
      final Location startRoom = TestLocationFactory.createSimpleLocation("start-room");

      // Key room (has the key)
      final Location keyRoom = TestLocationFactory.createSimpleLocation("key-room");
      final Item key = TestItemFactory.createTestKey();
      keyRoom.addItem(key);

      // Vault room (locked door)
      final TestOpenableLocation vaultRoom = TestOpenableLocation.builder("vault-room")
          .withUnlockTargets("door")
          .withOpenTargets("door")
          .withRequiredKey("key")
          .build();

      map.addLocation(startRoom);
      map.addLocation(keyRoom);
      map.addLocation(vaultRoom);

      // Connect: start → north → key-room
      //          start → east → vault-room
      TestLocationFactory.addBidirectionalConnection(startRoom, Direction.NORTH, keyRoom);
      TestLocationFactory.addBidirectionalConnection(startRoom, Direction.EAST, vaultRoom);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      // When - complete the puzzle
      // 1. Go north to key room
      engine.processCommand(SESSION_ID, "north");
      assertEquals("key-room", engine.getPlayer(SESSION_ID).getCurrentLocation().getName());

      // 2. Take the key
      engine.processCommand(SESSION_ID, "take key");
      assertTrue(engine.getPlayer(SESSION_ID).hasItem("key"));

      // 3. Go back south, then east to vault
      engine.processCommand(SESSION_ID, "south");
      engine.processCommand(SESSION_ID, "east");
      assertEquals("vault-room", engine.getPlayer(SESSION_ID).getCurrentLocation().getName());

      // 4. Unlock the door
      engine.processCommand(SESSION_ID, "unlock door");
      assertTrue(vaultRoom.isUnlocked());

      // 5. Open the door
      engine.processCommand(SESSION_ID, "open door");
      assertTrue(vaultRoom.isOpen());

      // Then - puzzle is complete
      final Player player = engine.getPlayer(SESSION_ID);
      assertTrue(player.hasItem("key"));
      assertTrue(vaultRoom.isUnlocked());
      assertTrue(vaultRoom.isOpen());
    }

    @Test
    @DisplayName("puzzle with auto-unlock: find key → travel → open (auto-unlocks)")
    void testPuzzle_autoUnlockOnOpen() {
      // Given
      final TestGameMap map = new TestGameMap();

      final Location keyRoom = TestLocationFactory.createSimpleLocation("key-room");
      final Item key = TestItemFactory.createTestKey();
      keyRoom.addItem(key);

      final TestOpenableLocation vaultRoom = TestOpenableLocation.builder("vault-room")
          .withUnlockTargets("door")
          .withOpenTargets("door")
          .withRequiredKey("key")
          .build();

      map.addLocation(keyRoom);
      map.addLocation(vaultRoom);
      TestLocationFactory.addBidirectionalConnection(keyRoom, Direction.NORTH, vaultRoom);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      // When - take key, go to vault, just open (skip unlock)
      engine.processCommand(SESSION_ID, "take key");
      engine.processCommand(SESSION_ID, "north");
      engine.processCommand(SESSION_ID, "open door");

      // Then - should auto-unlock and open
      assertTrue(vaultRoom.isUnlocked());
      assertTrue(vaultRoom.isOpen());
    }
  }

  @Nested
  @DisplayName("Multi-Step Exploration")
  class MultiStepExploration {

    @Test
    @DisplayName("explore multiple locations, collect items, return to start")
    void testExploration_collectItemsFromMultipleLocations() {
      // Given
      final TestGameMap map = new TestGameMap();

      final Location start = TestLocationFactory.createSimpleLocation("start");
      final Location forest = TestLocationFactory.createSimpleLocation("forest");
      final Location cave = TestLocationFactory.createSimpleLocation("cave");

      // Place items
      final Item key = TestItemFactory.createTestKey();
      final Item rope = TestItemFactory.createTestRope();
      final Item gem = TestItemFactory.createTestGem();

      start.addItem(key);
      forest.addItem(rope);
      cave.addItem(gem);

      map.addLocation(start);
      map.addLocation(forest);
      map.addLocation(cave);

      TestLocationFactory.addBidirectionalConnection(start, Direction.NORTH, forest);
      TestLocationFactory.addBidirectionalConnection(forest, Direction.EAST, cave);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      // When - explore and collect
      engine.processCommand(SESSION_ID, "take key");
      engine.processCommand(SESSION_ID, "north");
      engine.processCommand(SESSION_ID, "take rope");
      engine.processCommand(SESSION_ID, "east");
      engine.processCommand(SESSION_ID, "take gem");

      // Return to start
      engine.processCommand(SESSION_ID, "west");
      engine.processCommand(SESSION_ID, "south");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals("start", player.getCurrentLocation().getName());
      assertTrue(player.hasItem("key"));
      assertTrue(player.hasItem("rope"));
      assertTrue(player.hasItem("gem"));
      assertEquals(3, player.getInventory().size());

      // Items should be removed from locations
      assertFalse(start.hasItem("key"));
      assertFalse(forest.hasItem("rope"));
      assertFalse(cave.hasItem("gem"));
    }
  }
}
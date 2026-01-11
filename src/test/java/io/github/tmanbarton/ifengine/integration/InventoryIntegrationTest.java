package io.github.tmanbarton.ifengine.integration;

import io.github.tmanbarton.ifengine.Location;
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
 * Integration tests for inventory functionality.
 * <p>
 * These tests verify that inventory operations work correctly across multiple
 * commands and location changes - scenarios that unit tests don't cover because
 * they test handlers in isolation with manually constructed ParsedCommand objects.
 * <p>
 * Key scenarios tested:
 * - Item state persistence across movement
 * - Take/drop cycles with location changes
 * - Inventory display after various operations
 */
@DisplayName("Inventory Integration Tests")
class InventoryIntegrationTest {

  private static final String SESSION_ID = "test-session";
  private static final ResponseProvider RESPONSES = new DefaultResponses();

  @Nested
  class ItemPersistenceAcrossMovement {

    @Test
    @DisplayName("taken item stays in inventory after moving to new location")
    void testTakeItem_persistsAfterMovement() {
      // Given
      final TestGameEngine engine = TestFixtures.adventureScenario();
      engine.createPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "take key");
      engine.processCommand(SESSION_ID, "north");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertTrue(player.hasItem("key"));
    }

    @Test
    @DisplayName("taken item stays in inventory after multiple location changes")
    void testTakeItem_persistsAfterMultipleMoves() {
      // Given
      final TestGameEngine engine = TestFixtures.adventureScenario();
      engine.createPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "take key");
      engine.processCommand(SESSION_ID, "north");
      engine.processCommand(SESSION_ID, "east");
      engine.processCommand(SESSION_ID, "down");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertTrue(player.hasItem("key"));
      assertEquals("treasure-room", player.getCurrentLocation().getName());
    }

    @Test
    @DisplayName("dropped item stays at original location after player moves away")
    void testDropItem_staysAtLocationAfterPlayerMoves() {
      // Given
      final TestGameEngine engine = TestFixtures.adventureScenario();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "take key");
      engine.processCommand(SESSION_ID, "north");

      // When
      engine.processCommand(SESSION_ID, "drop key");
      final Location forestLocation = engine.getPlayer(SESSION_ID).getCurrentLocation();
      engine.processCommand(SESSION_ID, "south");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertFalse(player.hasItem("key"));
      assertTrue(forestLocation.hasItem("key"));
    }

    @Test
    @DisplayName("can retrieve item from location after returning")
    void testDropItem_canRetrieveAfterReturning() {
      // Given
      final TestGameEngine engine = TestFixtures.adventureScenario();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "take key");
      engine.processCommand(SESSION_ID, "north");
      engine.processCommand(SESSION_ID, "drop key");
      engine.processCommand(SESSION_ID, "south");

      // When
      engine.processCommand(SESSION_ID, "north");
      engine.processCommand(SESSION_ID, "take key");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertTrue(player.hasItem("key"));
    }
  }

  @Nested
  class TakeDropCycles {

    @Test
    @DisplayName("take then drop returns item to current location")
    void testTakeThenDrop_returnsItemToLocation() {
      // Given
      final TestGameEngine engine = TestFixtures.itemInteractionScenario();
      engine.createPlayer(SESSION_ID);
      final Location startLocation = engine.getPlayer(SESSION_ID).getCurrentLocation();

      // When
      engine.processCommand(SESSION_ID, "take key");
      assertFalse(startLocation.hasItem("key"));
      engine.processCommand(SESSION_ID, "drop key");

      // Then
      assertTrue(startLocation.hasItem("key"));
      assertFalse(engine.getPlayer(SESSION_ID).hasItem("key"));
    }

    @Test
    @DisplayName("take all then drop all works correctly")
    void testTakeAllDropAll_correctState() {
      // Given
      final TestGameEngine engine = TestFixtures.itemInteractionScenario();
      engine.createPlayer(SESSION_ID);
      final Location startLocation = engine.getPlayer(SESSION_ID).getCurrentLocation();
      final int originalItemCount = startLocation.getItems().size();

      // When
      engine.processCommand(SESSION_ID, "take all");
      assertEquals(0, startLocation.getItems().size());
      assertEquals(originalItemCount, engine.getPlayer(SESSION_ID).getInventory().size());
      engine.processCommand(SESSION_ID, "drop all");

      // Then
      assertEquals(originalItemCount, startLocation.getItems().size());
      assertEquals(0, engine.getPlayer(SESSION_ID).getInventory().size());
    }

    @Test
    @DisplayName("multiple take/drop operations maintain correct state")
    void testMultipleTakeDropOperations_correctState() {
      // Given
      final TestGameEngine engine = TestFixtures.itemInteractionScenario();
      engine.createPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "take key");
      engine.processCommand(SESSION_ID, "take rope");
      engine.processCommand(SESSION_ID, "drop key");
      engine.processCommand(SESSION_ID, "take gem");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertFalse(player.hasItem("key"));
      assertTrue(player.hasItem("rope"));
      assertTrue(player.hasItem("gem"));
      assertTrue(player.getCurrentLocation().hasItem("key"));
    }
  }

  @Nested
  class InventoryDisplay {

    @Test
    @DisplayName("inventory command shows empty message when nothing carried")
    void testInventory_emptyInventory() {
      // Given
      final TestGameEngine engine = TestFixtures.singleLocationPlayingScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "inventory");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getInventoryEmpty() + "\n\n", message);
    }

    @Test
    @DisplayName("inventory command shows items after taking them")
    void testInventory_afterTakingItems() {
      // Given
      final TestGameEngine engine = TestFixtures.itemInteractionScenario();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "take key");
      engine.processCommand(SESSION_ID, "take rope");

      // When
      engine.processCommand(SESSION_ID, "inventory");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals(2, player.getInventory().size());
      assertTrue(player.hasItem("key"));
      assertTrue(player.hasItem("rope"));
    }
  }

  @Nested
  class ItemsFromDifferentLocations {

    @Test
    @DisplayName("can collect items from multiple locations")
    void testCollectItems_fromMultipleLocations() {
      // Given
      final TestGameEngine engine = TestFixtures.adventureScenario();
      engine.createPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "take key");
      engine.processCommand(SESSION_ID, "north");
      engine.processCommand(SESSION_ID, "take rope");
      engine.processCommand(SESSION_ID, "east");
      engine.processCommand(SESSION_ID, "down");
      engine.processCommand(SESSION_ID, "take gem");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertTrue(player.hasItem("key"));
      assertTrue(player.hasItem("rope"));
      assertTrue(player.hasItem("gem"));
      assertEquals(3, player.getInventory().size());
    }

    @Test
    @DisplayName("can redistribute items to different locations")
    void testRedistributeItems_toDifferentLocations() {
      // Given
      final TestGameEngine engine = TestFixtures.adventureScenario();
      engine.createPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "take key");
      engine.processCommand(SESSION_ID, "north");
      engine.processCommand(SESSION_ID, "take rope");
      engine.processCommand(SESSION_ID, "drop key");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      final Location forestLocation = player.getCurrentLocation();
      assertFalse(player.hasItem("key"));
      assertTrue(player.hasItem("rope"));
      assertTrue(forestLocation.hasItem("key"));
    }
  }
}
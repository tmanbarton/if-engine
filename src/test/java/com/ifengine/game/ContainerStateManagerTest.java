package com.ifengine.game;

import com.ifengine.Container;
import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.SceneryContainer;
import com.ifengine.test.TestItemFactory;
import com.ifengine.test.TestLocationFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for ContainerStateManager.
 * Tests the dual-ownership pattern for inventory vs location containers.
 *
 * Inventory containers (bags, jars): items stay in player's possession, tracked by manager
 * Location containers (tables, shelves): items placed at location, delegated to Location
 */
@DisplayName("ContainerStateManager Tests")
class ContainerStateManagerTest {

  private ContainerStateManager manager;
  private Player player;
  private Location location;

  @BeforeEach
  void setUp() {
    manager = new ContainerStateManager();
    location = TestLocationFactory.createDefaultLocation();
    player = new Player(location);
    player.setGameState(GameState.PLAYING);
  }

  @Nested
  @DisplayName("Inventory Container Operations")
  class InventoryContainerOperations {

    @Test
    @DisplayName("Test markItemAsContained - inventory container stores in manager")
    void testMarkItemAsContained_inventoryContainer() {
      final Item key = TestItemFactory.createTestKey();
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 5);

      manager.markItemAsContained(key, bag, player);

      assertTrue(manager.isItemContained(key, player));
      assertEquals(bag, manager.getContainerForItem(key, player));
    }

    @Test
    @DisplayName("Test isItemContained - returns false for non-contained item")
    void testIsItemContained_notContained() {
      final Item key = TestItemFactory.createTestKey();

      assertFalse(manager.isItemContained(key, player));
    }

    @Test
    @DisplayName("Test getContainerForItem - returns container for contained item")
    void testGetContainerForItem_returnsContainer() {
      final Item key = TestItemFactory.createTestKey();
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 5);
      manager.markItemAsContained(key, bag, player);

      final Container result = manager.getContainerForItem(key, player);

      assertEquals(bag, result);
    }

    @Test
    @DisplayName("Test getContainerForItem - returns null for non-contained item")
    void testGetContainerForItem_notContained() {
      final Item key = TestItemFactory.createTestKey();

      final Container result = manager.getContainerForItem(key, player);

      assertNull(result);
    }

    @Test
    @DisplayName("Test getContainedItems - returns items in inventory container")
    void testGetContainedItems_inventoryContainer() {
      final Item key = TestItemFactory.createTestKey();
      final Item gem = TestItemFactory.createTestGem();
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 5);
      manager.markItemAsContained(key, bag, player);
      manager.markItemAsContained(gem, bag, player);

      final List<Item> containedItems = manager.getContainedItems(bag, player);

      assertEquals(2, containedItems.size());
      assertTrue(containedItems.contains(key));
      assertTrue(containedItems.contains(gem));
    }

    @Test
    @DisplayName("Test getContainedItems - returns empty list for empty container")
    void testGetContainedItems_emptyContainer() {
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 5);

      final List<Item> containedItems = manager.getContainedItems(bag, player);

      assertTrue(containedItems.isEmpty());
    }

    @Test
    @DisplayName("Test removeContainment - removes from inventory containment")
    void testRemoveContainment_inventoryContainer() {
      final Item key = TestItemFactory.createTestKey();
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 5);
      manager.markItemAsContained(key, bag, player);

      manager.removeContainment(key, player);

      assertFalse(manager.isItemContained(key, player));
      assertNull(manager.getContainerForItem(key, player));
    }

    @Test
    @DisplayName("Test clearAllInventoryContainment - clears all inventory state")
    void testClearAllInventoryContainment_clearsAll() {
      final Item key = TestItemFactory.createTestKey();
      final Item gem = TestItemFactory.createTestGem();
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 5);
      manager.markItemAsContained(key, bag, player);
      manager.markItemAsContained(gem, bag, player);

      manager.clearAllInventoryContainment();

      assertFalse(manager.isItemContained(key, player));
      assertFalse(manager.isItemContained(gem, player));
    }
  }

  @Nested
  @DisplayName("Location Container Operations")
  class LocationContainerOperations {

    @Test
    @DisplayName("Test markItemAsContained - location container delegates to Location")
    void testMarkItemAsContained_locationContainer() {
      final Item key = TestItemFactory.createTestKey();
      final SceneryContainer table = TestItemFactory.createSceneryContainer("table");
      location.addSceneryContainer(table);
      location.addItem(key);

      manager.markItemAsContained(key, table, player);

      assertTrue(manager.isItemContained(key, player));
      assertEquals(table, manager.getContainerForItem(key, player));
      // Also verify Location tracks it
      assertTrue(location.isItemInContainer(key));
    }

    @Test
    @DisplayName("Test isItemContained - checks location containers")
    void testIsItemContained_locationContainer() {
      final Item key = TestItemFactory.createTestKey();
      final SceneryContainer table = TestItemFactory.createSceneryContainer("table");
      location.addSceneryContainer(table);
      location.addItem(key);
      manager.markItemAsContained(key, table, player);

      assertTrue(manager.isItemContained(key, player));
    }

    @Test
    @DisplayName("Test getContainerForItem - finds location container")
    void testGetContainerForItem_locationContainer() {
      final Item key = TestItemFactory.createTestKey();
      final SceneryContainer table = TestItemFactory.createSceneryContainer("table");
      location.addSceneryContainer(table);
      location.addItem(key);
      manager.markItemAsContained(key, table, player);

      final Container result = manager.getContainerForItem(key, player);

      assertEquals(table, result);
    }

    @Test
    @DisplayName("Test getContainedItems - returns items in location container")
    void testGetContainedItems_locationContainer() {
      final Item key = TestItemFactory.createTestKey();
      final Item gem = TestItemFactory.createTestGem();
      final SceneryContainer table = TestItemFactory.createSceneryContainer("table");
      location.addSceneryContainer(table);
      location.addItem(key);
      location.addItem(gem);
      manager.markItemAsContained(key, table, player);
      manager.markItemAsContained(gem, table, player);

      final List<Item> containedItems = manager.getContainedItems(table, player);

      assertEquals(2, containedItems.size());
      assertTrue(containedItems.contains(key));
      assertTrue(containedItems.contains(gem));
    }

    @Test
    @DisplayName("Test removeContainment - removes from location containment")
    void testRemoveContainment_locationContainer() {
      final Item key = TestItemFactory.createTestKey();
      final SceneryContainer table = TestItemFactory.createSceneryContainer("table");
      location.addSceneryContainer(table);
      location.addItem(key);
      manager.markItemAsContained(key, table, player);

      manager.removeContainment(key, player);

      assertFalse(manager.isItemContained(key, player));
      assertFalse(location.isItemInContainer(key));
    }
  }

  @Nested
  @DisplayName("Mixed Container Operations")
  class MixedContainerOperations {

    @Test
    @DisplayName("Test isItemContained - checks both inventory and location")
    void testIsItemContained_checksBoth() {
      final Item keyInBag = TestItemFactory.createTestKey();
      final Item gemOnTable = TestItemFactory.createTestGem();
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 5);
      final SceneryContainer table = TestItemFactory.createSceneryContainer("table");
      location.addSceneryContainer(table);
      location.addItem(gemOnTable);

      manager.markItemAsContained(keyInBag, bag, player);
      manager.markItemAsContained(gemOnTable, table, player);

      assertTrue(manager.isItemContained(keyInBag, player));
      assertTrue(manager.isItemContained(gemOnTable, player));
    }

    @Test
    @DisplayName("Test getContainerForItem - inventory checked first")
    void testGetContainerForItem_inventoryPriority() {
      final Item key = TestItemFactory.createTestKey();
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 5);
      manager.markItemAsContained(key, bag, player);

      // Even if location also tracks a container (simulated), inventory should be found first
      final Container result = manager.getContainerForItem(key, player);

      assertEquals(bag, result);
    }

    @Test
    @DisplayName("Test clearAllInventoryContainment - does not affect location containers")
    void testClearAllInventoryContainment_doesNotAffectLocation() {
      final Item keyInBag = TestItemFactory.createTestKey();
      final Item gemOnTable = TestItemFactory.createTestGem();
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 5);
      final SceneryContainer table = TestItemFactory.createSceneryContainer("table");
      location.addSceneryContainer(table);
      location.addItem(gemOnTable);
      manager.markItemAsContained(keyInBag, bag, player);
      manager.markItemAsContained(gemOnTable, table, player);

      manager.clearAllInventoryContainment();

      assertFalse(manager.isItemContained(keyInBag, player));
      // Location container should still track the item
      assertTrue(manager.isItemContained(gemOnTable, player));
    }
  }
}
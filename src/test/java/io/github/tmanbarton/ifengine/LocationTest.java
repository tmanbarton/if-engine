package io.github.tmanbarton.ifengine;

import io.github.tmanbarton.ifengine.test.TestItemFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for Location class.
 * Tests items, connections, scenery, and container tracking.
 */
@DisplayName("Location Tests")
class LocationTest {

  private Location location;

  @BeforeEach
  void setUp() {
    location = new Location(
        "test-room",
        "A large test room with bare walls.",
        "Test room"
    );
  }

  @Nested
  class ItemManagement {

    @Test
    @DisplayName("Test addItem - multiple items")
    void testAddItem_multipleItems() {
      final Item key = TestItemFactory.createTestKey();
      final Item rope = TestItemFactory.createTestRope();

      location.addItem(key);
      location.addItem(rope);

      assertEquals(2, location.getItems().size());
      assertTrue(location.getItems().contains(key));
      assertTrue(location.getItems().contains(rope));
    }

    @Test
    @DisplayName("Test removeItem - removes item from location")
    void testRemoveItem_removesItem() {
      final Item key = TestItemFactory.createTestKey();
      location.addItem(key);

      final boolean result = location.removeItem(key);

      assertTrue(result);
      assertFalse(location.getItems().contains(key));
    }

    @Test
    @DisplayName("Test getItemByName - finds item by exact name")
    void testGetItemByName_exactMatch() {
      final Item key = TestItemFactory.createTestKey();
      location.addItem(key);

      final Item result = location.getItemByName("key");

      assertNotNull(result);
      assertEquals(key, result);
    }

    @Test
    @DisplayName("Test getItemByName - finds by alias")
    void testGetItemByName_byAlias() {
      final Item key = TestItemFactory.createItemWithAliases(
          "key",
          "A brass key",
          "Key here.",
          "A key.",
          Set.of("brass key")
      );
      location.addItem(key);

      final Item result = location.getItemByName("brass key");

      assertEquals(key, result);
    }
  }

  @Nested
  class ConnectionManagement {

    @Test
    @DisplayName("Test addConnection - multiple directions")
    void testAddConnection_multipleDirections() {
      final Location forest = new Location("forest", "A forest.", "Forest");
      final Location cave = new Location("cave", "A cave.", "Cave");

      location.addConnection(Direction.NORTH, forest);
      location.addConnection(Direction.EAST, cave);

      assertEquals(forest, location.getConnection(Direction.NORTH));
      assertEquals(cave, location.getConnection(Direction.EAST));
    }

    @Test
    @DisplayName("Test replaceConnection - overwrites existing connection")
    void testReplaceConnection_overwritesExisting() {
      final Location forest = new Location("forest", "A forest.", "Forest");
      final Location meadow = new Location("meadow", "A meadow.", "Meadow");
      location.addConnection(Direction.NORTH, forest);

      location.replaceConnection(Direction.NORTH, meadow);

      assertEquals(meadow, location.getConnection(Direction.NORTH));
    }

    @Test
    @DisplayName("Test getAvailableDirections - returns connected directions")
    void testGetAvailableDirections_returnsConnectedDirections() {
      final Location forest = new Location("forest", "A forest.", "Forest");
      final Location cave = new Location("cave", "A cave.", "Cave");
      location.addConnection(Direction.NORTH, forest);
      location.addConnection(Direction.EAST, cave);

      final Set<Direction> directions = location.getAvailableDirections();

      assertEquals(2, directions.size());
      assertTrue(directions.contains(Direction.NORTH));
      assertTrue(directions.contains(Direction.EAST));
    }
  }

  @Nested
  class SceneryObjectManagement {

    @Test
    @DisplayName("Test findSceneryObject - finds by name")
    void testFindSceneryObject_findsByName() {
      final SceneryObject tree = SceneryObject.builder("tree")
          .withInteraction(InteractionType.CLIMB, "You climb the tree.")
          .build();
      location.addSceneryObject(tree);

      final Optional<SceneryObject> result = location.findSceneryObject("tree");

      assertTrue(result.isPresent());
      assertEquals(tree, result.get());
    }

    @Test
    @DisplayName("Test findSceneryObject - finds by alias")
    void testFindSceneryObject_findsByAlias() {
      final SceneryObject tree = SceneryObject.builder("tree")
          .withAliases("oak", "big tree")
          .withInteraction(InteractionType.CLIMB, "You climb the tree.")
          .build();
      location.addSceneryObject(tree);

      assertTrue(location.findSceneryObject("oak").isPresent());
      assertTrue(location.findSceneryObject("big tree").isPresent());
    }

    @Test
    @DisplayName("Test findSceneryObjectsByInteraction - finds objects with interaction")
    void testFindSceneryObjectsByInteraction_findsWithInteraction() {
      final SceneryObject tree = SceneryObject.builder("tree")
          .withInteraction(InteractionType.CLIMB, "You climb the tree.")
          .build();
      final SceneryObject rock = SceneryObject.builder("rock")
          .withInteraction(InteractionType.KICK, "You kick the rock. Ouch.")
          .build();
      location.addSceneryObject(tree);
      location.addSceneryObject(rock);

      final List<SceneryObject> result = location.findSceneryObjectsByInteraction(InteractionType.CLIMB);

      assertEquals(1, result.size());
      assertEquals(tree, result.get(0));
    }
  }

  @Nested
  class LocationContainerManagement {

    @Test
    @DisplayName("Test addSceneryObject - auto-registers LocationContainer for container scenery")
    void testAddSceneryObject_registersContainerForContainerScenery() {
      final SceneryObject table = SceneryObject.builder("table")
          .withInteraction(InteractionType.LOOK, "A table.")
          .asContainer()
          .build();

      location.addSceneryObject(table);

      assertEquals(1, location.getLocationContainers().size());
      assertEquals(table, location.getLocationContainers().get(0).getSceneryObject());
      assertTrue(location.findSceneryObject("table").isPresent());
    }
  }

  @Nested
  class SceneryContainerAutoRegistration {

    @Test
    @DisplayName("Test addSceneryObject - auto-creates LocationContainer for container scenery")
    void testAddSceneryObject_autoCreatesLocationContainer() {
      final SceneryObject table = SceneryObject.builder("table")
          .withInteraction(InteractionType.LOOK, "A wooden table.")
          .asContainer()
          .build();

      location.addSceneryObject(table);

      assertEquals(1, location.getLocationContainers().size());
      assertEquals(table, location.getLocationContainers().get(0).getSceneryObject());
    }

    @Test
    @DisplayName("Test addSceneryObject - does not create container for non-container scenery")
    void testAddSceneryObject_noContainerForNonContainerScenery() {
      final SceneryObject tree = SceneryObject.builder("tree")
          .withInteraction(InteractionType.LOOK, "A tall tree.")
          .build();

      location.addSceneryObject(tree);

      assertTrue(location.getLocationContainers().isEmpty());
      assertTrue(location.findSceneryObject("tree").isPresent());
    }
  }

  @Nested
  class ItemContainmentTracking {

    @Test
    @DisplayName("Test setItemContainer - tracks containment")
    void testSetItemContainer_tracksContainment() {
      final Item key = TestItemFactory.createTestKey();
      final SceneryObject tableScenery = SceneryObject.builder("table")
          .withInteraction(InteractionType.LOOK, "A table.")
          .asContainer()
          .build();
      location.addItem(key);
      location.addSceneryObject(tableScenery);
      final LocationContainer table = location.getLocationContainers().get(0);

      location.setItemContainer(key, table);

      assertTrue(location.isItemInContainer(key));
      assertEquals(table, location.getContainerForItem(key));
    }

    @Test
    @DisplayName("Test removeItemFromContainer - removes tracking")
    void testRemoveItemFromContainer_removesTracking() {
      final Item key = TestItemFactory.createTestKey();
      final SceneryObject tableScenery = SceneryObject.builder("table")
          .withInteraction(InteractionType.LOOK, "A table.")
          .asContainer()
          .build();
      location.addItem(key);
      location.addSceneryObject(tableScenery);
      final LocationContainer table = location.getLocationContainers().get(0);
      location.setItemContainer(key, table);

      location.removeItemFromContainer(key);

      assertFalse(location.isItemInContainer(key));
      assertNull(location.getContainerForItem(key));
    }
  }
}
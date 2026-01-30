package io.github.tmanbarton.ifengine.game;

import io.github.tmanbarton.ifengine.Direction;
import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.test.TestItemFactory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for GameMap builder-style API.
 */
@DisplayName("GameMap Tests")
class GameMapTest {

  @Nested
  class BuildValidation {

    @Test
    @DisplayName("build - throws when no locations added")
    void testBuild_throwsWhenNoLocations() {
      final GameMap.Builder builder = new GameMap.Builder();

      final IllegalStateException exception = assertThrows(
          IllegalStateException.class,
          builder::build
      );

      assertEquals("GameMap must have at least one location", exception.getMessage());
    }

    @Test
    @DisplayName("build - throws when starting location not set")
    void testBuild_throwsWhenStartingLocationNotSet() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
      final GameMap.Builder builder = new GameMap.Builder()
          .addLocation(cottage);

      final IllegalStateException exception = assertThrows(
          IllegalStateException.class,
          builder::build
      );

      assertEquals(
          "Starting location must be set. Call setStartingLocation() before build()",
          exception.getMessage()
      );
    }

    @Test
    @DisplayName("build - succeeds with valid configuration")
    void testBuild_succeedsWithValidConfiguration() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");

      final GameMap gameMap = new GameMap.Builder()
          .addLocation(cottage)
          .setStartingLocation("cottage")
          .build();

      assertNotNull(gameMap);
      assertEquals(cottage, gameMap.getStartingLocation());
    }
  }

  @Nested
  class BuilderApi {

    @Test
    @DisplayName("addLocation - adds location to map")
    void testAddLocation_addsToMap() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");

      final GameMap gameMap = new GameMap.Builder()
          .addLocation(cottage)
          .setStartingLocation("cottage")
          .build();

      assertEquals(cottage, gameMap.getLocation("cottage"));
    }

    @Test
    @DisplayName("placeItem - registers item in map")
    void testPlaceItem_registersItemInMap() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
      final Item key = TestItemFactory.createTestKey();

      final GameMap gameMap = new GameMap.Builder()
          .addLocation(cottage)
          .placeItem(key, "cottage")
          .setStartingLocation("cottage")
          .build();

      assertEquals(key, gameMap.getItem("key"));
    }

    @Test
    @DisplayName("connect - creates bidirectional connection")
    void testConnect_createsBidirectionalConnection() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
      final Location forest = new Location("forest", "A dark forest.", "In the forest.");

      new GameMap.Builder()
          .addLocation(cottage)
          .addLocation(forest)
          .connect("cottage", Direction.NORTH, "forest")
          .setStartingLocation("cottage")
          .build();

      assertEquals(forest, cottage.getConnection(Direction.NORTH));
      assertEquals(cottage, forest.getConnection(Direction.SOUTH));
    }

    @Test
    @DisplayName("connect - throws when from location not found")
    void testConnect_throwsWhenFromNotFound() {
      final Location forest = new Location("forest", "A dark forest.", "In the forest.");
      final GameMap.Builder builder = new GameMap.Builder()
          .addLocation(forest);

      final IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> builder.connect("cottage", Direction.NORTH, "forest")
      );

      assertTrue(exception.getMessage().contains("cottage"));
    }

    @Test
    @DisplayName("connect - throws when to location not found")
    void testConnect_throwsWhenToNotFound() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
      final GameMap.Builder builder = new GameMap.Builder()
          .addLocation(cottage);

      final IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> builder.connect("cottage", Direction.NORTH, "forest")
      );

      assertTrue(exception.getMessage().contains("forest"));
    }

    @Test
    @DisplayName("connectOneWay - creates unidirectional connection")
    void testConnectOneWay_createsUnidirectionalConnection() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
      final Location forest = new Location("forest", "A dark forest.", "In the forest.");

      new GameMap.Builder()
          .addLocation(cottage)
          .addLocation(forest)
          .connectOneWay("cottage", Direction.NORTH, "forest")
          .setStartingLocation("cottage")
          .build();

      assertEquals(forest, cottage.getConnection(Direction.NORTH));
      assertNull(forest.getConnection(Direction.SOUTH));
    }

    @Test
    @DisplayName("setStartingLocation - sets starting location")
    void testSetStartingLocation_setsStartingLocation() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");

      final GameMap gameMap = new GameMap.Builder()
          .addLocation(cottage)
          .setStartingLocation("cottage")
          .build();

      assertEquals(cottage, gameMap.getStartingLocation());
    }

    @Test
    @DisplayName("setStartingLocation - throws when location not found")
    void testSetStartingLocation_throwsWhenNotFound() {
      final GameMap.Builder builder = new GameMap.Builder();

      final IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> builder.setStartingLocation("cottage")
      );

      assertTrue(exception.getMessage().contains("cottage"));
    }

    @Test
    @DisplayName("placeItem - places item in location")
    void testPlaceItem_placesItemInLocation() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
      final Item key = TestItemFactory.createTestKey();

      new GameMap.Builder()
          .addLocation(cottage)
          .placeItem(key, "cottage")
          .setStartingLocation("cottage")
          .build();

      assertTrue(cottage.hasItem("key"));
    }

    @Test
    @DisplayName("placeItem - throws when location not found")
    void testPlaceItem_throwsWhenLocationNotFound() {
      final Item key = TestItemFactory.createTestKey();
      final GameMap.Builder builder = new GameMap.Builder();

      final IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> builder.placeItem(key, "cottage")
      );

      assertTrue(exception.getMessage().contains("cottage"));
    }
  }

  @Nested
  class HiddenItemBuilder {

    @Test
    @DisplayName("placeHiddenItem - item is hidden at location")
    void testPlaceHiddenItem_itemIsHiddenAtLocation() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
      final Item key = TestItemFactory.createTestKey();

      new GameMap.Builder()
          .addLocation(cottage)
          .placeHiddenItem(key, "cottage", "A key is under the table.")
          .setStartingLocation("cottage")
          .build();

      assertFalse(cottage.hasItem("key"));
      assertTrue(cottage.isItemHidden(key));
    }

    @Test
    @DisplayName("placeHiddenItem - throws when location not found")
    void testPlaceHiddenItem_throwsWhenLocationNotFound() {
      final Item key = TestItemFactory.createTestKey();
      final GameMap.Builder builder = new GameMap.Builder();

      final IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> builder.placeHiddenItem(key, "cottage", "A key is under the table.")
      );

      assertTrue(exception.getMessage().contains("cottage"));
    }
  }

  @Nested
  class Reset {

    @Test
    @DisplayName("resetMap - restores items to initial locations")
    void testResetMap_restoresItemsToInitialLocations() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
      final Item key = TestItemFactory.createTestKey();

      final GameMap gameMap = new GameMap.Builder()
          .addLocation(cottage)
          .placeItem(key, "cottage")
          .setStartingLocation("cottage")
          .build();

      // Simulate taking the item
      cottage.removeItem(key);

      gameMap.resetMap();

      assertTrue(cottage.hasItem("key"));
    }

    @Test
    @DisplayName("resetMap - restores hidden items to hidden state")
    void testResetMap_restoresHiddenItemsToHiddenState() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
      final Item key = TestItemFactory.createTestKey();

      final GameMap gameMap = new GameMap.Builder()
          .addLocation(cottage)
          .placeHiddenItem(key, "cottage", "A key is under the table.")
          .setStartingLocation("cottage")
          .build();

      // Simulate reveal + take
      cottage.revealItem(key);
      cottage.removeItem(key);

      gameMap.resetMap();

      assertTrue(cottage.isItemHidden(key));
      assertFalse(cottage.hasItem("key"));
    }

    @Test
    @DisplayName("resetMap - revealed item returned to hidden after reset")
    void testResetMap_revealedItemReturnedToHidden() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
      final Item key = TestItemFactory.createTestKey();

      final GameMap gameMap = new GameMap.Builder()
          .addLocation(cottage)
          .placeHiddenItem(key, "cottage", "A key is under the table.")
          .setStartingLocation("cottage")
          .build();

      // Simulate reveal only (not taken)
      cottage.revealItem(key);
      assertTrue(cottage.hasItem("key"));

      gameMap.resetMap();

      // Should be hidden again
      assertTrue(cottage.isItemHidden(key));
      assertFalse(cottage.hasItem("key"));
    }

    @Test
    @DisplayName("resetMap - resets visited flags")
    void testResetMap_resetsVisitedFlags() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
      cottage.setVisited(true);

      final GameMap gameMap = new GameMap.Builder()
          .addLocation(cottage)
          .setStartingLocation("cottage")
          .build();

      gameMap.resetMap();

      assertFalse(cottage.isVisited());
    }
  }

  @Nested
  class DirectionOpposites {

    @Test
    @DisplayName("connect NORTH creates SOUTH return")
    void testConnect_northCreatesSouthReturn() {
      final Location a = new Location("a", "Location A", "A");
      final Location b = new Location("b", "Location B", "B");

      final GameMap gameMap = new GameMap.Builder()
          .addLocation(a)
          .addLocation(b)
          .connect("a", Direction.NORTH, "b")
          .setStartingLocation("a")
          .build();

      assertNotNull(gameMap.getLocation("b").getConnection(Direction.SOUTH));
    }

    @Test
    @DisplayName("connect EAST creates WEST return")
    void testConnect_eastCreatesWestReturn() {
      final Location a = new Location("a", "Location A", "A");
      final Location b = new Location("b", "Location B", "B");

      final GameMap gameMap = new GameMap.Builder()
          .addLocation(a)
          .addLocation(b)
          .connect("a", Direction.EAST, "b")
          .setStartingLocation("a")
          .build();

      assertNotNull(gameMap.getLocation("b").getConnection(Direction.WEST));
    }

    @Test
    @DisplayName("connect UP creates DOWN return")
    void testConnect_upCreatesDownReturn() {
      final Location a = new Location("a", "Location A", "A");
      final Location b = new Location("b", "Location B", "B");

      final GameMap gameMap = new GameMap.Builder()
          .addLocation(a)
          .addLocation(b)
          .connect("a", Direction.UP, "b")
          .setStartingLocation("a")
          .build();

      assertNotNull(gameMap.getLocation("b").getConnection(Direction.DOWN));
    }

    @Test
    @DisplayName("connect NORTHEAST creates SOUTHWEST return")
    void testConnect_northeastCreatesSouthwestReturn() {
      final Location a = new Location("a", "Location A", "A");
      final Location b = new Location("b", "Location B", "B");

      final GameMap gameMap = new GameMap.Builder()
          .addLocation(a)
          .addLocation(b)
          .connect("a", Direction.NORTHEAST, "b")
          .setStartingLocation("a")
          .build();

      assertNotNull(gameMap.getLocation("b").getConnection(Direction.SOUTHWEST));
    }

    @Test
    @DisplayName("connect IN creates OUT return")
    void testConnect_inCreatesOutReturn() {
      final Location a = new Location("a", "Location A", "A");
      final Location b = new Location("b", "Location B", "B");

      final GameMap gameMap = new GameMap.Builder()
          .addLocation(a)
          .addLocation(b)
          .connect("a", Direction.IN, "b")
          .setStartingLocation("a")
          .build();

      assertNotNull(gameMap.getLocation("b").getConnection(Direction.OUT));
    }
  }
}

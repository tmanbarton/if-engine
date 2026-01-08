package com.ifengine.game;

import com.ifengine.Direction;
import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.test.TestItemFactory;
import com.ifengine.test.TestLocationFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
  @DisplayName("Builder API")
  class BuilderApi {

    private GameMap gameMap;

    @BeforeEach
    void setUp() {
      gameMap = new GameMap();
    }

    @Test
    @DisplayName("addLocation - adds location to map")
    void testAddLocation_addsToMap() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");

      gameMap.addLocation(cottage);

      assertEquals(cottage, gameMap.getLocation("cottage"));
    }

    @Test
    @DisplayName("addLocation - returns GameMap for chaining")
    void testAddLocation_returnsGameMapForChaining() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");

      final GameMap result = gameMap.addLocation(cottage);

      assertEquals(gameMap, result);
    }

    @Test
    @DisplayName("addItem - adds item to map")
    void testAddItem_addsToMap() {
      final Item key = TestItemFactory.createTestKey();

      gameMap.addItem(key);

      assertEquals(key, gameMap.getItem("key"));
    }

    @Test
    @DisplayName("connect - creates bidirectional connection")
    void testConnect_createsBidirectionalConnection() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
      final Location forest = new Location("forest", "A dark forest.", "In the forest.");

      gameMap.addLocation(cottage)
          .addLocation(forest)
          .connect("cottage", Direction.NORTH, "forest");

      assertEquals(forest, cottage.getConnection(Direction.NORTH));
      assertEquals(cottage, forest.getConnection(Direction.SOUTH));
    }

    @Test
    @DisplayName("connect - throws when from location not found")
    void testConnect_throwsWhenFromNotFound() {
      final Location forest = new Location("forest", "A dark forest.", "In the forest.");
      gameMap.addLocation(forest);

      final IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> gameMap.connect("cottage", Direction.NORTH, "forest")
      );

      assertTrue(exception.getMessage().contains("cottage"));
    }

    @Test
    @DisplayName("connect - throws when to location not found")
    void testConnect_throwsWhenToNotFound() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
      gameMap.addLocation(cottage);

      final IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> gameMap.connect("cottage", Direction.NORTH, "forest")
      );

      assertTrue(exception.getMessage().contains("forest"));
    }

    @Test
    @DisplayName("connectOneWay - creates unidirectional connection")
    void testConnectOneWay_createsUnidirectionalConnection() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
      final Location forest = new Location("forest", "A dark forest.", "In the forest.");

      gameMap.addLocation(cottage)
          .addLocation(forest)
          .connectOneWay("cottage", Direction.NORTH, "forest");

      assertEquals(forest, cottage.getConnection(Direction.NORTH));
      assertNull(forest.getConnection(Direction.SOUTH));
    }

    @Test
    @DisplayName("setStartingLocation - sets starting location")
    void testSetStartingLocation_setsStartingLocation() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");

      gameMap.addLocation(cottage)
          .setStartingLocation("cottage");

      assertEquals(cottage, gameMap.getStartingLocation());
    }

    @Test
    @DisplayName("setStartingLocation - throws when location not found")
    void testSetStartingLocation_throwsWhenNotFound() {
      final IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> gameMap.setStartingLocation("cottage")
      );

      assertTrue(exception.getMessage().contains("cottage"));
    }

    @Test
    @DisplayName("getStartingLocation - throws when not set")
    void testGetStartingLocation_throwsWhenNotSet() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
      gameMap.addLocation(cottage);

      assertThrows(IllegalStateException.class, gameMap::getStartingLocation);
    }

    @Test
    @DisplayName("placeItem - places item in location")
    void testPlaceItem_placesItemInLocation() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
      final Item key = TestItemFactory.createTestKey();

      gameMap.addLocation(cottage)
          .addItem(key)
          .placeItem("key", "cottage");

      assertTrue(cottage.hasItem("key"));
    }

    @Test
    @DisplayName("placeItem - throws when item not found")
    void testPlaceItem_throwsWhenItemNotFound() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
      gameMap.addLocation(cottage);

      final IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> gameMap.placeItem("key", "cottage")
      );

      assertTrue(exception.getMessage().contains("key"));
    }

    @Test
    @DisplayName("placeItem - throws when location not found")
    void testPlaceItem_throwsWhenLocationNotFound() {
      final Item key = TestItemFactory.createTestKey();
      gameMap.addItem(key);

      final IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> gameMap.placeItem("key", "cottage")
      );

      assertTrue(exception.getMessage().contains("cottage"));
    }
  }

  @Nested
  @DisplayName("Reset")
  class Reset {

    @Test
    @DisplayName("resetMap - restores items to initial locations")
    void testResetMap_restoresItemsToInitialLocations() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
      final Item key = TestItemFactory.createTestKey();

      final GameMap gameMap = new GameMap()
          .addLocation(cottage)
          .addItem(key)
          .placeItem("key", "cottage")
          .setStartingLocation("cottage");

      // Simulate taking the item
      cottage.removeItem(key);

      gameMap.resetMap();

      assertTrue(cottage.hasItem("key"));
    }

    @Test
    @DisplayName("resetMap - resets visited flags")
    void testResetMap_resetsVisitedFlags() {
      final Location cottage = new Location("cottage", "A cozy cottage.", "In a cottage.");
      cottage.setVisited(true);

      final GameMap gameMap = new GameMap()
          .addLocation(cottage)
          .setStartingLocation("cottage");

      gameMap.resetMap();

      assertTrue(!cottage.isVisited());
    }
  }

  @Nested
  @DisplayName("Direction Opposites")
  class DirectionOpposites {

    private GameMap gameMap;

    @BeforeEach
    void setUp() {
      gameMap = new GameMap();
      gameMap.addLocation(new Location("a", "Location A", "A"));
      gameMap.addLocation(new Location("b", "Location B", "B"));
    }

    @Test
    @DisplayName("connect NORTH creates SOUTH return")
    void testConnect_northCreatesSouthReturn() {
      gameMap.connect("a", Direction.NORTH, "b");

      assertNotNull(gameMap.getLocation("b").getConnection(Direction.SOUTH));
    }

    @Test
    @DisplayName("connect EAST creates WEST return")
    void testConnect_eastCreatesWestReturn() {
      gameMap.connect("a", Direction.EAST, "b");

      assertNotNull(gameMap.getLocation("b").getConnection(Direction.WEST));
    }

    @Test
    @DisplayName("connect UP creates DOWN return")
    void testConnect_upCreatesDownReturn() {
      gameMap.connect("a", Direction.UP, "b");

      assertNotNull(gameMap.getLocation("b").getConnection(Direction.DOWN));
    }

    @Test
    @DisplayName("connect NORTHEAST creates SOUTHWEST return")
    void testConnect_northeastCreatesSouthwestReturn() {
      gameMap.connect("a", Direction.NORTHEAST, "b");

      assertNotNull(gameMap.getLocation("b").getConnection(Direction.SOUTHWEST));
    }

    @Test
    @DisplayName("connect IN creates OUT return")
    void testConnect_inCreatesOutReturn() {
      gameMap.connect("a", Direction.IN, "b");

      assertNotNull(gameMap.getLocation("b").getConnection(Direction.OUT));
    }
  }
}

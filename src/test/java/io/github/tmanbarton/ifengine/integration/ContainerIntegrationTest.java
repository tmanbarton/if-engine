package io.github.tmanbarton.ifengine.integration;

import io.github.tmanbarton.ifengine.InteractionType;
import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.ItemContainer;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.SceneryObject;
import io.github.tmanbarton.ifengine.game.GameState;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.response.DefaultResponses;
import io.github.tmanbarton.ifengine.response.ResponseProvider;
import io.github.tmanbarton.ifengine.test.JsonTestUtils;
import io.github.tmanbarton.ifengine.test.TestFixtures;
import io.github.tmanbarton.ifengine.test.TestGameEngine;
import io.github.tmanbarton.ifengine.test.TestGameEngineBuilder;
import io.github.tmanbarton.ifengine.test.TestGameMap;
import io.github.tmanbarton.ifengine.test.TestGameMapBuilder;
import io.github.tmanbarton.ifengine.test.TestItemFactory;
import io.github.tmanbarton.ifengine.test.TestOpenableItemContainer;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for container functionality.
 * <p>
 * These tests verify that container operations work correctly via real commands
 * through the full parser → handler flow.
 */
@DisplayName("Container Integration Tests")
class ContainerIntegrationTest {

  private static final String SESSION_ID = "test-session";
  private static final ResponseProvider RESPONSES = new DefaultResponses();

  @Nested
  class PutInInventoryContainer {

    @Test
    @DisplayName("item at location, container at location")
    void testPut_itemAtLocation_containerAtLocation() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();
      final Item coin = TestItemFactory.createSimpleItem("coin");
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");
      location.addItem(coin);
      location.addItem(bag);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "put coin in bag");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("coin", "in", "bag") + "\n\n", message);
      assertTrue(location.hasItem("coin"));
      assertTrue(location.hasItem("bag"));
    }

    @Test
    @DisplayName("item at location, container in inventory")
    void testPut_itemAtLocation_containerInInventory() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();
      final Item coin = TestItemFactory.createSimpleItem("coin");
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");
      location.addItem(coin);
      location.addItem(bag);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "take bag");

      // When
      final String response = engine.processCommand(SESSION_ID, "put coin in bag");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("coin", "in", "bag") + "\n\n", message);

      final Player player = engine.getPlayer(SESSION_ID);
      assertTrue(player.hasItem("coin"));
      assertTrue(player.hasItem("bag"));
      assertTrue(player.isItemContained(coin));
    }

    @Test
    @DisplayName("item in inventory, container at location")
    void testPut_itemInInventory_containerAtLocation() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();
      final Item coin = TestItemFactory.createSimpleItem("coin");
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");
      location.addItem(coin);
      location.addItem(bag);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "take coin");

      // When
      final String response = engine.processCommand(SESSION_ID, "put coin in bag");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("coin", "in", "bag") + "\n\n", message);

      final Player player = engine.getPlayer(SESSION_ID);
      assertFalse(player.hasItem("coin"));
      assertTrue(location.hasItem("coin"));
    }

    @Test
    @DisplayName("item in inventory, container in inventory")
    void testPut_itemInInventory_containerInInventory() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();
      final Item coin = TestItemFactory.createSimpleItem("coin");
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");
      location.addItem(coin);
      location.addItem(bag);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "take bag");
      engine.processCommand(SESSION_ID, "take coin");

      // When
      final String response = engine.processCommand(SESSION_ID, "put coin in bag");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("coin", "in", "bag") + "\n\n", message);

      final Player player = engine.getPlayer(SESSION_ID);
      assertTrue(player.hasItem("coin"));
      assertTrue(player.hasItem("bag"));
      assertTrue(player.isItemContained(coin));
    }

    @Test
    @DisplayName("item on location container, put in inventory container - container in inventory")
    void testPut_itemOnLocationContainer_toInventoryContainer_containerInInventory() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();

      final Item coin = TestItemFactory.createSimpleItem("coin");
      location.addItem(coin);

      // Scenery container (table) at location
      final SceneryObject table = SceneryObject.builder("table")
          .withInteraction(InteractionType.LOOK, "A table.")
          .asContainer()
          .withAllowedItems("coin")
          .build();
      location.addSceneryObject(table);

      // Inventory container (bag)
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");
      location.addItem(bag);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      // Put coin on table, take bag
      engine.processCommand(SESSION_ID, "put coin on table");
      engine.processCommand(SESSION_ID, "take bag");

      // When
      final String response = engine.processCommand(SESSION_ID, "put coin in bag");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("coin", "in", "bag") + "\n\n", message);

      final Player player = engine.getPlayer(SESSION_ID);
      assertTrue(player.hasItem("coin"));
      assertTrue(player.hasItem("bag"));
      assertTrue(player.isItemContained(coin));
    }

    @Test
    @DisplayName("item on location container, put in inventory container - container at location")
    void testPut_itemOnLocationContainer_toInventoryContainer_containerAtLocation() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();

      final Item coin = TestItemFactory.createSimpleItem("coin");
      location.addItem(coin);

      // Scenery container (table) at location
      final SceneryObject table = SceneryObject.builder("table")
          .withInteraction(InteractionType.LOOK, "A table.")
          .asContainer()
          .withAllowedItems("coin")
          .build();
      location.addSceneryObject(table);

      // Inventory container (bag) at location
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");
      location.addItem(bag);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      // Put coin on table (don't take bag)
      engine.processCommand(SESSION_ID, "put coin on table");

      // When
      final String response = engine.processCommand(SESSION_ID, "put coin in bag");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("coin", "in", "bag") + "\n\n", message);

      assertTrue(location.hasItem("coin"));
      assertTrue(location.hasItem("bag"));
    }
  }

  @Nested
  class PutOnLocationContainer {

    @Test
    @DisplayName("item at location")
    void testPut_itemAtLocation_onLocationContainer() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();

      final Item coin = TestItemFactory.createSimpleItem("coin");
      location.addItem(coin);

      final SceneryObject table = SceneryObject.builder("table")
          .withInteraction(InteractionType.LOOK, "A table.")
          .asContainer()
          .withAllowedItems("coin")
          .build();
      location.addSceneryObject(table);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "put coin on table");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("coin", "on", "table") + "\n\n", message);
      assertTrue(location.hasItem("coin"));
      assertTrue(location.isItemInContainer(coin));
    }

    @Test
    @DisplayName("item in inventory")
    void testPut_itemInInventory_onLocationContainer() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();

      final Item coin = TestItemFactory.createSimpleItem("coin");
      location.addItem(coin);

      final SceneryObject table = SceneryObject.builder("table")
          .withInteraction(InteractionType.LOOK, "A table.")
          .asContainer()
          .withAllowedItems("coin")
          .build();
      location.addSceneryObject(table);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "take coin");

      // When
      final String response = engine.processCommand(SESSION_ID, "put coin on table");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("coin", "on", "table") + "\n\n", message);

      final Player player = engine.getPlayer(SESSION_ID);
      assertFalse(player.hasItem("coin"));
      assertTrue(location.hasItem("coin"));
      assertTrue(location.isItemInContainer(coin));
    }

    @Test
    @DisplayName("item in inventory container - container in inventory")
    void testPut_itemInInventoryContainer_onLocationContainer_containerInInventory() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();

      final Item coin = TestItemFactory.createSimpleItem("coin");
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");
      location.addItem(coin);
      location.addItem(bag);

      final SceneryObject table = SceneryObject.builder("table")
          .withInteraction(InteractionType.LOOK, "A table.")
          .asContainer()
          .withAllowedItems("coin")
          .build();
      location.addSceneryObject(table);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      engine.processCommand(SESSION_ID, "take bag");
      engine.processCommand(SESSION_ID, "take coin");
      engine.processCommand(SESSION_ID, "put coin in bag");

      // When
      final String response = engine.processCommand(SESSION_ID, "put coin on table");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("coin", "on", "table") + "\n\n", message);

      final Player player = engine.getPlayer(SESSION_ID);
      assertFalse(player.hasItem("coin"));
      assertTrue(location.hasItem("coin"));
      assertTrue(location.isItemInContainer(coin));
    }

    @Test
    @DisplayName("item in inventory container - container at location")
    void testPut_itemInInventoryContainer_onLocationContainer_containerAtLocation() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();

      final Item coin = TestItemFactory.createSimpleItem("coin");
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");
      location.addItem(coin);
      location.addItem(bag);

      final SceneryObject table = SceneryObject.builder("table")
          .withInteraction(InteractionType.LOOK, "A table.")
          .asContainer()
          .withAllowedItems("coin")
          .build();
      location.addSceneryObject(table);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      engine.processCommand(SESSION_ID, "take coin");
      engine.processCommand(SESSION_ID, "put coin in bag");

      // When
      final String response = engine.processCommand(SESSION_ID, "put coin on table");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("coin", "on", "table") + "\n\n", message);

      assertTrue(location.hasItem("coin"));
      assertTrue(location.isItemInContainer(coin));
    }
  }

  @Nested
  class MoveInventoryContainer {

    @Test
    @DisplayName("take inventory container with no items")
    void testTake_inventoryContainer_noItems() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");
      location.addItem(bag);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "take bag");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getTakeSuccess() + "\n\n", message);

      final Player player = engine.getPlayer(SESSION_ID);
      assertTrue(player.hasItem("bag"));
      assertFalse(location.hasItem("bag"));
    }

    @Test
    @DisplayName("drop inventory container with no items")
    void testDrop_inventoryContainer_noItems() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");
      location.addItem(bag);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "take bag");

      // When
      final String response = engine.processCommand(SESSION_ID, "drop bag");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getDropSuccess() + "\n\n", message);

      final Player player = engine.getPlayer(SESSION_ID);
      assertFalse(player.hasItem("bag"));
      assertTrue(location.hasItem("bag"));
    }

    @Test
    @DisplayName("put inventory container with no items on location container")
    void testPut_inventoryContainerNoItems_onLocationContainer() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();

      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");
      location.addItem(bag);

      final SceneryObject table = SceneryObject.builder("table")
          .withInteraction(InteractionType.LOOK, "A table.")
          .asContainer()
          .withAllowedItems("bag")
          .build();
      location.addSceneryObject(table);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "take bag");

      // When
      final String response = engine.processCommand(SESSION_ID, "put bag on table");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("bag", "on", "table") + "\n\n", message);

      final Player player = engine.getPlayer(SESSION_ID);
      assertFalse(player.hasItem("bag"));
      assertTrue(location.hasItem("bag"));
      assertTrue(location.isItemInContainer(bag));
    }

    @Test
    @DisplayName("put inventory container with no items in another inventory container")
    void testPut_inventoryContainerNoItems_inInventoryContainer() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();

      final TestItemFactory.TestContainer smallBag = TestItemFactory.createTestContainer("small-bag", 5, "coin");
      final TestItemFactory.TestContainer largeBag = TestItemFactory.createTestContainer("large-bag", 10, "small-bag", "coin");
      location.addItem(smallBag);
      location.addItem(largeBag);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "take small-bag");
      engine.processCommand(SESSION_ID, "take large-bag");

      // When
      final String response = engine.processCommand(SESSION_ID, "put small-bag in large-bag");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("small-bag", "in", "large-bag") + "\n\n", message);

      final Player player = engine.getPlayer(SESSION_ID);
      assertTrue(player.hasItem("small-bag"));
      assertTrue(player.hasItem("large-bag"));
      assertTrue(player.isItemContained(smallBag));
    }

    @Test
    @DisplayName("take inventory container with items")
    void testTake_inventoryContainer_withItems() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();

      final Item coin = TestItemFactory.createSimpleItem("coin");
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");
      location.addItem(coin);
      location.addItem(bag);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      engine.processCommand(SESSION_ID, "put coin in bag");

      // When
      final String response = engine.processCommand(SESSION_ID, "take bag");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getTakeSuccess() + "\n\n", message);

      final Player player = engine.getPlayer(SESSION_ID);
      assertTrue(player.hasItem("bag"));
      assertTrue(player.hasItem("coin"));
    }

    @Test
    @DisplayName("drop inventory container with items")
    void testDrop_inventoryContainer_withItems() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();

      final Item coin = TestItemFactory.createSimpleItem("coin");
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");
      location.addItem(coin);
      location.addItem(bag);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      engine.processCommand(SESSION_ID, "take bag");
      engine.processCommand(SESSION_ID, "take coin");
      engine.processCommand(SESSION_ID, "put coin in bag");

      // When
      final String response = engine.processCommand(SESSION_ID, "drop bag");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getDropSuccess() + "\n\n", message);

      final Player player = engine.getPlayer(SESSION_ID);
      assertFalse(player.hasItem("bag"));
      assertFalse(player.hasItem("coin"));
      assertTrue(location.hasItem("bag"));
      assertTrue(location.hasItem("coin"));
    }

    @Test
    @DisplayName("put inventory container with items on location container")
    void testPut_inventoryContainerWithItems_onLocationContainer() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();

      final Item coin = TestItemFactory.createSimpleItem("coin");
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");
      location.addItem(coin);
      location.addItem(bag);

      final SceneryObject table = SceneryObject.builder("table")
          .withInteraction(InteractionType.LOOK, "A table.")
          .asContainer()
          .withAllowedItems("bag", "coin")
          .build();
      location.addSceneryObject(table);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      engine.processCommand(SESSION_ID, "take bag");
      engine.processCommand(SESSION_ID, "take coin");
      engine.processCommand(SESSION_ID, "put coin in bag");

      // When
      final String response = engine.processCommand(SESSION_ID, "put bag on table");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("bag", "on", "table") + "\n\n", message);

      final Player player = engine.getPlayer(SESSION_ID);
      assertFalse(player.hasItem("bag"));
      assertFalse(player.hasItem("coin"));
      assertTrue(location.hasItem("bag"));
      assertTrue(location.hasItem("coin"));
    }

    @Test
    @DisplayName("put inventory container with items in another inventory container")
    void testPut_inventoryContainerWithItems_inInventoryContainer() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();

      final Item coin = TestItemFactory.createSimpleItem("coin");
      final TestItemFactory.TestContainer smallBag = TestItemFactory.createTestContainer("small-bag", 5, "coin");
      final TestItemFactory.TestContainer largeBag = TestItemFactory.createTestContainer("large-bag", 10, "small-bag", "coin");
      location.addItem(coin);
      location.addItem(smallBag);
      location.addItem(largeBag);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      engine.processCommand(SESSION_ID, "take small-bag");
      engine.processCommand(SESSION_ID, "take large-bag");
      engine.processCommand(SESSION_ID, "take coin");
      engine.processCommand(SESSION_ID, "put coin in small-bag");

      // When
      final String response = engine.processCommand(SESSION_ID, "put small-bag in large-bag");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("small-bag", "in", "large-bag") + "\n\n", message);

      final Player player = engine.getPlayer(SESSION_ID);
      assertTrue(player.hasItem("small-bag"));
      assertTrue(player.hasItem("large-bag"));
      assertTrue(player.hasItem("coin"));
      assertTrue(player.isItemContained(smallBag));
    }
  }

  @Nested
  class SceneryObjectContainerApi {

    @Test
    @DisplayName("put item on scenery container created via addSceneryObject")
    void testPut_onSceneryContainerViaAddSceneryObject() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();

      final Item coin = TestItemFactory.createSimpleItem("coin");
      location.addItem(coin);

      // Create container via new SceneryObject API
      final SceneryObject table = SceneryObject.builder("table")
          .withInteraction(InteractionType.LOOK, "A wooden table.")
          .asContainer()
          .build();
      location.addSceneryObject(table);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "put coin on table");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("coin", "on", "table") + "\n\n", message);
      assertTrue(location.hasItem("coin"));
      assertTrue(location.isItemInContainer(coin));
    }

    @Test
    @DisplayName("put item in drawer with custom prepositions")
    void testPut_inDrawerWithCustomPrepositions() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();

      final Item key = TestItemFactory.createSimpleItem("key");
      location.addItem(key);

      // Create drawer container with "in" preposition
      final SceneryObject drawer = SceneryObject.builder("drawer")
          .withInteraction(InteractionType.LOOK, "A wooden drawer.")
          .asContainer()
          .withPrepositions("in", "into")
          .build();
      location.addSceneryObject(drawer);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "put key in drawer");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("key", "in", "drawer") + "\n\n", message);
      assertTrue(location.hasItem("key"));
      assertTrue(location.isItemInContainer(key));
    }

    @Test
    @DisplayName("put item in container with allowed items restriction")
    void testPut_withAllowedItemsRestriction() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();

      final Item book = TestItemFactory.createSimpleItem("book");
      final Item coin = TestItemFactory.createSimpleItem("coin");
      location.addItem(book);
      location.addItem(coin);

      // Create container that only accepts books
      final SceneryObject shelf = SceneryObject.builder("shelf")
          .withInteraction(InteractionType.LOOK, "A wooden shelf.")
          .asContainer()
          .withAllowedItems("book")
          .build();
      location.addSceneryObject(shelf);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      // When - put book (allowed)
      final String bookResponse = engine.processCommand(SESSION_ID, "put book on shelf");
      // When - put coin (not allowed)
      final String coinResponse = engine.processCommand(SESSION_ID, "put coin on shelf");

      // Then
      final String bookMessage = JsonTestUtils.extractMessage(bookResponse);
      assertEquals(RESPONSES.getPutSuccess("book", "on", "shelf") + "\n\n", bookMessage);

      final String coinMessage = JsonTestUtils.extractMessage(coinResponse);
      assertEquals(RESPONSES.getPutItemNotAccepted("shelf", "coin") + "\n\n", coinMessage);
    }

    @Test
    @DisplayName("wrong preposition rejected for container")
    void testPut_wrongPrepositionRejected() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();

      final Item key = TestItemFactory.createSimpleItem("key");
      location.addItem(key);

      // Create drawer that uses "in"
      final SceneryObject drawer = SceneryObject.builder("drawer")
          .withInteraction(InteractionType.LOOK, "A wooden drawer.")
          .asContainer()
          .withPrepositions("in", "into")
          .build();
      location.addSceneryObject(drawer);

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      // When - try to put "on" drawer (wrong preposition)
      final String response = engine.processCommand(SESSION_ID, "put key on drawer");

      // Then - should be rejected with message about correct preposition
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutInvalidPreposition("drawer", "in") + "\n\n", message);
    }
  }

  @Nested
  class PutInItemContainer {

    @Test
    @DisplayName("put item in ItemContainer - both in inventory")
    void testPut_bothInInventory() {
      // Given
      final TestGameEngine engine = TestFixtures.itemContainerScenario();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "take bag");
      engine.processCommand(SESSION_ID, "take gem");

      // When
      final String response = engine.processCommand(SESSION_ID, "put gem in bag");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("gem", "in", "bag") + "\n\n", message,
          "put gem in bag should succeed when both are in inventory");

      final Player player = engine.getPlayer(SESSION_ID);
      assertTrue(player.hasItem("gem"),
          "gem should remain in inventory (follows container)");
      assertTrue(player.hasItem("bag"),
          "bag should remain in inventory");
    }

    @Test
    @DisplayName("put item in ItemContainer - both at location")
    void testPut_bothAtLocation() {
      // Given
      final TestGameEngine engine = TestFixtures.itemContainerScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "put gem in bag");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("gem", "in", "bag") + "\n\n", message,
          "put gem in bag should succeed when both are at location");
    }

    @Test
    @DisplayName("put item in ItemContainer - item at location, container in inventory")
    void testPut_itemAtLocation_containerInInventory() {
      // Given
      final TestGameEngine engine = TestFixtures.itemContainerScenario();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "take bag");

      // When
      final String response = engine.processCommand(SESSION_ID, "put gem in bag");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("gem", "in", "bag") + "\n\n", message,
          "put gem in bag should succeed when item is at location and container in inventory");

      final Player player = engine.getPlayer(SESSION_ID);
      assertTrue(player.hasItem("gem"),
          "gem should move to inventory to follow its container");
    }

    @Test
    @DisplayName("put item in ItemContainer - item in inventory, container at location")
    void testPut_itemInInventory_containerAtLocation() {
      // Given
      final TestGameEngine engine = TestFixtures.itemContainerScenario();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "take gem");

      // When
      final String response = engine.processCommand(SESSION_ID, "put gem in bag");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("gem", "in", "bag") + "\n\n", message,
          "put gem in bag should succeed when item is in inventory and container at location");

      final Player player = engine.getPlayer(SESSION_ID);
      assertFalse(player.hasItem("gem"),
          "gem should move to location to follow its container");
    }

    @Test
    @DisplayName("put rejects when ItemContainer is at capacity")
    void testPut_capacityEnforced() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();
      final ItemContainer tinyBag = ItemContainer.builder("tiny-bag")
          .withCapacity(1)
          .build();
      location.addItem(tinyBag);
      location.addItem(TestItemFactory.createSimpleItem("gem"));
      location.addItem(TestItemFactory.createSimpleItem("rope"));

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      engine.processCommand(SESSION_ID, "put gem in tiny-bag");

      // When
      final String response = engine.processCommand(SESSION_ID, "put rope in tiny-bag");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutItemNotAccepted("tiny-bag", "rope") + "\n\n", message,
          "put should fail when container is at capacity");
    }

    @Test
    @DisplayName("put rejects when item is not in allowed items")
    void testPut_allowedItemsEnforced() {
      // Given
      final TestGameMap map = TestGameMapBuilder.singleLocation().build();
      final Location location = map.getStartingLocation();
      final ItemContainer restrictedBag = ItemContainer.builder("pouch")
          .withAllowedItems(Set.of("gem"))
          .build();
      location.addItem(restrictedBag);
      location.addItem(TestItemFactory.createSimpleItem("rope"));

      final TestGameEngine engine = TestGameEngineBuilder.withCustomMap(map)
          .withInitialPlayerState(GameState.PLAYING)
          .build();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "put rope in pouch");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutItemNotAccepted("pouch", "rope") + "\n\n", message,
          "put should fail when item is not in the container's allowed items");
    }
  }

  @Nested
  class PutInOpenableItemContainer {

    @Test
    @DisplayName("put into closed openable container returns closed message")
    void testPut_closedContainerReturnsClosed() {
      // Given
      final TestGameEngine engine = TestFixtures.openableItemContainerScenario();
      engine.createPlayer(SESSION_ID);

      // When - chest is closed by default
      final String response = engine.processCommand(SESSION_ID, "put gem in chest");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutContainerClosed("chest") + "\n\n", message,
          "put into closed openable container should return closed message");
    }

    @Test
    @DisplayName("put succeeds after unlock and open")
    void testPut_afterUnlockAndOpenSucceeds() {
      // Given
      final TestGameEngine engine = TestFixtures.openableItemContainerScenario();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "take key");
      engine.processCommand(SESSION_ID, "unlock chest");
      engine.processCommand(SESSION_ID, "open chest");

      // When
      final String response = engine.processCommand(SESSION_ID, "put gem in chest");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getPutSuccess("gem", "in", "chest") + "\n\n", message,
          "put should succeed after unlocking and opening the container");
    }
  }

  @Nested
  class MoveItemContainerWithContents {

    @Test
    @DisplayName("take ItemContainer - contained items follow to inventory")
    void testTake_containerWithItems() {
      // Given
      final TestGameEngine engine = TestFixtures.itemContainerScenario();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "put gem in bag");

      // When
      final String response = engine.processCommand(SESSION_ID, "take bag");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getTakeSuccess() + "\n\n", message,
          "taking a container with items should succeed");

      final Player player = engine.getPlayer(SESSION_ID);
      assertTrue(player.hasItem("bag"),
          "bag should be in inventory after take");
      assertTrue(player.hasItem("gem"),
          "gem should follow bag into inventory");
    }

    @Test
    @DisplayName("drop ItemContainer - contained items follow to location")
    void testDrop_containerWithItems() {
      // Given
      final TestGameEngine engine = TestFixtures.itemContainerScenario();
      engine.createPlayer(SESSION_ID);
      engine.processCommand(SESSION_ID, "take bag");
      engine.processCommand(SESSION_ID, "take gem");
      engine.processCommand(SESSION_ID, "put gem in bag");

      // When
      final String response = engine.processCommand(SESSION_ID, "drop bag");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getDropSuccess() + "\n\n", message,
          "dropping a container with items should succeed");

      final Player player = engine.getPlayer(SESSION_ID);
      assertFalse(player.hasItem("bag"),
          "bag should no longer be in inventory after drop");
      assertFalse(player.hasItem("gem"),
          "gem should follow bag out of inventory");
    }
  }
}
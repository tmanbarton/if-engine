package com.ifengine.command.handlers;

import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.SceneryContainer;
import com.ifengine.SceneryObject;
import com.ifengine.game.Player;
import com.ifengine.parser.CommandType;
import com.ifengine.parser.ObjectResolver;
import com.ifengine.parser.ParsedCommand;
import com.ifengine.response.DefaultResponses;
import com.ifengine.response.ResponseProvider;
import com.ifengine.test.TestItemFactory;
import com.ifengine.test.TestLocationFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for PutHandler.
 */
@DisplayName("PutHandler Tests")
class PutHandlerTest {

  private PutHandler handler;
  private Player player;
  private Location location;
  private ResponseProvider responses;

  @BeforeEach
  void setUp() {
    final ObjectResolver objectResolver = new ObjectResolver();
    responses = new DefaultResponses();
    handler = new PutHandler(objectResolver, responses);
    location = TestLocationFactory.createDefaultLocation();
    player = new Player(location);
  }

  @Nested
  @DisplayName("Basic Put")
  class BasicPut {

    @Test
    @DisplayName("Test handle - put item in inventory container")
    void testHandle_putItemInInventoryContainer() {
      final Item coin = TestItemFactory.createSimpleItem("coin");
      // TestContainer accepts "in/into" prepositions
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");

      player.addItem(coin);
      player.addItem(bag);

      final ParsedCommand command = createPutCommand("coin", "in", "bag");

      final String result = handler.handle(player, command);

      assertEquals(responses.getPutSuccess("coin", "in", "bag"), result);
    }

    @Test
    @DisplayName("Test handle - put item on location scenery container")
    void testHandle_putItemOnLocationSceneryContainer() {
      final Item coin = TestItemFactory.createSimpleItem("coin");
      player.addItem(coin);

      // SceneryContainer accepts "on/onto" prepositions by default
      final SceneryContainer table = createSceneryContainer("table", "coin");
      location.addSceneryContainer(table);

      final ParsedCommand command = createPutCommand("coin", "on", "table");

      final String result = handler.handle(player, command);

      assertEquals(responses.getPutSuccess("coin", "on", "table"), result);
    }

    @Test
    @DisplayName("Test handle - put item not in inventory or location")
    void testHandle_putItemNotPresent() {
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");
      player.addItem(bag);

      final ParsedCommand command = createPutCommand("coin", "in", "bag");

      final String result = handler.handle(player, command);

      assertEquals(responses.getPutItemNotPresent("coin"), result);
    }

    @Test
    @DisplayName("Test handle - put no object specified")
    void testHandle_putNoObjectSpecified() {
      final ParsedCommand command = createPutCommand(null, "in", "box");

      final String result = handler.handle(player, command);

      assertEquals(responses.getPutWhat(), result);
    }

    @Test
    @DisplayName("Test handle - put item but no container specified")
    void testHandle_putNoContainerSpecified() {
      final Item coin = TestItemFactory.createSimpleItem("coin");
      player.addItem(coin);

      final ParsedCommand command = createPutCommandNoContainer("coin");

      final String result = handler.handle(player, command);

      assertEquals(responses.getPutWhere("coin"), result);
    }
  }

  @Nested
  @DisplayName("Preposition Validation")
  class PrepositionValidation {

    @Test
    @DisplayName("Test handle - valid preposition in with inventory container")
    void testHandle_validPrepositionIn() {
      final Item coin = TestItemFactory.createSimpleItem("coin");
      // TestContainer accepts "in/into" prepositions
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");

      player.addItem(coin);
      player.addItem(bag);

      final ParsedCommand command = createPutCommand("coin", "in", "bag");

      final String result = handler.handle(player, command);

      assertEquals(responses.getPutSuccess("coin", "in", "bag"), result);
    }

    @Test
    @DisplayName("Test handle - valid preposition on with scenery container")
    void testHandle_validPrepositionOn() {
      final Item coin = TestItemFactory.createSimpleItem("coin");
      player.addItem(coin);

      // SceneryContainer accepts "on/onto" by default
      final SceneryContainer table = createSceneryContainer("table", "coin");
      location.addSceneryContainer(table);

      final ParsedCommand command = createPutCommand("coin", "on", "table");

      final String result = handler.handle(player, command);

      assertEquals(responses.getPutSuccess("coin", "on", "table"), result);
    }

    @Test
    @DisplayName("Test handle - invalid preposition at")
    void testHandle_invalidPrepositionAt() {
      final Item coin = TestItemFactory.createSimpleItem("coin");
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");
      player.addItem(coin);
      player.addItem(bag);

      final ParsedCommand command = createPutCommand("coin", "at", "bag");

      final String result = handler.handle(player, command);

      assertEquals(responses.getPutUnsupportedPreposition("at"), result);
    }

    @Test
    @DisplayName("Test handle - missing preposition")
    void testHandle_missingPreposition() {
      final Item coin = TestItemFactory.createSimpleItem("coin");
      player.addItem(coin);

      final ParsedCommand command = createPutCommandNoPreposition("coin", "bag");

      final String result = handler.handle(player, command);

      assertEquals(responses.getPutMissingPreposition("coin"), result);
    }

    @Test
    @DisplayName("Test handle - wrong preposition for scenery container")
    void testHandle_wrongPrepositionForSceneryContainer() {
      final Item coin = TestItemFactory.createSimpleItem("coin");
      player.addItem(coin);

      // SceneryContainer accepts "on/onto", not "in"
      final SceneryContainer table = createSceneryContainer("table", "coin");
      location.addSceneryContainer(table);

      final ParsedCommand command = createPutCommand("coin", "in", "table");

      final String result = handler.handle(player, command);

      assertEquals(responses.getPutInvalidPreposition("table", "on"), result);
    }

    @Test
    @DisplayName("Test handle - wrong preposition for inventory container")
    void testHandle_wrongPrepositionForInventoryContainer() {
      final Item coin = TestItemFactory.createSimpleItem("coin");
      // TestContainer accepts "in/into", not "on"
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");
      player.addItem(coin);
      player.addItem(bag);

      final ParsedCommand command = createPutCommand("coin", "on", "bag");

      final String result = handler.handle(player, command);

      assertEquals(responses.getPutInvalidPreposition("bag", "in"), result);
    }
  }

  @Nested
  @DisplayName("Container Validation")
  class ContainerValidation {

    @Test
    @DisplayName("Test handle - container not found")
    void testHandle_containerNotFound() {
      final Item coin = TestItemFactory.createSimpleItem("coin");
      player.addItem(coin);

      final ParsedCommand command = createPutCommand("coin", "in", "box");

      final String result = handler.handle(player, command);

      assertEquals(responses.getPutContainerNotFound("box"), result);
    }

    @Test
    @DisplayName("Test handle - target is scenery but not a container")
    void testHandle_targetNotAContainer() {
      final Item coin = TestItemFactory.createSimpleItem("coin");
      player.addItem(coin);

      // Add a regular scenery object (not a container)
      final SceneryObject tree = SceneryObject.builder("tree")
          .withInteraction(com.ifengine.InteractionType.CLIMB, "You climb the tree.")
          .build();
      location.addSceneryObject(tree);

      final ParsedCommand command = createPutCommand("coin", "in", "tree");

      final String result = handler.handle(player, command);

      assertEquals(responses.getPutNotAContainer("tree"), result);
    }

    @Test
    @DisplayName("Test handle - item not accepted by container")
    void testHandle_itemNotAccepted() {
      final Item gem = TestItemFactory.createSimpleItem("gem");
      // Bag only accepts "coin"
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");
      player.addItem(gem);
      player.addItem(bag);

      final ParsedCommand command = createPutCommand("gem", "in", "bag");

      final String result = handler.handle(player, command);

      assertEquals(responses.getPutItemNotAccepted("bag", "gem"), result);
    }
  }

  @Nested
  @DisplayName("Circular Containment")
  class CircularContainment {

    @Test
    @DisplayName("Test handle - put container in itself")
    void testHandle_putContainerInItself() {
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10);
      player.addItem(bag);

      final ParsedCommand command = createPutCommand("bag", "in", "bag");

      final String result = handler.handle(player, command);

      assertEquals(responses.getPutCircularContainment(), result);
    }
  }

  @Nested
  @DisplayName("Container In Container")
  class ContainerInContainer {

    @Test
    @DisplayName("Test handle - put container in another container")
    void testHandle_putContainerInContainer() {
      // Create a small bag that can go inside a larger box
      final TestItemFactory.TestContainer smallBag = TestItemFactory.createTestContainer("small-bag", 5, "coin");
      final TestItemFactory.TestContainer largeBox = TestItemFactory.createTestContainer("large-box", 10, "small-bag", "coin");
      player.addItem(smallBag);
      player.addItem(largeBox);

      final ParsedCommand command = createPutCommand("small-bag", "in", "large-box");

      final String result = handler.handle(player, command);

      assertEquals(responses.getPutSuccess("small-bag", "in", "large-box"), result);
      // Verify container state
      assertTrue(largeBox.containsItem("small-bag"));
      assertTrue(player.isItemContained(smallBag));
      assertEquals(largeBox, player.getContainerForItem(smallBag));
    }

    @Test
    @DisplayName("Test handle - put container with items into another container")
    void testHandle_putContainerWithItemsIntoContainer() {
      // Create containers
      final TestItemFactory.TestContainer smallBag = TestItemFactory.createTestContainer("small-bag", 5, "coin");
      final TestItemFactory.TestContainer largeBox = TestItemFactory.createTestContainer("large-box", 10, "small-bag", "coin");
      final Item coin = TestItemFactory.createSimpleItem("coin");

      // Add items to player inventory and set up containment
      player.addItem(smallBag);
      player.addItem(coin);
      player.addItem(largeBox);

      // Put coin in small-bag first
      final ParsedCommand putCoinInBag = createPutCommand("coin", "in", "small-bag");
      handler.handle(player, putCoinInBag);

      // Now put small-bag (with coin inside) into large-box
      final ParsedCommand putBagInBox = createPutCommand("small-bag", "in", "large-box");
      final String result = handler.handle(player, putBagInBox);

      assertEquals(responses.getPutSuccess("small-bag", "in", "large-box"), result);
      // Verify nested containment
      assertTrue(largeBox.containsItem("small-bag"));
      assertTrue(player.isItemContained(smallBag));
    }
  }

  @Nested
  @DisplayName("Item Movement Between Containers")
  class ItemMovementBetweenContainers {

    @Test
    @DisplayName("Test handle - put item from inventory into inventory container in inventory")
    void testHandle_putItemFromInventoryIntoInventoryContainerInInventory() {
      final Item coin = TestItemFactory.createSimpleItem("coin");
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");

      player.addItem(coin);
      player.addItem(bag);

      final ParsedCommand command = createPutCommand("coin", "in", "bag");

      final String result = handler.handle(player, command);

      assertEquals(responses.getPutSuccess("coin", "in", "bag"), result);
      // Item should still be in inventory (contained in bag which is in inventory)
      assertTrue(player.getInventory().contains(coin));
      assertTrue(player.isItemContained(coin));
      assertEquals(bag, player.getContainerForItem(coin));
    }

    @Test
    @DisplayName("Test handle - put item from location into inventory container in inventory")
    void testHandle_putItemFromLocationIntoInventoryContainerInInventory() {
      final Item coin = TestItemFactory.createSimpleItem("coin");
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");

      location.addItem(coin);
      player.addItem(bag);

      final ParsedCommand command = createPutCommand("coin", "in", "bag");

      final String result = handler.handle(player, command);

      assertEquals(responses.getPutSuccess("coin", "in", "bag"), result);
      // Item should be moved from location to inventory
      assertFalse(location.getItems().contains(coin));
      assertTrue(player.getInventory().contains(coin));
      assertTrue(player.isItemContained(coin));
    }

    @Test
    @DisplayName("Test handle - put item from container into other container")
    void testHandle_putItemFromContainerIntoOtherContainer() {
      final Item coin = TestItemFactory.createSimpleItem("coin");
      final TestItemFactory.TestContainer bag1 = TestItemFactory.createTestContainer("bag", 10, "coin");
      final TestItemFactory.TestContainer bag2 = TestItemFactory.createTestContainer("sack", 10, "coin");

      player.addItem(coin);
      player.addItem(bag1);
      player.addItem(bag2);

      // First put coin in bag1
      final ParsedCommand putInBag1 = createPutCommand("coin", "in", "bag");
      handler.handle(player, putInBag1);

      // Verify coin is in bag1
      assertTrue(player.isItemContained(coin));
      assertEquals(bag1, player.getContainerForItem(coin));

      // Now move coin from bag1 to sack
      final ParsedCommand putInBag2 = createPutCommand("coin", "in", "sack");
      final String result = handler.handle(player, putInBag2);

      assertEquals(responses.getPutSuccess("coin", "in", "sack"), result);
      // Coin should now be in sack, not bag
      assertTrue(player.isItemContained(coin));
      assertEquals(bag2, player.getContainerForItem(coin));
      assertFalse(bag1.containsItem("coin"));
      assertTrue(bag2.containsItem("coin"));
    }

    @Test
    @DisplayName("Test handle - put item from location to inventory container at location")
    void testHandle_putItemFromLocationToInventoryContainerAtLocation() {
      final Item coin = TestItemFactory.createSimpleItem("coin");
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 10, "coin");

      location.addItem(coin);
      location.addItem(bag);

      final ParsedCommand command = createPutCommand("coin", "in", "bag");

      final String result = handler.handle(player, command);

      assertEquals(responses.getPutSuccess("coin", "in", "bag"), result);
      // Both items should be at location
      assertTrue(location.getItems().contains(coin));
      assertTrue(location.getItems().contains(bag));
    }
  }

  @Nested
  @DisplayName("Supported Verbs")
  class SupportedVerbs {

    @Test
    @DisplayName("Test getSupportedVerbs - includes put")
    void testGetSupportedVerbs_includesPut() {
      final List<String> verbs = handler.getSupportedVerbs();

      assertTrue(verbs.contains("put"));
    }

    @Test
    @DisplayName("Test getSupportedVerbs - includes place")
    void testGetSupportedVerbs_includesPlace() {
      final List<String> verbs = handler.getSupportedVerbs();

      assertTrue(verbs.contains("place"));
    }

    @Test
    @DisplayName("Test getSupportedVerbs - includes insert")
    void testGetSupportedVerbs_includesInsert() {
      final List<String> verbs = handler.getSupportedVerbs();

      assertTrue(verbs.contains("insert"));
    }

    @Test
    @DisplayName("Test canHandle - returns true for supported verb")
    void testCanHandle_supportedVerb() {
      assertTrue(handler.canHandle("put"));
      assertTrue(handler.canHandle("place"));
      assertTrue(handler.canHandle("insert"));
    }

    @Test
    @DisplayName("Test canHandle - returns false for unsupported verb")
    void testCanHandle_unsupportedVerb() {
      assertFalse(handler.canHandle("take"));
      assertFalse(handler.canHandle("drop"));
    }
  }

  @Nested
  @DisplayName("Item Location After Put")
  class ItemLocationAfterPut {

    @Test
    @DisplayName("Test handle - item from inventory to scenery container stays at location")
    void testHandle_itemFromInventoryToSceneryContainer() {
      final Item coin = TestItemFactory.createSimpleItem("coin");
      player.addItem(coin);

      final SceneryContainer table = createSceneryContainer("table", "coin");
      location.addSceneryContainer(table);

      final ParsedCommand command = createPutCommand("coin", "on", "table");

      handler.handle(player, command);

      // Item should be removed from player inventory
      assertFalse(player.getInventory().contains(coin));
      // Item should be at location
      assertTrue(location.getItems().contains(coin));
    }

    @Test
    @DisplayName("Test handle - item from location to scenery container stays at location")
    void testHandle_itemFromLocationToSceneryContainer() {
      final Item coin = TestItemFactory.createSimpleItem("coin");
      location.addItem(coin);

      final SceneryContainer table = createSceneryContainer("table", "coin");
      location.addSceneryContainer(table);

      final ParsedCommand command = createPutCommand("coin", "on", "table");

      handler.handle(player, command);

      // Item should still be at location
      assertTrue(location.getItems().contains(coin));
    }
  }

  /**
   * Helper method to create a SceneryContainer (supports "on/onto" prepositions by default).
   */
  private SceneryContainer createSceneryContainer(final String name, final String... acceptedItems) {
    final SceneryObject sceneryObject = SceneryObject.builder(name)
        .withInteraction(com.ifengine.InteractionType.LOOK, "You see a " + name + ".")
        .build();
    return new SceneryContainer(sceneryObject, Set.of(acceptedItems));
  }

  /**
   * Helper method to create a put command with item, preposition, and container.
   */
  private ParsedCommand createPutCommand(final String itemName, final String preposition, final String containerName) {
    final List<String> directObjects = itemName != null && !itemName.isEmpty()
        ? List.of(itemName)
        : new ArrayList<>();
    final List<String> indirectObjects = containerName != null && !containerName.isEmpty()
        ? List.of(containerName)
        : new ArrayList<>();
    return new ParsedCommand("put", directObjects, indirectObjects, preposition,
        CommandType.SINGLE, false, "put " + itemName + " " + preposition + " " + containerName);
  }

  /**
   * Helper method to create a put command with no container.
   */
  private ParsedCommand createPutCommandNoContainer(final String itemName) {
    final List<String> directObjects = itemName != null && !itemName.isEmpty()
        ? List.of(itemName)
        : new ArrayList<>();
    return new ParsedCommand("put", directObjects, new ArrayList<>(), null,
        CommandType.SINGLE, false, "put " + itemName);
  }

  /**
   * Helper method to create a put command with no preposition.
   */
  private ParsedCommand createPutCommandNoPreposition(final String itemName, final String containerName) {
    final List<String> directObjects = itemName != null && !itemName.isEmpty()
        ? List.of(itemName)
        : new ArrayList<>();
    final List<String> indirectObjects = containerName != null && !containerName.isEmpty()
        ? List.of(containerName)
        : new ArrayList<>();
    return new ParsedCommand("put", directObjects, indirectObjects, null,
        CommandType.SINGLE, false, "put " + itemName + " " + containerName);
  }
}
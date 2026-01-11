package io.github.tmanbarton.ifengine.command.handlers;

import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.parser.CommandType;
import io.github.tmanbarton.ifengine.parser.ContextManager;
import io.github.tmanbarton.ifengine.parser.ObjectResolver;
import io.github.tmanbarton.ifengine.parser.ParsedCommand;
import io.github.tmanbarton.ifengine.response.DefaultResponses;
import io.github.tmanbarton.ifengine.response.ResponseProvider;
import io.github.tmanbarton.ifengine.test.TestItemFactory;
import io.github.tmanbarton.ifengine.test.TestLocationFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for DropHandler.
 */
@DisplayName("DropHandler Tests")
class DropHandlerTest {

  private DropHandler handler;
  private Player player;
  private Location location;
  private ResponseProvider responses;

  @BeforeEach
  void setUp() {
    final ObjectResolver objectResolver = new ObjectResolver();
    final ContextManager contextManager = new ContextManager();
    responses = new DefaultResponses();
    handler = new DropHandler(objectResolver, contextManager, responses);
    location = TestLocationFactory.createDefaultLocation();
    player = new Player(location);
  }

  @Nested
  class BasicDrop {

    @Test
    @DisplayName("Test handle - drop item from inventory")
    void testHandle_dropItemFromInventory() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      final ParsedCommand command = createDropCommand("key");

      final String result = handler.handle(player, command);

      assertEquals(responses.getDropSuccess(), result);
      assertFalse(player.hasItem("key"));
      assertTrue(location.getItems().contains(key));
    }

    @Test
    @DisplayName("Test handle - drop item not in inventory")
    void testHandle_dropItemNotInInventory() {
      final ParsedCommand command = createDropCommand("key");

      final String result = handler.handle(player, command);

      assertEquals(responses.getDropDontHave("key"), result);
    }

    @Test
    @DisplayName("Test handle - drop with no object and no items")
    void testHandle_dropNoObjectNoItems() {
      final ParsedCommand command = createDropCommand();

      final String result = handler.handle(player, command);

      assertEquals(responses.getDropNotCarryingAnything(), result);
    }
  }

  @Nested
  class DropAll {

    @Test
    @DisplayName("Test handle - drop all from inventory")
    void testHandle_dropAllFromInventory() {
      final Item key = TestItemFactory.createTestKey();
      final Item rope = TestItemFactory.createTestRope();
      player.addItem(key);
      player.addItem(rope);
      final ParsedCommand command = createDropCommand("all");

      final String result = handler.handle(player, command);

      assertEquals(responses.getDropAllSuccess(), result);
      assertTrue(player.getInventory().isEmpty());
      assertTrue(location.getItems().contains(key));
      assertTrue(location.getItems().contains(rope));
    }

    @Test
    @DisplayName("Test handle - drop all from empty inventory")
    void testHandle_dropAllFromEmptyInventory() {
      final ParsedCommand command = createDropCommand("all");

      final String result = handler.handle(player, command);

      assertEquals(responses.getDropNotCarryingAnything(), result);
    }

    @Test
    @DisplayName("Test handle - drop everything synonym")
    void testHandle_dropEverything() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      final ParsedCommand command = createDropCommand("everything");

      final String result = handler.handle(player, command);

      assertEquals(responses.getDropSuccess(), result);
      assertFalse(player.hasItem("key"));
    }

    @Test
    @DisplayName("Test handle - drop all with single item")
    void testHandle_dropAllWithSingleItem() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      final ParsedCommand command = createDropCommand("all");

      final String result = handler.handle(player, command);

      // Single item returns getDropSuccess, not getDropAllSuccess
      assertEquals(responses.getDropSuccess(), result);
    }
  }

  @Nested
  class InferredObject {

    @Test
    @DisplayName("Test handle - drop single item in inventory without specifying")
    void testHandle_dropSingleItemInInventory() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      final ParsedCommand command = createDropCommand();

      final String result = handler.handle(player, command);

      assertEquals(responses.getDropSuccess(), result);
      assertFalse(player.hasItem("key"));
    }

    @Test
    @DisplayName("Test handle - drop with multiple items requires disambiguation")
    void testHandle_dropMultipleItemsInInventory() {
      final Item key = TestItemFactory.createTestKey();
      final Item rope = TestItemFactory.createTestRope();
      player.addItem(key);
      player.addItem(rope);
      final ParsedCommand command = createDropCommand();

      final String result = handler.handle(player, command);

      assertEquals(responses.getDropNeedToSpecify(), result);
      // Neither item should be dropped
      assertTrue(player.hasItem("key"));
      assertTrue(player.hasItem("rope"));
    }
  }

  @Nested
  class ContainerState {

    @Test
    @DisplayName("Test handle - drop contained item removes from container state")
    void testHandle_dropContainedItem() {
      final Item key = TestItemFactory.createTestKey();
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 5);
      player.addItem(key);
      player.addItem(bag);
      player.markItemAsContained(key, bag);
      bag.insertItem(key);
      final ParsedCommand command = createDropCommand("key");

      handler.handle(player, command);

      assertFalse(player.isItemContained(key));
      assertFalse(bag.containsItem("key"));
    }

    @Test
    @DisplayName("Test handle - drop container moves contained items to location")
    void testHandle_dropContainerWithContents() {
      final Item key = TestItemFactory.createTestKey();
      final Item gem = TestItemFactory.createTestGem();
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 5);
      player.addItem(key);
      player.addItem(gem);
      player.addItem(bag);
      player.markItemAsContained(key, bag);
      player.markItemAsContained(gem, bag);
      final ParsedCommand command = createDropCommand("bag");

      handler.handle(player, command);

      // Container should be at location
      assertTrue(location.getItems().contains(bag));
      // Contained items should also be at location
      assertTrue(location.getItems().contains(key));
      assertTrue(location.getItems().contains(gem));
      // Items should be removed from player inventory
      assertFalse(player.hasItem("key"));
      assertFalse(player.hasItem("gem"));
    }
  }

  @Nested
  class SupportedVerbs {

    @Test
    @DisplayName("Test getSupportedVerbs - includes drop")
    void testGetSupportedVerbs_includesDrop() {
      final List<String> verbs = handler.getSupportedVerbs();

      assertTrue(verbs.contains("drop"));
    }

    @Test
    @DisplayName("Test getSupportedVerbs - includes throw")
    void testGetSupportedVerbs_includesThrow() {
      final List<String> verbs = handler.getSupportedVerbs();

      assertTrue(verbs.contains("throw"));
    }

    @Test
    @DisplayName("Test canHandle - returns true for supported verb")
    void testCanHandle_supportedVerb() {
      assertTrue(handler.canHandle("drop"));
      assertTrue(handler.canHandle("throw"));
    }

    @Test
    @DisplayName("Test canHandle - returns false for unsupported verb")
    void testCanHandle_unsupportedVerb() {
      assertFalse(handler.canHandle("take"));
      assertFalse(handler.canHandle("look"));
    }
  }

  @Nested
  class CaseSensitivity {

    @Test
    @DisplayName("Test handle - drop with uppercase object name")
    void testHandle_dropUppercaseObject() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      final ParsedCommand command = createDropCommand("KEY");

      final String result = handler.handle(player, command);

      assertEquals(responses.getDropSuccess(), result);
      assertFalse(player.hasItem("key"));
    }

    @Test
    @DisplayName("Test handle - drop with mixed case object name")
    void testHandle_dropMixedCaseObject() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      final ParsedCommand command = createDropCommand("KeY");

      final String result = handler.handle(player, command);

      assertEquals(responses.getDropSuccess(), result);
    }
  }

  @Nested
  class ItemStateAfterDrop {

    @Test
    @DisplayName("Test handle - item removed from inventory after drop")
    void testHandle_itemRemovedFromInventory() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      final ParsedCommand command = createDropCommand("key");

      handler.handle(player, command);

      assertFalse(player.getInventory().contains(key));
    }

    @Test
    @DisplayName("Test handle - item added to location after drop")
    void testHandle_itemAddedToLocation() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      final ParsedCommand command = createDropCommand("key");

      handler.handle(player, command);

      assertTrue(location.getItems().contains(key));
    }

    @Test
    @DisplayName("Test handle - multiple drops work correctly")
    void testHandle_multipleDrops() {
      final Item key = TestItemFactory.createTestKey();
      final Item rope = TestItemFactory.createTestRope();
      player.addItem(key);
      player.addItem(rope);

      handler.handle(player, createDropCommand("key"));
      handler.handle(player, createDropCommand("rope"));

      assertTrue(player.getInventory().isEmpty());
      assertEquals(2, location.getItems().size());
      assertTrue(location.getItems().contains(key));
      assertTrue(location.getItems().contains(rope));
    }
  }

  /**
   * Helper method to create a drop command with a specified object.
   */
  private ParsedCommand createDropCommand(final String objectName) {
    final List<String> directObjects = objectName != null && !objectName.isEmpty()
        ? List.of(objectName)
        : new ArrayList<>();
    return new ParsedCommand("drop", directObjects, new ArrayList<>(), null,
        CommandType.SINGLE, false, "drop " + objectName);
  }

  /**
   * Helper method to create a drop command with no object.
   */
  private ParsedCommand createDropCommand() {
    return new ParsedCommand("drop", new ArrayList<>(), new ArrayList<>(), null,
        CommandType.SINGLE, false, "drop");
  }
}
package io.github.tmanbarton.ifengine.command.handlers;

import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.game.SceneryInteractionHandler;
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
 * Unit tests for TakeHandler.
 */
@DisplayName("TakeHandler Tests")
class TakeHandlerTest {

  private TakeHandler handler;
  private Player player;
  private Location location;
  private ResponseProvider responses;

  @BeforeEach
  void setUp() {
    final ObjectResolver objectResolver = new ObjectResolver();
    final SceneryInteractionHandler sceneryHandler = new SceneryInteractionHandler();
    final ContextManager contextManager = new ContextManager();
    responses = new DefaultResponses();
    handler = new TakeHandler(objectResolver, sceneryHandler, contextManager, responses);
    location = TestLocationFactory.createDefaultLocation();
    player = new Player(location);
  }

  @Nested
  @DisplayName("Basic Take")
  class BasicTake {

    @Test
    @DisplayName("Test handle - take item from location")
    void testHandle_takeItemFromLocation() {
      final Item key = TestItemFactory.createTestKey();
      location.addItem(key);
      final ParsedCommand command = createTakeCommand("key");

      final String result = handler.handle(player, command);

      assertEquals(responses.getTakeSuccess(), result);
      assertTrue(player.hasItem("key"));
      assertFalse(location.getItems().contains(key));
    }

    @Test
    @DisplayName("Test handle - take item already in inventory")
    void testHandle_takeItemAlreadyInInventory() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      final ParsedCommand command = createTakeCommand("key");

      final String result = handler.handle(player, command);

      assertEquals(responses.getTakeAlreadyHave(), result);
    }

    @Test
    @DisplayName("Test handle - take item not present")
    void testHandle_takeItemNotPresent() {
      final ParsedCommand command = createTakeCommand("nonexistent");

      final String result = handler.handle(player, command);

      assertEquals(responses.getItemNotPresent("nonexistent"), result);
    }

    @Test
    @DisplayName("Test handle - take with no object specified and single item")
    void testHandle_takeNoObjectSingleItem() {
      final Item key = TestItemFactory.createTestKey();
      location.addItem(key);
      final ParsedCommand command = createTakeCommand();

      final String result = handler.handle(player, command);

      assertEquals(responses.getTakeSuccess(), result);
      assertTrue(player.hasItem("key"));
    }

    @Test
    @DisplayName("Test handle - take with no object and multiple items")
    void testHandle_takeNoObjectMultipleItems() {
      final Item key = TestItemFactory.createTestKey();
      final Item rope = TestItemFactory.createTestRope();
      location.addItem(key);
      location.addItem(rope);
      final ParsedCommand command = createTakeCommand();

      final String result = handler.handle(player, command);

      assertEquals(responses.getTakeNeedToSpecify(), result);
    }

    @Test
    @DisplayName("Test handle - take with no object and no items")
    void testHandle_takeNoObjectNoItems() {
      final ParsedCommand command = createTakeCommand();

      final String result = handler.handle(player, command);

      assertEquals(responses.getTakeNoItemsAvailable(), result);
    }
  }

  @Nested
  @DisplayName("Take All")
  class TakeAll {

    @Test
    @DisplayName("Test handle - take all from location")
    void testHandle_takeAllFromLocation() {
      final Item key = TestItemFactory.createTestKey();
      final Item rope = TestItemFactory.createTestRope();
      location.addItem(key);
      location.addItem(rope);
      final ParsedCommand command = createTakeCommand("all");

      final String result = handler.handle(player, command);

      assertEquals(responses.getTakeAllSuccess(), result);
      assertTrue(player.hasItem("key"));
      assertTrue(player.hasItem("rope"));
      assertTrue(location.getItems().isEmpty());
    }

    @Test
    @DisplayName("Test handle - take all from empty location")
    void testHandle_takeAllFromEmptyLocation() {
      final ParsedCommand command = createTakeCommand("all");

      final String result = handler.handle(player, command);

      assertEquals(responses.getTakeNoItemsAvailable(), result);
    }

    @Test
    @DisplayName("Test handle - take everything synonym")
    void testHandle_takeEverything() {
      final Item key = TestItemFactory.createTestKey();
      location.addItem(key);
      final ParsedCommand command = createTakeCommand("everything");

      final String result = handler.handle(player, command);

      assertEquals(responses.getTakeSuccess(), result);
      assertTrue(player.hasItem("key"));
    }

    @Test
    @DisplayName("Test handle - take all with single item")
    void testHandle_takeAllWithSingleItem() {
      final Item key = TestItemFactory.createTestKey();
      location.addItem(key);
      final ParsedCommand command = createTakeCommand("all");

      final String result = handler.handle(player, command);

      // Single item returns getTakeSuccess, not getTakeAllSuccess
      assertEquals(responses.getTakeSuccess(), result);
    }
  }

  @Nested
  @DisplayName("Inferred Object")
  class InferredObject {

    @Test
    @DisplayName("Test handle - take single item at location without specifying")
    void testHandle_takeSingleItemAtLocation() {
      final Item key = TestItemFactory.createTestKey();
      location.addItem(key);
      final ParsedCommand command = createTakeCommand();

      final String result = handler.handle(player, command);

      assertEquals(responses.getTakeSuccess(), result);
      assertTrue(player.hasItem("key"));
    }

    @Test
    @DisplayName("Test handle - take multiple items at location requires disambiguation")
    void testHandle_takeMultipleItemsAtLocation() {
      final Item key = TestItemFactory.createTestKey();
      final Item rope = TestItemFactory.createTestRope();
      location.addItem(key);
      location.addItem(rope);
      final ParsedCommand command = createTakeCommand();

      final String result = handler.handle(player, command);

      assertEquals(responses.getTakeNeedToSpecify(), result);
      // Neither item should be taken
      assertFalse(player.hasItem("key"));
      assertFalse(player.hasItem("rope"));
    }
  }

  @Nested
  @DisplayName("Supported Verbs")
  class SupportedVerbs {

    @Test
    @DisplayName("Test getSupportedVerbs - includes take")
    void testGetSupportedVerbs_includesTake() {
      final List<String> verbs = handler.getSupportedVerbs();

      assertTrue(verbs.contains("take"));
    }

    @Test
    @DisplayName("Test getSupportedVerbs - includes get")
    void testGetSupportedVerbs_includesGet() {
      final List<String> verbs = handler.getSupportedVerbs();

      assertTrue(verbs.contains("get"));
    }

    @Test
    @DisplayName("Test getSupportedVerbs - includes grab")
    void testGetSupportedVerbs_includesGrab() {
      final List<String> verbs = handler.getSupportedVerbs();

      assertTrue(verbs.contains("grab"));
    }

    @Test
    @DisplayName("Test canHandle - returns true for supported verb")
    void testCanHandle_supportedVerb() {
      assertTrue(handler.canHandle("take"));
      assertTrue(handler.canHandle("get"));
      assertTrue(handler.canHandle("grab"));
    }

    @Test
    @DisplayName("Test canHandle - returns false for unsupported verb")
    void testCanHandle_unsupportedVerb() {
      assertFalse(handler.canHandle("drop"));
      assertFalse(handler.canHandle("look"));
    }
  }

  @Nested
  @DisplayName("Case Sensitivity")
  class CaseSensitivity {

    @Test
    @DisplayName("Test handle - take with uppercase object name")
    void testHandle_takeUppercaseObject() {
      final Item key = TestItemFactory.createTestKey();
      location.addItem(key);
      final ParsedCommand command = createTakeCommand("KEY");

      final String result = handler.handle(player, command);

      assertEquals(responses.getTakeSuccess(), result);
      assertTrue(player.hasItem("key"));
    }

    @Test
    @DisplayName("Test handle - take with mixed case object name")
    void testHandle_takeMixedCaseObject() {
      final Item key = TestItemFactory.createTestKey();
      location.addItem(key);
      final ParsedCommand command = createTakeCommand("KeY");

      final String result = handler.handle(player, command);

      assertEquals(responses.getTakeSuccess(), result);
    }
  }

  @Nested
  @DisplayName("Item State After Take")
  class ItemStateAfterTake {

    @Test
    @DisplayName("Test handle - item removed from location after take")
    void testHandle_itemRemovedFromLocation() {
      final Item key = TestItemFactory.createTestKey();
      location.addItem(key);
      final ParsedCommand command = createTakeCommand("key");

      handler.handle(player, command);

      assertFalse(location.getItems().contains(key));
    }

    @Test
    @DisplayName("Test handle - item added to inventory after take")
    void testHandle_itemAddedToInventory() {
      final Item key = TestItemFactory.createTestKey();
      location.addItem(key);
      final ParsedCommand command = createTakeCommand("key");

      handler.handle(player, command);

      assertTrue(player.getInventory().contains(key));
    }

    @Test
    @DisplayName("Test handle - multiple takes work correctly")
    void testHandle_multipleTakes() {
      final Item key = TestItemFactory.createTestKey();
      final Item rope = TestItemFactory.createTestRope();
      location.addItem(key);
      location.addItem(rope);

      handler.handle(player, createTakeCommand("key"));
      handler.handle(player, createTakeCommand("rope"));

      assertEquals(2, player.getInventory().size());
      assertTrue(player.hasItem("key"));
      assertTrue(player.hasItem("rope"));
      assertTrue(location.getItems().isEmpty());
    }
  }

  /**
   * Helper method to create a take command with a specified object.
   */
  private ParsedCommand createTakeCommand(final String objectName) {
    final List<String> directObjects = objectName != null && !objectName.isEmpty()
        ? List.of(objectName)
        : new ArrayList<>();
    return new ParsedCommand("take", directObjects, new ArrayList<>(), null,
        CommandType.SINGLE, false, "take " + objectName);
  }

  /**
   * Helper method to create a take command with no object.
   */
  private ParsedCommand createTakeCommand() {
    return new ParsedCommand("take", new ArrayList<>(), new ArrayList<>(), null,
        CommandType.SINGLE, false, "take");
  }
}
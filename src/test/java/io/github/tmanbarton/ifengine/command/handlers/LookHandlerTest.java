package io.github.tmanbarton.ifengine.command.handlers;

import io.github.tmanbarton.ifengine.InteractionType;
import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.SceneryObject;
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
 * Unit tests for LookHandler.
 */
@DisplayName("LookHandler Tests")
class LookHandlerTest {

  private LookHandler handler;
  private Player player;
  private Location location;
  private ResponseProvider responses;

  @BeforeEach
  void setUp() {
    final ObjectResolver objectResolver = new ObjectResolver();
    final SceneryInteractionHandler sceneryHandler = new SceneryInteractionHandler();
    final ContextManager contextManager = new ContextManager();
    responses = new DefaultResponses();
    handler = new LookHandler(objectResolver, sceneryHandler, contextManager, responses);
    location = TestLocationFactory.createDefaultLocation();
    player = new Player(location);
  }

  @Nested
  class LookAround {

    @Test
    @DisplayName("Test handle - look shows location description")
    void testHandle_lookShowsLocationDescription() {
      final ParsedCommand command = createLookCommand();

      final String result = handler.handle(player, command);

      assertTrue(result.contains(location.getLongDescription()));
    }

    @Test
    @DisplayName("Test handle - look around shows location description")
    void testHandle_lookAroundShowsLocationDescription() {
      final ParsedCommand command = createLookAroundCommand();

      final String result = handler.handle(player, command);

      assertTrue(result.contains(location.getLongDescription()));
    }

    @Test
    @DisplayName("Test handle - look shows items at location")
    void testHandle_lookShowsItemsAtLocation() {
      final Item key = TestItemFactory.createTestKey();
      location.addItem(key);
      final ParsedCommand command = createLookCommand();

      final String result = handler.handle(player, command);

      assertTrue(result.contains(key.getLocationDescription()));
    }
  }

  @Nested
  class LookAtObject {

    @Test
    @DisplayName("Test handle - look at item in inventory")
    void testHandle_lookAtItemInInventory() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      final ParsedCommand command = createLookAtCommand("key");

      final String result = handler.handle(player, command);

      assertEquals(key.getDetailedDescription(), result);
    }

    @Test
    @DisplayName("Test handle - look at item at location")
    void testHandle_lookAtItemAtLocation() {
      final Item key = TestItemFactory.createTestKey();
      location.addItem(key);
      final ParsedCommand command = createLookAtCommand("key");

      final String result = handler.handle(player, command);

      assertEquals(key.getDetailedDescription(), result);
    }

    @Test
    @DisplayName("Test handle - look at item not present")
    void testHandle_lookAtItemNotPresent() {
      final ParsedCommand command = createLookAtCommand("nonexistent");

      final String result = handler.handle(player, command);

      assertEquals(responses.getLookAtObjectNotPresent("nonexistent"), result);
    }

    @Test
    @DisplayName("Test handle - inventory takes priority over location - technically shouldn't happen if game is designed well (no duplicate items), but good to test")
    void testHandle_inventoryPriorityOverLocation() {
      // Create two keys with different descriptions
      final Item inventoryKey = TestItemFactory.createItem("key",
          "My inventory key",
          "Inventory key at location",
          "Detailed inventory key description.");
      final Item locationKey = TestItemFactory.createItem("key",
          "Location key",
          "Location key here",
          "Detailed location key description.");
      player.addItem(inventoryKey);
      location.addItem(locationKey);
      final ParsedCommand command = createLookAtCommand("key");

      final String result = handler.handle(player, command);

      assertEquals(inventoryKey.getDetailedDescription(), result);
    }
  }

  @Nested
  class LookAtScenery {

    @Test
    @DisplayName("Test handle - look at scenery object")
    void testHandle_lookAtSceneryObject() {
      final SceneryObject tree = SceneryObject.builder("tree")
          .withInteraction(InteractionType.LOOK, "A tall oak tree with spreading branches.")
          .build();
      location.addSceneryObject(tree);
      final ParsedCommand command = createLookAtCommand("tree");

      final String result = handler.handle(player, command);

      assertEquals("A tall oak tree with spreading branches.", result);
    }

    @Test
    @DisplayName("Test handle - scenery without look interaction")
    void testHandle_sceneryWithoutLookInteraction() {
      final SceneryObject tree = SceneryObject.builder("tree")
          .withInteraction(InteractionType.CLIMB, "You climb the tree.")
          .build();
      location.addSceneryObject(tree);
      final ParsedCommand command = createLookAtCommand("tree");

      final String result = handler.handle(player, command);

      assertEquals(responses.getLookAtObjectNotPresent("tree"), result);
    }
  }

  @Nested
  class SupportedVerbs {

    @Test
    @DisplayName("Test getSupportedVerbs - includes look")
    void testGetSupportedVerbs_includesLook() {
      final List<String> verbs = handler.getSupportedVerbs();

      assertTrue(verbs.contains("look"));
    }

    @Test
    @DisplayName("Test getSupportedVerbs - includes l")
    void testGetSupportedVerbs_includesL() {
      final List<String> verbs = handler.getSupportedVerbs();

      assertTrue(verbs.contains("l"));
    }

    @Test
    @DisplayName("Test getSupportedVerbs - includes examine")
    void testGetSupportedVerbs_includesExamine() {
      final List<String> verbs = handler.getSupportedVerbs();

      assertTrue(verbs.contains("examine"));
    }

    @Test
    @DisplayName("Test getSupportedVerbs - includes x")
    void testGetSupportedVerbs_includesX() {
      final List<String> verbs = handler.getSupportedVerbs();

      assertTrue(verbs.contains("x"));
    }

    @Test
    @DisplayName("Test canHandle - returns true for supported verb")
    void testCanHandle_supportedVerb() {
      assertTrue(handler.canHandle("look"));
      assertTrue(handler.canHandle("l"));
      assertTrue(handler.canHandle("examine"));
      assertTrue(handler.canHandle("x"));
    }

    @Test
    @DisplayName("Test canHandle - returns false for unsupported verb")
    void testCanHandle_unsupportedVerb() {
      assertFalse(handler.canHandle("take"));
      assertFalse(handler.canHandle("drop"));
    }
  }

  @Nested
  class CaseSensitivity {

    @Test
    @DisplayName("Test handle - look at with uppercase object name")
    void testHandle_lookAtUppercaseObject() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      final ParsedCommand command = createLookAtCommand("KEY");

      final String result = handler.handle(player, command);

      assertEquals(key.getDetailedDescription(), result);
    }

    @Test
    @DisplayName("Test handle - look at with mixed case object name")
    void testHandle_lookAtMixedCaseObject() {
      final Item key = TestItemFactory.createTestKey();
      location.addItem(key);
      final ParsedCommand command = createLookAtCommand("KeY");

      final String result = handler.handle(player, command);

      assertEquals(key.getDetailedDescription(), result);
    }
  }

  /**
   * Helper method to create a bare look command (look at location).
   */
  private ParsedCommand createLookCommand() {
    return new ParsedCommand("look", new ArrayList<>(), new ArrayList<>(), null,
        CommandType.SINGLE, false, "look");
  }

  /**
   * Helper method to create a "look around" command.
   */
  private ParsedCommand createLookAroundCommand() {
    return new ParsedCommand("look", new ArrayList<>(), new ArrayList<>(), "around",
        CommandType.SINGLE, false, "look around");
  }

  /**
   * Helper method to create a look at command with a specified object.
   */
  private ParsedCommand createLookAtCommand(final String objectName) {
    return new ParsedCommand("look", new ArrayList<>(), List.of(objectName), "at",
        CommandType.SINGLE, false, "look at " + objectName);
  }
}
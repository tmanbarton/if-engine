package com.ifengine.game;

import com.ifengine.InteractionType;
import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.SceneryObject;
import com.ifengine.command.handlers.ClimbHandler;
import com.ifengine.parser.CommandType;
import com.ifengine.parser.ParsedCommand;
import com.ifengine.response.DefaultResponses;
import com.ifengine.response.ResponseProvider;
import com.ifengine.test.TestItemFactory;
import com.ifengine.test.TestLocationFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for AbstractInteractionHandler.
 * Uses ClimbHandler as a concrete implementation to test the template method pattern.
 *
 * The AbstractInteractionHandler implements a unified fallback pattern:
 * 1. No object specified → try to infer from available scenery
 * 2. Object is a real item → return "can't interact" response
 * 3. Object is scenery with matching interaction → return scenery response
 * 4. Object not present → return "not present" response
 */
@DisplayName("AbstractInteractionHandler Tests")
class AbstractInteractionHandlerTest {

  private ClimbHandler handler;
  private ResponseProvider responses;
  private Player player;
  private Location location;

  @BeforeEach
  void setUp() {
    responses = new DefaultResponses();
    handler = new ClimbHandler(responses);
    location = TestLocationFactory.createDefaultLocation();
    player = new Player(location);
    player.setGameState(GameState.PLAYING);
  }

  /**
   * Creates a ParsedCommand with no direct objects.
   */
  private ParsedCommand createCommandNoObject(final String verb) {
    return new ParsedCommand(
        verb,
        Collections.emptyList(),
        Collections.emptyList(),
        null,
        CommandType.SINGLE,
        false,
        verb
    );
  }

  /**
   * Creates a ParsedCommand with a direct object.
   */
  private ParsedCommand createCommand(final String verb, final String directObject) {
    return new ParsedCommand(
        verb,
        List.of(directObject),
        Collections.emptyList(),
        null,
        CommandType.SINGLE,
        false,
        verb + " " + directObject
    );
  }

  @Nested
  @DisplayName("Object Inference (No Object Specified)")
  class ObjectInference {

    @Test
    @DisplayName("Test handle - no object, no climbable scenery returns not present")
    void testHandle_noObjectNoClimbableScenery() {
      final ParsedCommand command = createCommandNoObject("climb");

      final String result = handler.handle(player, command);

      assertEquals(responses.getClimbNotPresent(), result);
    }

    @Test
    @DisplayName("Test handle - no object, single climbable scenery is used automatically")
    void testHandle_noObjectSingleClimbableScenery() {
      final SceneryObject tree = SceneryObject.builder("tree")
          .withInteraction(InteractionType.CLIMB, "You climb the tree successfully.")
          .build();
      location.addSceneryObject(tree);
      final ParsedCommand command = createCommandNoObject("climb");

      final String result = handler.handle(player, command);

      assertEquals("You climb the tree successfully.", result);
    }

    @Test
    @DisplayName("Test handle - no object, multiple climbable scenery uses first one")
    void testHandle_noObjectMultipleClimbableScenery() {
      // Current implementation uses first matching scenery when multiple exist
      final SceneryObject tree = SceneryObject.builder("tree")
          .withInteraction(InteractionType.CLIMB, "You climb the tree.")
          .build();
      final SceneryObject ladder = SceneryObject.builder("ladder")
          .withInteraction(InteractionType.CLIMB, "You climb the ladder.")
          .build();
      location.addSceneryObject(tree);
      location.addSceneryObject(ladder);
      final ParsedCommand command = createCommandNoObject("climb");

      final String result = handler.handle(player, command);

      // First scenery object added is used
      assertEquals("You climb the tree.", result);
    }

    @Test
    @DisplayName("Test handle - no object, scenery without climb interaction is ignored")
    void testHandle_noObjectSceneryWithoutClimbInteraction() {
      final SceneryObject window = SceneryObject.builder("window")
          .withInteraction(InteractionType.PUNCH, "You punch the window.")
          .build();
      location.addSceneryObject(window);
      final ParsedCommand command = createCommandNoObject("climb");

      final String result = handler.handle(player, command);

      assertEquals(responses.getClimbNotPresent(), result);
    }
  }

  @Nested
  @DisplayName("Real Item Fallback")
  class RealItemFallback {

    @Test
    @DisplayName("Test handle - attempting to climb item in inventory returns cant climb")
    void testHandle_climbInventoryItem() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      final ParsedCommand command = createCommand("climb", "key");

      final String result = handler.handle(player, command);

      assertEquals(responses.getCantClimb(), result);
    }

    @Test
    @DisplayName("Test handle - attempting to climb item at location returns cant climb")
    void testHandle_climbLocationItem() {
      final Item rope = TestItemFactory.createTestRope();
      location.addItem(rope);
      final ParsedCommand command = createCommand("climb", "rope");

      final String result = handler.handle(player, command);

      assertEquals(responses.getCantClimb(), result);
    }

    @Test
    @DisplayName("Test handle - inventory item takes priority over location item with same name")
    void testHandle_inventoryItemPriorityOverLocation() {
      // If there's a "rope" in both inventory and location, inventory is checked first
      final Item inventoryRope = TestItemFactory.createTestRope();
      final Item locationRope = TestItemFactory.createTestRope();
      player.addItem(inventoryRope);
      location.addItem(locationRope);
      final ParsedCommand command = createCommand("climb", "rope");

      final String result = handler.handle(player, command);

      // Should still return "can't climb" because it's a real item
      assertEquals(responses.getCantClimb(), result);
    }
  }

  @Nested
  @DisplayName("Scenery Interaction")
  class SceneryInteraction {

    @Test
    @DisplayName("Test handle - climb scenery with climb response returns scenery response")
    void testHandle_climbSceneryWithClimbResponse() {
      final SceneryObject tree = SceneryObject.builder("tree")
          .withInteraction(InteractionType.CLIMB, "You climb the tall oak tree.")
          .build();
      location.addSceneryObject(tree);
      final ParsedCommand command = createCommand("climb", "tree");

      final String result = handler.handle(player, command);

      assertEquals("You climb the tall oak tree.", result);
    }

    @Test
    @DisplayName("Test handle - climb scenery with alias uses scenery response")
    void testHandle_climbSceneryWithAlias() {
      final SceneryObject tree = SceneryObject.builder("tree")
          .withAliases("oak", "big tree")
          .withInteraction(InteractionType.CLIMB, "You climb the oak tree.")
          .build();
      location.addSceneryObject(tree);
      final ParsedCommand command = createCommand("climb", "oak");

      final String result = handler.handle(player, command);

      assertEquals("You climb the oak tree.", result);
    }

    @Test
    @DisplayName("Test handle - climb scenery without climb interaction returns cant climb")
    void testHandle_climbSceneryWithoutClimbInteraction() {
      // Scenery exists but doesn't support CLIMB interaction
      final SceneryObject window = SceneryObject.builder("window")
          .withInteraction(InteractionType.PUNCH, "You punch the window.")
          .build();
      location.addSceneryObject(window);
      // Also add something climbable so we hit the "exists but doesn't support" path
      final SceneryObject tree = SceneryObject.builder("tree")
          .withInteraction(InteractionType.CLIMB, "You climb the tree.")
          .build();
      location.addSceneryObject(tree);
      final ParsedCommand command = createCommand("climb", "window");

      final String result = handler.handle(player, command);

      // Window doesn't support climb, but there ARE climbable objects at location
      assertEquals(responses.getCantClimb(), result);
    }

    @Test
    @DisplayName("Test handle - case insensitive scenery matching")
    void testHandle_caseInsensitiveSceneryMatching() {
      final SceneryObject tree = SceneryObject.builder("tree")
          .withInteraction(InteractionType.CLIMB, "You climb the tree.")
          .build();
      location.addSceneryObject(tree);
      final ParsedCommand command = createCommand("climb", "TREE");

      final String result = handler.handle(player, command);

      assertEquals("You climb the tree.", result);
    }
  }

  @Nested
  @DisplayName("Not Present Fallback")
  class NotPresentFallback {

    @Test
    @DisplayName("Test handle - object not found anywhere returns not present")
    void testHandle_objectNotFound() {
      final ParsedCommand command = createCommand("climb", "unicorn");

      final String result = handler.handle(player, command);

      assertEquals(responses.getClimbNotPresent(), result);
    }

    @Test
    @DisplayName("Test handle - object not at current location returns not present")
    void testHandle_objectNotAtCurrentLocation() {
      // Even if there's a tree at another location, it's not present here
      final ParsedCommand command = createCommand("climb", "tree");

      final String result = handler.handle(player, command);

      assertEquals(responses.getClimbNotPresent(), result);
    }
  }

  @Nested
  @DisplayName("Priority Order")
  class PriorityOrder {

    @Test
    @DisplayName("Test handle - real item takes priority over scenery with same name")
    void testHandle_realItemPriorityOverScenery() {
      // If there's both a real item "rope" and scenery "rope", real item wins
      final Item rope = TestItemFactory.createTestRope();
      location.addItem(rope);
      final SceneryObject ropeScenery = SceneryObject.builder("rope")
          .withInteraction(InteractionType.CLIMB, "You climb the rope to the rafters.")
          .build();
      location.addSceneryObject(ropeScenery);
      final ParsedCommand command = createCommand("climb", "rope");

      final String result = handler.handle(player, command);

      // Real item found first, so "can't climb" response
      assertEquals(responses.getCantClimb(), result);
    }
  }
}
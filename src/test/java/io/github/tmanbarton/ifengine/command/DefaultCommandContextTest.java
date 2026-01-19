package io.github.tmanbarton.ifengine.command;

import io.github.tmanbarton.ifengine.InteractionType;
import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.SceneryObject;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.parser.ObjectResolver;
import io.github.tmanbarton.ifengine.response.DefaultResponses;
import io.github.tmanbarton.ifengine.response.ResponseProvider;
import io.github.tmanbarton.ifengine.test.TestItemFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for DefaultCommandContext.
 */
@DisplayName("DefaultCommandContext")
class DefaultCommandContextTest {

  private Location location;
  private Player player;
  private ResponseProvider responses;
  private ObjectResolver objectResolver;
  private DefaultCommandContext context;

  @BeforeEach
  void setUp() {
    location = new Location("room", "A test room.", "Test room.");
    player = new Player(location);
    responses = new DefaultResponses();
    objectResolver = new ObjectResolver();
    context = new DefaultCommandContext(responses, objectResolver, player);
  }

  @Nested
  class PutItemInContainer {

    @Test
    @DisplayName("moves item from inventory to scenery container and returns success message")
    void testPutItemInContainer_success() {
      // Given
      final Item ladder = TestItemFactory.createSimpleItem("ladder");
      player.addItem(ladder);

      final SceneryObject wall = SceneryObject.builder("wall")
          .withInteraction(InteractionType.LOOK, "A tall stone wall.")
          .asContainer()
          .withAllowedItems("ladder")
          .withPrepositions("on", "against")
          .build();
      location.addSceneryObject(wall);

      // When
      final String result = context.putItemInContainer("ladder", "wall", "on");

      // Then
      assertEquals(responses.getPutSuccess("ladder", "on", "wall"), result);
      assertFalse(player.getInventory().contains(ladder));
      assertTrue(location.getItems().contains(ladder));
      assertTrue(location.isItemInContainer(ladder));
    }

    @Test
    @DisplayName("returns error when item is not found in inventory or location")
    void testPutItemInContainer_itemNotFound() {
      // Given
      final SceneryObject wallContainer = SceneryObject.builder("wall")
          .withInteraction(InteractionType.LOOK, "A tall stone wall.")
          .asContainer()
          .withAllowedItems("ladder")
          .build();
      location.addSceneryObject(wallContainer);

      // When
      final String result = context.putItemInContainer("ladder", "wall", "on");

      // Then
      assertEquals(responses.getPutItemNotPresent("ladder"), result);
    }

    @Test
    @DisplayName("returns error when container is not found")
    void testPutItemInContainer_containerNotFound() {
      // Given
      final Item ladder = TestItemFactory.createSimpleItem("ladder");
      player.addItem(ladder);

      // When
      final String result = context.putItemInContainer("ladder", "wall", "on");

      // Then
      assertEquals(responses.getPutContainerNotFound("wall"), result);
    }

    @Test
    @DisplayName("returns error when preposition is invalid for container")
    void testPutItemInContainer_invalidPreposition() {
      // Given
      final Item ladder = TestItemFactory.createSimpleItem("ladder");
      player.addItem(ladder);

      // SceneryObject defaults to "on/onto" prepositions when asContainer() is used
      final SceneryObject wallContainer = SceneryObject.builder("wall")
          .withInteraction(InteractionType.LOOK, "A tall stone wall.")
          .asContainer()
          .withAllowedItems("ladder")
          .build();
      location.addSceneryObject(wallContainer);

      // When - using "in" which is not valid for this container
      final String result = context.putItemInContainer("ladder", "wall", "in");

      // Then
      assertEquals(responses.getPutInvalidPreposition("wall", "on"), result);
    }

    @Test
    @DisplayName("returns error when item is not accepted by container")
    void testPutItemInContainer_itemNotAccepted() {
      // Given
      final Item rope = TestItemFactory.createSimpleItem("rope");
      player.addItem(rope);

      // Wall only accepts ladder
      final SceneryObject wallContainer = SceneryObject.builder("wall")
          .withInteraction(InteractionType.LOOK, "A tall stone wall.")
          .asContainer()
          .withAllowedItems("ladder")
          .build();
      location.addSceneryObject(wallContainer);

      // When
      final String result = context.putItemInContainer("rope", "wall", "on");

      // Then
      assertEquals(responses.getPutItemNotAccepted("wall", "rope"), result);
    }
  }

  @Nested
  class IsItemInContainer {

    @Test
    @DisplayName("returns true when item is in the specified container")
    void testIsItemInContainer_withContainerName_true() {
      // Given
      final Item key = TestItemFactory.createSimpleItem("key");
      player.addItem(key);

      final SceneryObject stump = SceneryObject.builder("stump")
          .withInteraction(InteractionType.LOOK, "A hollow stump.")
          .asContainer()
          .withPrepositions("in")
          .build();
      location.addSceneryObject(stump);

      context.putItemInContainer("key", "stump", "in");

      // When
      final boolean result = context.isItemInContainer("key", "stump");

      // Then
      assertTrue(result);
    }

    @Test
    @DisplayName("returns false when item is in a different container")
    void testIsItemInContainer_withContainerName_wrongContainer() {
      // Given
      final Item key = TestItemFactory.createSimpleItem("key");
      player.addItem(key);

      final SceneryObject stump = SceneryObject.builder("stump")
          .withInteraction(InteractionType.LOOK, "A hollow stump.")
          .asContainer()
          .withPrepositions("in")
          .build();
      final SceneryObject box = SceneryObject.builder("box")
          .withInteraction(InteractionType.LOOK, "A wooden box.")
          .asContainer()
          .withPrepositions("in")
          .build();
      location.addSceneryObject(stump);
      location.addSceneryObject(box);

      context.putItemInContainer("key", "stump", "in");

      // When
      final boolean result = context.isItemInContainer("key", "box");

      // Then
      assertFalse(result);
    }

    @Test
    @DisplayName("returns false when item is not in any container")
    void testIsItemInContainer_withContainerName_notInContainer() {
      // Given
      final Item key = TestItemFactory.createSimpleItem("key");
      location.addItem(key);

      final SceneryObject stump = SceneryObject.builder("stump")
          .withInteraction(InteractionType.LOOK, "A hollow stump.")
          .asContainer()
          .withPrepositions("in")
          .build();
      location.addSceneryObject(stump);

      // When
      final boolean result = context.isItemInContainer("key", "stump");

      // Then
      assertFalse(result);
    }

    @Test
    @DisplayName("returns true when item is in any container (no container name)")
    void testIsItemInContainer_anyContainer_true() {
      // Given
      final Item key = TestItemFactory.createSimpleItem("key");
      player.addItem(key);

      final SceneryObject stump = SceneryObject.builder("stump")
          .withInteraction(InteractionType.LOOK, "A hollow stump.")
          .asContainer()
          .withPrepositions("in")
          .build();
      location.addSceneryObject(stump);

      context.putItemInContainer("key", "stump", "in");

      // When
      final boolean result = context.isItemInContainer("key");

      // Then
      assertTrue(result);
    }

    @Test
    @DisplayName("returns false when item is not in any container (no container name)")
    void testIsItemInContainer_anyContainer_false() {
      // Given
      final Item key = TestItemFactory.createSimpleItem("key");
      location.addItem(key);

      // When
      final boolean result = context.isItemInContainer("key");

      // Then
      assertFalse(result);
    }

    @Test
    @DisplayName("returns false when item does not exist")
    void testIsItemInContainer_itemNotFound() {
      // When
      final boolean result = context.isItemInContainer("nonexistent");

      // Then
      assertFalse(result);
    }
  }
}
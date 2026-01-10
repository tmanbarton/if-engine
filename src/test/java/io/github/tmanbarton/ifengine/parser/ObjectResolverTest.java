package io.github.tmanbarton.ifengine.parser;

import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.test.TestItemFactory;
import io.github.tmanbarton.ifengine.test.TestLocationFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for ObjectResolver.
 */
@DisplayName("ObjectResolver Tests")
class ObjectResolverTest {

  private ObjectResolver resolver;
  private Player player;
  private Location location;

  @BeforeEach
  void setUp() {
    resolver = new ObjectResolver();
    location = TestLocationFactory.createDefaultLocation();
    player = new Player(location);
  }

  @Nested
  @DisplayName("Basic Resolution")
  class BasicResolution {

    @Test
    @DisplayName("Test resolveObject - item in inventory")
    void testResolveObject_itemInInventory() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);

      final ObjectResolver.ResolutionResult result = resolver.resolveObject("key", player);

      assertTrue(result.isSuccess());
      assertNotNull(result.getItem());
      assertEquals("key", result.getItem().getName());
    }

    @Test
    @DisplayName("Test resolveObject - item at location")
    void testResolveObject_itemAtLocation() {
      final Item key = TestItemFactory.createTestKey();
      location.addItem(key);

      final ObjectResolver.ResolutionResult result = resolver.resolveObject("key", player);

      assertTrue(result.isSuccess());
      assertNotNull(result.getItem());
      assertEquals("key", result.getItem().getName());
    }

    @Test
    @DisplayName("Test resolveObject - item not found")
    void testResolveObject_itemNotFound() {
      final ObjectResolver.ResolutionResult result = resolver.resolveObject("nonexistent", player);

      assertFalse(result.isSuccess());
      assertNull(result.getItem());
    }

    @Test
    @DisplayName("Test resolveObject - empty search term")
    void testResolveObject_emptySearchTerm() {
      final Item key = TestItemFactory.createTestKey();
      location.addItem(key);

      final ObjectResolver.ResolutionResult result = resolver.resolveObject("", player);

      assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Test resolveObject - case insensitive")
    void testResolveObject_caseInsensitive() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);

      final ObjectResolver.ResolutionResult result = resolver.resolveObject("KEY", player);

      assertTrue(result.isSuccess());
      assertEquals("key", result.getItem().getName());
    }
  }

  @Nested
  @DisplayName("Priority Resolution")
  class PriorityResolution {

    @Test
    @DisplayName("Test resolveObject - inventory takes priority over location")
    void testResolveObject_inventoryTakesPriorityOverLocation() {
      // Create two different keys with the same name
      final Item inventoryKey = TestItemFactory.createItem("key", "My key", "Key here", "A key I own");
      final Item locationKey = TestItemFactory.createItem("key", "Floor key", "Key on floor", "A key on the floor");

      player.addItem(inventoryKey);
      location.addItem(locationKey);

      final ObjectResolver.ResolutionResult result = resolver.resolveObject("key", player);

      assertTrue(result.isSuccess());
      // Should return the inventory key, not the location key
      assertEquals("My key", result.getItem().getInventoryDescription());
    }

    @Test
    @DisplayName("Test resolveObject - item with alias resolution")
    void testResolveObject_itemWithAliasResolution() {
      final Item key = TestItemFactory.createItemWithAliases(
          "brass-key",
          "A brass key",
          "There is a brass key here.",
          "A shiny brass key.",
          Set.of("key", "shiny key")
      );
      location.addItem(key);

      // Should find by alias
      final ObjectResolver.ResolutionResult result = resolver.resolveObject("key", player);

      assertTrue(result.isSuccess());
      assertEquals("brass-key", result.getItem().getName());
    }

    @Test
    @DisplayName("Test resolveObject - alias match in inventory")
    void testResolveObject_aliasMatchInInventory() {
      final Item gem = TestItemFactory.createItemWithAliases(
          "red-gem",
          "A red gem",
          "There is a red gem here.",
          "A sparkling red gem.",
          Set.of("gem", "ruby")
      );
      player.addItem(gem);

      final ObjectResolver.ResolutionResult result = resolver.resolveObject("ruby", player);

      assertTrue(result.isSuccess());
      assertEquals("red-gem", result.getItem().getName());
    }
  }

  @Nested
  @DisplayName("Implied Object Resolution")
  class ImpliedObjectResolution {

    @Test
    @DisplayName("Test resolveImpliedObject - single item at location for take")
    void testResolveImpliedObject_singleItemAtLocation() {
      final Item key = TestItemFactory.createTestKey();
      location.addItem(key);

      final ObjectResolver.ResolutionResult result = resolver.resolveImpliedObject("take", player);

      assertTrue(result.isSuccess());
      assertEquals("key", result.getItem().getName());
    }

    @Test
    @DisplayName("Test resolveImpliedObject - multiple items at location")
    void testResolveImpliedObject_multipleItemsAtLocation() {
      final Item key = TestItemFactory.createTestKey();
      final Item rope = TestItemFactory.createTestRope();
      location.addItem(key);
      location.addItem(rope);

      final ObjectResolver.ResolutionResult result = resolver.resolveImpliedObject("take", player);

      // Multiple items - cannot infer
      assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Test resolveImpliedObject - no items at location")
    void testResolveImpliedObject_noItemsAtLocation() {
      final ObjectResolver.ResolutionResult result = resolver.resolveImpliedObject("take", player);

      assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Test resolveImpliedObject - single inventory item for drop")
    void testResolveImpliedObject_singleInventoryItemForDrop() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);

      final ObjectResolver.ResolutionResult result = resolver.resolveImpliedObject("drop", player);

      assertTrue(result.isSuccess());
      assertEquals("key", result.getItem().getName());
    }

    @Test
    @DisplayName("Test resolveImpliedObject - multiple inventory items for drop - technically can't happen if game is designed correctly, but good to test")
    void testResolveImpliedObject_multipleInventoryItemsForDrop() {
      final Item key = TestItemFactory.createTestKey();
      final Item rope = TestItemFactory.createTestRope();
      player.addItem(key);
      player.addItem(rope);

      final ObjectResolver.ResolutionResult result = resolver.resolveImpliedObject("drop", player);

      assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Test resolveImpliedObject - look with single item available")
    void testResolveImpliedObject_lookWithSingleItem() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);

      final ObjectResolver.ResolutionResult result = resolver.resolveImpliedObject("look", player);

      assertTrue(result.isSuccess());
      assertEquals("key", result.getItem().getName());
    }

    @Test
    @DisplayName("Test resolveImpliedObject - unknown verb returns not found")
    void testResolveImpliedObject_unknownVerb() {
      final Item key = TestItemFactory.createTestKey();
      location.addItem(key);

      final ObjectResolver.ResolutionResult result = resolver.resolveImpliedObject("dance", player);

      assertFalse(result.isSuccess());
    }
  }

  @Nested
  @DisplayName("Pronoun Detection")
  class PronounDetection {

    @Test
    @DisplayName("Test resolveObject - pronoun 'it' returns not found")
    void testResolveObject_pronounIt() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);

      // "it" is a pronoun and should signal implied object resolution
      final ObjectResolver.ResolutionResult result = resolver.resolveObject("it", player);

      assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Test resolveObject - pronoun 'that' returns not found")
    void testResolveObject_pronounThat() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);

      final ObjectResolver.ResolutionResult result = resolver.resolveObject("that", player);

      assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Test resolveObject - pronoun 'this' returns not found")
    void testResolveObject_pronounThis() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);

      final ObjectResolver.ResolutionResult result = resolver.resolveObject("this", player);

      assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Test resolveObject - regular word is not treated as pronoun")
    void testResolveObject_regularWord() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);

      // "key" should resolve to the item, not be treated as a pronoun
      final ObjectResolver.ResolutionResult result = resolver.resolveObject("key", player);

      assertTrue(result.isSuccess());
    }
  }

  @Nested
  @DisplayName("Whitespace Handling")
  class WhitespaceHandling {

    @Test
    @DisplayName("Test resolveObject - handles leading whitespace")
    void testResolveObject_leadingWhitespace() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);

      final ObjectResolver.ResolutionResult result = resolver.resolveObject("  key", player);

      assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Test resolveObject - handles trailing whitespace")
    void testResolveObject_trailingWhitespace() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);

      final ObjectResolver.ResolutionResult result = resolver.resolveObject("key  ", player);

      assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Test resolveObject - handles whitespace only")
    void testResolveObject_whitespaceOnly() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);

      final ObjectResolver.ResolutionResult result = resolver.resolveObject("   ", player);

      assertFalse(result.isSuccess());
    }
  }

  @Nested
  @DisplayName("Multi-Word Object Names")
  class MultiWordObjectNames {

    @Test
    @DisplayName("Test resolveObject - multi-word item name")
    void testResolveObject_multiWordItemName() {
      final Item item = TestItemFactory.createItem(
          "rusty key",
          "A rusty key",
          "There is a rusty key here.",
          "An old rusty key."
      );
      player.addItem(item);

      final ObjectResolver.ResolutionResult result = resolver.resolveObject("rusty key", player);

      assertTrue(result.isSuccess());
      assertEquals("rusty key", result.getItem().getName());
    }

    @Test
    @DisplayName("Test resolveObject - partial name does not match")
    void testResolveObject_partialNameNoMatch() {
      final Item item = TestItemFactory.createItem(
          "rusty key",
          "A rusty key",
          "There is a rusty key here.",
          "An old rusty key."
      );
      player.addItem(item);

      // "rusty" alone should not match "rusty key" (exact matching only)
      final ObjectResolver.ResolutionResult result = resolver.resolveObject("rusty", player);

      assertFalse(result.isSuccess());
    }
  }

  @Nested
  @DisplayName("Resolution Result API")
  class ResolutionResultAPI {

    @Test
    @DisplayName("Test ResolutionResult.success - creates successful result")
    void testResolutionResult_success() {
      final Item key = TestItemFactory.createTestKey();

      final ObjectResolver.ResolutionResult result = ObjectResolver.ResolutionResult.success(key);

      assertTrue(result.isSuccess());
      assertNotNull(result.getItem());
      assertEquals(key, result.getItem());
    }

    @Test
    @DisplayName("Test ResolutionResult.notFound - creates not found result")
    void testResolutionResult_notFound() {
      final ObjectResolver.ResolutionResult result = ObjectResolver.ResolutionResult.notFound();

      assertFalse(result.isSuccess());
      assertNull(result.getItem());
    }
  }
}
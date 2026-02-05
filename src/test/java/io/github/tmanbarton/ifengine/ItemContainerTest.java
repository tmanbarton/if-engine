package io.github.tmanbarton.ifengine;

import io.github.tmanbarton.ifengine.test.TestItemFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemContainerTest {

  @Nested
  class CanAccept {

    @Test
    @DisplayName("accepts any item when no restrictions are set")
    void testCanAccept_acceptsAnyItemWhenNoRestrictions() {
      // Given
      final ItemContainer container = ItemContainer.builder("bag")
          .build();
      final Item item = TestItemFactory.createSimpleItem("key");

      // When
      final boolean result = container.canAccept(item);

      // Then
      assertTrue(result, "container with no allowed-item restrictions should accept any item");
    }

    @Test
    @DisplayName("rejects item not in allowed items set")
    void testCanAccept_rejectsDisallowedItem() {
      // Given
      final ItemContainer container = ItemContainer.builder("bag")
          .withAllowedItems(Set.of("gem"))
          .build();
      final Item item = TestItemFactory.createSimpleItem("key");

      // When
      final boolean result = container.canAccept(item);

      // Then
      assertFalse(result, "container should reject items not in the allowed items set");
    }

    @Test
    @DisplayName("accepts item that is in allowed items set")
    void testCanAccept_acceptsAllowedItem() {
      // Given
      final ItemContainer container = ItemContainer.builder("bag")
          .withAllowedItems(Set.of("key"))
          .build();
      final Item item = TestItemFactory.createSimpleItem("key");

      // When
      final boolean result = container.canAccept(item);

      // Then
      assertTrue(result, "container should accept items in the allowed items set");
    }

    @Test
    @DisplayName("rejects item when container is at capacity")
    void testCanAccept_rejectsWhenFull() {
      // Given
      final ItemContainer container = ItemContainer.builder("bag")
          .withCapacity(1)
          .build();
      final Item first = TestItemFactory.createSimpleItem("key");
      container.insertItem(first);
      final Item second = TestItemFactory.createSimpleItem("gem");

      // When
      final boolean result = container.canAccept(second);

      // Then
      assertFalse(result, "container at capacity should reject additional items");
    }
  }

  @Nested
  class InsertItem {

    @Test
    @DisplayName("successfully inserts an accepted item")
    void testInsertItem_succeeds() {
      // Given
      final ItemContainer container = ItemContainer.builder("bag").build();
      final Item item = TestItemFactory.createSimpleItem("key");

      // When
      final boolean result = container.insertItem(item);

      // Then
      assertTrue(result, "inserting an accepted item should succeed");
      assertTrue(container.containsItem("key"),
          "container should contain the inserted item");
    }

    @Test
    @DisplayName("rejects duplicate item with same name")
    void testInsertItem_rejectsDuplicate() {
      // Given
      final ItemContainer container = ItemContainer.builder("bag").build();
      final Item item = TestItemFactory.createSimpleItem("key");
      container.insertItem(item);

      // When
      final boolean result = container.insertItem(item);

      // Then
      assertFalse(result, "inserting a duplicate item should fail");
    }

    @Test
    @DisplayName("rejects item that is not accepted")
    void testInsertItem_rejectsWhenNotAccepted() {
      // Given
      final ItemContainer container = ItemContainer.builder("bag")
          .withAllowedItems(Set.of("gem"))
          .build();
      final Item item = TestItemFactory.createSimpleItem("key");

      // When
      final boolean result = container.insertItem(item);

      // Then
      assertFalse(result, "inserting a disallowed item should fail");
    }
  }

  @Nested
  class IsFull {

    @Test
    @DisplayName("returns true when container is at capacity")
    void testIsFull_trueAtCapacity() {
      // Given
      final ItemContainer container = ItemContainer.builder("bag")
          .withCapacity(2)
          .build();
      container.insertItem(TestItemFactory.createSimpleItem("key"));
      container.insertItem(TestItemFactory.createSimpleItem("gem"));

      // When
      final boolean result = container.isFull();

      // Then
      assertTrue(result, "container with 2 items at capacity 2 should be full");
    }

    @Test
    @DisplayName("returns false when capacity is unlimited (0)")
    void testIsFull_falseWhenUnlimited() {
      // Given
      final ItemContainer container = ItemContainer.builder("bag").build();
      container.insertItem(TestItemFactory.createSimpleItem("key"));

      // When
      final boolean result = container.isFull();

      // Then
      assertFalse(result, "container with unlimited capacity should never be full");
    }
  }

  @Nested
  class ContainsItem {

    @Test
    @DisplayName("matches item names case-insensitively")
    void testContainsItem_caseInsensitive() {
      // Given
      final ItemContainer container = ItemContainer.builder("bag").build();
      container.insertItem(TestItemFactory.createSimpleItem("key"));

      // When / Then
      assertTrue(container.containsItem("Key"),
          "containsItem should match 'Key' against inserted 'key' case-insensitively");
      assertTrue(container.containsItem("KEY"),
          "containsItem should match 'KEY' against inserted 'key' case-insensitively");
    }
  }

  @Nested
  class ContainerStateDescription {

    @Test
    @DisplayName("describes empty container")
    void testGetContainerStateDescription_empty() {
      // Given
      final ItemContainer container = ItemContainer.builder("bag").build();

      // When
      final String description = container.getContainerStateDescription();

      // Then
      assertEquals("The bag is empty.", description,
          "empty container should describe itself as empty");
    }

    @Test
    @DisplayName("lists items in container")
    void testGetContainerStateDescription_withItems() {
      // Given
      final ItemContainer container = ItemContainer.builder("bag").build();
      container.insertItem(TestItemFactory.createSimpleItem("key"));

      // When
      final String description = container.getContainerStateDescription();

      // Then
      assertEquals("The bag contains: key", description,
          "container with items should list its contents");
    }
  }
}
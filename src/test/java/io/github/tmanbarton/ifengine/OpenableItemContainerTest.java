package io.github.tmanbarton.ifengine;

import io.github.tmanbarton.ifengine.test.TestItemFactory;
import io.github.tmanbarton.ifengine.test.TestOpenableItemContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenableItemContainerTest {

  @Nested
  class CanAcceptOpenState {

    @Test
    @DisplayName("rejects any item when container is closed")
    void testCanAccept_rejectsWhenClosed() {
      // Given
      final TestOpenableItemContainer container =
          new TestOpenableItemContainer("chest", "key", 5);
      final Item item = TestItemFactory.createSimpleItem("gem");

      // When
      final boolean result = container.canAccept(item);

      // Then
      assertFalse(result,
          "closed openable container should reject all items regardless of restrictions");
    }

    @Test
    @DisplayName("accepts item when container is open")
    void testCanAccept_acceptsWhenOpen() {
      // Given
      final TestOpenableItemContainer container =
          new TestOpenableItemContainer("chest", "key", 5);
      container.setUnlocked(true);
      container.setOpen(true);
      final Item item = TestItemFactory.createSimpleItem("gem");

      // When
      final boolean result = container.canAccept(item);

      // Then
      assertTrue(result,
          "open container with no restrictions should accept any item");
    }

    @Test
    @DisplayName("rejects item when open but at capacity")
    void testCanAccept_rejectsWhenOpenButFull() {
      // Given
      final TestOpenableItemContainer container =
          new TestOpenableItemContainer("chest", "key", 1);
      container.setUnlocked(true);
      container.setOpen(true);
      container.insertItem(TestItemFactory.createSimpleItem("gem"));
      final Item second = TestItemFactory.createSimpleItem("rope");

      // When
      final boolean result = container.canAccept(second);

      // Then
      assertFalse(result,
          "open container at capacity should reject additional items");
    }

    @Test
    @DisplayName("rejects disallowed item even when open")
    void testCanAccept_rejectsWhenOpenButDisallowed() {
      // Given
      final TestOpenableItemContainer container =
          new TestOpenableItemContainer("chest", "key", true, 5,
              Set.of("gem"), List.of("in", "into"));
      container.setUnlocked(true);
      container.setOpen(true);
      final Item item = TestItemFactory.createSimpleItem("rope");

      // When
      final boolean result = container.canAccept(item);

      // Then
      assertFalse(result,
          "open container with item restrictions should reject disallowed items");
    }
  }

  @Nested
  class InsertWhenClosed {

    @Test
    @DisplayName("insert fails when container is closed")
    void testInsertItem_failsWhenClosed() {
      // Given
      final TestOpenableItemContainer container =
          new TestOpenableItemContainer("chest", "key", 5);
      final Item item = TestItemFactory.createSimpleItem("gem");

      // When
      final boolean result = container.insertItem(item);

      // Then
      assertFalse(result,
          "inserting into a closed container should fail");
      assertFalse(container.containsItem("gem"),
          "closed container should not contain the rejected item");
    }
  }

  @Nested
  class UnlockOpenInsertFlow {

    @Test
    @DisplayName("insert succeeds after container is unlocked and opened")
    void testInsertItem_succeedsAfterUnlockAndOpen() {
      // Given
      final TestOpenableItemContainer container =
          new TestOpenableItemContainer("chest", "key", 5);
      final Item item = TestItemFactory.createSimpleItem("gem");

      // When
      container.setUnlocked(true);
      container.setOpen(true);
      final boolean result = container.insertItem(item);

      // Then
      assertTrue(result,
          "insert should succeed on an unlocked and opened container");
      assertTrue(container.containsItem("gem"),
          "container should contain the inserted item after unlock and open");
    }
  }

  @Nested
  class ContainerStateDescription {

    @Test
    @DisplayName("describes empty container")
    void testGetContainerStateDescription_empty() {
      // Given
      final TestOpenableItemContainer container =
          new TestOpenableItemContainer("chest", "key", 5);

      // When
      final String description = container.getContainerStateDescription();

      // Then
      assertEquals("The chest is empty.", description,
          "empty openable container should describe itself as empty");
    }

    @Test
    @DisplayName("lists items in container")
    void testGetContainerStateDescription_withItems() {
      // Given
      final TestOpenableItemContainer container =
          new TestOpenableItemContainer("chest", "key", 5);
      container.setUnlocked(true);
      container.setOpen(true);
      container.insertItem(TestItemFactory.createSimpleItem("gem"));

      // When
      final String description = container.getContainerStateDescription();

      // Then
      assertEquals("The chest contains: gem", description,
          "openable container with items should list its contents");
    }
  }
}
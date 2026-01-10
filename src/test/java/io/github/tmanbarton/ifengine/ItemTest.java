package io.github.tmanbarton.ifengine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for Item class.
 * Tests name matching and equality.
 */
@DisplayName("Item Tests")
class ItemTest {

  @Nested
  @DisplayName("Name Matching")
  class NameMatching {

    @Test
    @DisplayName("Test matchesName - exact match")
    void testMatchesName_exactMatch() {
      final Item item = new Item("key", "A key", "Key here.", "A key.");

      assertTrue(item.matchesName("key"));
    }

    @Test
    @DisplayName("Test matchesName - case insensitive")
    void testMatchesName_caseInsensitive() {
      final Item item = new Item("key", "A key", "Key here.", "A key.");

      assertTrue(item.matchesName("KEY"));
      assertTrue(item.matchesName("Key"));
    }

    @Test
    @DisplayName("Test matchesName - alias match")
    void testMatchesName_aliasMatch() {
      final Item item = new Item(
          "key",
          "A brass key",
          "Key here.",
          "A key.",
          Set.of("brass key", "ornate key")
      );

      assertTrue(item.matchesName("brass key"));
      assertTrue(item.matchesName("ornate key"));
    }

    @Test
    @DisplayName("Test matchesName - no match")
    void testMatchesName_noMatch() {
      final Item item = new Item("key", "A key", "Key here.", "A key.");

      assertFalse(item.matchesName("rope"));
      assertFalse(item.matchesName("keys"));
    }
  }

  @Nested
  @DisplayName("Equality")
  class Equality {

    @Test
    @DisplayName("Test equals - equal items return true")
    void testEquals_equalItems() {
      final Item item1 = new Item("key", "A key", "Key here.", "A key.", Set.of("alias"));
      final Item item2 = new Item("key", "A key", "Key here.", "A key.", Set.of("alias"));

      assertEquals(item1, item2);
    }

    @Test
    @DisplayName("Test equals - different name returns false")
    void testEquals_differentName() {
      final Item item1 = new Item("key", "A key", "Key here.", "A key.");
      final Item item2 = new Item("rope", "A key", "Key here.", "A key.");

      assertNotEquals(item1, item2);
    }
  }
}
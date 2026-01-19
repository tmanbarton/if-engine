package io.github.tmanbarton.ifengine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for LocationContainer class.
 */
@DisplayName("LocationContainer Tests")
class LocationContainerTest {

  @Nested
  class SceneryObjectConstructor {

    @Test
    @DisplayName("Test constructor - reads isContainer from SceneryObject")
    void testConstructor_readsContainerConfig() {
      final SceneryObject table = SceneryObject.builder("table")
          .withInteraction(InteractionType.LOOK, "A wooden table.")
          .asContainer()
          .build();

      final LocationContainer container = new LocationContainer(table);

      assertEquals(table, container.getSceneryObject());
    }

    @Test
    @DisplayName("Test getPreferredPrepositions - returns SceneryObject prepositions")
    void testGetPreferredPrepositions_returnsSceneryPrepositions() {
      final SceneryObject drawer = SceneryObject.builder("drawer")
          .withInteraction(InteractionType.LOOK, "A wooden drawer.")
          .asContainer()
          .withPrepositions("in", "into")
          .build();

      final LocationContainer container = new LocationContainer(drawer);

      final List<String> prepositions = container.getPreferredPrepositions();
      assertEquals(2, prepositions.size());
      assertTrue(prepositions.contains("in"));
      assertTrue(prepositions.contains("into"));
    }

    @Test
    @DisplayName("Test getPreferredPrepositions - uses default on/onto from SceneryObject")
    void testGetPreferredPrepositions_usesDefaultOnOnto() {
      final SceneryObject table = SceneryObject.builder("table")
          .withInteraction(InteractionType.LOOK, "A wooden table.")
          .asContainer()
          .build();

      final LocationContainer container = new LocationContainer(table);

      final List<String> prepositions = container.getPreferredPrepositions();
      assertEquals(2, prepositions.size());
      assertTrue(prepositions.contains("on"));
      assertTrue(prepositions.contains("onto"));
    }

    @Test
    @DisplayName("Test canAccept - uses SceneryObject allowedItemNames")
    void testCanAccept_usesSceneryAllowedItems() {
      final SceneryObject table = SceneryObject.builder("table")
          .withInteraction(InteractionType.LOOK, "A wooden table.")
          .asContainer()
          .withAllowedItems("book", "key")
          .build();

      final LocationContainer container = new LocationContainer(table);

      final Item book = new Item("book", "a book", "A book.", "A dusty book.");
      final Item coin = new Item("coin", "a coin", "A coin.", "A gold coin.");

      assertTrue(container.canAccept(book));
      assertFalse(container.canAccept(coin));
    }

    @Test
    @DisplayName("Test canAccept - accepts any item when no restrictions")
    void testCanAccept_acceptsAnyWhenNoRestrictions() {
      final SceneryObject table = SceneryObject.builder("table")
          .withInteraction(InteractionType.LOOK, "A wooden table.")
          .asContainer()
          .build();

      final LocationContainer container = new LocationContainer(table);

      final Item book = new Item("book", "a book", "A book.", "A dusty book.");
      final Item coin = new Item("coin", "a coin", "A coin.", "A gold coin.");

      assertTrue(container.canAccept(book));
      assertTrue(container.canAccept(coin));
    }
  }

  @Nested
  class LegacyConstructor {

    @Test
    @DisplayName("Test legacy constructor - still works for backwards compatibility")
    void testLegacyConstructor_stillWorks() {
      final SceneryObject table = SceneryObject.builder("table")
          .withInteraction(InteractionType.LOOK, "A wooden table.")
          .build();

      final LocationContainer container = new LocationContainer(table, Set.of("book"));

      final Item book = new Item("book", "a book", "A book.", "A dusty book.");
      final Item coin = new Item("coin", "a coin", "A coin.", "A gold coin.");

      assertTrue(container.canAccept(book));
      assertFalse(container.canAccept(coin));
    }
  }
}

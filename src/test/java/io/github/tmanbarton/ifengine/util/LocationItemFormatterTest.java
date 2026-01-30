package io.github.tmanbarton.ifengine.util;

import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.test.TestItemFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocationItemFormatterTest {

  private Location location;
  private Player player;

  @BeforeEach
  void setUp() {
    location = new Location("test-room", "A test room.", "Test room");
    player = new Player(location);
  }

  @Nested
  class RevealedDescriptionOverride {

    @Test
    @DisplayName("uses revealed description for items revealed from hidden state")
    void testFormatItems_usesRevealedDescription() {
      // Given
      final Item key = TestItemFactory.createTestKey();
      final String revealedDesc = "There's a key under the table.";
      location.addHiddenItem(key, revealedDesc);
      location.revealItem(key);

      // When
      final String result = LocationItemFormatter.formatItems(
          location.getItems(), player, location, true);

      // Then
      assertEquals(revealedDesc, result);
    }

    @Test
    @DisplayName("uses normal location description for regular items")
    void testFormatItems_usesNormalDescription() {
      // Given
      final Item key = TestItemFactory.createTestKey();
      location.addItem(key);

      // When
      final String result = LocationItemFormatter.formatItems(
          location.getItems(), player, location, true);

      // Then
      assertEquals(key.getLocationDescription(), result);
    }

    @Test
    @DisplayName("uses normal description after item taken and re-dropped")
    void testFormatItems_usesNormalDescriptionAfterTakeAndDrop() {
      // Given
      final Item key = TestItemFactory.createTestKey();
      location.addHiddenItem(key, "There's a key under the table.");
      location.revealItem(key);

      // Simulate take + drop
      location.removeItem(key);
      location.addItem(key);

      // When
      final String result = LocationItemFormatter.formatItems(
          location.getItems(), player, location, true);

      // Then
      assertEquals(key.getLocationDescription(), result);
    }
  }
}
package com.ifengine.command;

import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.game.Player;
import com.ifengine.parser.ObjectResolver;
import com.ifengine.response.DefaultResponses;
import com.ifengine.response.ResponseProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for DefaultCommandContext.
 */
@DisplayName("DefaultCommandContext")
class DefaultCommandContextTest {

  private Location location;
  private Player player;
  private ResponseProvider responseProvider;
  private ObjectResolver objectResolver;
  private DefaultCommandContext context;

  @BeforeEach
  void setUp() {
    location = new Location("room", "A test room.", "Test room.");
    player = new Player(location);
    responseProvider = new DefaultResponses();
    objectResolver = new ObjectResolver();
    context = new DefaultCommandContext(responseProvider, objectResolver, player);
  }

  @Nested
  @DisplayName("resolveItem")
  class ResolveItem {

    @Test
    @DisplayName("finds item at player's location")
    void testResolveItem_findsItemAtLocation() {
      // Given
      final Item key = new Item("key", "a key", "A key.", "An old key.");
      location.addItem(key);

      // When
      final Optional<Item> result = context.resolveItem("key", player);

      // Then
      assertTrue(result.isPresent());
      assertEquals("key", result.get().getName());
    }

    @Test
    @DisplayName("finds item in player's inventory")
    void testResolveItem_findsItemInInventory() {
      // Given
      final Item gem = new Item("gem", "a gem", "A gem.", "A shiny gem.");
      player.addItem(gem);

      // When
      final Optional<Item> result = context.resolveItem("gem", player);

      // Then
      assertTrue(result.isPresent());
      assertEquals("gem", result.get().getName());
    }

    @Test
    @DisplayName("returns empty when item not found")
    void testResolveItem_returnsEmptyWhenNotFound() {
      // When
      final Optional<Item> result = context.resolveItem("nonexistent", player);

      // Then
      assertFalse(result.isPresent());
    }
  }
}
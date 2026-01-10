package io.github.tmanbarton.ifengine.command;

import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.parser.ObjectResolver;
import io.github.tmanbarton.ifengine.response.ResponseProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Context provided to custom command handlers.
 * <p>
 * Provides access to game utilities that custom handlers may need, including:
 * <ul>
 *   <li>ResponseProvider for consistent game messaging</li>
 *   <li>ObjectResolver for finding items by name</li>
 *   <li>Current location information</li>
 *   <li>Convenience methods for common operations</li>
 * </ul>
 */
public interface CommandContext {

  /**
   * Returns the response provider for consistent game messaging.
   *
   * @return the response provider
   */
  @Nonnull
  ResponseProvider getResponseProvider();

  /**
   * Returns the object resolver for finding items by name.
   * <p>
   * May be null in test contexts.
   *
   * @return the object resolver, or null if not available
   */
  @Nullable
  ObjectResolver getObjectResolver();

  /**
   * Returns the player's current location.
   *
   * @return the current location
   */
  @Nonnull
  Location getCurrentLocation();

  /**
   * Resolves an item by name from the player's inventory or current location.
   * <p>
   * This is a convenience method that handles the common case of looking up
   * an item that may be in inventory or at the current location.
   *
   * @param name the name to search for
   * @param player the player whose inventory and location to search
   * @return the item if found, or empty if not found
   */
  @Nonnull
  Optional<Item> resolveItem(@Nonnull String name, @Nonnull Player player);

  /**
   * Checks if the player has an item in their inventory.
   *
   * @param itemName the name of the item to check for
   * @return true if the player has the item
   */
  boolean playerHasItem(@Nonnull String itemName);
}

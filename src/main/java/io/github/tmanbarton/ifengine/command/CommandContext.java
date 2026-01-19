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

  /**
   * Puts an item into a container, returning a response message.
   * <p>
   * Handles all aspects of container insertion:
   * <ul>
   *   <li>Finding the item in inventory or at the current location</li>
   *   <li>Finding the container (inventory container or scenery container)</li>
   *   <li>Validating the preposition matches the container's accepted prepositions</li>
   *   <li>Checking if the container accepts the item</li>
   *   <li>Removing the item from inventory if needed</li>
   *   <li>Inserting the item into the container</li>
   *   <li>Tracking containment state</li>
   * </ul>
   * <p>
   * Example usage in a custom command handler:
   * <pre>
   * .withCommand("lean", (player, cmd, ctx) -> {
   *     return ctx.putItemInContainer("ladder", "wall", "on");
   * })
   * </pre>
   *
   * @param itemName the name of the item to put in the container
   * @param containerName the name of the container
   * @param preposition the preposition to use (e.g., "in", "on", "into", "onto")
   * @return a response message (success or error description)
   */
  @Nonnull
  String putItemInContainer(@Nonnull String itemName, @Nonnull String containerName, @Nonnull String preposition);

  /**
   * Checks if an item is in any container at the current location.
   *
   * @param itemName the name of the item to check
   * @return true if the item is in a container, false otherwise
   */
  boolean isItemInContainer(@Nonnull String itemName);

  /**
   * Checks if an item is in a specific container at the current location.
   *
   * @param itemName the name of the item to check
   * @param containerName the name of the container to check
   * @return true if the item is in the specified container, false otherwise
   */
  boolean isItemInContainer(@Nonnull String itemName, @Nonnull String containerName);
}

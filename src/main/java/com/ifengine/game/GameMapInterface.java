package com.ifengine.game;

import com.ifengine.Item;
import com.ifengine.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Interface for game maps that provide locations and items for the game world.
 * This allows for polymorphic use of production GameMap and test TestGameMap implementations.
 */
public interface GameMapInterface {

  /**
   * Gets a location by its unique key/name.
   *
   * @param locationKey the unique identifier for the location
   * @return the location with the given key, or null if not found
   */
  @Nullable
  Location getLocation(@Nonnull String locationKey);

  /**
   * Gets an item by its unique key/name.
   *
   * @param itemKey the unique identifier for the item
   * @return the item with the given key, or null if not found
   */
  @Nullable
  Item getItem(@Nonnull String itemKey);

  /**
   * Gets all locations in this game map.
   *
   * @return an unmodifiable collection of all locations
   */
  @Nonnull
  Collection<Location> getAllLocations();

  /**
   * Gets all items in this game map.
   *
   * @return an unmodifiable collection of all items
   */
  @Nonnull
  Collection<Item> getAllItems();

  /**
   * Gets the starting location for new players.
   *
   * @return the starting location, should not be null for valid game maps
   */
  @Nonnull
  Location getStartingLocation();

  /**
   * Resets the game map to its initial state.
   * This includes resetting item locations, clearing visited flags, and restoring any other initial state.
   * Implementation varies between production and test maps.
   */
  void resetMap();
}
package com.ifengine.test;

import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.game.GameMapInterface;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Test-specific implementation of GameMapInterface for isolated testing.
 * <p>
 * TestGameMap provides a controlled game world that doesn't depend on production
 * content definitions. It supports:
 * <ul>
 *   <li>Adding locations and items programmatically</li>
 *   <li>Tracking initial item locations for reset functionality</li>
 *   <li>Full GameMapInterface compatibility for use with GameEngine</li>
 * </ul>
 * <p>
 * Use TestGameMapBuilder for fluent construction, or static factory methods
 * for simple scenarios.
 */
public class TestGameMap implements GameMapInterface {

  private final Map<String, Location> locations;
  private final Map<String, Item> items;
  private final Map<String, String> initialItemLocations;
  private Location startingLocation;

  /**
   * Creates an empty TestGameMap.
   * Use addLocation and addItem to populate.
   */
  public TestGameMap() {
    this.locations = new HashMap<>();
    this.items = new HashMap<>();
    this.initialItemLocations = new HashMap<>();
    this.startingLocation = null;
  }

  /**
   * Creates an empty TestGameMap.
   *
   * @return a new empty TestGameMap
   */
  @Nonnull
  public static TestGameMap createEmpty() {
    return new TestGameMap();
  }

  /**
   * Creates a TestGameMap with the specified locations.
   * The first location becomes the starting location.
   *
   * @param locations the locations to add
   * @return a new TestGameMap with the locations
   */
  @Nonnull
  public static TestGameMap createWithLocations(@Nonnull final Location... locations) {
    final TestGameMap map = new TestGameMap();
    for (final Location location : locations) {
      map.addLocation(location);
    }
    return map;
  }

  /**
   * Creates a TestGameMap with the specified items.
   * Note: items are not placed in any location - use addLocation and placeItem.
   *
   * @param items the items to add
   * @return a new TestGameMap with the items
   */
  @Nonnull
  public static TestGameMap createWithItems(@Nonnull final Item... items) {
    final TestGameMap map = new TestGameMap();
    for (final Item item : items) {
      map.addItem(item);
    }
    return map;
  }

  /**
   * Creates a TestGameMap with a single location and items placed there.
   *
   * @param location the location
   * @param items the items to place at the location
   * @return a new TestGameMap with the location and items
   */
  @Nonnull
  public static TestGameMap createWithContent(@Nonnull final Location location,
                                              @Nonnull final Item... items) {
    final TestGameMap map = new TestGameMap();
    map.addLocation(location);
    for (final Item item : items) {
      map.addItem(item);
      location.addItem(item);
      map.recordInitialItemLocation(item.getName(), location.getName());
    }
    return map;
  }

  /**
   * Adds a location to this map.
   * The first location added becomes the starting location.
   *
   * @param location the location to add
   */
  public void addLocation(@Nonnull final Location location) {
    locations.put(location.getName(), location);
    if (startingLocation == null) {
      startingLocation = location;
    }
  }

  /**
   * Adds an item to this map's item registry.
   * Note: This does not place the item in a location.
   *
   * @param item the item to add
   */
  public void addItem(@Nonnull final Item item) {
    items.put(item.getName(), item);
  }

  /**
   * Sets the starting location for the game.
   *
   * @param location the starting location (must already be in the map)
   * @throws IllegalArgumentException if location is not in the map
   */
  public void setStartingLocation(@Nonnull final Location location) {
    if (!locations.containsKey(location.getName())) {
      throw new IllegalArgumentException(
          "Location '" + location.getName() + "' is not in this map. Add it first with addLocation().");
    }
    startingLocation = location;
    startingLocation.setVisited(true);
  }

  /**
   * Records where an item was initially placed (for reset functionality).
   *
   * @param itemName the name of the item
   * @param locationName the name of the location where it was placed
   */
  public void recordInitialItemLocation(@Nonnull final String itemName,
                                        @Nonnull final String locationName) {
    initialItemLocations.put(itemName, locationName);
  }

  /**
   * Places an item at a location and records its initial position.
   *
   * @param item the item to place
   * @param location the location to place it at
   */
  public void placeItem(@Nonnull final Item item, @Nonnull final Location location) {
    if (!items.containsKey(item.getName())) {
      addItem(item);
    }
    location.addItem(item);
    recordInitialItemLocation(item.getName(), location.getName());
  }

  @Override
  @Nullable
  public Location getLocation(@Nonnull final String locationKey) {
    return locations.get(locationKey);
  }

  @Override
  @Nullable
  public Item getItem(@Nonnull final String itemKey) {
    return items.get(itemKey);
  }

  @Override
  @Nonnull
  public Collection<Location> getAllLocations() {
    return Collections.unmodifiableCollection(locations.values());
  }

  @Override
  @Nonnull
  public Collection<Item> getAllItems() {
    return Collections.unmodifiableCollection(items.values());
  }

  @Override
  @Nonnull
  public Location getStartingLocation() {
    if (startingLocation == null) {
      throw new IllegalStateException(
          "No starting location set. Add at least one location with addLocation().");
    }
    return startingLocation;
  }

  @Override
  public void resetMap() {
    // Reset visited flags on all locations
    for (final Location location : locations.values()) {
      location.setVisited(false);
    }

    // Reset item locations
    resetItemLocations();
  }

  /**
   * Resets items to their initial locations.
   */
  private void resetItemLocations() {
    // Remove all items from all locations
    for (final Location location : locations.values()) {
      for (final Item item : location.getItems()) {
        location.removeItem(item);
      }
    }

    // Place items back in their initial locations
    for (final Map.Entry<String, String> entry : initialItemLocations.entrySet()) {
      final String itemName = entry.getKey();
      final String locationName = entry.getValue();

      final Item item = items.get(itemName);
      final Location location = locations.get(locationName);

      if (item != null && location != null) {
        location.addItem(item);
      }
    }
  }

  /**
   * Gets a location by name, for test assertions.
   *
   * @param name the location name
   * @return the location, or null if not found
   */
  @Nullable
  public Location getLocationByName(@Nonnull final String name) {
    return locations.get(name);
  }

  /**
   * Gets an item by name, for test assertions.
   *
   * @param name the item name
   * @return the item, or null if not found
   */
  @Nullable
  public Item getItemByName(@Nonnull final String name) {
    return items.get(name);
  }
}
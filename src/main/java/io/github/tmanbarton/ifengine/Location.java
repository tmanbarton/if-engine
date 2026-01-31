package io.github.tmanbarton.ifengine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;

/**
 * Represents a location in the game world.
 */
public class Location {
  private final String name;
  private final String longDescription;
  private final String shortDescription;
  private final Map<Direction, Location> connections;
  private final List<Item> items;
  private final List<SceneryObject> sceneryObjects;
  private final List<LocationContainer> locationContainers;
  private final Map<Item, LocationContainer> itemContainment;
  private final Set<Item> hiddenItems;
  private final Map<Item, String> revealedLocationDescriptions;
  private boolean visited;

  public Location(@Nonnull final String name, @Nonnull final String longDescription, @Nonnull final String shortDescription) {
    this.name = name;
    this.longDescription = longDescription;
    this.shortDescription = shortDescription;
    this.connections = new HashMap<>();
    this.items = new ArrayList<>();
    this.sceneryObjects = new ArrayList<>();
    this.locationContainers = new ArrayList<>();
    this.itemContainment = new HashMap<>();
    this.hiddenItems = new LinkedHashSet<>();
    this.revealedLocationDescriptions = new HashMap<>();
    this.visited = false;
  }

  public Location(@Nonnull final String name, @Nonnull final String longDescription, @Nonnull final String shortDescription, @Nonnull final List<SceneryObject> sceneryObjects) {
    this.name = name;
    this.longDescription = longDescription;
    this.shortDescription = shortDescription;
    this.connections = new HashMap<>();
    this.items = new ArrayList<>();
    this.sceneryObjects = new ArrayList<>(sceneryObjects);
    this.locationContainers = new ArrayList<>();
    this.itemContainment = new HashMap<>();
    this.hiddenItems = new LinkedHashSet<>();
    this.revealedLocationDescriptions = new HashMap<>();
    this.visited = false;
  }

  public void addConnection(@Nonnull final Direction direction, @Nonnull final Location location) {
    connections.put(direction, location);
  }

  public void replaceConnection(@Nonnull final Direction direction, @Nonnull final Location location) {
    connections.put(direction, location);
  }

  @Nullable
  public Location getConnection(@Nonnull final Direction direction) {
    return connections.get(direction);
  }

  @Nonnull
  public Set<Direction> getAvailableDirections() {
    return connections.keySet();
  }

  public void addItem(@Nonnull final Item item) {
    items.add(item);
  }

  public boolean removeItem(@Nonnull final Item item) {
    revealedLocationDescriptions.remove(item);
    return items.remove(item);
  }

  @Nonnull
  public List<Item> getItems() {
    return new ArrayList<>(items);
  }

  /**
   * Finds an item at this location by name (case-insensitive).
   *
   * @param itemName the name of the item to find
   * @return the first matching item, or null if not found
   */
  @Nullable
  public Item getItemByName(@Nonnull final String itemName) {
    return items.stream()
      .filter(item -> item.matchesName(itemName))
      .findFirst()
      .orElse(null);
  }

  /**
   * Checks if this location has an item with the given name.
   *
   * @param itemName the name of the item to check for
   * @return true if an item with that name exists at this location
   */
  public boolean hasItem(@Nonnull final String itemName) {
    return getItemByName(itemName) != null;
  }

  /**
   * Adds a scenery object to this location.
   * <p>
   * If the scenery object is configured as a container (via {@code asContainer()} on its builder),
   * a {@link LocationContainer} is automatically created and registered.
   *
   * @param sceneryObject the scenery object to add
   */
  public void addSceneryObject(@Nonnull final SceneryObject sceneryObject) {
    sceneryObjects.add(sceneryObject);

    // Auto-create LocationContainer if scenery is configured as a container
    if (sceneryObject.isContainer()) {
      final LocationContainer container = new LocationContainer(sceneryObject);
      locationContainers.add(container);
    }
  }

  public void removeSceneryObject(@Nonnull final SceneryObject sceneryObject) {
    sceneryObjects.remove(sceneryObject);
  }

  @Nonnull
  public List<SceneryObject> getSceneryObjects() {
    return new ArrayList<>(sceneryObjects);
  }

  /**
   * Gets all scenery containers at this location.
   *
   * @return defensive copy of scenery containers list
   */
  @Nonnull
  public List<LocationContainer> getLocationContainers() {
    return new ArrayList<>(locationContainers);
  }

  @Nonnull
  public Optional<SceneryObject> findSceneryObject(@Nullable final String objectName) {
    if (objectName == null) {
      return Optional.empty();
    }

    return sceneryObjects.stream()
        .filter(sceneryObject -> sceneryObject.matches(objectName))
        .findFirst();
  }

  /**
   * Finds scenery objects that support a given interaction type.
   *
   * @param interactionType the interaction type to search for
   * @return a list of scenery objects that support the interaction type
   */
  @Nonnull
  public List<SceneryObject> findSceneryObjectsByInteraction(@Nonnull final InteractionType interactionType) {
    return sceneryObjects.stream()
        .filter(sceneryObject -> sceneryObject.getResponse(interactionType).isPresent())
        .toList();
  }

  @Nonnull
  public String getLongDescription() {
    return longDescription;
  }

  @Nonnull
  public String getShortDescription() {
    return shortDescription;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public boolean isVisited() {
    return visited;
  }

  public void setVisited(final boolean visited) {
    this.visited = visited;
  }

  /**
   * Records that an item is contained in a scenery container.
   *
   * @param item the item to track
   * @param container the scenery container holding the item
   */
  public void setItemContainer(@Nonnull final Item item, @Nonnull final LocationContainer container) {
    itemContainment.put(item, container);
  }

  /**
   * Checks if an item is in a scenery container.
   *
   * @param item the item to check
   * @return true if the item is in a scenery container, false otherwise
   */
  public boolean isItemInContainer(@Nonnull final Item item) {
    return itemContainment.containsKey(item);
  }

  /**
   * Gets the scenery container that contains an item, or null if not contained.
   *
   * @param item the item to check
   * @return the scenery container holding the item, or null if not in a container
   */
  @Nullable
  public LocationContainer getContainerForItem(@Nonnull final Item item) {
    return itemContainment.get(item);
  }

  /**
   * Removes item from container tracking (when item is taken).
   *
   * @param item the item to remove from tracking
   */
  public void removeItemFromContainer(@Nonnull final Item item) {
    itemContainment.remove(item);
  }

  /**
   * Adds a hidden item to this location.
   * Hidden items are not visible to the player until revealed.
   * The revealed location description is shown after the item is revealed
   * but before the player takes it.
   *
   * @param item the item to hide at this location
   * @param revealedLocationDescription the description shown after the item is revealed
   */
  public void addHiddenItem(@Nonnull final Item item, @Nonnull final String revealedLocationDescription) {
    hiddenItems.add(item);
    revealedLocationDescriptions.put(item, revealedLocationDescription);
  }

  /**
   * Reveals a hidden item, making it visible and takeable.
   * The item is moved from the hidden set to the visible items list.
   * Its revealed location description is preserved until the item is taken.
   *
   * @param item the item to reveal
   * @return true if the item was hidden and is now revealed, false if not hidden
   */
  public boolean revealItem(@Nonnull final Item item) {
    if (hiddenItems.remove(item)) {
      items.add(item);
      return true;
    }
    return false;
  }

  /**
   * Reveals a hidden item by name (case-insensitive).
   *
   * @param itemName the name of the hidden item to reveal
   * @return true if a matching hidden item was found and revealed, false otherwise
   */
  public boolean revealHiddenItemByName(@Nonnull final String itemName) {
    return getHiddenItemByName(itemName)
        .map(this::revealItem)
        .orElse(false);
  }

  /**
   * Checks if an item is hidden at this location.
   *
   * @param item the item to check
   * @return true if the item is hidden, false otherwise
   */
  public boolean isItemHidden(@Nonnull final Item item) {
    return hiddenItems.contains(item);
  }

  /**
   * Checks if an item is hidden at this location by name (case-insensitive, supports aliases).
   *
   * @param itemName the name to search for
   * @return true if a matching hidden item exists, false otherwise
   */
  public boolean isItemHiddenByName(@Nonnull final String itemName) {
    return getHiddenItemByName(itemName).isPresent();
  }

  /**
   * Finds a hidden item by name (case-insensitive, supports aliases).
   *
   * @param itemName the name to search for
   * @return the matching hidden item, or empty if not found
   */
  @Nonnull
  public Optional<Item> getHiddenItemByName(@Nonnull final String itemName) {
    return hiddenItems.stream()
        .filter(item -> item.matchesName(itemName))
        .findFirst();
  }

  /**
   * Gets all hidden items at this location.
   *
   * @return defensive copy of hidden items set
   */
  @Nonnull
  public Set<Item> getHiddenItems() {
    return new LinkedHashSet<>(hiddenItems);
  }

  /**
   * Removes all hidden items and their revealed descriptions.
   * Used during game reset to restore initial state.
   */
  public void clearHiddenItems() {
    hiddenItems.clear();
    revealedLocationDescriptions.clear();
  }

  /**
   * Gets the revealed location description for an item, if one exists.
   * This description is used instead of the item's default location description
   * when the item was revealed from a hidden state and has not yet been taken.
   *
   * @param item the item to check
   * @return the revealed description, or null if no override exists
   */
  @Nullable
  public String getRevealedLocationDescription(@Nonnull final Item item) {
    return revealedLocationDescriptions.get(item);
  }

  @Override
  public String toString() {
    return name;
  }
}
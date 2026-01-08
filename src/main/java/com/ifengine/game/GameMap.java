package com.ifengine.game;

import com.ifengine.Direction;
import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.content.GameContent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Standard GameMapInterface implementation for building game worlds.
 * <p>
 * GameMap can be built in two ways:
 *
 * <h2>1. Builder-style API (recommended for simple games)</h2>
 * <pre>
 * GameMap map = new GameMap();
 * map.addLocation(new Location("cottage", "A cozy cottage...", "In a cottage."));
 * map.addLocation(new Location("forest", "A dark forest...", "In the forest."));
 * map.connect("cottage", Direction.NORTH, "forest");
 * map.setStartingLocation("cottage");
 *
 * GameEngine engine = new GameEngine(map);
 * </pre>
 *
 * <h2>2. GameContent interface (for complex games)</h2>
 * <pre>
 * GameContent content = new MyAdventureContent();
 * GameMap map = new GameMap(content);
 * GameEngine engine = new GameEngine(map, content.getResponseProvider());
 * </pre>
 */
public class GameMap implements GameMapInterface {

  private GameContent gameContent;
  private final Map<String, Location> locations = new HashMap<>();
  private final Map<String, Item> items = new HashMap<>();
  private String startingLocationKey;

  // For reset support: track initial item placements
  private final Map<String, String> initialItemPlacements = new HashMap<>();

  /**
   * Creates an empty GameMap for builder-style construction.
   * <p>
   * Use {@link #addLocation(Location)}, {@link #addItem(Item)},
   * {@link #connect(String, Direction, String)}, and {@link #setStartingLocation(String)}
   * to build the game world.
   */
  public GameMap() {
    // Empty map for builder-style construction
  }

  /**
   * Creates a GameMap from the given game content.
   * <p>
   * Initializes the map by:
   * <ol>
   *   <li>Getting locations and items from game content</li>
   *   <li>Setting up connections between locations</li>
   *   <li>Placing items in their starting locations</li>
   *   <li>Adding scenery objects</li>
   * </ol>
   *
   * @param gameContent the game content to build from
   */
  public GameMap(@Nonnull final GameContent gameContent) {
    Objects.requireNonNull(gameContent, "game content cannot be null");
    initializeFromContent(gameContent);
  }

  /**
   * Initializes the map from a GameContent instance.
   *
   * @param gameContent the game content to initialize from
   */
  private void initializeFromContent(@Nonnull final GameContent gameContent) {
    this.gameContent = gameContent;
    this.locations.clear();
    this.locations.putAll(gameContent.getLocations());
    this.items.clear();
    this.items.putAll(gameContent.getItems());
    this.startingLocationKey = gameContent.getStartingLocationKey();

    // Initialize the game world
    gameContent.setupConnections();
    gameContent.placeItems();
    gameContent.addScenery();
  }

  /**
   * Adds a location to the game map.
   * <p>
   * The location's name (from {@link Location#getName()}) is used as its key.
   *
   * @param location the location to add
   * @return this GameMap for method chaining
   */
  @Nonnull
  public GameMap addLocation(@Nonnull final Location location) {
    Objects.requireNonNull(location, "location cannot be null");
    locations.put(location.getName(), location);
    return this;
  }

  /**
   * Adds an item to the game map.
   * <p>
   * The item's name (from {@link Item#getName()}) is used as its key.
   * Note: This registers the item but does not place it in a location.
   * Use {@link #placeItem(String, String)} to place items.
   *
   * @param item the item to add
   * @return this GameMap for method chaining
   */
  @Nonnull
  public GameMap addItem(@Nonnull final Item item) {
    Objects.requireNonNull(item, "item cannot be null");
    items.put(item.getName(), item);
    return this;
  }

  /**
   * Places an item in a location.
   * <p>
   * The item must have been added via {@link #addItem(Item)} first.
   *
   * @param itemKey the key/name of the item to place
   * @param locationKey the key/name of the location to place it in
   * @return this GameMap for method chaining
   * @throws IllegalArgumentException if the item or location is not found
   */
  @Nonnull
  public GameMap placeItem(@Nonnull final String itemKey, @Nonnull final String locationKey) {
    final Item item = items.get(itemKey);
    if (item == null) {
      throw new IllegalArgumentException("Item not found: " + itemKey);
    }
    final Location location = locations.get(locationKey);
    if (location == null) {
      throw new IllegalArgumentException("Location not found: " + locationKey);
    }
    location.addItem(item);
    initialItemPlacements.put(itemKey, locationKey);
    return this;
  }

  /**
   * Creates a bidirectional connection between two locations.
   * <p>
   * For example, {@code connect("cottage", Direction.NORTH, "forest")} creates:
   * <ul>
   *   <li>cottage -> NORTH -> forest</li>
   *   <li>forest -> SOUTH -> cottage</li>
   * </ul>
   *
   * @param fromLocationKey the key of the first location
   * @param direction the direction from the first location to the second
   * @param toLocationKey the key of the second location
   * @return this GameMap for method chaining
   * @throws IllegalArgumentException if either location is not found
   */
  @Nonnull
  public GameMap connect(@Nonnull final String fromLocationKey,
                         @Nonnull final Direction direction,
                         @Nonnull final String toLocationKey) {
    final Location from = locations.get(fromLocationKey);
    final Location to = locations.get(toLocationKey);

    if (from == null) {
      throw new IllegalArgumentException("Location not found: " + fromLocationKey);
    }
    if (to == null) {
      throw new IllegalArgumentException("Location not found: " + toLocationKey);
    }

    from.addConnection(direction, to);
    to.addConnection(getOppositeDirection(direction), from);
    return this;
  }

  /**
   * Creates a one-way connection between two locations.
   * <p>
   * Unlike {@link #connect(String, Direction, String)}, this does not create
   * a connection in the opposite direction.
   *
   * @param fromLocationKey the key of the source location
   * @param direction the direction of travel
   * @param toLocationKey the key of the destination location
   * @return this GameMap for method chaining
   * @throws IllegalArgumentException if either location is not found
   */
  @Nonnull
  public GameMap connectOneWay(@Nonnull final String fromLocationKey,
                               @Nonnull final Direction direction,
                               @Nonnull final String toLocationKey) {
    final Location from = locations.get(fromLocationKey);
    final Location to = locations.get(toLocationKey);

    if (from == null) {
      throw new IllegalArgumentException("Location not found: " + fromLocationKey);
    }
    if (to == null) {
      throw new IllegalArgumentException("Location not found: " + toLocationKey);
    }

    from.addConnection(direction, to);
    return this;
  }

  /**
   * Sets the starting location for new players.
   *
   * @param locationKey the key of the starting location
   * @return this GameMap for method chaining
   * @throws IllegalArgumentException if the location is not found
   */
  @Nonnull
  public GameMap setStartingLocation(@Nonnull final String locationKey) {
    if (!locations.containsKey(locationKey)) {
      throw new IllegalArgumentException("Location not found: " + locationKey);
    }
    this.startingLocationKey = locationKey;
    return this;
  }

  @Nonnull
  private Direction getOppositeDirection(@Nonnull final Direction direction) {
    return switch (direction) {
      case NORTH -> Direction.SOUTH;
      case SOUTH -> Direction.NORTH;
      case EAST -> Direction.WEST;
      case WEST -> Direction.EAST;
      case UP -> Direction.DOWN;
      case DOWN -> Direction.UP;
      case NORTHEAST -> Direction.SOUTHWEST;
      case NORTHWEST -> Direction.SOUTHEAST;
      case SOUTHEAST -> Direction.NORTHWEST;
      case SOUTHWEST -> Direction.NORTHEAST;
      case IN -> Direction.OUT;
      case OUT -> Direction.IN;
    };
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
    if (startingLocationKey == null) {
      throw new IllegalStateException(
          "Starting location not set. Call setStartingLocation() before using the map.");
    }
    final Location startLocation = locations.get(startingLocationKey);
    if (startLocation == null) {
      throw new IllegalStateException(
          "Starting location '" + startingLocationKey + "' not found in locations.");
    }
    return startLocation;
  }

  @Override
  public void resetMap() {
    if (gameContent != null) {
      // GameContent-based map: create fresh copy
      final GameContent freshContent = gameContent.createFreshCopy();
      initializeFromContent(freshContent);
    } else {
      // Builder-based map: restore initial item placements
      // First, remove all items from all locations
      for (final Location location : locations.values()) {
        location.getItems().forEach(location::removeItem);
      }

      // Restore items to their initial locations
      for (final Map.Entry<String, String> entry : initialItemPlacements.entrySet()) {
        final Item item = items.get(entry.getKey());
        final Location location = locations.get(entry.getValue());
        if (item != null && location != null) {
          location.addItem(item);
        }
      }
    }

    // Reset visited flags on all locations
    for (final Location location : locations.values()) {
      location.setVisited(false);
    }
  }

  /**
   * Gets the game content this map was built from, if any.
   *
   * @return the game content, or null if built using builder API
   */
  @Nullable
  public GameContent getGameContent() {
    return gameContent;
  }
}
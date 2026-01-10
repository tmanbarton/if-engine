package com.ifengine.game;

import com.ifengine.Direction;
import com.ifengine.Item;
import com.ifengine.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * GameMapInterface implementation for building game worlds.
 * <p>
 * Use the builder-style API to construct your game:
 * <pre>
 * GameMap map = new GameMap()
 *     .addLocation(new Location("cottage", "A cozy cottage...", "In a cottage."))
 *     .addLocation(new Location("forest", "A dark forest...", "In the forest."))
 *     .addItem(new Item("key", "a key", "A key lies here.", "A rusty key."))
 *     .connect("cottage", Direction.NORTH, "forest")
 *     .placeItem("key", "cottage")
 *     .setStartingLocation("cottage");
 *
 * GameEngine engine = new GameEngine(map);
 * </pre>
 */
public class GameMap implements GameMapInterface {

  private final Map<String, Location> locations = new HashMap<>();
  private final Map<String, Item> items = new HashMap<>();
  private String startingLocationKey;

  // For reset support: track initial item placements
  private final Map<String, String> initialItemPlacements = new HashMap<>();

  // Intro configuration
  private boolean skipIntro = false;
  private String introQuestion;
  private IntroHandler introHandler;
  private String customYesResponse;
  private String customNoResponse;

  /**
   * Creates an empty GameMap.
   * <p>
   * Use {@link #addLocation(Location)}, {@link #addItem(Item)},
   * {@link #connect(String, Direction, String)}, and {@link #setStartingLocation(String)}
   * to build the game world.
   */
  public GameMap() {
    // Empty map for builder-style construction
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
    // Remove all items from all locations
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

    // Reset visited flags on all locations
    for (final Location location : locations.values()) {
      location.setVisited(false);
    }
  }

  /**
   * Configures the game to skip the intro and start directly in PLAYING state.
   * <p>
   * When enabled, new players will not be asked the "have you played before?" question
   * and will start immediately in gameplay.
   *
   * @return this GameMap for method chaining
   */
  @Nonnull
  public GameMap skipIntro() {
    this.skipIntro = true;
    return this;
  }

  /**
   * Configures a custom intro message with custom yes/no responses.
   * <p>
   * The intro message is displayed when a new player joins. When the player
   * answers "yes" (or variants like "y", "yeah", "sure", "ok"), the yesResponse
   * is shown and the game transitions to PLAYING state. When they answer "no"
   * (or variants), the noResponse is shown and they stay in the intro state.
   * <p>
   * Use {@link #withIntro(String, IntroHandler)} for full control over response handling.
   *
   * @param message the intro message/question to display
   * @param yesResponse the message to show when player answers yes
   * @param noResponse the message to show when player answers no
   * @return this GameMap for method chaining
   */
  @Nonnull
  public GameMap withIntro(@Nonnull final String introQuestion,
                           @Nonnull final String yesResponse,
                           @Nonnull final String noResponse) {
    Objects.requireNonNull(introQuestion, "message cannot be null");
    Objects.requireNonNull(yesResponse, "yesResponse cannot be null");
    Objects.requireNonNull(noResponse, "noResponse cannot be null");
    this.introQuestion = introQuestion;
    this.introHandler = null;
    this.customYesResponse = yesResponse;
    this.customNoResponse = noResponse;
    return this;
  }

  /**
   * Configures a custom intro message and handler.
   * <p>
   * The intro message is displayed when a new player joins. The handler is invoked
   * with the player's response to determine what message to show and whether to
   * transition to the PLAYING state.
   *
   * @param message the intro message to display
   * @param handler the handler for processing the player's response
   * @return this GameMap for method chaining
   */
  @Nonnull
  public GameMap withIntro(@Nonnull final String message, @Nonnull final IntroHandler handler) {
    Objects.requireNonNull(message, "message cannot be null");
    Objects.requireNonNull(handler, "handler cannot be null");
    this.introQuestion = message;
    this.introHandler = handler;
    this.customYesResponse = null;
    this.customNoResponse = null;
    return this;
  }

  /**
   * Returns whether the intro should be skipped.
   *
   * @return true if the intro should be skipped
   */
  public boolean shouldSkipIntro() {
    return skipIntro;
  }

  /**
   * Returns the custom intro message, if configured.
   *
   * @return the intro message, or null if not configured
   */
  @Nullable
  public String getIntroQuestion() {
    return introQuestion;
  }

  /**
   * Returns the custom intro handler, if configured.
   *
   * @return the intro handler, or null if not configured
   */
  @Nullable
  public IntroHandler getIntroHandler() {
    return introHandler;
  }

  /**
   * Returns the custom yes response, if configured.
   *
   * @return the custom yes response, or null if not configured
   */
  @Nullable
  public String getCustomYesResponse() {
    return customYesResponse;
  }

  /**
   * Returns the custom no response, if configured.
   *
   * @return the custom no response, or null if not configured
   */
  @Nullable
  public String getCustomNoResponse() {
    return customNoResponse;
  }

  /**
   * Returns whether a custom intro handler is configured.
   *
   * @return true if a custom intro handler is set
   */
  public boolean hasCustomIntroHandler() {
    return introQuestion != null && introHandler != null;
  }

  /**
   * Returns whether a custom intro with simple yes response is configured.
   *
   * @return true if a custom intro message and yes response are set (without custom handler)
   */
  public boolean hasCustomIntroMessage() {
    return introQuestion != null && customYesResponse != null;
  }

  /**
   * Returns whether any custom intro is configured.
   *
   * @return true if any custom intro configuration is set
   */
  public boolean hasCustomIntro() {
    return introQuestion != null && (introHandler != null || customYesResponse != null);
  }
}
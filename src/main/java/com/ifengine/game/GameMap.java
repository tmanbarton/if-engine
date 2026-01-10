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
import java.util.function.Consumer;

/**
 * GameMapInterface implementation for building game worlds.
 * <p>
 * Use the Builder to construct your game:
 * <pre>
 * GameMap map = new GameMap.Builder()
 *     .addLocation(new Location("cottage", "A cozy cottage...", "In a cottage."))
 *     .addLocation(new Location("forest", "A dark forest...", "In the forest."))
 *     .addItem(new Item("key", "a key", "A key lies here.", "A rusty key."))
 *     .connect("cottage", Direction.NORTH, "forest")
 *     .placeItem("key", "cottage")
 *     .setStartingLocation("cottage")
 *     .build();
 *
 * GameEngine engine = new GameEngine(map);
 * </pre>
 */
public class GameMap implements GameMapInterface {

  private final Map<String, Location> locations;
  private final Map<String, Item> items;
  private final String startingLocationKey;
  private final Map<String, String> initialItemPlacements;

  // Intro configuration
  private final boolean skipIntro;
  private final IntroHandler introHandler;
  private final String customYesResponse;
  private final String customNoResponse;

  // Hint configuration
  private final HintConfiguration hintConfiguration;

  /**
   * Creates a GameMap from a Builder.
   *
   * @param builder the builder containing the map configuration
   */
  GameMap(@Nonnull final Builder builder) {
    this.locations = new HashMap<>(builder.locations);
    this.items = new HashMap<>(builder.items);
    this.startingLocationKey = builder.startingLocationKey;
    this.initialItemPlacements = new HashMap<>(builder.initialItemPlacements);
    this.skipIntro = builder.skipIntro;
    this.introHandler = builder.introHandler;
    this.customYesResponse = builder.customYesResponse;
    this.customNoResponse = builder.customNoResponse;
    this.hintConfiguration = builder.hintConfiguration;
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
   * Returns whether the intro should be skipped.
   *
   * @return true if the intro should be skipped
   */
  public boolean shouldSkipIntro() {
    return skipIntro;
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
    return introHandler != null;
  }

  /**
   * Returns whether a custom intro with simple yes/no responses is configured.
   *
   * @return true if custom yes/no responses are set (without custom handler)
   */
  public boolean hasCustomIntroMessage() {
    return customYesResponse != null && introHandler == null;
  }

  /**
   * Returns whether any custom intro is configured.
   *
   * @return true if any custom intro configuration is set
   */
  public boolean hasCustomIntro() {
    return introHandler != null || customYesResponse != null;
  }

  /**
   * Returns the hint configuration, if configured.
   *
   * @return the hint configuration, or null if not configured
   */
  @Nullable
  public HintConfiguration getHintConfiguration() {
    return hintConfiguration;
  }

  /**
   * Builder for constructing GameMap instances.
   * <p>
   * The builder validates that:
   * <ul>
   *   <li>At least one location has been added</li>
   *   <li>A starting location has been set</li>
   * </ul>
   */
  public static class Builder {

    private final Map<String, Location> locations = new HashMap<>();
    private final Map<String, Item> items = new HashMap<>();
    private String startingLocationKey;
    private final Map<String, String> initialItemPlacements = new HashMap<>();

    // Intro configuration
    private boolean skipIntro = false;
    private IntroHandler introHandler;
    private String customYesResponse;
    private String customNoResponse;

    // Hint configuration
    private HintConfiguration hintConfiguration;

    /**
     * Creates an empty Builder.
     */
    public Builder() {
      // Empty builder for fluent construction
    }

    /**
     * Adds a location to the game map.
     * <p>
     * The location's name (from {@link Location#getName()}) is used as its key.
     *
     * @param location the location to add
     * @return this Builder for method chaining
     */
    @Nonnull
    public Builder addLocation(@Nonnull final Location location) {
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
     * @return this Builder for method chaining
     */
    @Nonnull
    public Builder addItem(@Nonnull final Item item) {
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
     * @return this Builder for method chaining
     * @throws IllegalArgumentException if the item or location is not found
     */
    @Nonnull
    public Builder placeItem(@Nonnull final String itemKey, @Nonnull final String locationKey) {
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
     * @return this Builder for method chaining
     * @throws IllegalArgumentException if either location is not found
     */
    @Nonnull
    public Builder connect(@Nonnull final String fromLocationKey,
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
     * @return this Builder for method chaining
     * @throws IllegalArgumentException if either location is not found
     */
    @Nonnull
    public Builder connectOneWay(@Nonnull final String fromLocationKey,
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
     * @return this Builder for method chaining
     * @throws IllegalArgumentException if the location is not found
     */
    @Nonnull
    public Builder setStartingLocation(@Nonnull final String locationKey) {
      if (!locations.containsKey(locationKey)) {
        throw new IllegalArgumentException("Location not found: " + locationKey);
      }
      this.startingLocationKey = locationKey;
      return this;
    }

    /**
     * Configures the game to skip the intro and start directly in PLAYING state.
     * <p>
     * When enabled, new players will not be asked the "have you played before?" question
     * and will start immediately in gameplay.
     *
     * @return this Builder for method chaining
     */
    @Nonnull
    public Builder skipIntro() {
      this.skipIntro = true;
      return this;
    }

    /**
     * Configures custom yes/no responses for the intro.
     * <p>
     * When the player answers "yes" (or variants like "y", "yeah", "sure", "ok"),
     * the yesResponse is shown and the game transitions to PLAYING state.
     * When they answer "no" (or variants), the noResponse is shown and they
     * stay in the intro state.
     * <p>
     * The intro question itself should be displayed in the HTML/frontend layer,
     * not stored in the game map.
     * <p>
     * Use {@link #withIntroHandler(IntroHandler)} for full control over response handling.
     *
     * @param yesResponse the message to show when player answers yes
     * @param noResponse the message to show when player answers no
     * @return this Builder for method chaining
     */
    @Nonnull
    public Builder withIntroResponses(@Nonnull final String yesResponse,
                                      @Nonnull final String noResponse) {
      Objects.requireNonNull(yesResponse, "yesResponse cannot be null");
      Objects.requireNonNull(noResponse, "noResponse cannot be null");
      this.introHandler = null;
      this.customYesResponse = yesResponse;
      this.customNoResponse = noResponse;
      return this;
    }

    /**
     * Configures a custom intro handler.
     * <p>
     * The handler is invoked with the player's response to determine what message
     * to show and whether to transition to the PLAYING state.
     * <p>
     * The intro question itself should be displayed in the HTML/frontend layer,
     * not stored in the game map.
     *
     * @param handler the handler for processing the player's response
     * @return this Builder for method chaining
     */
    @Nonnull
    public Builder withIntroHandler(@Nonnull final IntroHandler handler) {
      Objects.requireNonNull(handler, "handler cannot be null");
      this.introHandler = handler;
      this.customYesResponse = null;
      this.customNoResponse = null;
      return this;
    }

    /**
     * Configures the hint system with progressive hints for puzzle phases.
     * <p>
     * Example usage:
     * <pre>
     * .withHints(hints -> hints
     *     .addPhase("FIND_KEY",
     *         "Something important might be nearby...",
     *         "Check around the old tree.",
     *         "Take the brass key from the Lightning Tree.")
     *     .addPhase("UNLOCK_SHED",
     *         "That key must be for something...",
     *         "Try using the key on the shed's lock.",
     *         "Type 'unlock shed' to use your key.")
     *     .determiner((player, gameMap) -> {
     *         if (player.hasItem("key")) {
     *             return "UNLOCK_SHED";
     *         }
     *         return "FIND_KEY";
     *     })
     * )
     * </pre>
     *
     * @param configurer a consumer that configures the HintConfigurationBuilder
     * @return this Builder for method chaining
     */
    @Nonnull
    public Builder withHints(@Nonnull final Consumer<HintConfigurationBuilder> configurer) {
      Objects.requireNonNull(configurer, "configurer cannot be null");
      final HintConfigurationBuilder hintBuilder = new HintConfigurationBuilder();
      configurer.accept(hintBuilder);
      this.hintConfiguration = hintBuilder.build();
      return this;
    }

    /**
     * Builds the GameMap after validating the configuration.
     *
     * @return a new GameMap instance
     * @throws IllegalStateException if no locations have been added
     * @throws IllegalStateException if starting location has not been set
     */
    @Nonnull
    public GameMap build() {
      if (locations.isEmpty()) {
        throw new IllegalStateException("GameMap must have at least one location");
      }
      if (startingLocationKey == null) {
        throw new IllegalStateException(
            "Starting location must be set. Call setStartingLocation() before build()");
      }
      return new GameMap(this);
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
  }
}

package com.ifengine.game;

import com.ifengine.Direction;
import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.command.CustomCommandHandler;
import com.ifengine.command.CustomCommandRegistration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
 *     .connect("cottage", Direction.NORTH, "forest")
 *     .placeItem(new Item("key", "a key", "A key lies here.", "A rusty key."), "cottage")
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
  private final String customIntroMessage;

  // Hint configuration
  private final HintConfiguration hintConfiguration;

  // Custom commands
  private final List<CustomCommandRegistration> customCommands;

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
    this.customIntroMessage = builder.customIntroMessage;
    this.hintConfiguration = builder.hintConfiguration;
    this.customCommands = List.copyOf(builder.customCommands);
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
   * Returns whether custom yes/no intro responses are configured.
   *
   * @return true if custom yes/no responses are set (without custom handler)
   */
  public boolean hasCustomIntroResponses() {
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
   * Returns whether a custom intro message is configured.
   *
   * @return true if a custom intro message is set
   */
  public boolean hasCustomIntroMessage() {
    return customIntroMessage != null;
  }

  /**
   * Returns the custom intro message, if configured.
   *
   * @return the custom intro message, or null if not configured
   */
  @Nullable
  public String getCustomIntroMessage() {
    return customIntroMessage;
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
   * Returns the list of custom command registrations.
   *
   * @return immutable list of custom command registrations
   */
  @Nonnull
  public List<CustomCommandRegistration> getCustomCommands() {
    return customCommands;
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
    private String customIntroMessage;

    // Hint configuration
    private HintConfiguration hintConfiguration;

    // Custom commands
    private final List<CustomCommandRegistration> customCommands = new ArrayList<>();

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
     * Adds an item and places it in a location.
     * <p>
     * The item's name (from {@link Item#getName()}) is used as its key.
     *
     * @param item the item to add and place
     * @param locationKey the key/name of the location to place it in
     * @return this Builder for method chaining
     * @throws IllegalArgumentException if the location is not found
     */
    @Nonnull
    public Builder placeItem(@Nonnull final Item item, @Nonnull final String locationKey) {
      Objects.requireNonNull(item, "item cannot be null");
      final Location location = locations.get(locationKey);
      if (location == null) {
        throw new IllegalArgumentException("Location not found: " + locationKey);
      }
      items.put(item.getName(), item);
      location.addItem(item);
      initialItemPlacements.put(item.getName(), locationKey);
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
     * Configures custom yes/no responses for the intro question.
     * <p>
     * This is designed for yes/no questions where both answers start the game,
     * such as "Have you played interactive fiction before?" or "Ready to begin?".
     * <p>
     * Both "yes" and "no" answers transition the game to PLAYING state, allowing
     * gameplay to begin. The difference is only in the response message shown.
     * <p>
     * Accepted yes variants: "yes", "y", "yeah", "yep", "sure"
     * Accepted no variants: "no", "n", "nah", "nope", "no thanks"
     * <p>
     * The intro question itself should be displayed in the HTML/frontend layer,
     * not stored in the game map.
     * <p>
     * Use {@link #withIntroHandler(IntroHandler)} for full control over response
     * handling, including keeping the player in the intro state on certain answers.
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
     * Configures an intro message shown before the first location description.
     * <p>
     * This message is displayed after the yes/no response (if any) and before
     * showing the player's starting location. Use this to set the scene or
     * provide story context when the game begins.
     * <p>
     * Can be used alone (with default yes/no handling) or combined with
     * {@link #withIntroResponses(String, String)} for custom yes/no messages.
     *
     * @param introMessage the message to show before the location description
     * @return this Builder for method chaining
     */
    @Nonnull
    public Builder withIntroMessage(@Nonnull final String introMessage) {
      Objects.requireNonNull(introMessage, "introMessage cannot be null");
      this.customIntroMessage = introMessage;
      return this;
    }

    /**
     * Configures the hint system with progressive hints for puzzle phases.
     * <p>
     * Example usage:
     * <pre>
     * .withHints(hints -> hints
     *     .addPhase("find-key",
     *         "Something important might be nearby...",
     *         "Check around the old tree.",
     *         "Take the brass key from the table.")
     *     .addPhase("unlock-shed",
     *         "That key must be for something...",
     *         "Try using the key on the shed's lock.",
     *         "Type 'unlock shed' to use your key.")
     *     .determiner((player, gameMap) -> {
     *         if (player.hasItem("key")) {
     *             return "unlock-shed";
     *         }
     *         return "find-key";
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
     * Registers a custom command with no aliases.
     * <p>
     * <b>Delegation:</b> Return {@code null} from the handler to delegate to the built-in
     * handler for this verb. This allows custom handlers to handle specific cases while
     * falling back to default behavior for everything else.
     * <p>
     * Example usage:
     * <pre>
     * .withCommand("xyzzy", (player, cmd, ctx) -> "Nothing happens.")
     *
     * // Partial override - handle special case, delegate otherwise
     * .withCommand("eat", (player, cmd, ctx) -> {
     *     if (cmd.getFirstDirectObject().equals("magic apple")) {
     *         return "You feel a surge of power!";
     *     }
     *     return null; // Delegate to default eat behavior
     * })
     * </pre>
     *
     * @param verb the verb that triggers this command
     * @param handler the handler for the command
     * @return this Builder for method chaining
     */
    @Nonnull
    public Builder withCommand(@Nonnull final String verb,
                               @Nonnull final CustomCommandHandler handler) {
      return withCommand(verb, List.of(), handler);
    }

    /**
     * Registers a custom command with aliases.
     * <p>
     * <b>Delegation:</b> Return {@code null} from the handler to delegate to the built-in
     * handler for this verb. This allows custom handlers to handle specific cases while
     * falling back to default behavior for everything else.
     * <p>
     * Example usage:
     * <pre>
     * .withCommand("search", List.of("find", "look for"), (player, cmd, ctx) -> {
     *     String target = cmd.getFirstDirectObject();
     *     if (target.isEmpty()) {
     *         return "Search what?";
     *     }
     *     return "You search the " + target + " but find nothing.";
     * })
     * </pre>
     *
     * @param verb the primary verb that triggers this command
     * @param aliases additional verbs that trigger this command
     * @param handler the handler for the command
     * @return this Builder for method chaining
     */
    @Nonnull
    public Builder withCommand(@Nonnull final String verb,
                               @Nonnull final List<String> aliases,
                               @Nonnull final CustomCommandHandler handler) {
      Objects.requireNonNull(verb, "verb cannot be null");
      Objects.requireNonNull(aliases, "aliases cannot be null");
      Objects.requireNonNull(handler, "handler cannot be null");
      customCommands.add(new CustomCommandRegistration(verb, aliases, handler));
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

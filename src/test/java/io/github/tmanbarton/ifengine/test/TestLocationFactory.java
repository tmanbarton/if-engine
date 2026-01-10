package io.github.tmanbarton.ifengine.test;

import io.github.tmanbarton.ifengine.Direction;
import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.SceneryContainer;
import io.github.tmanbarton.ifengine.SceneryObject;

import javax.annotation.Nonnull;

/**
 * Factory for creating test locations without production dependencies.
 * <p>
 * Provides methods to create various types of locations for testing:
 * <ul>
 *   <li>Simple locations with auto-generated descriptions</li>
 *   <li>Named locations with custom descriptions</li>
 *   <li>Pre-built test locations (forest, cave)</li>
 *   <li>Connected location pairs</li>
 * </ul>
 */
public final class TestLocationFactory {

  private TestLocationFactory() {
    // Private constructor - static factory only
  }

  /**
   * Creates a simple test location with auto-generated descriptions.
   *
   * @param name the location name
   * @return a new test location
   */
  @Nonnull
  public static Location createSimpleLocation(@Nonnull final String name) {
    return new Location(
        name,
        "You are in the " + name + ". This is a test location for unit testing.",
        "The " + name + "."
    );
  }

  /**
   * Creates a test location with custom descriptions.
   *
   * @param name the location name
   * @param longDescription the detailed description
   * @param shortDescription the brief description
   * @return a new test location
   */
  @Nonnull
  public static Location createLocation(@Nonnull final String name,
                                        @Nonnull final String longDescription,
                                        @Nonnull final String shortDescription) {
    return new Location(name, longDescription, shortDescription);
  }

  /**
   * Creates a default test location with preset descriptions.
   *
   * @return a new default test location
   */
  @Nonnull
  public static Location createDefaultLocation() {
    return new Location(
        "test-location",
        "You are in a test location. It's a plain room suitable for testing.",
        "A test location."
    );
  }

  /**
   * Creates a test forest location.
   *
   * @return a new test forest location
   */
  @Nonnull
  public static Location createTestForest() {
    return new Location(
        "forest",
        "You are in a dense test forest. Trees surround you on all sides.",
        "A test forest."
    );
  }

  /**
   * Creates a test cave location.
   *
   * @return a new test cave location
   */
  @Nonnull
  public static Location createTestCave() {
    return new Location(
        "cave",
        "You are in a dark test cave. The walls are damp and cold.",
        "A test cave."
    );
  }

  /**
   * Creates two connected locations with bidirectional connections.
   *
   * @param name1 the first location name
   * @param name2 the second location name
   * @param direction the direction from location1 to location2
   * @return an array containing [location1, location2]
   */
  @Nonnull
  public static Location[] createConnectedLocations(@Nonnull final String name1,
                                                    @Nonnull final String name2,
                                                    @Nonnull final Direction direction) {
    final Location location1 = createSimpleLocation(name1);
    final Location location2 = createSimpleLocation(name2);

    // Create bidirectional connection
    location1.addConnection(direction, location2);
    location2.addConnection(getOppositeDirection(direction), location1);

    return new Location[] { location1, location2 };
  }

  /**
   * Adds a one-way connection from one location to another.
   *
   * @param from the source location
   * @param direction the direction of the connection
   * @param to the destination location
   */
  public static void addConnection(@Nonnull final Location from,
                                   @Nonnull final Direction direction,
                                   @Nonnull final Location to) {
    from.addConnection(direction, to);
  }

  /**
   * Adds a bidirectional connection between two locations.
   *
   * @param location1 the first location
   * @param direction the direction from location1 to location2
   * @param location2 the second location
   */
  public static void addBidirectionalConnection(@Nonnull final Location location1,
                                                @Nonnull final Direction direction,
                                                @Nonnull final Location location2) {
    location1.addConnection(direction, location2);
    location2.addConnection(getOppositeDirection(direction), location1);
  }

  /**
   * Creates a location with an item already present.
   *
   * @param name the location name
   * @param item the item to add
   * @return a new location containing the item
   */
  @Nonnull
  public static Location createLocationWithItem(@Nonnull final String name,
                                                @Nonnull final Item item) {
    final Location location = createSimpleLocation(name);
    location.addItem(item);
    return location;
  }

  /**
   * Adds an item to an existing location.
   *
   * @param location the location to add the item to
   * @param item the item to add
   */
  public static void addItemToLocation(@Nonnull final Location location,
                                       @Nonnull final Item item) {
    location.addItem(item);
  }

  /**
   * Adds a scenery object to an existing location.
   *
   * @param location the location to add scenery to
   * @param sceneryObject the scenery object to add
   */
  public static void addSceneryObject(@Nonnull final Location location,
                                      @Nonnull final SceneryObject sceneryObject) {
    location.addSceneryObject(sceneryObject);
  }

  /**
   * Adds a scenery container to an existing location.
   *
   * @param location the location to add the container to
   * @param container the scenery container to add
   */
  public static void addSceneryContainer(@Nonnull final Location location,
                                         @Nonnull final SceneryContainer container) {
    location.addSceneryContainer(container);
  }

  /**
   * Gets the opposite direction for bidirectional connections.
   *
   * @param direction the direction to get the opposite of
   * @return the opposite direction
   */
  @Nonnull
  public static Direction getOppositeDirection(@Nonnull final Direction direction) {
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
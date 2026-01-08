package com.ifengine.test;

import com.ifengine.Direction;
import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.SceneryContainer;
import com.ifengine.SceneryObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Fluent builder for creating TestGameMap instances with granular control.
 * <p>
 * TestGameMapBuilder supports immutable builder pattern - each with* method
 * returns a new builder instance, enabling safe method chaining.
 * <p>
 * Example usage:
 * <pre>
 * TestGameMap map = TestGameMapBuilder.singleLocation()
 *     .withItems("key", "rope")
 *     .withLocations("forest", "cave")
 *     .withConnection("minimal-location", Direction.NORTH, "forest")
 *     .build();
 * </pre>
 */
public final class TestGameMapBuilder {

  private static final String DEFAULT_STARTING_LOCATION = "minimal-location";

  private final Set<String> locationNames;
  private final Set<String> itemNames;
  private final List<ConnectionSpec> connections;
  private final List<SceneryContainerSpec> sceneryContainers;
  private final String startingLocationName;

  private TestGameMapBuilder(@Nonnull final Set<String> locationNames,
                             @Nonnull final Set<String> itemNames,
                             @Nonnull final List<ConnectionSpec> connections,
                             @Nonnull final List<SceneryContainerSpec> sceneryContainers,
                             @Nonnull final String startingLocationName) {
    this.locationNames = Set.copyOf(locationNames);
    this.itemNames = Set.copyOf(itemNames);
    this.connections = List.copyOf(connections);
    this.sceneryContainers = List.copyOf(sceneryContainers);
    this.startingLocationName = startingLocationName;
  }

  /**
   * Creates a builder with a single minimal location.
   *
   * @return a new builder with one location
   */
  @Nonnull
  public static TestGameMapBuilder singleLocation() {
    return new TestGameMapBuilder(
        Set.of(DEFAULT_STARTING_LOCATION),
        Set.of(),
        List.of(),
        List.of(),
        DEFAULT_STARTING_LOCATION
    );
  }

  /**
   * Creates a builder with a single named location.
   *
   * @param locationName the name for the single location
   * @return a new builder with the named location
   */
  @Nonnull
  public static TestGameMapBuilder singleNamedLocation(@Nonnull final String locationName) {
    return new TestGameMapBuilder(
        Set.of(locationName),
        Set.of(),
        List.of(),
        List.of(),
        locationName
    );
  }

  /**
   * Creates a builder with two connected locations and basic items.
   * Location1 is connected north to Location2.
   * Includes key and rope items.
   *
   * @return a new builder with two locations and items
   */
  @Nonnull
  public static TestGameMapBuilder twoLocations() {
    return new TestGameMapBuilder(
        Set.of("location1", "location2"),
        Set.of("key", "rope"),
        List.of(new ConnectionSpec("location1", Direction.NORTH, "location2")),
        List.of(),
        "location1"
    );
  }

  /**
   * Creates a builder with two connected locations but no items.
   * Location1 is connected north to Location2.
   *
   * @return a new builder with two empty locations
   */
  @Nonnull
  public static TestGameMapBuilder twoLocationsEmpty() {
    return new TestGameMapBuilder(
        Set.of("location1", "location2"),
        Set.of(),
        List.of(new ConnectionSpec("location1", Direction.NORTH, "location2")),
        List.of(),
        "location1"
    );
  }

  /**
   * Adds multiple locations to the builder.
   *
   * @param names the location names to add
   * @return a new builder with the additional locations
   */
  @Nonnull
  public TestGameMapBuilder withLocations(@Nonnull final String... names) {
    final Set<String> newLocations = new HashSet<>(this.locationNames);
    newLocations.addAll(Set.of(names));
    return new TestGameMapBuilder(newLocations, itemNames, connections, sceneryContainers, startingLocationName);
  }

  /**
   * Adds a single location to the builder.
   *
   * @param name the location name to add
   * @return a new builder with the additional location
   */
  @Nonnull
  public TestGameMapBuilder withLocation(@Nonnull final String name) {
    return withLocations(name);
  }

  /**
   * Adds multiple items to the builder.
   * Supported semantic names: "key", "rope", "gem"
   * Other names create simple generic items.
   *
   * @param names the item names to add
   * @return a new builder with the additional items
   */
  @Nonnull
  public TestGameMapBuilder withItems(@Nonnull final String... names) {
    final Set<String> newItems = new HashSet<>(this.itemNames);
    newItems.addAll(Set.of(names));
    return new TestGameMapBuilder(locationNames, newItems, connections, sceneryContainers, startingLocationName);
  }

  /**
   * Adds a single item to the builder.
   *
   * @param name the item name to add
   * @return a new builder with the additional item
   */
  @Nonnull
  public TestGameMapBuilder withItem(@Nonnull final String name) {
    return withItems(name);
  }

  /**
   * Adds a connection between two locations.
   * Connections are automatically made bidirectional during build.
   *
   * @param fromLocation the source location name
   * @param direction the direction of travel
   * @param toLocation the destination location name
   * @return a new builder with the additional connection
   */
  @Nonnull
  public TestGameMapBuilder withConnection(@Nonnull final String fromLocation,
                                           @Nonnull final Direction direction,
                                           @Nonnull final String toLocation) {
    final List<ConnectionSpec> newConnections = new ArrayList<>(this.connections);
    newConnections.add(new ConnectionSpec(fromLocation, direction, toLocation));
    return new TestGameMapBuilder(locationNames, itemNames, newConnections, sceneryContainers, startingLocationName);
  }

  /**
   * Adds a scenery container to the starting location.
   *
   * @param containerName the name of the container
   * @param allowedItemNames names of items that can be inserted
   * @return a new builder with the scenery container
   */
  @Nonnull
  public TestGameMapBuilder withSceneryContainer(@Nonnull final String containerName,
                                                 @Nonnull final String... allowedItemNames) {
    final List<SceneryContainerSpec> newContainers = new ArrayList<>(this.sceneryContainers);
    newContainers.add(new SceneryContainerSpec(containerName, Set.of(allowedItemNames)));
    return new TestGameMapBuilder(locationNames, itemNames, connections, newContainers, startingLocationName);
  }

  /**
   * Sets the starting location.
   *
   * @param locationName the name of the starting location
   * @return a new builder with the specified starting location
   */
  @Nonnull
  public TestGameMapBuilder withStartingLocation(@Nonnull final String locationName) {
    return new TestGameMapBuilder(locationNames, itemNames, connections, sceneryContainers, locationName);
  }

  /**
   * Builds the TestGameMap with all configured locations, items, and connections.
   *
   * @return a new TestGameMap
   */
  @Nonnull
  public TestGameMap build() {
    final TestGameMap map = new TestGameMap();

    // Create and add locations
    final Map<String, Location> locationMap = new HashMap<>();
    for (final String name : locationNames) {
      final Location location = TestLocationFactory.createSimpleLocation(name);
      locationMap.put(name, location);
      map.addLocation(location);
    }

    // Set starting location
    final Location startLocation = locationMap.get(startingLocationName);
    if (startLocation != null) {
      map.setStartingLocation(startLocation);
    }

    // Create connections (bidirectional)
    for (final ConnectionSpec conn : connections) {
      final Location from = locationMap.get(conn.fromLocation);
      final Location to = locationMap.get(conn.toLocation);
      if (from != null && to != null) {
        from.addConnection(conn.direction, to);
        to.addConnection(TestLocationFactory.getOppositeDirection(conn.direction), from);
      }
    }

    // Create and place items in starting location
    final Location itemLocation = startLocation != null ? startLocation : locationMap.values().iterator().next();
    for (final String itemName : itemNames) {
      final Item item = createItemByName(itemName);
      map.placeItem(item, itemLocation);
    }

    // Add scenery containers to starting location
    for (final SceneryContainerSpec spec : sceneryContainers) {
      final SceneryObject sceneryObject = SceneryObject.builder(spec.containerName).build();
      final SceneryContainer container = new SceneryContainer(sceneryObject, spec.allowedItems);
      if (startLocation != null) {
        startLocation.addSceneryContainer(container);
      }
    }

    return map;
  }

  /**
   * Creates an item by name using semantic item creation.
   * Known names get specific test items; others get generic items.
   */
  @Nonnull
  private Item createItemByName(@Nonnull final String name) {
    return switch (name.toLowerCase()) {
      case "key" -> TestItemFactory.createTestKey();
      case "rope" -> TestItemFactory.createTestRope();
      case "gem" -> TestItemFactory.createTestGem();
      default -> TestItemFactory.createSimpleItem(name);
    };
  }

  /**
   * Internal record for connection specifications.
   */
  private record ConnectionSpec(String fromLocation, Direction direction, String toLocation) {}

  /**
   * Internal record for scenery container specifications.
   */
  private record SceneryContainerSpec(String containerName, Set<String> allowedItems) {}
}
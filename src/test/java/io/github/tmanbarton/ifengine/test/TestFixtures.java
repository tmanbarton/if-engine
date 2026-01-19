package io.github.tmanbarton.ifengine.test;

import io.github.tmanbarton.ifengine.Direction;
import io.github.tmanbarton.ifengine.InteractionType;
import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.SceneryObject;
import io.github.tmanbarton.ifengine.game.GameState;

import javax.annotation.Nonnull;

/**
 * Pre-built test scenarios for common testing patterns.
 * <p>
 * TestFixtures provides convenience methods that combine builders to create
 * fully configured test engines for specific testing needs.
 * <p>
 * Example usage:
 * <pre>
 * TestGameEngine engine = TestFixtures.itemInteractionScenario();
 * engine.createPlayer("test-session");
 * String result = engine.processCommand("test-session", "take key");
 * </pre>
 */
public final class TestFixtures {

  private TestFixtures() {
    // Private constructor - static methods only
  }

  /**
   * Single location, PLAYING state.
   * Use for: basic command testing
   *
   * @return a configured TestGameEngine
   */
  @Nonnull
  public static TestGameEngine singleLocationPlayingScenario() {
    return TestGameEngineBuilder.singleLocation()
        .withInitialPlayerState(GameState.PLAYING)
        .build();
  }

  /**
   * Single location, WAITING_FOR_START_ANSWER state.
   * Use for: testing instruction/intro flow
   *
   * @return a configured TestGameEngine
   */
  @Nonnull
  public static TestGameEngine singleLocationInstructionScenario() {
    return TestGameEngineBuilder.singleLocation()
        .withInitialPlayerState(GameState.WAITING_FOR_START_ANSWER)
        .build();
  }

  /**
   * Two connected locations, no items, PLAYING state.
   * Use for: navigation testing
   *
   * @return a configured TestGameEngine
   */
  @Nonnull
  public static TestGameEngine twoLocationScenario() {
    return TestGameEngineBuilder.twoLocationsEmpty()
        .withInitialPlayerState(GameState.PLAYING)
        .build();
  }

  /**
   * One location with a single key item, PLAYING state.
   * Use for: single item interaction testing
   *
   * @return a configured TestGameEngine
   */
  @Nonnull
  public static TestGameEngine singleItemInteractionScenario() {
    return TestGameEngineBuilder.singleLocationWithKey()
        .withInitialPlayerState(GameState.PLAYING)
        .build();
  }

  /**
   * One location with three items (key, rope, gem), PLAYING state.
   * Use for: inventory/take/drop testing
   *
   * @return a configured TestGameEngine
   */
  @Nonnull
  public static TestGameEngine itemInteractionScenario() {
    final TestGameMap map = TestGameMapBuilder.singleLocation()
        .withItems("key", "rope", "gem")
        .build();
    return TestGameEngineBuilder.withCustomMap(map)
        .withInitialPlayerState(GameState.PLAYING)
        .build();
  }

  /**
   * One location with scenery objects supporting various interactions.
   * Scenery: tree (climbable), window (punchable/kickable), rope (climbable)
   * Use for: scenery object interaction testing
   *
   * @return a configured TestGameEngine
   */
  @Nonnull
  public static TestGameEngine sceneryInteractionScenario() {
    final TestGameMap map = TestGameMapBuilder.singleLocation().build();
    final Location location = map.getStartingLocation();

    // Add scenery objects with various interactions
    location.addSceneryObject(SceneryObject.builder("tree")
        .withInteraction(InteractionType.CLIMB, "You climb the tree and see far into the distance.")
        .build());

    location.addSceneryObject(SceneryObject.builder("window")
        .withInteraction(InteractionType.PUNCH, "You punch the window but it doesn't break.")
        .withInteraction(InteractionType.KICK, "You kick the window but it holds firm.")
        .build());

    location.addSceneryObject(SceneryObject.builder("rope")
        .withInteraction(InteractionType.CLIMB, "You climb the rope to reach the rafters.")
        .build());

    return TestGameEngineBuilder.withCustomMap(map)
        .withInitialPlayerState(GameState.PLAYING)
        .build();
  }

  /**
   * One location with a single tree scenery object.
   * Use for: inference testing (only one interactable object)
   *
   * @return a configured TestGameEngine
   */
  @Nonnull
  public static TestGameEngine singleSceneryInteractionScenario() {
    final TestGameMap map = TestGameMapBuilder.singleLocation().build();
    final Location location = map.getStartingLocation();

    location.addSceneryObject(SceneryObject.builder("tree")
        .withInteraction(InteractionType.CLIMB, "You climb the tree successfully.")
        .build());

    return TestGameEngineBuilder.withCustomMap(map)
        .withInitialPlayerState(GameState.PLAYING)
        .build();
  }

  /**
   * One location with both items and scenery objects.
   * Items: key, rope
   * Scenery: tree, window, table
   * Use for: precedence rules and mixed interactions
   *
   * @return a configured TestGameEngine
   */
  @Nonnull
  public static TestGameEngine mixedInteractionScenario() {
    final TestGameMap map = TestGameMapBuilder.singleLocation()
        .withItems("key", "rope")
        .build();
    final Location location = map.getStartingLocation();

    location.addSceneryObject(SceneryObject.builder("tree")
        .withInteraction(InteractionType.CLIMB, "You climb the tree.")
        .build());

    location.addSceneryObject(SceneryObject.builder("window")
        .build());

    location.addSceneryObject(SceneryObject.builder("table")
        .build());

    return TestGameEngineBuilder.withCustomMap(map)
        .withInitialPlayerState(GameState.PLAYING)
        .build();
  }

  /**
   * Four locations with multiple items distributed across them.
   * Locations: minimal-location, forest, cave, treasure-room
   * Use for: complex multi-location gameplay
   *
   * @return a configured TestGameEngine
   */
  @Nonnull
  public static TestGameEngine adventureScenario() {
    final TestGameMap map = new TestGameMap();

    // Create locations
    final Location start = TestLocationFactory.createSimpleLocation("minimal-location");
    final Location forest = TestLocationFactory.createTestForest();
    final Location cave = TestLocationFactory.createTestCave();
    final Location treasureRoom = TestLocationFactory.createSimpleLocation("treasure-room");

    map.addLocation(start);
    map.addLocation(forest);
    map.addLocation(cave);
    map.addLocation(treasureRoom);

    // Connect locations
    TestLocationFactory.addBidirectionalConnection(start, Direction.NORTH, forest);
    TestLocationFactory.addBidirectionalConnection(forest, Direction.EAST, cave);
    TestLocationFactory.addBidirectionalConnection(cave, Direction.DOWN, treasureRoom);

    // Create and place items
    final Item key = TestItemFactory.createTestKey();
    final Item rope = TestItemFactory.createTestRope();
    final Item gem = TestItemFactory.createTestGem();

    map.placeItem(key, start);
    map.placeItem(rope, forest);
    map.placeItem(gem, treasureRoom);

    return TestGameEngineBuilder.withCustomMap(map)
        .withInitialPlayerState(GameState.PLAYING)
        .build();
  }

  /**
   * Three locations for puzzle testing.
   * Locations: minimal-location, locked-room, key-room
   * Items: key in key-room, treasure in locked-room
   * Use for: lock/unlock mechanics testing
   *
   * @return a configured TestGameEngine
   */
  @Nonnull
  public static TestGameEngine puzzleScenario() {
    final TestGameMap map = new TestGameMap();

    // Create locations
    final Location start = TestLocationFactory.createSimpleLocation("minimal-location");
    final Location lockedRoom = TestLocationFactory.createSimpleLocation("locked-room");
    final Location keyRoom = TestLocationFactory.createSimpleLocation("key-room");

    map.addLocation(start);
    map.addLocation(lockedRoom);
    map.addLocation(keyRoom);

    // Connect locations
    TestLocationFactory.addBidirectionalConnection(start, Direction.NORTH, lockedRoom);
    TestLocationFactory.addBidirectionalConnection(start, Direction.EAST, keyRoom);

    // Create and place items
    final Item key = TestItemFactory.createTestKey();
    final Item treasure = TestItemFactory.createSimpleItem("treasure");

    map.placeItem(key, keyRoom);
    map.placeItem(treasure, lockedRoom);

    return TestGameEngineBuilder.withCustomMap(map)
        .withInitialPlayerState(GameState.PLAYING)
        .build();
  }

  /**
   * One location with a scenery container for put command testing.
   * Container: box (accepts test-coin, test-widget, test-token)
   * Items: test-coin, test-widget, test-token
   * Use for: put command with location containers
   *
   * @return a configured TestGameEngine
   */
  @Nonnull
  public static TestGameEngine locationContainerScenario() {
    final TestGameMap map = TestGameMapBuilder.singleLocation()
        .withItems("test-coin", "test-widget", "test-token")
        .build();
    final Location location = map.getStartingLocation();

    // Add scenery container
    final SceneryObject box = SceneryObject.builder("box")
        .asContainer()
        .withAllowedItems("test-coin", "test-widget", "test-token")
        .build();
    location.addSceneryObject(box);

    return TestGameEngineBuilder.withCustomMap(map)
        .withInitialPlayerState(GameState.PLAYING)
        .build();
  }

  /**
   * Creates a scenario for testing restart confirmation flow.
   * Single location, WAITING_FOR_RESTART_CONFIRMATION state.
   *
   * @return a configured TestGameEngine
   */
  @Nonnull
  public static TestGameEngine restartConfirmationScenario() {
    return TestGameEngineBuilder.singleLocation()
        .withInitialPlayerState(GameState.WAITING_FOR_RESTART_CONFIRMATION)
        .build();
  }

  /**
   * Creates a scenario for testing quit confirmation flow.
   * Single location, WAITING_FOR_QUIT_CONFIRMATION state.
   *
   * @return a configured TestGameEngine
   */
  @Nonnull
  public static TestGameEngine quitConfirmationScenario() {
    return TestGameEngineBuilder.singleLocation()
        .withInitialPlayerState(GameState.WAITING_FOR_QUIT_CONFIRMATION)
        .build();
  }

  /**
   * Creates a scenario for testing code-based unlock/open.
   * Single location with a TestOpenableItem that requires code "1, 2, 3, 4".
   * Use for: testing WAITING_FOR_UNLOCK_CODE and WAITING_FOR_OPEN_CODE states
   *
   * @return a configured TestGameEngine
   */
  @Nonnull
  public static TestGameEngine codeBasedItemScenario() {
    final TestOpenableItem chest = TestOpenableItem.builder("lockbox")
        .withUnlockTargets("lockbox", "box")
        .withOpenTargets("lockbox", "box")
        .withInferredTargetNames("lockbox", "box")
        .withExpectedCode("1, 2, 3, 4")
        .build();

    final TestGameMap map = TestGameMapBuilder.singleLocation().build();
    map.getStartingLocation().addItem(chest);

    return TestGameEngineBuilder.withCustomMap(map)
        .withInitialPlayerState(GameState.PLAYING)
        .build();
  }

  /**
   * Creates a scenario for testing unlock/open with an OpenableLocation.
   * Player starts at a TestOpenableLocation with a "door" that requires a "key".
   * A key item is placed at the location for testing.
   * Use for: UnlockHandler, OpenHandler testing
   *
   * @return a configured TestGameEngine
   */
  @Nonnull
  public static TestGameEngine openableLocationScenario() {
    final TestOpenableLocation openableLocation = TestOpenableLocation.builder("test-vault")
        .withUnlockTargets("door", "vault", "vault door")
        .withOpenTargets("door", "vault", "vault door")
        .withInferredTargetNames("door", "vault")
        .withRequiredKey("key")
        .build();

    final Item key = TestItemFactory.createTestKey();
    openableLocation.addItem(key);

    final TestGameMap map = TestGameMap.createWithLocations(openableLocation);

    return TestGameEngineBuilder.withCustomMap(map)
        .withInitialPlayerState(GameState.PLAYING)
        .build();
  }

  /**
   * Creates a scenario for testing unlock/open without a key available.
   * Player starts at a TestOpenableLocation but has no key.
   * Use for: testing "no key" error paths
   *
   * @return a configured TestGameEngine
   */
  @Nonnull
  public static TestGameEngine openableLocationNoKeyScenario() {
    final TestOpenableLocation openableLocation = TestOpenableLocation.builder("test-vault")
        .withUnlockTargets("door", "vault", "vault door")
        .withOpenTargets("door", "vault", "vault door")
        .withInferredTargetNames("door", "vault")
        .withRequiredKey("key")
        .build();

    final TestGameMap map = TestGameMap.createWithLocations(openableLocation);

    return TestGameEngineBuilder.withCustomMap(map)
        .withInitialPlayerState(GameState.PLAYING)
        .build();
  }
}
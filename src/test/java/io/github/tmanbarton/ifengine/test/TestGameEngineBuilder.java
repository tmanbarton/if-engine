package io.github.tmanbarton.ifengine.test;

import io.github.tmanbarton.ifengine.Direction;
import io.github.tmanbarton.ifengine.game.GameState;
import io.github.tmanbarton.ifengine.response.DefaultResponses;
import io.github.tmanbarton.ifengine.response.ResponseProvider;

import javax.annotation.Nonnull;

/**
 * Fluent builder for creating TestGameEngine instances with predefined scenarios.
 * <p>
 * TestGameEngineBuilder provides static factory methods for common test scenarios
 * and supports chaining with withInitialPlayerState() for state control.
 * <p>
 * Example usage:
 * <pre>
 * TestGameEngine engine = TestGameEngineBuilder.singleLocation()
 *     .withInitialPlayerState(GameState.PLAYING)
 *     .build();
 * </pre>
 */
public final class TestGameEngineBuilder {

  private final TestGameMap testGameMap;
  private final GameState initialState;
  private final ResponseProvider responseProvider;

  private TestGameEngineBuilder(@Nonnull final TestGameMap testGameMap,
                                @Nonnull final GameState initialState,
                                @Nonnull final ResponseProvider responseProvider) {
    this.testGameMap = testGameMap;
    this.initialState = initialState;
    this.responseProvider = responseProvider;
  }

  /**
   * Creates a builder with a single location and no items.
   * Default state: PLAYING
   *
   * @return a new builder
   */
  @Nonnull
  public static TestGameEngineBuilder singleLocation() {
    final TestGameMap map = TestGameMapBuilder.singleLocation().build();
    return new TestGameEngineBuilder(map, GameState.PLAYING, new DefaultResponses());
  }

  /**
   * Creates a builder with two connected locations and basic items.
   * Locations are connected north-south.
   * Items: key, rope at location1.
   * Default state: PLAYING
   *
   * @return a new builder
   */
  @Nonnull
  public static TestGameEngineBuilder twoLocations() {
    final TestGameMap map = TestGameMapBuilder.twoLocations().build();
    return new TestGameEngineBuilder(map, GameState.PLAYING, new DefaultResponses());
  }

  /**
   * Creates a builder with two connected locations but no items.
   * Default state: PLAYING
   *
   * @return a new builder
   */
  @Nonnull
  public static TestGameEngineBuilder twoLocationsEmpty() {
    final TestGameMap map = TestGameMapBuilder.twoLocationsEmpty().build();
    return new TestGameEngineBuilder(map, GameState.PLAYING, new DefaultResponses());
  }

  /**
   * Creates a builder with a single location and a test key.
   * Default state: PLAYING
   *
   * @return a new builder
   */
  @Nonnull
  public static TestGameEngineBuilder singleLocationWithKey() {
    final TestGameMap map = TestGameMapBuilder.singleLocation()
        .withItem("key")
        .build();
    return new TestGameEngineBuilder(map, GameState.PLAYING, new DefaultResponses());
  }

  /**
   * Creates a builder with an adventure scenario:
   * - 3 locations (minimal-location, forest, cave) with connections
   * - Multiple items (key, rope, gem)
   * Default state: PLAYING
   *
   * @return a new builder
   */
  @Nonnull
  public static TestGameEngineBuilder adventureScenario() {
    final TestGameMap map = TestGameMapBuilder.singleLocation()
        .withLocations("forest", "cave")
        .withItems("key", "rope", "gem")
        .withConnection("minimal-location", Direction.NORTH, "forest")
        .withConnection("forest", Direction.EAST, "cave")
        .build();
    return new TestGameEngineBuilder(map, GameState.PLAYING, new DefaultResponses());
  }

  /**
   * Creates a builder with a custom TestGameMap.
   *
   * @param testGameMap the custom map to use
   * @return a new builder
   */
  @Nonnull
  public static TestGameEngineBuilder withCustomMap(@Nonnull final TestGameMap testGameMap) {
    return new TestGameEngineBuilder(testGameMap, GameState.PLAYING, new DefaultResponses());
  }

  /**
   * Sets the initial player state.
   *
   * @param state the initial game state
   * @return a new builder with the specified state
   */
  @Nonnull
  public TestGameEngineBuilder withInitialPlayerState(@Nonnull final GameState state) {
    return new TestGameEngineBuilder(testGameMap, state, responseProvider);
  }

  /**
   * Sets a custom response provider.
   *
   * @param responseProvider the response provider to use
   * @return a new builder with the specified response provider
   */
  @Nonnull
  public TestGameEngineBuilder withResponseProvider(@Nonnull final ResponseProvider responseProvider) {
    return new TestGameEngineBuilder(testGameMap, initialState, responseProvider);
  }

  /**
   * Builds the TestGameEngine.
   *
   * @return a new TestGameEngine
   */
  @Nonnull
  public TestGameEngine build() {
    return new TestGameEngine(testGameMap, responseProvider, initialState);
  }
}
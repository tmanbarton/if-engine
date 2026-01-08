package com.ifengine.game;

import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.content.GameContent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Standard GameMapInterface implementation that builds from GameContent.
 * <p>
 * GameMap is the bridge between game content definitions (GameContent) and
 * the game engine (GameEngine). It:
 * <ul>
 *   <li>Takes a GameContent implementation</li>
 *   <li>Initializes connections and item placement</li>
 *   <li>Provides the GameMapInterface for the engine to use</li>
 *   <li>Handles game reset by creating fresh game content</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 * GameContent gameContent = new MyAdventureContent();
 * GameMap map = new GameMap(gameContent);
 * GameEngine engine = new GameEngine(map, content.getResponseProvider());
 * </pre>
 */
public class GameMap implements GameMapInterface {

  private GameContent gameContent;
  private Map<String, Location> locations;
  private Map<String, Item> items;

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
    this.locations = gameContent.getLocations();
    this.items = gameContent.getItems();

    // Initialize the game world
    gameContent.setupConnections();
    gameContent.placeItems();
    gameContent.addScenery();
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
    final String startKey = gameContent.getStartingLocationKey();
    final Location startLocation = locations.get(startKey);
    if (startLocation == null) {
      throw new IllegalStateException(
          "Starting location '" + startKey + "' not found in locations. " +
          "Ensure getStartingLocationKey() returns a valid key from getLocations().");
    }
    return startLocation;
  }

  @Override
  public void resetMap() {
    // Create a fresh copy of the game content and reinitialize
    final GameContent freshContent = gameContent.createFreshCopy();
    initializeFromContent(freshContent);

    // Reset visited flags on all locations
    for (final Location location : locations.values()) {
      location.setVisited(false);
    }
  }

  /**
   * Gets the game content this map was built from.
   *
   * @return the game content
   */
  @Nonnull
  public GameContent getGameContent() {
    return gameContent;
  }
}
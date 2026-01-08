package com.ifengine.content;

import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.response.DefaultResponses;
import com.ifengine.response.ResponseProvider;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Interface for defining game content.
 * <p>
 * Game developers implement this interface to create their own interactive fiction games
 * using the IFEngine. The implementation defines:
 * <ul>
 *   <li>Locations - the places players can visit</li>
 *   <li>Items - objects players can interact with</li>
 *   <li>Connections - how locations connect to each other</li>
 *   <li>Item placement - where items start in the game world</li>
 *   <li>Custom responses - optional custom narrator voice</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * public class MyInteractiveFiction implements GameContent {
 *     private final Map&lt;String, Location&gt; locations = new HashMap&lt;&gt;();
 *     private final Map&lt;String, Item&gt; items = new HashMap&lt;&gt;();
 *
 *     public MyInteractiveFiction() {
 *         // Create locations
 *         locations.put("forest", new Location("forest", "Dark Forest", "A dark, mysterious forest..."));
 *         locations.put("cabin", new Location("cabin", "Old Cabin", "A weathered cabin..."));
 *
 *         // Create items
 *         items.put("key", new Item("key", "A rusty key", "inventory desc", "location desc", "detailed desc"));
 *     }
 *
 *     {@literal @}Override
 *     public void setupConnections() {
 *         locations.get("forest").addConnection(Direction.NORTH, locations.get("cabin"));
 *         locations.get("cabin").addConnection(Direction.SOUTH, locations.get("forest"));
 *     }
 *
 *     {@literal @}Override
 *     public void placeItems() {
 *         locations.get("cabin").addItem(items.get("key"));
 *     }
 *     // ... other methods
 * }
 * </pre>
 */
public interface GameContent {

  /**
   * Gets all locations in the game, keyed by their unique identifier.
   * <p>
   * Locations should be created in the constructor or initialization method,
   * before setupConnections() is called.
   *
   * @return a map of location keys to Location objects
   */
  @Nonnull
  Map<String, Location> getLocations();

  /**
   * Gets all items in the game, keyed by their unique identifier.
   * <p>
   * Items should be created in the constructor or initialization method,
   * before placeItems() is called.
   *
   * @return a map of item keys to Item objects
   */
  @Nonnull
  Map<String, Item> getItems();

  /**
   * Gets the key of the starting location where new players begin.
   * <p>
   * This key must match one of the keys in getLocations().
   *
   * @return the starting location key
   */
  @Nonnull
  String getStartingLocationKey();

  /**
   * Sets up connections between locations.
   * <p>
   * Called during GameMap initialization after locations are created.
   * Use this method to connect locations in both directions as needed.
   * <p>
   * Example:
   * <pre>
   * locations.get("forest").addConnection(Direction.NORTH, locations.get("cabin"));
   * locations.get("cabin").addConnection(Direction.SOUTH, locations.get("forest"));
   * </pre>
   */
  void setupConnections();

  /**
   * Places items in their starting locations.
   * <p>
   * Called during GameMap initialization after connections are set up.
   * Use this method to add items to locations.
   * <p>
   * Example:
   * <pre>
   * locations.get("cabin").addItem(items.get("key"));
   * </pre>
   */
  void placeItems();

  /**
   * Adds scenery objects to locations.
   * <p>
   * Called during GameMap initialization after items are placed.
   * Scenery objects are non-takeable environmental objects that players
   * can examine and interact with.
   * <p>
   * Default implementation does nothing (no scenery).
   */
  default void addScenery() {
    // Default: no scenery
  }

  /**
   * Gets the response provider for customizing game text.
   * <p>
   * Override this method to provide custom narrator voice and responses.
   * Default implementation returns DefaultResponses.
   *
   * @return the response provider to use
   */
  @Nonnull
  default ResponseProvider getResponseProvider() {
    return new DefaultResponses();
  }

  /**
   * Creates a fresh copy of the game content for reset purposes.
   * <p>
   * When the game is restarted, this method is called to get a fresh
   * copy of all content with items back in their original locations
   * and all state reset.
   * <p>
   * The default implementation creates a new instance using reflection,
   * which works for classes with no-arg constructors. Override this
   * method if your implementation requires special initialization.
   *
   * @return a fresh instance of this GameContent
   */
  @Nonnull
  default GameContent createFreshCopy() {
    try {
      return getClass().getDeclaredConstructor().newInstance();
    } catch (final Exception e) {
      throw new RuntimeException("Failed to create fresh copy of GameContent. " +
          "Override createFreshCopy() if your class needs special initialization.", e);
    }
  }
}
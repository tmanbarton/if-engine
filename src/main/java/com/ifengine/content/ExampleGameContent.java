package com.ifengine.content;

import com.ifengine.Direction;
import com.ifengine.InteractionType;
import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.SceneryObject;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Example game content demonstrating how to create a game using IFEngine.
 * <p>
 * This simple 3-location game includes:
 * <ul>
 *   <li>A cottage (starting location)</li>
 *   <li>A forest path to the north</li>
 *   <li>A clearing to the east of the forest</li>
 * </ul>
 * <p>
 * Items include a lantern in the cottage and a key in the clearing.
 * Scenery includes trees in the forest and wildflowers in the clearing.
 * <p>
 * To create your own game, use this as a template:
 * <pre>
 * public class MyGame implements GameContent {
 *     // 1. Declare locations and items maps
 *     // 2. Create them in constructor
 *     // 3. Implement setupConnections() to link locations
 *     // 4. Implement placeItems() to put items in locations
 *     // 5. Implement addScenery() to add non-takeable objects
 *     // 6. Optionally override getResponseProvider() for custom text
 * }
 * </pre>
 */
public class ExampleGameContent implements GameContent {

  private final Map<String, Location> locations = new HashMap<>();
  private final Map<String, Item> items = new HashMap<>();

  /**
   * Creates the example game content.
   * Locations and items are created here, connections and placement
   * happen in setupConnections() and placeItems().
   */
  public ExampleGameContent() {
    createLocations();
    createItems();
  }

  private void createLocations() {
    // Starting location - a cozy cottage
    locations.put("cottage", new Location(
        "cottage",
        "You are inside a small, cozy cottage. Sunlight streams through a dusty window. " +
            "A wooden door leads north to the outside.",
        "Inside the cottage."
    ));

    // Forest path - north of the cottage
    locations.put("forest", new Location(
        "forest",
        "You stand on a narrow forest path. Ancient trees tower above you, their branches " +
            "forming a green canopy overhead. The path continues east into a brighter area. " +
            "The cottage lies to the south.",
        "On the forest path."
    ));

    // Clearing - east of the forest
    locations.put("clearing", new Location(
        "clearing",
        "You emerge into a sun-dappled clearing. Wildflowers dot the grass, and birdsong " +
            "fills the air. An old stone well stands in the center. The forest path leads west.",
        "In a sunny clearing."
    ));
  }

  private void createItems() {
    // Lantern in the cottage
    items.put("lantern", new Item(
        "lantern",
        "a brass lantern",
        "A brass lantern sits on a wooden shelf.",
        "An old brass lantern, slightly tarnished but still functional. " +
            "It feels solid and well-made.",
        Set.of("lamp", "brass lantern", "light")
    ));

    // Key in the clearing
    items.put("key", new Item(
        "key",
        "a rusty key",
        "A rusty key lies in the grass near the well.",
        "A small iron key, covered in rust. It looks old but might still work.",
        Set.of("rusty key", "iron key", "small key")
    ));

    // A note in the cottage
    items.put("note", new Item(
        "note",
        "a folded note",
        "A folded note rests on the table.",
        "The note reads: 'The key to the cellar is hidden in the clearing. " +
            "Don't forget the lantern - it's dark down there.'",
        Set.of("paper", "letter", "folded note")
    ));
  }

  @Override
  @Nonnull
  public Map<String, Location> getLocations() {
    return locations;
  }

  @Override
  @Nonnull
  public Map<String, Item> getItems() {
    return items;
  }

  @Override
  @Nonnull
  public String getStartingLocationKey() {
    return "cottage";
  }

  @Override
  public void setupConnections() {
    final Location cottage = locations.get("cottage");
    final Location forest = locations.get("forest");
    final Location clearing = locations.get("clearing");

    // Cottage <-> Forest (north/south)
    cottage.addConnection(Direction.NORTH, forest);
    forest.addConnection(Direction.SOUTH, cottage);

    // Forest <-> Clearing (east/west)
    forest.addConnection(Direction.EAST, clearing);
    clearing.addConnection(Direction.WEST, forest);
  }

  @Override
  public void placeItems() {
    // Place lantern and note in cottage
    locations.get("cottage").addItem(items.get("lantern"));
    locations.get("cottage").addItem(items.get("note"));

    // Place key in clearing
    locations.get("clearing").addItem(items.get("key"));
  }

  @Override
  public void addScenery() {
    // Trees in the forest
    final SceneryObject trees = SceneryObject.builder("trees")
        .withAliases("tree", "ancient trees", "branches", "canopy")
        .withInteraction(InteractionType.LOOK,
            "The ancient trees stretch high into the sky. Their gnarled trunks are covered " +
            "in moss, and their interlocking branches block most of the sunlight.")
        .withInteraction(InteractionType.CLIMB,
            "You grab a branch and start to scramble up. After a few pulls you look back down. Uh oh. " +
                    "You just remembered you're afraid of heights, so you carefully drop back to the ground.")
        .withInteraction(InteractionType.PUNCH,
            "You punch the tree trunk. Ouch! That was unwise. The tree doesn't seem to notice.")
        .withInteraction(InteractionType.KICK,
            "You kick the tree trunk and immediately regret it. The tree stands unmoved.")
        .build();
    locations.get("forest").addSceneryObject(trees);

    // Wildflowers in the clearing
    final SceneryObject wildflowers = SceneryObject.builder("wildflowers")
        .withAliases("flowers", "flower", "grass", "plants")
        .withInteraction(InteractionType.LOOK,
            "A colorful carpet of wildflowers - purple asters, yellow buttercups, and white " +
            "daisies - sways gently in the breeze. Bees buzz lazily from bloom to bloom.")
        .withInteraction(InteractionType.TAKE,
            "You consider picking some flowers, but they're so beautiful here. " +
            "Best to leave them for others to enjoy.")
        .build();
    locations.get("clearing").addSceneryObject(wildflowers);

    // The well in the clearing
    final SceneryObject well = SceneryObject.builder("well")
        .withAliases("stone well", "old well")
        .withInteraction(InteractionType.LOOK,
            "An old stone well, its mortar crumbling in places. A wooden bucket hangs from " +
            "a frayed rope. You peer down but can't see the bottom - it's very dark.")
        .withInteraction(InteractionType.CLIMB,
            "Climbing into a dark well seems like a terrible idea. You'd need a water-proof light source " +
            "at minimum, and probably a better rope.")
        .withInteraction(InteractionType.DRINK,
            "You lower the bucket and bring up some water. It's surprisingly cool and refreshing.")
        .build();
    locations.get("clearing").addSceneryObject(well);

    // Window in the cottage
    final SceneryObject window = SceneryObject.builder("window")
        .withAliases("dusty window", "glass")
        .withInteraction(InteractionType.LOOK,
            "Through the dusty glass, you can see a forest path leading north from the cottage.")
        .build();
    locations.get("cottage").addSceneryObject(window);
  }
}
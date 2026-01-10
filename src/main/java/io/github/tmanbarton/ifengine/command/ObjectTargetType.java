package io.github.tmanbarton.ifengine.command;

/**
 * Defines what type of objects a command handler can target.
 *
 * This enum specifies which categories of objects a command can work with,
 * providing a clean separation between different interaction types in the game world.
 *
 * <h3>Object Categories in the Game World</h3>
 * The game world contains three distinct types of interactive objects:
 * <ul>
 *   <li><b>ITEMS</b> - Real game items that can be taken, dropped, and carried</li>
 *   <li><b>SCENERY</b> - Environmental features that provide atmosphere and interaction</li>
 *   <li><b>LOCATION</b> - Special location-based interactions and features</li>
 * </ul>
 *
 * <h3>Why This Separation Matters</h3>
 * Different commands naturally work with different object types:
 * <ul>
 *   <li><b>take/drop</b> - Only work with ITEMS (can't take scenery or locations)</li>
 *   <li><b>climb/break</b> - Only work with SCENERY (can't climb items)</li>
 *   <li><b>unlock/open</b> - Can work with ITEMS, SCENERY, or LOCATION features</li>
 *   <li><b>look</b> - Can examine ITEMS, SCENERY, or LOCATION details</li>
 * </ul>
 *
 * <h3>Location-Based Interactions</h3>
 * Some locations have special interactive features that don't fit the item/scenery model:
 * <ul>
 *   <li><b>Doors</b> - Location-based unlock/open without requiring door objects</li>
 *   <li><b>Mechanisms</b> - Location-specific interactions like levers or switches</li>
 * </ul>
 *
 * <h3>Handler Configuration</h3>
 * Command handlers specify which object types they support:
 * <pre>
 * TakeHandler: [ITEMS] - can only take real items
 * ClimbHandler: [SCENERY] - can only climb scenery objects
 * UnlockHandler: [ITEMS, SCENERY, LOCATION] - can unlock any type
 * LookHandler: [ITEMS, SCENERY, LOCATION] - can examine any type
 * </pre>
 *
 * @see io.github.tmanbarton.ifengine.command.ResolutionMode
 * @see io.github.tmanbarton.ifengine.command.BaseCommandHandler
 * @see io.github.tmanbarton.ifengine.Item
 * @see io.github.tmanbarton.ifengine.SceneryObject
 * @see io.github.tmanbarton.ifengine.Location
 */
public enum ObjectTargetType {

  /**
   * Real game items that can be taken, dropped, and carried in inventory.
   *
   * Items are concrete objects in the game world that players can manipulate:
   * picking up, dropping, examining, and using in various ways.
   *
   * <h4>Characteristics:</h4>
   * <ul>
   *   <li>Can be taken and added to player inventory</li>
   *   <li>Can be dropped and removed from inventory</li>
   *   <li>Have weight and may affect carry capacity</li>
   *   <li>Can be moved between locations</li>
   *   <li>Can be used in commands that require physical objects</li>
   * </ul>
   *
   * <h4>Examples:</h4>
   * <ul>
   *   <li>key - can be taken, carried, used to unlock things</li>
   *   <li>book - can be taken, read, examined</li>
   * </ul>
   *
   * <h4>Commands That Target Items:</h4>
   * <ul>
   *   <li><b>take, get, grab</b> - pick up items</li>
   *   <li><b>drop, put down</b> - drop carried items</li>
   *   <li><b>look, examine</b> - examine item details</li>
   *   <li><b>inventory</b> - list carried items</li>
   * </ul>
   *
   * <h4>Inferred Object Rule:</h4>
   * When no object is specified (INFERRED mode), commands can automatically
   * select items, but only when exactly one relevant item is available.
   */
  ITEMS,

  /**
   * Environmental scenery objects that provide atmosphere and limited interaction.
   *
   * Scenery objects are part of the environment and cannot be taken or moved,
   * but can be interacted with in specific ways to provide descriptions,
   * atmosphere, or special effects.
   *
   * <h4>Characteristics:</h4>
   * <ul>
   *   <li>Cannot be taken or moved</li>
   *   <li>Provide atmospheric descriptions</li>
   *   <li>Support specific interaction types (climb, break, etc.)</li>
   *   <li>Remain in their original location</li>
   *   <li>May trigger special responses or effects</li>
   * </ul>
   *
   * <h4>Examples:</h4>
   * <ul>
   *   <li>tree - can be climbed, provides descriptions</li>
   *   <li>table - can be examined, provides atmosphere</li>
   *   <li>rocks - can be examined for detail</li>
   * </ul>
   *
   * <h4>Commands That Target Scenery:</h4>
   * <ul>
   *   <li><b>climb</b> - climb trees, rocks, etc.</li>
   *   <li><b>break, smash</b> - break scenery objects</li>
   *   <li><b>look, examine</b> - examine scenery details</li>
   * </ul>
   *
   * <h4>Critical Rule: No Auto-Inference</h4>
   * Scenery objects are NEVER automatically selected in INFERRED mode.
   * Players must explicitly specify scenery objects to interact with them.
   * This prevents confusion between items and scenery.
   */
  SCENERY,

  /**
   * Location-specific interactive features and mechanisms.
   *
   * Some locations have special interactive capabilities that don't fit the
   * item or scenery model. These are features of the location itself that
   * can be interacted with to change game state or unlock new content.
   *
   * <h4>Characteristics:</h4>
   * <ul>
   *   <li>Tied to specific locations</li>
   *   <li>May require specific items or conditions</li>
   *   <li>Can change location state or behavior</li>
   *   <li>May unlock new areas or add items</li>
   *   <li>Support context-sensitive interactions</li>
   * </ul>
   *
   * <h4>Examples:</h4>
   * <ul>
   *   <li><b>Doors</b> - unlock/open without requiring door objects</li>
   *   <li><b>Mechanisms</b> - levers, switches, control panels</li>
   *   <li><b>Barriers</b> - gates, walls that can be opened/removed</li>
   * </ul>
   *
   * <h4>Commands That Target Locations:</h4>
   * <ul>
   *   <li><b>unlock</b> - unlock location features (doors)</li>
   *   <li><b>open</b> - open location features (doors)</li>
   *   <li><b>activate</b> - activate location mechanisms</li>
   *   <li><b>look</b> - examine location-specific details</li>
   * </ul>
   *
   * <h4>Inferred Object Support:</h4>
   * Location commands CAN work in INFERRED mode when the current location
   * supports the requested action.
   */
  LOCATION;

  /**
   * Checks if this target type represents real game items.
   *
   * @return true if this is ITEMS type
   */
  public boolean isItems() {
    return this == ITEMS;
  }

  /**
   * Checks if this target type represents scenery objects.
   *
   * @return true if this is SCENERY type
   */
  public boolean isScenery() {
    return this == SCENERY;
  }

  /**
   * Checks if this target type represents location features.
   *
   * @return true if this is LOCATION type
   */
  public boolean isLocation() {
    return this == LOCATION;
  }

  /**
   * Checks if this target type supports automatic object inference.
   *
   * Based on the critical rule that inferred objects are always items,
   * plus the special case that location commands can work in inferred mode
   * when targeting location-specific features.
   *
   * @return true if this type supports INFERRED resolution mode
   */
  public boolean supportsInference() {
    return this == ITEMS || this == LOCATION;
  }

  /**
   * Checks if this target type requires explicit object specification.
   *
   * Scenery objects must always be explicitly specified to prevent confusion
   * between items and environmental features.
   *
   * @return true if this type requires explicit specification
   */
  public boolean requiresExplicitSpecification() {
    return this == SCENERY;
  }
}
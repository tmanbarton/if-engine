package io.github.tmanbarton.ifengine.example;

import io.github.tmanbarton.ifengine.Item;
import java.util.Set;

/**
 * Example Item subclass demonstrating how to add custom properties.
 * <p>
 * This shows the pattern for extending Item with game-specific attributes
 * like point values, cursed status, or any other custom properties.
 *
 * <h2>Usage</h2>
 * <pre>
 * TreasureItem gem = new TreasureItem(
 *     "gem", "a sparkling gem", "A gem glitters here.",
 *     "A flawless ruby worth a fortune.", 100, false);
 *
 * // Check properties in game logic
 * if (player.hasItem("gem")) {
 *   TreasureItem treasure = (TreasureItem) player.getItem("gem");
 *   score += treasure.getPointValue();
 * }
 * </pre>
 */
public class TreasureItem extends Item {

  private final int pointValue;
  private final boolean cursed;

  /**
   * Creates a treasure item with point value and cursed status.
   *
   * @param name the item's unique identifier
   * @param inventoryDescription shown in player's inventory
   * @param locationDescription shown when item is at a location
   * @param detailedDescription shown when examining the item
   * @param pointValue points awarded when collected or at game end
   * @param cursed whether this item has negative effects
   */
  public TreasureItem(final String name, final String inventoryDescription,
                      final String locationDescription, final String detailedDescription,
                      final int pointValue, final boolean cursed) {
    super(name, inventoryDescription, locationDescription, detailedDescription);
    this.pointValue = pointValue;
    this.cursed = cursed;
  }

  /**
   * Creates a treasure item with aliases.
   *
   * @param name the item's unique identifier
   * @param inventoryDescription shown in player's inventory
   * @param locationDescription shown when item is at a location
   * @param detailedDescription shown when examining the item
   * @param aliases alternate names for the item
   * @param pointValue points awarded when collected or at game end
   * @param cursed whether this item has negative effects
   */
  public TreasureItem(final String name, final String inventoryDescription,
                      final String locationDescription, final String detailedDescription,
                      final Set<String> aliases, final int pointValue, final boolean cursed) {
    super(name, inventoryDescription, locationDescription, detailedDescription, aliases);
    this.pointValue = pointValue;
    this.cursed = cursed;
  }

  /**
   * Returns the point value of this treasure.
   *
   * @return points awarded for this item
   */
  public int getPointValue() {
    return pointValue;
  }

  /**
   * Returns whether this item is cursed.
   *
   * @return true if the item has negative effects
   */
  public boolean isCursed() {
    return cursed;
  }
}

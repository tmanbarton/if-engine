package io.github.tmanbarton.ifengine.example;

import io.github.tmanbarton.ifengine.OpenResult;
import io.github.tmanbarton.ifengine.OpenableItem;
import io.github.tmanbarton.ifengine.UnlockResult;
import io.github.tmanbarton.ifengine.game.GameMapInterface;
import io.github.tmanbarton.ifengine.game.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Example OpenableItem subclass demonstrating key-based unlocking.
 * <p>
 * This shows how to create lockable items that require a specific key to open.
 *
 * <h2>Usage</h2>
 * <pre>
 * Lockbox chest = new Lockbox(
 *     "chest",
 *     "a wooden chest",
 *     "A sturdy wooden chest sits in the corner.",
 *     "An old chest with iron bands. It has a rusty lock.",
 *     "key"  // requires the "key" item to unlock
 * );
 * </pre>
 */
public class Lockbox extends OpenableItem {

  private final String requiredKeyName;

  /**
   * Creates a lockbox that requires a specific key to unlock.
   *
   * @param name the item's unique identifier
   * @param inventoryDescription shown in player's inventory
   * @param locationDescription shown when item is at a location
   * @param detailedDescription shown when examining the item
   * @param requiredKeyName the name of the item needed to unlock this
   */
  public Lockbox(@Nonnull final String name,
                 @Nonnull final String inventoryDescription,
                 @Nonnull final String locationDescription,
                 @Nonnull final String detailedDescription,
                 @Nonnull final String requiredKeyName) {
    super(name, inventoryDescription, locationDescription, detailedDescription,
        Set.of(), true);
    this.requiredKeyName = requiredKeyName;
  }

  /**
   * Creates a lockbox with aliases.
   *
   * @param name the item's unique identifier
   * @param inventoryDescription shown in player's inventory
   * @param locationDescription shown when item is at a location
   * @param detailedDescription shown when examining the item
   * @param aliases alternate names for the item
   * @param requiredKeyName the name of the item needed to unlock this
   */
  public Lockbox(@Nonnull final String name,
                 @Nonnull final String inventoryDescription,
                 @Nonnull final String locationDescription,
                 @Nonnull final String detailedDescription,
                 @Nonnull final Set<String> aliases,
                 @Nonnull final String requiredKeyName) {
    super(name, inventoryDescription, locationDescription, detailedDescription,
        aliases, true);
    this.requiredKeyName = requiredKeyName;
  }

  @Override
  @Nonnull
  public Set<String> getInferredTargetNames() {
    return Set.of(getName());
  }

  @Override
  public boolean matchesUnlockTarget(@Nonnull final String target) {
    return getName().equalsIgnoreCase(target) || hasAlias(target);
  }

  @Override
  public boolean matchesOpenTarget(@Nonnull final String target) {
    return getName().equalsIgnoreCase(target) || hasAlias(target);
  }

  @Override
  @Nonnull
  public UnlockResult tryUnlock(@Nonnull final Player player,
                                @Nullable final String providedAnswer,
                                @Nonnull final GameMapInterface gameMap) {
    if (isUnlocked()) {
      return new UnlockResult(false, "It's already unlocked.");
    }

    if (!player.hasItem(requiredKeyName)) {
      return new UnlockResult(false, "You don't have the right key.");
    }

    setUnlocked(true);
    return new UnlockResult(true, "You unlock the " + getName() + ".");
  }

  @Override
  @Nonnull
  public OpenResult tryOpen(@Nonnull final Player player,
                            @Nullable final String providedAnswer,
                            @Nonnull final GameMapInterface gameMap) {
    if (isOpen()) {
      return new OpenResult(false, "It's already open.");
    }

    if (!isUnlocked()) {
      // Try to auto-unlock with key
      if (player.hasItem(requiredKeyName)) {
        setUnlocked(true);
        setOpen(true);
        return new OpenResult(true,
            "You unlock the " + getName() + " and open it.");
      }
      return new OpenResult(false, "It's locked.");
    }

    setOpen(true);
    return new OpenResult(true, "You open the " + getName() + ".");
  }
}

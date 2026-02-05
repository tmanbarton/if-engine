package io.github.tmanbarton.ifengine.example;

import io.github.tmanbarton.ifengine.OpenResult;
import io.github.tmanbarton.ifengine.OpenableItemContainer;
import io.github.tmanbarton.ifengine.UnlockResult;
import io.github.tmanbarton.ifengine.game.GameMapInterface;
import io.github.tmanbarton.ifengine.game.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Example OpenableItemContainer subclass: a lockable chest that holds items.
 * <p>
 * Combines key-based unlocking (like {@link Lockbox}) with container behavior
 * (like {@link io.github.tmanbarton.ifengine.ItemContainer}).
 * The chest must be unlocked and opened before items can be put inside.
 *
 * <h2>Usage</h2>
 * <pre>
 * LockableChest chest = new LockableChest(
 *     "chest",
 *     "a wooden chest",
 *     "A sturdy wooden chest sits in the corner.",
 *     "An old chest with iron bands. It has a rusty lock.",
 *     Set.of("box", "wooden chest"),
 *     "key",
 *     5
 * );
 * </pre>
 */
public class LockableChest extends OpenableItemContainer {

  private final String requiredKeyName;

  /**
   * Creates a lockable chest that requires a key to unlock and can hold items.
   *
   * @param name the item's unique identifier
   * @param inventoryDescription shown in player's inventory
   * @param locationDescription shown when item is at a location
   * @param detailedDescription shown when examining the item
   * @param aliases alternate names for the item
   * @param requiredKeyName the name of the item needed to unlock this
   * @param capacity maximum number of items (0 = unlimited)
   */
  public LockableChest(@Nonnull final String name,
                       @Nonnull final String inventoryDescription,
                       @Nonnull final String locationDescription,
                       @Nonnull final String detailedDescription,
                       @Nonnull final Set<String> aliases,
                       @Nonnull final String requiredKeyName,
                       final int capacity) {
    super(name, inventoryDescription, locationDescription, detailedDescription,
        aliases, true, capacity, Set.of(),
        List.of("in", "into"));
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
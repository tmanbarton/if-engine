package com.ifengine;

import com.ifengine.game.GameMapInterface;
import com.ifengine.game.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Abstract base class for items that can be unlocked and opened.
 * Implements the {@link Openable} interface for unified unlock/open handling.
 * Extends {@link Item} to inherit all item properties.
 *
 * <p>Subclasses must implement:
 * <ul>
 *   <li>{@link #getInferredTargetNames()} - names used for command inference</li>
 *   <li>{@link #matchesUnlockTarget(String)} - target name matching for unlock</li>
 *   <li>{@link #matchesOpenTarget(String)} - target name matching for open</li>
 *   <li>{@link #tryUnlock(Player, String, GameMapInterface)} - unlock logic</li>
 *   <li>{@link #tryOpen(Player, String, GameMapInterface)} - open logic</li>
 * </ul>
 */
public abstract class OpenableItem extends Item implements Openable {

  private boolean unlocked;
  private boolean open = false;
  private final boolean requiresUnlocking;

  /**
   * Constructs an OpenableItem with all properties.
   *
   * @param name the item name
   * @param inventoryDescription the description when in inventory
   * @param locationDescription the description when at a location
   * @param detailedDescription the detailed description for examine/look
   * @param aliases alternative names for the item
   * @param requiresUnlocking true if the item starts locked, false if no lock
   */
  protected OpenableItem(@Nonnull final String name,
                         @Nonnull final String inventoryDescription,
                         @Nonnull final String locationDescription,
                         @Nonnull final String detailedDescription,
                         @Nonnull final Set<String> aliases,
                         final boolean requiresUnlocking) {
    super(name, inventoryDescription, locationDescription, detailedDescription, aliases);
    this.requiresUnlocking = requiresUnlocking;
    // If no lock required, start unlocked
    this.unlocked = !requiresUnlocking;
  }

  // ===== Openable Interface - State Management =====

  @Override
  public final boolean isUnlocked() {
    return unlocked;
  }

  @Override
  public final void setUnlocked(final boolean unlocked) {
    this.unlocked = unlocked;
  }

  @Override
  public final boolean isOpen() {
    return open;
  }

  @Override
  public final void setOpen(final boolean open) {
    this.open = open;
  }

  // ===== Openable Interface - Configuration =====

  @Override
  public final boolean requiresUnlocking() {
    return requiresUnlocking;
  }

  // ===== Abstract Methods for Subclass Implementation =====

  @Override
  @Nonnull
  public abstract Set<String> getInferredTargetNames();

  @Override
  public abstract boolean matchesUnlockTarget(@Nonnull String name);

  @Override
  public abstract boolean matchesOpenTarget(@Nonnull String name);

  @Override
  @Nonnull
  public abstract UnlockResult tryUnlock(@Nonnull Player player, @Nullable String providedAnswer, @Nonnull GameMapInterface gameMap);

  @Override
  @Nonnull
  public abstract OpenResult tryOpen(@Nonnull Player player, @Nullable String providedAnswer, @Nonnull GameMapInterface gameMap);
}
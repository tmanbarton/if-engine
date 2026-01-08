package com.ifengine;

import com.ifengine.game.GameMapInterface;
import com.ifengine.game.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Abstract base class for locations that can be unlocked and opened.
 * Implements the {@link Openable} interface for unified unlock/open handling.
 * Provides state management and template methods for descriptions.
 * Subclasses implement location-specific behavior via abstract methods.
 */
public abstract class OpenableLocation extends Location implements Openable {

  private boolean unlocked = false;
  private boolean open = false;

  public OpenableLocation(@Nonnull final String name, @Nonnull final String longDescription, @Nonnull final String shortDescription) {
    super(name, longDescription, shortDescription);
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

  /**
   * {@inheritDoc}
   * Openable locations require unlocking by default. Subclasses can override if needed.
   */
  @Override
  public boolean requiresUnlocking() {
    return true;
  }

  // ===== Openable Interface - Target Matching =====

  @Override
  public abstract boolean matchesUnlockTarget(@Nonnull String name);

  @Override
  public abstract boolean matchesOpenTarget(@Nonnull String name);

  @Override
  @Nonnull
  public abstract Set<String> getInferredTargetNames();

  // ===== Openable Interface - Unlock/Open Attempts =====

  /**
   * {@inheritDoc}
   * Default implementation uses key-based unlocking.
   * Subclasses can override for code-based or other mechanisms.
   */
  @Override
  @Nonnull
  public UnlockResult tryUnlock(@Nonnull final Player player, @Nullable final String providedAnswer, @Nonnull final GameMapInterface gameMap) {
    if (unlocked) {
      return new UnlockResult(false, getAlreadyUnlockedMessage());
    }

    // Check if providedAnswer refers to the required key (e.g., "unlock shed with key")
    if (providedAnswer != null) {
      final Item providedItem = player.getInventoryItemByName(providedAnswer);
      final boolean isRequiredKey = providedItem != null
          && providedItem.matchesName(getRequiredKeyName());
      if (!isRequiredKey) {
        // Not the required key - reject codes and wrong items
        return new UnlockResult(false, getUnlockNoKeyMessage());
      }
    }

    if (!player.hasItem(getRequiredKeyName())) {
      return new UnlockResult(false, getUnlockNoKeyMessage());
    }

    // Success - unlock the location
    unlocked = true;
    final String message = onUnlock(gameMap);
    return new UnlockResult(true, message);
  }

  /**
   * {@inheritDoc}
   * Handles auto-unlock when player has key and tries to open a locked location.
   */
  @Override
  @Nonnull
  public OpenResult tryOpen(@Nonnull final Player player, @Nullable final String providedAnswer, @Nonnull final GameMapInterface gameMap) {
    if (open) {
      return new OpenResult(false, getAlreadyOpenMessage());
    }

    // Check if providedAnswer refers to the required key (e.g., "open shed with key")
    if (providedAnswer != null) {
      final Item providedItem = player.getInventoryItemByName(providedAnswer);
      final boolean isRequiredKey = providedItem != null
          && providedItem.matchesName(getRequiredKeyName());
      if (!isRequiredKey) {
        // Not the required key - reject codes and wrong items
        return new OpenResult(false, getOpenLockedNoKeyMessage());
      }
    }

    if (!unlocked) {
      // Check if player can auto-unlock (has key)
      if (player.hasItem(getRequiredKeyName())) {
        // Auto-unlock and open
        unlocked = true;
        open = true;
        final String message = onUnlockAndOpen(gameMap);
        return new OpenResult(true, message);
      }
      return new OpenResult(false, getOpenLockedNoKeyMessage());
    }

    // Already unlocked, just open
    open = true;
    final String message = onOpen(gameMap);
    return new OpenResult(true, message);
  }

  // ===== Abstract Methods for Subclass Customization =====

  /**
   * Returns the long description to display when this location is unlocked but not open.
   */
  @Nonnull
  protected abstract String getUnlockedLongDescription();

  /**
   * Returns the short description to display when this location is unlocked but not open.
   */
  @Nonnull
  protected abstract String getUnlockedShortDescription();

  /**
   * Returns the long description to display when this location is open.
   */
  @Nonnull
  protected abstract String getOpenLongDescription();

  /**
   * Returns the short description to display when this location is open.
   */
  @Nonnull
  protected abstract String getOpenShortDescription();

  /**
   * Called when this location is successfully unlocked.
   * Subclasses should implement location-specific behavior.
   *
   * @param gameMap the game map for accessing game objects
   * @return the message to display to the player
   */
  @Nonnull
  public abstract String onUnlock(@Nonnull GameMapInterface gameMap);

  /**
   * Called when this location is successfully opened.
   * Subclasses should implement location-specific behavior (e.g., item population).
   *
   * @param gameMap the game map for accessing game objects
   * @return the message to display to the player
   */
  @Nonnull
  public abstract String onOpen(@Nonnull GameMapInterface gameMap);

  /**
   * Called when this location is unlocked and opened in one action
   * (player has key and tries to open locked location).
   * Subclasses should implement location-specific behavior (e.g., item population).
   *
   * @param gameMap the game map for accessing game objects
   * @return the message to display to the player
   */
  @Nonnull
  public abstract String onUnlockAndOpen(@Nonnull GameMapInterface gameMap);

  /**
   * Returns the name of the key required to unlock this location.
   *
   * @return the key item name (e.g., "key", "vault-key")
   */
  @Nonnull
  public abstract String getRequiredKeyName();

  // ===== Response messages for handlers =====

  /**
   * Returns the message when player tries to unlock but it's already unlocked.
   */
  @Nonnull
  public abstract String getAlreadyUnlockedMessage();

  /**
   * Returns the message when player tries to unlock but doesn't have the key.
   */
  @Nonnull
  public abstract String getUnlockNoKeyMessage();

  /**
   * Returns the message when player tries to open but it's already open.
   */
  @Nonnull
  public abstract String getAlreadyOpenMessage();

  /**
   * Returns the message when player tries to open but it's locked and they don't have the key.
   */
  @Nonnull
  public abstract String getOpenLockedNoKeyMessage();
// TODO add getUnlockNoLockMessage() for when the openablelocation can be opened but doesn't have a lock.
  // ===== Template Methods for Descriptions =====

  /**
   * Template method: returns the appropriate long description based on state.
   * Open state takes precedence over unlocked state.
   */
  @Override
  @Nonnull
  public String getLongDescription() {
    if (open) {
      return getOpenLongDescription();
    } else if (unlocked) {
      return getUnlockedLongDescription();
    } else {
      return super.getLongDescription();
    }
  }

  /**
   * Template method: returns the appropriate short description based on state.
   * Open state takes precedence over unlocked state.
   */
  @Override
  @Nonnull
  public String getShortDescription() {
    if (open) {
      return getOpenShortDescription();
    } else if (unlocked) {
      return getUnlockedShortDescription();
    } else {
      return super.getShortDescription();
    }
  }
}
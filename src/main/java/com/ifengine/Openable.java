package com.ifengine;

import com.ifengine.game.GameMapInterface;
import com.ifengine.game.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Interface for objects that can be unlocked and opened.
 * Supports both locations (like doors, sheds) and items (like chests, jars).
 *
 * <p>Key design principles:
 * <ul>
 *   <li>{@link #tryUnlock(Player, String, GameMapInterface)} encapsulates ALL unlock logic -
 *       each implementation decides its mechanism (key item, code, word, none)</li>
 *   <li>{@link #tryOpen(Player, String, GameMapInterface)} handles opening with full game context</li>
 *   <li>{@link #requiresUnlocking()} determines if unlocking is needed (cookie jar = false)</li>
 * </ul>
 */
public interface Openable {

  // ===== State Management =====

  /**
   * Returns whether this object is unlocked.
   *
   * @return true if unlocked, false if locked
   */
  boolean isUnlocked();

  /**
   * Sets the unlocked state of this object.
   *
   * @param unlocked true to unlock, false to lock
   */
  void setUnlocked(boolean unlocked);

  /**
   * Returns whether this object is open.
   *
   * @return true if open, false if closed
   */
  boolean isOpen();

  /**
   * Sets the open state of this object.
   *
   * @param open true to open, false to close
   */
  void setOpen(boolean open);

  // ===== Configuration =====

  /**
   * Returns whether this object requires unlocking before it can be opened.
   * Objects that return false (like a cookie jar) start unlocked and can be
   * opened directly.
   *
   * @return true if unlocking is required, false if it can be opened directly
   */
  boolean requiresUnlocking();

  /**
   * Returns whether this object uses code/word-based unlocking.
   * <p>
   * Code-based locks prompt the player for a code/word if none is provided.
   * Key-based locks check for a key item in inventory and don't prompt.
   * <p>
   * Default implementation returns false (key-based).
   *
   * @return true if uses code/word unlocking, false if key-based
   */
  default boolean usesCodeBasedUnlocking() {
    return false;
  }

  /**
   * Returns names that can be used when inferring this object as the target
   * of unlock/open commands. These are the names a player might use to refer
   * to this openable thing.
   *
   * @return set of valid inferred target names (lowercase)
   */
  @Nonnull
  Set<String> getInferredTargetNames();

  // ===== Target Matching =====

  /**
   * Checks if the given name matches any valid unlock target for this object.
   *
   * @param name the name to check (case-insensitive)
   * @return true if the name matches a valid unlock target
   */
  boolean matchesUnlockTarget(@Nonnull String name);

  /**
   * Checks if the given name matches any valid open target for this object.
   *
   * @param name the name to check (case-insensitive)
   * @return true if the name matches a valid open target
   */
  boolean matchesOpenTarget(@Nonnull String name);

  // ===== Unlock/Open Attempts =====

  /**
   * Attempts to unlock this object.
   *
   * <p>Each implementation decides its unlock mechanism:
   * <ul>
   *   <li><b>Key-based (Shed):</b> checks player.hasItem("key"), ignores providedAnswer</li>
   *   <li><b>Code-based (Vault):</b> validates providedAnswer against expected code</li>
   *   <li><b>Word-based (Cryptex):</b> validates providedAnswer against expected word</li>
   *   <li><b>No lock (Cookie jar):</b> requiresUnlocking() = false, this method returns success</li>
   * </ul>
   *
   * <p>If providedAnswer is null and one is required, the implementation should
   * return a failure result with a prompt message.
   *
   * @param player the player attempting to unlock
   * @param providedAnswer the code/word provided by the player, or null if none
   * @param gameMap the game map for accessing game objects
   * @return the result of the unlock attempt
   */
  @Nonnull
  UnlockResult tryUnlock(@Nonnull Player player, @Nullable String providedAnswer, @Nonnull GameMapInterface gameMap);

  /**
   * Attempts to open this object.
   *
   * <p>This method should:
   * <ul>
   *   <li>Return failure if already open</li>
   *   <li>Return failure if locked (not unlocked)</li>
   *   <li>Set open state and return success otherwise</li>
   * </ul>
   *
   * <p>Implementations may perform side effects on success (e.g., revealing items,
   * updating scenery) as part of the open action.
   *
   * @param player the player attempting to open
   * @param providedAnswer the code/word provided by the player, or null if none (for auto-unlock)
   * @param gameMap the game map for accessing game objects
   * @return the result of the open attempt
   */
  @Nonnull
  OpenResult tryOpen(@Nonnull Player player, @Nullable String providedAnswer, @Nonnull GameMapInterface gameMap);
}
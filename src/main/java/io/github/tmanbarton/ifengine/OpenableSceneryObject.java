package io.github.tmanbarton.ifengine;

import io.github.tmanbarton.ifengine.game.GameMapInterface;
import io.github.tmanbarton.ifengine.game.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for scenery objects that can be unlocked and opened.
 * Implements the {@link Openable} interface for unified unlock/open handling.
 * Extends {@link SceneryObject} to inherit all scenery properties.
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
public abstract class OpenableSceneryObject extends SceneryObject implements Openable {

  private boolean unlocked;
  private boolean open = false;
  private final boolean requiresUnlocking;

  /**
   * Constructs an OpenableSceneryObject with all properties.
   *
   * @param name the scenery object name
   * @param aliases alternative names for the scenery object
   * @param responses standard interaction type responses
   * @param customResponses custom verb responses
   * @param isContainer whether this scenery object is a container
   * @param allowedItemNames names of items allowed in this container
   * @param prepositions valid prepositions for this container
   * @param requiresUnlocking true if the object starts locked, false if no lock
   */
  protected OpenableSceneryObject(
      @Nonnull final String name,
      @Nonnull final Set<String> aliases,
      @Nonnull final Map<InteractionType, String> responses,
      @Nonnull final Map<String, String> customResponses,
      final boolean isContainer,
      @Nonnull final Set<String> allowedItemNames,
      @Nonnull final List<String> prepositions,
      final boolean requiresUnlocking
  ) {
    super(name, aliases, responses, customResponses, isContainer, allowedItemNames, prepositions);
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
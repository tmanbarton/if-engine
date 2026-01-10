package io.github.tmanbarton.ifengine.game;

import javax.annotation.Nonnull;

/**
 * Represents a puzzle phase with three progressive hints.
 * <p>
 * Each phase has:
 * <ul>
 *   <li>Level 1: A subtle nudge in the right direction</li>
 *   <li>Level 2: A more direct hint</li>
 *   <li>Level 3: An explicit answer telling the player what to do</li>
 * </ul>
 *
 * @param phaseKey unique identifier for this phase
 * @param hint1 subtle nudge (level 1)
 * @param hint2 more direct hint (level 2)
 * @param hint3 explicit answer (level 3)
 */
public record HintPhase(
    @Nonnull String phaseKey,
    @Nonnull String hint1,
    @Nonnull String hint2,
    @Nonnull String hint3
) {

  /**
   * Gets the hint for the specified level.
   * Levels are capped at 3 - requesting level 4+ returns hint3.
   *
   * @param level the hint level (1, 2, or 3+)
   * @return the appropriate hint for the level
   */
  @Nonnull
  public String getHint(final int level) {
    return switch (Math.min(level, 3)) {
      case 1 -> hint1;
      case 2 -> hint2;
      default -> hint3;
    };
  }
}

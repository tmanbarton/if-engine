package io.github.tmanbarton.ifengine.game;

import javax.annotation.Nonnull;

/**
 * Functional interface for determining the current hint phase based on game state.
 * <p>
 * Implementations can check player inventory, location, flags, or any other
 * game state to determine which puzzle phase the player is currently in.
 * <p>
 * Example (state-based):
 * <pre>
 * (player, gameMap) -> {
 *     if (player.hasItem("key")) {
 *         return "unlock-shed";
 *     }
 *     return "find-key";
 * }
 * </pre>
 * <p>
 * Example (flag-based):
 * <pre>
 * (player, gameMap) -> {
 *     if (player.hasFlag("TALKED_TO_NPC")) {
 *         return "NEXT_PUZZLE";
 *     }
 *     return "FIND_NPC";
 * }
 * </pre>
 */
@FunctionalInterface
public interface HintPhaseDeterminer {

  /**
   * Determines the current hint phase based on player and game state.
   *
   * @param player the player requesting the hint
   * @param gameMap the game map for accessing world state
   * @return the phase key identifying the current puzzle phase
   */
  @Nonnull
  String determinePhase(@Nonnull Player player, @Nonnull GameMapInterface gameMap);
}

package io.github.tmanbarton.ifengine.game;

import javax.annotation.Nonnull;

/**
 * Functional interface for handling custom intro responses.
 * <p>
 * Implement this to customize how the game responds to player input
 * during the intro phase. The handler receives the player's input
 * and returns an {@link IntroResult} indicating the message to display
 * and whether to transition to the PLAYING state.
 * <p>
 * Example usage:
 * <pre>
 * IntroHandler handler = (player, response, gameMap) -> {
 *     if ("yes".equalsIgnoreCase(response)) {
 *         return IntroResult.playing("Welcome to the adventure!");
 *     }
 *     return IntroResult.waiting("Please answer yes or no.");
 * };
 * </pre>
 */
@FunctionalInterface
public interface IntroHandler {

  /**
   * Handles the player's response during the intro phase.
   *
   * @param player the player responding to the intro
   * @param response the player's input text
   * @param gameMap the game map, for accessing game world data
   * @return an IntroResult containing the message and transition decision
   */
  @Nonnull
  IntroResult handle(@Nonnull Player player, @Nonnull String response, @Nonnull GameMapInterface gameMap);
}

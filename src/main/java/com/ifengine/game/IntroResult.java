package com.ifengine.game;

import javax.annotation.Nonnull;

/**
 * Result from an intro handler, containing the message to display and whether
 * to transition to the PLAYING state.
 *
 * @param message the message to display to the player
 * @param transitionToPlaying true to transition to PLAYING state, false to stay in WAITING state
 */
public record IntroResult(
    @Nonnull String message,
    boolean transitionToPlaying
) {

  /**
   * Creates a result that transitions to PLAYING state.
   *
   * @param message the message to display
   * @return an IntroResult that transitions to PLAYING
   */
  @Nonnull
  public static IntroResult playing(@Nonnull final String message) {
    return new IntroResult(message, true);
  }

  /**
   * Creates a result that stays in WAITING state.
   *
   * @param message the message to display
   * @return an IntroResult that stays in WAITING
   */
  @Nonnull
  public static IntroResult waiting(@Nonnull final String message) {
    return new IntroResult(message, false);
  }
}

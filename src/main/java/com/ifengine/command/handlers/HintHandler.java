package com.ifengine.command.handlers;

import com.ifengine.command.BaseCommandHandler;
import com.ifengine.game.Player;
import com.ifengine.parser.ParsedCommand;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Handles hint commands for providing player guidance.
 *
 * This is a simplified hint handler that provides basic hint functionality.
 * Games should extend this class and override {@link #getHint(Player)} to provide
 * game-specific hints based on player location, inventory, and puzzle progress.
 *
 * The default implementation returns a generic hint message.
 */
public class HintHandler implements BaseCommandHandler {

  private static final String DEFAULT_HINT = "Try exploring your surroundings. Look at things, pick them up, and see what you can interact with.";

  public HintHandler() {
  }

  @Override
  @Nonnull
  public List<String> getSupportedVerbs() {
    return List.of("hint", "hints");
  }

  @Override
  @Nonnull
  public String handle(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    return getHint(player);
  }

  /**
   * Returns a hint for the player based on their current game state.
   * Override this method to provide game-specific hints.
   *
   * @param player the player requesting the hint
   * @return a hint string to help the player progress
   */
  @Nonnull
  protected String getHint(@Nonnull final Player player) {
    return DEFAULT_HINT;
  }
}
package io.github.tmanbarton.ifengine.command.handlers;

import io.github.tmanbarton.ifengine.command.BaseCommandHandler;
import io.github.tmanbarton.ifengine.game.GameMapInterface;
import io.github.tmanbarton.ifengine.game.HintConfiguration;
import io.github.tmanbarton.ifengine.game.HintPhase;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.parser.ParsedCommand;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Handles hint commands for providing player guidance.
 * <p>
 * Supports progressive hints (3 levels per puzzle phase) when configured with
 * a {@link HintConfiguration}. Without configuration, returns a default hint.
 * <p>
 * Hint levels:
 * <ul>
 *   <li>Level 1: A subtle nudge in the right direction</li>
 *   <li>Level 2: A more direct hint</li>
 *   <li>Level 3: An explicit answer telling the player what to do</li>
 * </ul>
 * <p>
 * When the player advances to a new puzzle phase, hint counts reset so they
 * receive level 1 hints for the new phase.
 */
public class HintHandler implements BaseCommandHandler {

  private static final int MAX_HINT_LEVEL = 3;

  private final HintConfiguration config;
  private final GameMapInterface gameMap;

  /**
   * Creates a HintHandler with optional configuration.
   *
   * @param config the hint configuration, or null for default behavior
   * @param gameMap the game map for phase determination
   */
  public HintHandler(@Nullable final HintConfiguration config,
                     @Nonnull final GameMapInterface gameMap) {
    this.config = config;
    this.gameMap = gameMap;
  }

  /**
   * Creates a HintHandler with no configuration.
   * Returns default hint message for all requests.
   */
  public HintHandler() {
    this.config = null;
    this.gameMap = null;
  }

  @Override
  @Nonnull
  public List<String> getSupportedVerbs() {
    return List.of("hint", "hints");
  }

  @Override
  @Nonnull
  public String handle(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    if (config == null || gameMap == null) {
      return HintConfiguration.DEFAULT_HINT;
    }

    // Determine current phase
    final String currentPhase = config.getDeterminer().determinePhase(player, gameMap);
    final HintPhase phase = config.getPhase(currentPhase);

    if (phase == null) {
      return HintConfiguration.DEFAULT_HINT;
    }

    // Check if phase changed - reset hint count for new phase
    final String lastPhase = player.getLastHintPhase();
    if (!currentPhase.equals(lastPhase)) {
      player.setLastHintPhase(currentPhase);
      // Hint count for new phase starts at 0, will be incremented below
    }

    // Increment hint count and get hint level (capped at MAX_HINT_LEVEL)
    player.incrementHintCount(currentPhase);
    final int hintCount = player.getHintCount(currentPhase);
    final int hintLevel = Math.min(hintCount, MAX_HINT_LEVEL);

    return phase.getHint(hintLevel);
  }
}

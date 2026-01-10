package com.ifengine.game;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Configuration for the hint system.
 * <p>
 * Contains all puzzle phases with their hints and the determiner
 * for identifying the current phase.
 */
public class HintConfiguration {

  /**
   * Default hint message shown when no configuration is provided.
   */
  public static final String DEFAULT_HINT =
      "Try exploring your surroundings. Look at things, pick them up, and see what you can interact with.";

  private final Map<String, HintPhase> phases;
  private final HintPhaseDeterminer determiner;

  HintConfiguration(
      @Nonnull final Map<String, HintPhase> phases,
      @Nonnull final HintPhaseDeterminer determiner) {
    this.phases = Map.copyOf(phases);
    this.determiner = determiner;
  }

  /**
   * Gets a hint phase by its key.
   *
   * @param phaseKey the phase key
   * @return the hint phase, or null if not found
   */
  @Nullable
  public HintPhase getPhase(@Nonnull final String phaseKey) {
    return phases.get(phaseKey);
  }

  /**
   * Gets the phase determiner.
   *
   * @return the hint phase determiner
   */
  @Nonnull
  public HintPhaseDeterminer getDeterminer() {
    return determiner;
  }
}

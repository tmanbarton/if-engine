package io.github.tmanbarton.ifengine.game;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Builder for creating {@link HintConfiguration} instances.
 * <p>
 * Example usage:
 * <pre>
 * HintConfiguration config = new HintConfigurationBuilder()
 *     .addPhase("find-key",
 *         "Something important might be nearby...",
 *         "Check around the old tree. Something brass...",
 *         "Take the brass key from the table.")
 *     .addPhase("unlock-shed",
 *         "That key must be for something...",
 *         "Try using the key on the shed's lock.",
 *         "Type 'unlock shed' to use your key.")
 *     .determiner((player, gameMap) -> {
 *         if (player.hasItem("key")) {
 *             return "unlock-shed";
 *         }
 *         return "find-key";
 *     })
 *     .build();
 * </pre>
 */
public class HintConfigurationBuilder {

  private final Map<String, HintPhase> phases = new HashMap<>();
  private HintPhaseDeterminer determiner;

  /**
   * Adds a hint phase with three progressive hints.
   *
   * @param phaseKey unique identifier for this phase
   * @param hint1 subtle nudge (level 1)
   * @param hint2 more direct hint (level 2)
   * @param hint3 explicit answer (level 3)
   * @return this builder for method chaining
   */
  @Nonnull
  public HintConfigurationBuilder addPhase(
      @Nonnull final String phaseKey,
      @Nonnull final String hint1,
      @Nonnull final String hint2,
      @Nonnull final String hint3) {
    Objects.requireNonNull(phaseKey, "phaseKey cannot be null");
    Objects.requireNonNull(hint1, "hint1 cannot be null");
    Objects.requireNonNull(hint2, "hint2 cannot be null");
    Objects.requireNonNull(hint3, "hint3 cannot be null");
    phases.put(phaseKey, new HintPhase(phaseKey, hint1, hint2, hint3));
    return this;
  }

  /**
   * Sets the phase determiner that identifies the current puzzle phase.
   *
   * @param determiner the phase determiner
   * @return this builder for method chaining
   */
  @Nonnull
  public HintConfigurationBuilder determiner(@Nonnull final HintPhaseDeterminer determiner) {
    Objects.requireNonNull(determiner, "determiner cannot be null");
    this.determiner = determiner;
    return this;
  }

  /**
   * Builds the hint configuration.
   *
   * @return the configured HintConfiguration
   * @throws IllegalStateException if no determiner was set
   */
  @Nonnull
  public HintConfiguration build() {
    if (determiner == null) {
      throw new IllegalStateException("Determiner must be set before building HintConfiguration");
    }
    return new HintConfiguration(phases, determiner);
  }
}

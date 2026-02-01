package io.github.tmanbarton.ifengine.test;

import io.github.tmanbarton.ifengine.InteractionType;
import io.github.tmanbarton.ifengine.OpenResult;
import io.github.tmanbarton.ifengine.OpenableSceneryObject;
import io.github.tmanbarton.ifengine.UnlockResult;
import io.github.tmanbarton.ifengine.game.GameMapInterface;
import io.github.tmanbarton.ifengine.game.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test implementation of OpenableSceneryObject for unit testing.
 * Simulates a key-based or code-based unlock mechanism.
 */
public class TestOpenableSceneryObject extends OpenableSceneryObject {

  public static final String DEFAULT_UNLOCK_MESSAGE = "You unlock the safe.";
  public static final String DEFAULT_OPEN_MESSAGE = "You open the safe.";
  public static final String DEFAULT_ALREADY_UNLOCKED_MESSAGE = "It's already unlocked.";
  public static final String DEFAULT_UNLOCK_NO_KEY_MESSAGE = "You don't have the key.";
  public static final String DEFAULT_ALREADY_OPEN_MESSAGE = "It's already open.";
  public static final String DEFAULT_OPEN_LOCKED_MESSAGE = "It's locked.";
  public static final String DEFAULT_PROMPT_MESSAGE = "Enter the code.";
  public static final String DEFAULT_WRONG_CODE_MESSAGE = "That's not the right code.";
  public static final String DEFAULT_NO_LOCK_MESSAGE = "It doesn't have a lock.";

  private final Set<String> unlockTargets;
  private final Set<String> openTargets;
  private final Set<String> inferredTargetNames;
  private final String requiredKeyName;
  private final String expectedCode;

  private TestOpenableSceneryObject(@Nonnull final Builder builder) {
    super(
        builder.name,
        builder.aliases,
        builder.interactions,
        Map.of(),
        false,
        Set.of(),
        List.of(),
        builder.requiresUnlocking
    );
    this.unlockTargets = builder.unlockTargets;
    this.openTargets = builder.openTargets;
    this.inferredTargetNames = builder.inferredTargetNames;
    this.requiredKeyName = builder.requiredKeyName;
    this.expectedCode = builder.expectedCode;
}

  @Nonnull
  public static Builder openableBuilder(@Nonnull final String name) {
    return new Builder(name);
  }

  @Nonnull
  public static TestOpenableSceneryObject createSimple(@Nonnull final String name) {
    return openableBuilder(name).build();
  }

  @Override
  @Nonnull
  public Set<String> getInferredTargetNames() {
    return inferredTargetNames;
  }

  @Override
  public boolean matchesUnlockTarget(@Nonnull final String name) {
    return unlockTargets.stream()
        .anyMatch(target -> target.equalsIgnoreCase(name));
  }

  @Override
  public boolean matchesOpenTarget(@Nonnull final String name) {
    return openTargets.stream()
        .anyMatch(target -> target.equalsIgnoreCase(name));
  }

  @Override
  public boolean usesCodeBasedUnlocking() {
    return expectedCode != null;
  }

  @Override
  @Nonnull
  public UnlockResult tryUnlock(@Nonnull final Player player, @Nullable final String providedAnswer, @Nonnull final GameMapInterface gameMap) {
    if (!requiresUnlocking()) {
      return new UnlockResult(false, DEFAULT_NO_LOCK_MESSAGE);
    }

    if (isUnlocked()) {
      return new UnlockResult(false, DEFAULT_ALREADY_UNLOCKED_MESSAGE);
    }

    // Code-based unlock (if expectedCode is set)
    if (expectedCode != null) {
      if (providedAnswer == null || providedAnswer.isBlank()) {
        return new UnlockResult(false, DEFAULT_PROMPT_MESSAGE);
      }

      final List<String> normalizedExpected = normalizeCode(expectedCode);
      final List<String> normalizedInput = normalizeCode(providedAnswer);
      if (!normalizedInput.equals(normalizedExpected)) {
        return new UnlockResult(false, DEFAULT_WRONG_CODE_MESSAGE);
      }

      setUnlocked(true);
      return new UnlockResult(true, DEFAULT_UNLOCK_MESSAGE);
    }

    // Key-based unlock
    if (!player.hasItem(requiredKeyName)) {
      return new UnlockResult(false, DEFAULT_UNLOCK_NO_KEY_MESSAGE);
    }

    setUnlocked(true);
    return new UnlockResult(true, DEFAULT_UNLOCK_MESSAGE);
  }

  /**
   * Normalizes a code by splitting on spaces and/or commas.
   * Supports formats: "1 2 3 4", "1,2,3,4", "1, 2, 3, 4"
   */
  @Nonnull
  private List<String> normalizeCode(@Nonnull final String code) {
    return Arrays.stream(code.split("[\\s,]+"))
        .filter(s -> !s.isEmpty())
        .toList();
  }

  @Override
  @Nonnull
  public OpenResult tryOpen(@Nonnull final Player player, @Nullable final String providedAnswer, @Nonnull final GameMapInterface gameMap) {
    if (isOpen()) {
      return new OpenResult(false, DEFAULT_ALREADY_OPEN_MESSAGE);
    }

    if (!isUnlocked()) {
      // Code-based auto-unlock (if expectedCode is set)
      if (expectedCode != null) {
        if (providedAnswer == null || providedAnswer.isBlank()) {
          return new OpenResult(false, DEFAULT_PROMPT_MESSAGE);
        }

        final List<String> normalizedExpected = normalizeCode(expectedCode);
        final List<String> normalizedInput = normalizeCode(providedAnswer);
        if (!normalizedInput.equals(normalizedExpected)) {
          return new OpenResult(false, DEFAULT_WRONG_CODE_MESSAGE);
        }

        // Correct code - unlock and open
        setUnlocked(true);
        setOpen(true);
        return new OpenResult(true, DEFAULT_UNLOCK_MESSAGE + " " + DEFAULT_OPEN_MESSAGE);
      }

      // Check for auto-unlock with key
      if (requiresUnlocking() && player.hasItem(requiredKeyName)) {
        setUnlocked(true);
        setOpen(true);
        return new OpenResult(true, DEFAULT_UNLOCK_MESSAGE + " " + DEFAULT_OPEN_MESSAGE);
      }
      return new OpenResult(false, DEFAULT_OPEN_LOCKED_MESSAGE);
    }

    setOpen(true);
    return new OpenResult(true, DEFAULT_OPEN_MESSAGE);
  }

  public static final class Builder {

    private final String name;
    private Set<String> aliases = Set.of();
    private boolean requiresUnlocking = true;
    private Set<String> unlockTargets = Set.of("safe");
    private Set<String> openTargets = Set.of("safe");
    private Set<String> inferredTargetNames = Set.of("safe");
    private String requiredKeyName = "key";
    private String expectedCode = null;
    private Map<InteractionType, String> interactions = new HashMap<>();

    private Builder(@Nonnull final String name) {
      this.name = name;
      // Default interaction so SceneryObject builder validation isn't needed
      // (we bypass the builder and use the protected constructor directly)
      this.interactions.put(InteractionType.LOOK, "You see a " + name + ".");
    }

    @Nonnull
    public Builder withAliases(@Nonnull final String... aliases) {
      this.aliases = Set.of(aliases);
      return this;
    }

    @Nonnull
    public Builder withRequiresUnlocking(final boolean requiresUnlocking) {
      this.requiresUnlocking = requiresUnlocking;
      return this;
    }

    @Nonnull
    public Builder withUnlockTargets(@Nonnull final String... targets) {
      this.unlockTargets = Set.of(targets);
      return this;
    }

    @Nonnull
    public Builder withOpenTargets(@Nonnull final String... targets) {
      this.openTargets = Set.of(targets);
      return this;
    }

    @Nonnull
    public Builder withInferredTargetNames(@Nonnull final String... names) {
      this.inferredTargetNames = Set.of(names);
      return this;
    }

    @Nonnull
    public Builder withRequiredKey(@Nonnull final String keyName) {
      this.requiredKeyName = keyName;
      return this;
    }

    @Nonnull
    public Builder withExpectedCode(@Nonnull final String code) {
      this.expectedCode = code;
      return this;
    }

    @Nonnull
    public Builder withInteraction(@Nonnull final InteractionType type, @Nonnull final String response) {
      this.interactions.put(type, response);
      return this;
    }

    @Nonnull
    public TestOpenableSceneryObject build() {
      return new TestOpenableSceneryObject(this);
    }
  }
}
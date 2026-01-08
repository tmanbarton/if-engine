package com.ifengine.test;

import com.ifengine.OpenResult;
import com.ifengine.OpenableItem;
import com.ifengine.UnlockResult;
import com.ifengine.game.GameMapInterface;
import com.ifengine.game.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Test implementation of OpenableItem for unit testing.
 * Simulates a key-based unlock mechanism similar to OpenableLocation.
 */
public class TestOpenableItem extends OpenableItem {

  public static final String DEFAULT_UNLOCK_MESSAGE = "You unlock the chest.";
  public static final String DEFAULT_OPEN_MESSAGE = "You open the chest.";
  public static final String DEFAULT_ALREADY_UNLOCKED_MESSAGE = "It's already unlocked.";
  public static final String DEFAULT_UNLOCK_NO_KEY_MESSAGE = "You don't have the key.";
  public static final String DEFAULT_ALREADY_OPEN_MESSAGE = "It's already open.";
  public static final String DEFAULT_OPEN_LOCKED_MESSAGE = "It's locked.";
  public static final String DEFAULT_PROMPT_MESSAGE = "Enter the code.";
  public static final String DEFAULT_WRONG_CODE_MESSAGE = "That's not the right code.";

  private final Set<String> unlockTargets;
  private final Set<String> openTargets;
  private final Set<String> inferredTargetNames;
  private final String requiredKeyName;
  private final String expectedCode;

  private TestOpenableItem(@Nonnull final Builder builder) {
    super(
        builder.name,
        builder.inventoryDescription,
        builder.locationDescription,
        builder.detailedDescription,
        builder.aliases,
        builder.requiresUnlocking
    );
    this.unlockTargets = builder.unlockTargets;
    this.openTargets = builder.openTargets;
    this.inferredTargetNames = builder.inferredTargetNames;
    this.requiredKeyName = builder.requiredKeyName;
    this.expectedCode = builder.expectedCode;
  }

  @Nonnull
  public static Builder builder(@Nonnull final String name) {
    return new Builder(name);
  }

  @Nonnull
  public static TestOpenableItem createSimple(@Nonnull final String name) {
    return builder(name).build();
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
    if (isUnlocked()) {
      return new UnlockResult(false, DEFAULT_ALREADY_UNLOCKED_MESSAGE);
    }

    if (!requiresUnlocking()) {
      setUnlocked(true);
      return new UnlockResult(true, DEFAULT_UNLOCK_MESSAGE);
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
    private String inventoryDescription = "a chest";
    private String locationDescription = "A chest sits here.";
    private String detailedDescription = "A sturdy wooden chest.";
    private Set<String> aliases = Set.of();
    private boolean requiresUnlocking = true;
    private Set<String> unlockTargets = Set.of("chest");
    private Set<String> openTargets = Set.of("chest");
    private Set<String> inferredTargetNames = Set.of("chest");
    private String requiredKeyName = "key";
    private String expectedCode = null;

    private Builder(@Nonnull final String name) {
      this.name = name;
    }

    @Nonnull
    public Builder withInventoryDescription(@Nonnull final String description) {
      this.inventoryDescription = description;
      return this;
    }

    @Nonnull
    public Builder withLocationDescription(@Nonnull final String description) {
      this.locationDescription = description;
      return this;
    }

    @Nonnull
    public Builder withDetailedDescription(@Nonnull final String description) {
      this.detailedDescription = description;
      return this;
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
    public TestOpenableItem build() {
      return new TestOpenableItem(this);
    }
  }
}
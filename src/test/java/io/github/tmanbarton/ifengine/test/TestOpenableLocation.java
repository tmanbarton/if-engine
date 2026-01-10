package io.github.tmanbarton.ifengine.test;

import io.github.tmanbarton.ifengine.OpenableLocation;
import io.github.tmanbarton.ifengine.game.GameMapInterface;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Test implementation of OpenableLocation for unit testing.
 * <p>
 * Provides configurable behavior and exposes constants for test assertions.
 * Use the builder to create instances with specific configurations.
 * <p>
 * Example usage:
 * <pre>
 * TestOpenableLocation vault = TestOpenableLocation.builder("vault")
 *     .withRequiredKey("vault-key")
 *     .withUnlockTargets("vault", "door", "vault door")
 *     .withOpenTargets("vault", "door", "vault door")
 *     .build();
 * </pre>
 */
public class TestOpenableLocation extends OpenableLocation {

  // ===== Constants for test assertions =====
  public static final String DEFAULT_UNLOCK_MESSAGE = "You unlock the door.";
  public static final String DEFAULT_OPEN_MESSAGE = "You open the door.";
  public static final String DEFAULT_UNLOCK_AND_OPEN_MESSAGE = "You unlock and open the door.";
  public static final String DEFAULT_ALREADY_UNLOCKED_MESSAGE = "It's already unlocked.";
  public static final String DEFAULT_UNLOCK_NO_KEY_MESSAGE = "You don't have the key.";
  public static final String DEFAULT_ALREADY_OPEN_MESSAGE = "It's already open.";
  public static final String DEFAULT_OPEN_LOCKED_NO_KEY_MESSAGE = "It's locked, and you don't have the key.";

  public static final String DEFAULT_LONG_DESCRIPTION = "A test location with an unlockable door.";
  public static final String DEFAULT_SHORT_DESCRIPTION = "Test location.";
  public static final String DEFAULT_UNLOCKED_LONG_DESCRIPTION = "A test location. The door is unlocked.";
  public static final String DEFAULT_UNLOCKED_SHORT_DESCRIPTION = "Test location (door unlocked).";
  public static final String DEFAULT_OPEN_LONG_DESCRIPTION = "A test location. The door stands open.";
  public static final String DEFAULT_OPEN_SHORT_DESCRIPTION = "Test location (door open).";

  public static final String DEFAULT_REQUIRED_KEY = "key";

  private final Set<String> unlockTargets;
  private final Set<String> openTargets;
  private final Set<String> inferredTargetNames;
  private final String requiredKeyName;

  private final String unlockMessage;
  private final String openMessage;
  private final String unlockAndOpenMessage;
  private final String alreadyUnlockedMessage;
  private final String unlockNoKeyMessage;
  private final String alreadyOpenMessage;
  private final String openLockedNoKeyMessage;

  private final String unlockedLongDescription;
  private final String unlockedShortDescription;
  private final String openLongDescription;
  private final String openShortDescription;

  private TestOpenableLocation(@Nonnull final Builder builder) {
    super(builder.name, builder.longDescription, builder.shortDescription);
    this.unlockTargets = builder.unlockTargets;
    this.openTargets = builder.openTargets;
    this.inferredTargetNames = builder.inferredTargetNames;
    this.requiredKeyName = builder.requiredKeyName;
    this.unlockMessage = builder.unlockMessage;
    this.openMessage = builder.openMessage;
    this.unlockAndOpenMessage = builder.unlockAndOpenMessage;
    this.alreadyUnlockedMessage = builder.alreadyUnlockedMessage;
    this.unlockNoKeyMessage = builder.unlockNoKeyMessage;
    this.alreadyOpenMessage = builder.alreadyOpenMessage;
    this.openLockedNoKeyMessage = builder.openLockedNoKeyMessage;
    this.unlockedLongDescription = builder.unlockedLongDescription;
    this.unlockedShortDescription = builder.unlockedShortDescription;
    this.openLongDescription = builder.openLongDescription;
    this.openShortDescription = builder.openShortDescription;
  }

  /**
   * Creates a builder for a TestOpenableLocation with the given name.
   *
   * @param name the location name
   * @return a new builder
   */
  @Nonnull
  public static Builder builder(@Nonnull final String name) {
    return new Builder(name);
  }

  /**
   * Creates a simple TestOpenableLocation with default settings.
   * Unlock/open targets: "door"
   * Required key: "key"
   *
   * @param name the location name
   * @return a new TestOpenableLocation
   */
  @Nonnull
  public static TestOpenableLocation createSimple(@Nonnull final String name) {
    return builder(name).build();
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
  @Nonnull
  protected String getUnlockedLongDescription() {
    return unlockedLongDescription;
  }

  @Override
  @Nonnull
  protected String getUnlockedShortDescription() {
    return unlockedShortDescription;
  }

  @Override
  @Nonnull
  protected String getOpenLongDescription() {
    return openLongDescription;
  }

  @Override
  @Nonnull
  protected String getOpenShortDescription() {
    return openShortDescription;
  }

  @Override
  @Nonnull
  public String onUnlock(@Nonnull final GameMapInterface gameMap) {
    return unlockMessage;
  }

  @Override
  @Nonnull
  public String onOpen(@Nonnull final GameMapInterface gameMap) {
    return openMessage;
  }

  @Override
  @Nonnull
  public String onUnlockAndOpen(@Nonnull final GameMapInterface gameMap) {
    return unlockAndOpenMessage;
  }

  @Override
  @Nonnull
  public String getRequiredKeyName() {
    return requiredKeyName;
  }

  @Override
  @Nonnull
  public Set<String> getInferredTargetNames() {
    return inferredTargetNames;
  }

  @Override
  @Nonnull
  public String getAlreadyUnlockedMessage() {
    return alreadyUnlockedMessage;
  }

  @Override
  @Nonnull
  public String getUnlockNoKeyMessage() {
    return unlockNoKeyMessage;
  }

  @Override
  @Nonnull
  public String getAlreadyOpenMessage() {
    return alreadyOpenMessage;
  }

  @Override
  @Nonnull
  public String getOpenLockedNoKeyMessage() {
    return openLockedNoKeyMessage;
  }

  /**
   * Builder for TestOpenableLocation.
   */
  public static final class Builder {

    private final String name;
    private String longDescription = DEFAULT_LONG_DESCRIPTION;
    private String shortDescription = DEFAULT_SHORT_DESCRIPTION;
    private Set<String> unlockTargets = Set.of("door");
    private Set<String> openTargets = Set.of("door");
    private Set<String> inferredTargetNames = Set.of("door");
    private String requiredKeyName = DEFAULT_REQUIRED_KEY;

    private String unlockMessage = DEFAULT_UNLOCK_MESSAGE;
    private String openMessage = DEFAULT_OPEN_MESSAGE;
    private String unlockAndOpenMessage = DEFAULT_UNLOCK_AND_OPEN_MESSAGE;
    private String alreadyUnlockedMessage = DEFAULT_ALREADY_UNLOCKED_MESSAGE;
    private String unlockNoKeyMessage = DEFAULT_UNLOCK_NO_KEY_MESSAGE;
    private String alreadyOpenMessage = DEFAULT_ALREADY_OPEN_MESSAGE;
    private String openLockedNoKeyMessage = DEFAULT_OPEN_LOCKED_NO_KEY_MESSAGE;

    private String unlockedLongDescription = DEFAULT_UNLOCKED_LONG_DESCRIPTION;
    private String unlockedShortDescription = DEFAULT_UNLOCKED_SHORT_DESCRIPTION;
    private String openLongDescription = DEFAULT_OPEN_LONG_DESCRIPTION;
    private String openShortDescription = DEFAULT_OPEN_SHORT_DESCRIPTION;

    private Builder(@Nonnull final String name) {
      this.name = name;
    }

    @Nonnull
    public Builder withLongDescription(@Nonnull final String longDescription) {
      this.longDescription = longDescription;
      return this;
    }

    @Nonnull
    public Builder withShortDescription(@Nonnull final String shortDescription) {
      this.shortDescription = shortDescription;
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
    public Builder withUnlockMessage(@Nonnull final String message) {
      this.unlockMessage = message;
      return this;
    }

    @Nonnull
    public Builder withOpenMessage(@Nonnull final String message) {
      this.openMessage = message;
      return this;
    }

    @Nonnull
    public Builder withUnlockAndOpenMessage(@Nonnull final String message) {
      this.unlockAndOpenMessage = message;
      return this;
    }

    @Nonnull
    public Builder withAlreadyUnlockedMessage(@Nonnull final String message) {
      this.alreadyUnlockedMessage = message;
      return this;
    }

    @Nonnull
    public Builder withUnlockNoKeyMessage(@Nonnull final String message) {
      this.unlockNoKeyMessage = message;
      return this;
    }

    @Nonnull
    public Builder withAlreadyOpenMessage(@Nonnull final String message) {
      this.alreadyOpenMessage = message;
      return this;
    }

    @Nonnull
    public Builder withOpenLockedNoKeyMessage(@Nonnull final String message) {
      this.openLockedNoKeyMessage = message;
      return this;
    }

    @Nonnull
    public Builder withUnlockedLongDescription(@Nonnull final String description) {
      this.unlockedLongDescription = description;
      return this;
    }

    @Nonnull
    public Builder withUnlockedShortDescription(@Nonnull final String description) {
      this.unlockedShortDescription = description;
      return this;
    }

    @Nonnull
    public Builder withOpenLongDescription(@Nonnull final String description) {
      this.openLongDescription = description;
      return this;
    }

    @Nonnull
    public Builder withOpenShortDescription(@Nonnull final String description) {
      this.openShortDescription = description;
      return this;
    }

    @Nonnull
    public TestOpenableLocation build() {
      return new TestOpenableLocation(this);
    }
  }
}
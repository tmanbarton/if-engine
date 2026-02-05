package io.github.tmanbarton.ifengine.test;

import io.github.tmanbarton.ifengine.OpenResult;
import io.github.tmanbarton.ifengine.OpenableItemContainer;
import io.github.tmanbarton.ifengine.UnlockResult;
import io.github.tmanbarton.ifengine.game.GameMapInterface;
import io.github.tmanbarton.ifengine.game.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Test implementation of OpenableItemContainer for unit testing.
 * Simulates a key-based lockable container (like a locked chest that holds items).
 */
public class TestOpenableItemContainer extends OpenableItemContainer {

  public static final String DEFAULT_UNLOCK_MESSAGE = "You unlock the chest.";
  public static final String DEFAULT_OPEN_MESSAGE = "You open the chest.";
  public static final String DEFAULT_ALREADY_UNLOCKED_MESSAGE = "It's already unlocked.";
  public static final String DEFAULT_UNLOCK_NO_KEY_MESSAGE = "You don't have the key.";
  public static final String DEFAULT_ALREADY_OPEN_MESSAGE = "It's already open.";
  public static final String DEFAULT_OPEN_LOCKED_MESSAGE = "It's locked.";

  private final String requiredKeyName;
  private final Set<String> targetNames;

  public TestOpenableItemContainer(@Nonnull final String name,
                                   @Nonnull final String requiredKeyName,
                                   final boolean requiresUnlocking,
                                   final int capacity,
                                   @Nonnull final Set<String> allowedItemNames,
                                   @Nonnull final List<String> prepositions) {
    super(name,
        "A " + name,
        "There is a " + name + " here.",
        "A lockable " + name + " for testing.",
        Set.of(),
        requiresUnlocking,
        capacity,
        allowedItemNames,
        prepositions);
    this.requiredKeyName = requiredKeyName;
    this.targetNames = Set.of(name);
  }

  public TestOpenableItemContainer(@Nonnull final String name,
                                   @Nonnull final String requiredKeyName,
                                   final int capacity) {
    this(name, requiredKeyName, true, capacity, Set.of(), List.of("in", "into"));
  }

  @Override
  @Nonnull
  public Set<String> getInferredTargetNames() {
    return targetNames;
  }

  @Override
  public boolean matchesUnlockTarget(@Nonnull final String name) {
    return targetNames.stream()
        .anyMatch(target -> target.equalsIgnoreCase(name));
  }

  @Override
  public boolean matchesOpenTarget(@Nonnull final String name) {
    return targetNames.stream()
        .anyMatch(target -> target.equalsIgnoreCase(name));
  }

  @Override
  @Nonnull
  public UnlockResult tryUnlock(@Nonnull final Player player,
                                @Nullable final String providedAnswer,
                                @Nonnull final GameMapInterface gameMap) {
    if (isUnlocked()) {
      return new UnlockResult(false, DEFAULT_ALREADY_UNLOCKED_MESSAGE);
    }

    if (!player.hasItem(requiredKeyName)) {
      return new UnlockResult(false, DEFAULT_UNLOCK_NO_KEY_MESSAGE);
    }

    setUnlocked(true);
    return new UnlockResult(true, DEFAULT_UNLOCK_MESSAGE);
  }

  @Override
  @Nonnull
  public OpenResult tryOpen(@Nonnull final Player player,
                            @Nullable final String providedAnswer,
                            @Nonnull final GameMapInterface gameMap) {
    if (isOpen()) {
      return new OpenResult(false, DEFAULT_ALREADY_OPEN_MESSAGE);
    }

    if (!isUnlocked()) {
      if (player.hasItem(requiredKeyName)) {
        setUnlocked(true);
        setOpen(true);
        return new OpenResult(true,
            DEFAULT_UNLOCK_MESSAGE + " " + DEFAULT_OPEN_MESSAGE);
      }
      return new OpenResult(false, DEFAULT_OPEN_LOCKED_MESSAGE);
    }

    setOpen(true);
    return new OpenResult(true, DEFAULT_OPEN_MESSAGE);
  }
}
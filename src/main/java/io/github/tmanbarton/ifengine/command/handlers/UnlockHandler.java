package io.github.tmanbarton.ifengine.command.handlers;

import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.Openable;
import io.github.tmanbarton.ifengine.OpenableItem;
import io.github.tmanbarton.ifengine.OpenableLocation;
import io.github.tmanbarton.ifengine.UnlockResult;
import io.github.tmanbarton.ifengine.command.BaseCommandHandler;
import io.github.tmanbarton.ifengine.game.GameMapInterface;
import io.github.tmanbarton.ifengine.game.GameState;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.parser.ContextManager;
import io.github.tmanbarton.ifengine.parser.ObjectResolver;
import io.github.tmanbarton.ifengine.parser.ParsedCommand;
import io.github.tmanbarton.ifengine.response.ResponseProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Handles unlock commands for unlocking locked objects.
 */
public class UnlockHandler implements BaseCommandHandler {

  private final ObjectResolver objectResolver;
  private final ContextManager contextManager;
  private final GameMapInterface gameMap;
  private final ResponseProvider responseProvider;

  public UnlockHandler(
    @Nonnull final ObjectResolver objectResolver,
    @Nonnull final ContextManager contextManager,
    @Nonnull final GameMapInterface gameMap,
    @Nonnull final ResponseProvider responseProvider
  ) {
    this.objectResolver = objectResolver;
    this.contextManager = contextManager;
    this.gameMap = gameMap;
    this.responseProvider = responseProvider;
  }

  @Override
  @Nonnull
  public List<String> getSupportedVerbs() {
    return List.of("unlock");
  }

  @Override
  @Nonnull
  public String handle(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    return handleUnlock(player, command);
  }

  @Nonnull
  private String handleUnlock(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    // Extract provided answer FIRST (before any object resolution)
    final String providedAnswer = extractProvidedAnswer(command);

    if (command.getDirectObjects().isEmpty()) {
      // Try inferred object resolution
      final ObjectResolver.ResolutionResult result = objectResolver.resolveImpliedObject("unlock", player);
      if (result.isSuccess()) {
        return unlockObject(player, result.getItem().getName(), providedAnswer);
      }
      return responseProvider.getUnlockNothingToUnlock();
    }

    final String objectName = command.getFirstDirectObject();

    // Try object resolution for pronouns like "it"
    ObjectResolver.ResolutionResult result = objectResolver.resolveObject(objectName, player);

    // Only fall back to inferred object resolution for pronouns
    if (!result.isSuccess() && contextManager.isPronoun(objectName)) {
      result = objectResolver.resolveImpliedObject("unlock", player);
      if (result.isSuccess()) {
        return unlockObject(player, result.getItem().getName(), providedAnswer);
      }
    }

    return unlockObject(player, objectName, providedAnswer);
  }

  /**
   * Extracts the provided answer (code/word) from the command.
   * Requires "with" preposition format:
   * - "unlock lockbox with 1, 2, 3, 4" -> "1, 2, 3, 4"
   * - "unlock lockbox" -> null (prompts for code)
   *
   * Note: "unlock lockbox 1234" is parsed as a single object "lockbox 1234",
   * not as object + code. Use "with" to specify codes inline.
   *
   * @param command the parsed command
   * @return the provided answer, or null if none
   */
  @Nullable
  private String extractProvidedAnswer(@Nonnull final ParsedCommand command) {
    if (command.hasIndirectObjects()) {
      return String.join(" ", command.getIndirectObjects());
    }
    return null;
  }

  @Nonnull
  private String unlockObject(@Nonnull final Player player, @Nonnull final String objectName,
                              @Nullable final String providedAnswer) {
    final Location currentLocation = player.getCurrentLocation();

    // Priority 1: Check for OpenableItem in player's inventory
    final Optional<OpenableItem> inventoryItem = findOpenableItemInInventory(player, objectName);
    if (inventoryItem.isPresent()) {
      final OpenableItem openable = inventoryItem.get();
      final UnlockResult result = openable.tryUnlock(player, providedAnswer, gameMap);
      handlePromptState(player, openable, result, providedAnswer);
      return result.message();
    }

    // Priority 2: Check for OpenableItem at current location
    final Optional<OpenableItem> locationItem = findOpenableItemAtLocation(currentLocation, objectName);
    if (locationItem.isPresent()) {
      final OpenableItem openable = locationItem.get();
      final UnlockResult result = openable.tryUnlock(player, providedAnswer, gameMap);
      handlePromptState(player, openable, result, providedAnswer);
      return result.message();
    }

    // Priority 3: Check if at an OpenableLocation and trying to unlock a valid target
    if (currentLocation instanceof OpenableLocation openable
        && openable.matchesUnlockTarget(objectName)) {
      final UnlockResult result = openable.tryUnlock(player, providedAnswer, gameMap);
      handlePromptState(player, openable, result, providedAnswer);
      return result.message();
    }

    return responseProvider.getUnlockCantUnlock(objectName);
  }

  /**
   * Handles setting the prompt state when the player needs to enter a code/word.
   * Sets the pending openable and game state when:
   * - No answer was provided (providedAnswer is null)
   * - The unlock attempt failed
   * - The object is still locked
   * - The object requires unlocking
   *
   * @param player the player
   * @param openable the openable object
   * @param result the unlock result
   * @param providedAnswer the provided answer, or null if none
   */
  private void handlePromptState(@Nonnull final Player player, @Nonnull final Openable openable,
                                 @Nonnull final UnlockResult result, @Nullable final String providedAnswer) {
    final boolean isPrompt = providedAnswer == null
        && !result.success()
        && !openable.isUnlocked()
        && openable.requiresUnlocking()
        && openable.usesCodeBasedUnlocking();

    if (isPrompt) {
      player.setPendingOpenable(openable);
      player.setGameState(GameState.WAITING_FOR_UNLOCK_CODE);
    }
  }

  @Nonnull
  private Optional<OpenableItem> findOpenableItemInInventory(@Nonnull final Player player, @Nonnull final String objectName) {
    for (final Item item : player.getInventory()) {
      if (item instanceof OpenableItem openableItem && openableItem.matchesUnlockTarget(objectName)) {
        return Optional.of(openableItem);
      }
    }
    return Optional.empty();
  }

  @Nonnull
  private Optional<OpenableItem> findOpenableItemAtLocation(@Nonnull final Location location, @Nonnull final String objectName) {
    for (final Item item : location.getItems()) {
      if (item instanceof OpenableItem openableItem && openableItem.matchesUnlockTarget(objectName)) {
        return Optional.of(openableItem);
      }
    }
    return Optional.empty();
  }
}
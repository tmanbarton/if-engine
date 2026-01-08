package com.ifengine.command.handlers;

import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.Openable;
import com.ifengine.OpenResult;
import com.ifengine.OpenableItem;
import com.ifengine.OpenableLocation;
import com.ifengine.command.BaseCommandHandler;
import com.ifengine.game.GameMapInterface;
import com.ifengine.game.GameState;
import com.ifengine.game.Player;
import com.ifengine.parser.ContextManager;
import com.ifengine.parser.ObjectResolver;
import com.ifengine.parser.ParsedCommand;
import com.ifengine.response.ResponseProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Handles open commands for opening closed objects.
 */
public class OpenHandler implements BaseCommandHandler {

  private final ObjectResolver objectResolver;
  private final ContextManager contextManager;
  private final GameMapInterface gameMap;
  private final ResponseProvider responseProvider;

  public OpenHandler(
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
    return List.of("open");
  }

  @Override
  @Nonnull
  public String handle(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    return handleOpen(player, command);
  }

  @Nonnull
  private String handleOpen(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    // Extract provided answer FIRST (before any object resolution)
    final String providedAnswer = extractProvidedAnswer(command);

    if (command.getDirectObjects().isEmpty()) {
      // Try inferred object resolution
      final ObjectResolver.ResolutionResult result = objectResolver.resolveImpliedObject("open", player);
      if (result.isSuccess()) {
        return openObject(player, result.getItem().getName(), providedAnswer);
      }
      return responseProvider.getOpenNothingToOpen();
    }

    final String objectName = command.getFirstDirectObject();

    // Try object resolution for pronouns like "it"
    ObjectResolver.ResolutionResult result = objectResolver.resolveObject(objectName, player);

    // Only fall back to inferred object resolution for pronouns
    if (!result.isSuccess() && contextManager.isPronoun(objectName)) {
      result = objectResolver.resolveImpliedObject("open", player);
      if (result.isSuccess()) {
        return openObject(player, result.getItem().getName(), providedAnswer);
      }
    }

    return openObject(player, objectName, providedAnswer);
  }

  /**
   * Extracts the provided answer (code/word) from the command.
   * Requires "with" preposition format:
   * - "open lockbox with 1, 2, 3, 4" -> "1, 2, 3, 4"
   * - "open lockbox" -> null (prompts for code)
   *
   * Note: "open lockbox 1234" is parsed as a single object "lockbox 1234",
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
  private String openObject(@Nonnull final Player player, @Nonnull final String objectName,
                            @Nullable final String providedAnswer) {
    final Location currentLocation = player.getCurrentLocation();

    // Priority 1: Check for OpenableItem in player's inventory
    final Optional<OpenableItem> inventoryItem = findOpenableItemInInventory(player, objectName);
    if (inventoryItem.isPresent()) {
      final OpenableItem openable = inventoryItem.get();
      final OpenResult result = openable.tryOpen(player, providedAnswer, gameMap);
      handlePromptState(player, openable, result, providedAnswer);
      return result.message();
    }

    // Priority 2: Check for OpenableItem at current location
    final Optional<OpenableItem> locationItem = findOpenableItemAtLocation(currentLocation, objectName);
    if (locationItem.isPresent()) {
      final OpenableItem openable = locationItem.get();
      final OpenResult result = openable.tryOpen(player, providedAnswer, gameMap);
      handlePromptState(player, openable, result, providedAnswer);
      return result.message();
    }

    // Priority 3: Check if at an OpenableLocation and trying to open a valid target
    if (currentLocation instanceof OpenableLocation openable
        && openable.matchesOpenTarget(objectName)) {
      final OpenResult result = openable.tryOpen(player, providedAnswer, gameMap);
      handlePromptState(player, openable, result, providedAnswer);
      return result.message();
    }

    return responseProvider.getOpenCantOpen(objectName);
  }

  /**
   * Handles setting the prompt state when the player needs to enter a code/word.
   * Sets the pending openable and game state when:
   * - No answer was provided (providedAnswer is null)
   * - The open attempt failed
   * - The object is still locked (not unlocked)
   * - The object is not yet open
   * - The object requires unlocking
   *
   * @param player the player
   * @param openable the openable object
   * @param result the open result
   * @param providedAnswer the provided answer, or null if none
   */
  private void handlePromptState(@Nonnull final Player player, @Nonnull final Openable openable,
                                 @Nonnull final OpenResult result, @Nullable final String providedAnswer) {
    final boolean isPrompt = providedAnswer == null
        && !result.success()
        && !openable.isUnlocked()
        && !openable.isOpen()
        && openable.requiresUnlocking()
        && openable.usesCodeBasedUnlocking();

    if (isPrompt) {
      player.setPendingOpenable(openable);
      player.setGameState(GameState.WAITING_FOR_OPEN_CODE);
    }
  }

  @Nonnull
  private Optional<OpenableItem> findOpenableItemInInventory(@Nonnull final Player player, @Nonnull final String objectName) {
    for (final Item item : player.getInventory()) {
      if (item instanceof OpenableItem openableItem && openableItem.matchesOpenTarget(objectName)) {
        return Optional.of(openableItem);
      }
    }
    return Optional.empty();
  }

  @Nonnull
  private Optional<OpenableItem> findOpenableItemAtLocation(@Nonnull final Location location, @Nonnull final String objectName) {
    for (final Item item : location.getItems()) {
      if (item instanceof OpenableItem openableItem && openableItem.matchesOpenTarget(objectName)) {
        return Optional.of(openableItem);
      }
    }
    return Optional.empty();
  }
}
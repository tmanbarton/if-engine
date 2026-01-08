package com.ifengine.command.handlers;

import com.ifengine.Container;
import com.ifengine.InteractionType;
import com.ifengine.Item;
import com.ifengine.command.BaseCommandHandler;
import com.ifengine.game.Player;
import com.ifengine.game.SceneryInteractionHandler;
import com.ifengine.parser.ContextManager;
import com.ifengine.parser.ObjectResolver;
import com.ifengine.parser.ParsedCommand;
import com.ifengine.response.ResponseProvider;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles take/get/grab commands for picking up items.
 */
public class TakeHandler implements BaseCommandHandler {

  private final ObjectResolver objectResolver;
  private final SceneryInteractionHandler sceneryHandler;
  private final ContextManager contextManager;
  private final ResponseProvider responseProvider;

  public TakeHandler(
    @Nonnull final ObjectResolver objectResolver,
    @Nonnull final SceneryInteractionHandler sceneryHandler,
    @Nonnull final ContextManager contextManager,
    @Nonnull final ResponseProvider responseProvider
  ) {
    this.objectResolver = objectResolver;
    this.sceneryHandler = sceneryHandler;
    this.contextManager = contextManager;
    this.responseProvider = responseProvider;
  }

  @Override
  @Nonnull
  public List<String> getSupportedVerbs() {
    return List.of("take", "get", "grab", "pick up");
  }

  @Override
  @Nonnull
  public String handle(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    return handleTake(player, command);
  }

  @Nonnull
  private String handleTake(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    ObjectResolver.ResolutionResult result;
    String objectName = "";

    if (command.getDirectObjects().isEmpty()) {
      // No object specified
      final List<Item> locationItems = player.getCurrentLocation().getItems();
      if (locationItems.size() == 1) {
        // Take only available item
        final Item item = locationItems.get(0);
        player.addItem(item);
        player.getCurrentLocation().removeItem(item);
        return responseProvider.getTakeSuccess();
      } else if (locationItems.size() > 1) {
        return responseProvider.getTakeNeedToSpecify();
      }

      // No items to take
      return responseProvider.getTakeNoItemsAvailable();
    } else {
      // Object specified - resolve it
      objectName = command.getFirstDirectObject();

      // Handle "take all" specially
      if ("all".equals(objectName) || "everything".equals(objectName)) {
        return handleTakeAll(player);
      }

      result = objectResolver.resolveObject(objectName, player);

      // Only fall back to inferred object resolution for pronouns
      if (!result.isSuccess() && contextManager.isPronoun(objectName)) {
        result = objectResolver.resolveImpliedObject("take", player);
      }
    }

    if (result.isSuccess()) {
      final Item item = result.getItem();

      // Check if item is in current location (can't take from inventory)
      if (!player.getCurrentLocation().getItems().contains(item)) {
        return responseProvider.getTakeAlreadyHave();
      }

      // If item was in a location container, remove it from that container
      if (player.getCurrentLocation().isItemInContainer(item)) {
        final Container locationContainer = player.getCurrentLocation().getContainerForItem(item);
        if (locationContainer != null) {
          locationContainer.removeItem(item);
        }
        player.getCurrentLocation().removeItemFromContainer(item);
      }

      player.addItem(item);
      player.getCurrentLocation().removeItem(item);

      // Clear containment state if item was in an inventory container
      if (player.isItemContained(item)) {
        final Container container = player.getContainerForItem(item);
        if (container != null) {
          container.removeItem(item);
        }
        player.removeContainment(item);
      }

      // If item is a container, also take all contained items
      if (item instanceof Container container) {
        final List<Item> containedItems = player.getContainedItems(container);
        for (final Item containedItem : containedItems) {
          if (player.getCurrentLocation().getItems().contains(containedItem)) {
            player.addItem(containedItem);
            player.getCurrentLocation().removeItem(containedItem);
          }
        }
      }

      return responseProvider.getTakeSuccess();
    } else {
      // Check for scenery interaction before giving generic message
      final Optional<String> sceneryResponse = sceneryHandler.handleInteraction(
          player, InteractionType.TAKE, objectName);
      if (sceneryResponse.isPresent()) {
        return sceneryResponse.get();
      }
      return responseProvider.getItemNotPresent(objectName);
    }
  }

  @Nonnull
  private String handleTakeAll(@Nonnull final Player player) {
    final List<Item> locationItems = new ArrayList<>(player.getCurrentLocation().getItems());

    if (locationItems.isEmpty()) {
      return responseProvider.getTakeNoItemsAvailable();
    }

    for (final Item item : locationItems) {
      player.addItem(item);
      player.getCurrentLocation().removeItem(item);
    }

    return locationItems.size() == 1
        ? responseProvider.getTakeSuccess()
        : responseProvider.getTakeAllSuccess();
  }
}
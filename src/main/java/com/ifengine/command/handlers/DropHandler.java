package com.ifengine.command.handlers;

import com.ifengine.Container;
import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.command.BaseCommandHandler;
import com.ifengine.game.Player;
import com.ifengine.parser.ContextManager;
import com.ifengine.parser.ObjectResolver;
import com.ifengine.parser.ParsedCommand;
import com.ifengine.response.ResponseProvider;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles drop commands for dropping items from inventory.
 */
public class DropHandler implements BaseCommandHandler {

  private final ObjectResolver objectResolver;
  private final ContextManager contextManager;
  private final ResponseProvider responseProvider;

  public DropHandler(
    @Nonnull final ObjectResolver objectResolver,
    @Nonnull final ContextManager contextManager,
    @Nonnull final ResponseProvider responseProvider
  ) {
    this.objectResolver = objectResolver;
    this.contextManager = contextManager;
    this.responseProvider = responseProvider;
  }

  @Override
  @Nonnull
  public List<String> getSupportedVerbs() {
    return List.of("drop", "throw", "set down");
  }

  @Override
  @Nonnull
  public String handle(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    return handleDrop(player, command);
  }

  @Nonnull
  private String handleDrop(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    ObjectResolver.ResolutionResult result;
    String objectName = "";

    if (command.getDirectObjects().isEmpty()) {
      // No object specified
      final List<Item> inventoryItems = player.getInventory();
      if (inventoryItems.size() == 1) {
        // Drop only available item
        final Item item = inventoryItems.get(0);
        final Location currentLocation = player.getCurrentLocation();
        currentLocation.addItem(item);
        player.removeItem(item);
        return responseProvider.getDropSuccess();
      } else if (inventoryItems.size() > 1) {
        return responseProvider.getDropNeedToSpecify();
      }

      // No items to drop
      return responseProvider.getDropNotCarryingAnything();
    } else {
      // Object specified - resolve it
      objectName = command.getFirstDirectObject();

      // Handle "drop all" specially
      if ("all".equals(objectName) || "everything".equals(objectName)) {
        return handleDropAll(player);
      }

      result = objectResolver.resolveObject(objectName, player);

      // Only fall back to inferred object resolution for pronouns
      if (!result.isSuccess() && contextManager.isPronoun(objectName)) {
        result = objectResolver.resolveImpliedObject("drop", player);
      }
    }

    if (result.isSuccess()) {
      final Item item = result.getItem();

      // Check if item is in inventory
      if (!player.getInventory().contains(item)) {
        return responseProvider.getDropDontHave(item.getName());
      }

      // Drop the item
      player.removeItem(item);
      player.getCurrentLocation().addItem(item);

      // Clear containment state if item was in a container
      if (player.isItemContained(item)) {
        final Container container = player.getContainerForItem(item);
        if (container != null) {
          container.removeItem(item);
        }
        player.removeContainment(item);
      }

      // If item is a container, move all contained items to location
      if (item instanceof Container container) {
        final List<Item> containedItems = player.getContainedItems(container);
        for (final Item containedItem : containedItems) {
          if (player.getInventory().contains(containedItem)) {
            player.removeItem(containedItem);
            player.getCurrentLocation().addItem(containedItem);
          }
        }
      }

      return responseProvider.getDropSuccess();
    } else {
      return responseProvider.getDropDontHave(objectName);
    }
  }

  @Nonnull
  private String handleDropAll(@Nonnull final Player player) {
    final List<Item> inventoryItems = new ArrayList<>(player.getInventory());

    if (inventoryItems.isEmpty()) {
      return responseProvider.getDropNotCarryingAnything();
    }

    for (final Item item : inventoryItems) {
      player.removeItem(item);
      player.getCurrentLocation().addItem(item);
    }

    return inventoryItems.size() == 1
        ? responseProvider.getDropSuccess()
        : responseProvider.getDropAllSuccess();
  }
}
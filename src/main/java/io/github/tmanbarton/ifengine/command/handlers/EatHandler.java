package io.github.tmanbarton.ifengine.command.handlers;

import io.github.tmanbarton.ifengine.InteractionType;
import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.command.BaseCommandHandler;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.game.SceneryInteractionHandler;
import io.github.tmanbarton.ifengine.parser.ContextManager;
import io.github.tmanbarton.ifengine.parser.ParsedCommand;
import io.github.tmanbarton.ifengine.response.ResponseProvider;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Handles eat commands for consuming edible items.
 */
public class EatHandler implements BaseCommandHandler {

  /**
   * Set of item names that can be eaten.
   * Games should add edible item names to this set.
   */
  public static final Set<String> EDIBLE_ITEMS = new HashSet<>();

  private final SceneryInteractionHandler sceneryHandler;
  private final ResponseProvider responseProvider;

  public EatHandler(@Nonnull final SceneryInteractionHandler sceneryHandler, @Nonnull final ResponseProvider responseProvider) {
    this.sceneryHandler = sceneryHandler;
    this.responseProvider = responseProvider;
  }

  @Nonnull
  @Override
  public List<String> getSupportedVerbs() {
    return List.of("eat");
  }

  @Nonnull
  @Override
  public String handle(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    if (command.getDirectObjects().isEmpty()) {
      return handleEatWithoutObject(player);
    } else {
      return handleEatWithObject(player, command.getFirstDirectObject());
    }
  }

  @Nonnull
  private String handleEatWithoutObject(@Nonnull final Player player) {
    final List<Item> inventoryEdibleItems = player.getInventory().stream()
      .filter(item -> EDIBLE_ITEMS.contains(item.getName()))
      .toList();

    if (!inventoryEdibleItems.isEmpty()) {
      if (inventoryEdibleItems.size() == 1) {
        final Item item = inventoryEdibleItems.get(0);
        return eatItem(player, item, true);
      } else {
        return responseProvider.getEatWhat();
      }
    }

    final List<Item> locationEdibleItems = player.getCurrentLocation().getItems().stream()
      .filter(item -> EDIBLE_ITEMS.contains(item.getName()))
      .toList();

    if (locationEdibleItems.isEmpty()) {
      return responseProvider.getEatNothingAvailable();
    } else if (locationEdibleItems.size() == 1) {
      final Item item = locationEdibleItems.get(0);
      return eatItem(player, item, false);
    } else {
      return responseProvider.getEatWhat();
    }
  }

  @Nonnull
  private String handleEatWithObject(@Nonnull final Player player, @Nonnull final String itemName) {
    // Handle the pronoun "it" by falling back to implicit resolution
    if ("it".equals(itemName)) {
      return handleEatWithoutObject(player);
    }

    Item item = player.getInventoryItemByName(itemName);
    final boolean fromInventory;

    if (item != null) {
      fromInventory = true;
    } else {
      item = player.getCurrentLocation().getItems().stream()
        .filter(locationItem -> locationItem.getName().equalsIgnoreCase(itemName))
        .findFirst()
        .orElse(null);
      fromInventory = false;
    }

    if (item == null) {
      // No real item found - check for scenery with EAT interaction
      final Optional<String> sceneryResponse = sceneryHandler.handleInteraction(
          player, InteractionType.EAT, itemName);
      if (sceneryResponse.isPresent()) {
        return sceneryResponse.get();
      }
      // Check if scenery exists but doesn't support eating
      if (player.getCurrentLocation().findSceneryObject(itemName).isPresent()) {
        return responseProvider.getEatNotEdible();
      }
      return responseProvider.getEatDontHave(itemName);
    }

    if (!EDIBLE_ITEMS.contains(item.getName())) {
      return responseProvider.getEatNotEdible();
    }

    return eatItem(player, item, fromInventory);
  }

  @Nonnull
  private String eatItem(
      @Nonnull final Player player,
      @Nonnull final Item item,
      final boolean fromInventory
  ) {
    if (fromInventory) {
      player.removeItem(item);
    } else {
      player.getCurrentLocation().removeItem(item);
    }

    return responseProvider.getEatSuccess();
  }
}
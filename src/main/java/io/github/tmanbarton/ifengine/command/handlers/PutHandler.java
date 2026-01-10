package io.github.tmanbarton.ifengine.command.handlers;

import io.github.tmanbarton.ifengine.Container;
import io.github.tmanbarton.ifengine.ContainerType;
import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.SceneryContainer;
import io.github.tmanbarton.ifengine.command.BaseCommandHandler;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.parser.ObjectResolver;
import io.github.tmanbarton.ifengine.parser.ParsedCommand;
import io.github.tmanbarton.ifengine.response.ResponseProvider;
import io.github.tmanbarton.ifengine.util.ContainerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static io.github.tmanbarton.ifengine.constants.PrepositionConstants.IN;
import static io.github.tmanbarton.ifengine.constants.PrepositionConstants.INTO;
import static io.github.tmanbarton.ifengine.constants.PrepositionConstants.ON;
import static io.github.tmanbarton.ifengine.constants.PrepositionConstants.ONTO;

/**
 * Handles put commands for placing items into containers.
 */
public class PutHandler implements BaseCommandHandler {

  private final ObjectResolver objectResolver;
  private final ResponseProvider responseProvider;

  public PutHandler(
    @Nonnull final ObjectResolver objectResolver,
    @Nonnull final ResponseProvider responseProvider
  ) {
    this.objectResolver = objectResolver;
    this.responseProvider = responseProvider;
  }

  @Override
  @Nonnull
  public List<String> getSupportedVerbs() {
    return List.of("put", "place", "insert");
  }

  @Override
  @Nonnull
  public String handle(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    return handlePut(player, command);
  }

  @Nonnull
  private String handlePut(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    // Check if item is specified
    if (command.getDirectObjects().isEmpty()) {
      return responseProvider.getPutWhat();
    }

    final String itemName = command.getFirstDirectObject();

    // Check if container is specified
    if (command.getIndirectObjects().isEmpty()) {
      return responseProvider.getPutWhere(itemName);
    }

    final String containerName = command.getFirstIndirectObject();
    final String preposition = command.getPreposition();

    // Check if preposition is present
    if (preposition == null || preposition.isEmpty()) {
      return responseProvider.getPutMissingPreposition(itemName);
    }

    // Resolve container first to determine type
    final Container container = findContainer(containerName, player);

    // Validate preposition
    if (!isSupportedPreposition(preposition)) {
      return responseProvider.getPutUnsupportedPreposition(preposition);
    }
    if (container != null && !isValidPrepositionForContainer(container, preposition)) {
      return responseProvider.getPutInvalidPreposition(
          containerName, container.getPreferredPrepositions().getFirst());
    }

    if (container == null) {
      // Check if it's a scenery object that exists but isn't a container
      final java.util.Optional<io.github.tmanbarton.ifengine.SceneryObject> sceneryObject =
          player.getCurrentLocation().findSceneryObject(containerName);
      if (sceneryObject.isPresent()) {
        return responseProvider.getPutNotAContainer(containerName);
      }
      return responseProvider.getPutContainerNotFound(containerName);
    }

    // Find item
    final Item item = findItemForContainer(itemName, player);

    if (item == null) {
      return responseProvider.getPutItemNotPresent(itemName);
    }

    // Check if container accepts this item
    if (!container.canAccept(item)) {
      return responseProvider.getPutItemNotAccepted(containerName, itemName);
    }

    // Check if container is full
    if (container.isFull()) {
      return responseProvider.getPutContainerFull(containerName);
    }

    // Check for circular containment
    if (item instanceof Container && wouldCreateCircularReference((Container) item, container, player)) {
      return responseProvider.getPutCircularContainment();
    }

    // Remove item from previous container if any
    if (player.isItemContained(item)) {
      final Container oldContainer = player.getContainerForItem(item);
      player.removeContainment(item);
      if (oldContainer != null) {
        oldContainer.removeItem(item);
      }
    }

    if (player.getCurrentLocation().isItemInContainer(item)) {
      final SceneryContainer oldLocationContainer = player.getCurrentLocation().getContainerForItem(item);
      if (oldLocationContainer != null) {
        oldLocationContainer.removeItem(item);
      }
      player.getCurrentLocation().removeItemFromContainer(item);
    }

    // Attempt to insert item
    final boolean success = container.insertItem(item);

    if (!success) {
      return responseProvider.getPutFailed(itemName, containerName);
    }

    // Handle item placement based on container type
    final ContainerType containerType = ContainerHelper.getContainerType(container);

    if (containerType == ContainerType.INVENTORY) {
      handleInventoryContainerInsertion(player, item, container);
    } else {
      handleLocationContainerInsertion(player, item, (SceneryContainer) container);
    }

    return responseProvider.getPutSuccess(itemName, preposition, containerName);
  }

  private boolean isSupportedPreposition(@Nonnull final String preposition) {
    return IN.equals(preposition) || INTO.equals(preposition)
        || ON.equals(preposition) || ONTO.equals(preposition);
  }

  private boolean isValidPrepositionForContainer(
      @Nonnull final Container container,
      @Nonnull final String preposition
  ) {
    return container.getPreferredPrepositions().contains(preposition);
  }

  @Nullable
  private Container findContainer(@Nonnull final String containerName, @Nonnull final Player player) {
    // Check inventory for inventory containers first
    for (final Item item : player.getInventory()) {
      if (item instanceof Container && item.matchesName(containerName)) {
        return (Container) item;
      }
    }

    // Check location items for inventory containers
    for (final Item item : player.getCurrentLocation().getItems()) {
      if (item instanceof Container && item.matchesName(containerName)) {
        return (Container) item;
      }
    }

    // Check scenery containers at location
    final Location location = player.getCurrentLocation();
    for (final SceneryContainer sceneryContainer : location.getSceneryContainers()) {
      if (sceneryContainer.getSceneryObject().matches(containerName)) {
        return sceneryContainer;
      }
    }

    return null;
  }

  @Nullable
  private Item findItemForContainer(@Nonnull final String itemName, @Nonnull final Player player) {
    // Check inventory first
    for (final Item item : player.getInventory()) {
      if (item.matchesName(itemName)) {
        return item;
      }
    }

    // Also check location
    for (final Item item : player.getCurrentLocation().getItems()) {
      if (item.matchesName(itemName)) {
        return item;
      }
    }

    return null;
  }

  private boolean wouldCreateCircularReference(
      @Nonnull final Container itemContainer,
      @Nonnull final Container targetContainer,
      @Nonnull final Player player
  ) {
    if (itemContainer.equals(targetContainer)) {
      return true;
    }
    return isContainerInside(targetContainer, itemContainer, player);
  }

  private boolean isContainerInside(
      @Nonnull final Container searchContainer,
      @Nonnull final Container parentContainer,
      @Nonnull final Player player
  ) {
    final List<Item> containedItems = player.getContainedItems(parentContainer);

    for (final Item containedItem : containedItems) {
      if (containedItem.equals(searchContainer)) {
        return true;
      }
      if (containedItem instanceof Container nestedContainer) {
        if (isContainerInside(searchContainer, nestedContainer, player)) {
          return true;
        }
      }
    }

    return false;
  }

  private void handleInventoryContainerInsertion(
      @Nonnull final Player player,
      @Nonnull final Item item,
      @Nonnull final Container container
  ) {
    final Item containerItem = (Item) container;
    final boolean containerInInventory = player.getInventory().contains(containerItem);
    final boolean itemInInventory = player.getInventory().contains(item);
    final boolean itemAtLocation = player.getCurrentLocation().getItems().contains(item);

    if (containerInInventory && itemAtLocation) {
      player.getCurrentLocation().removeItem(item);
      player.addItem(item);
    } else if (!containerInInventory && itemInInventory) {
      player.removeItem(item);
      player.getCurrentLocation().addItem(item);
    }

    player.markItemAsContained(item, container);
  }

  private void handleLocationContainerInsertion(
      @Nonnull final Player player,
      @Nonnull final Item item,
      @Nonnull final SceneryContainer sceneryContainer
  ) {
    final boolean itemInInventory = player.getInventory().contains(item);

    if (itemInInventory) {
      player.removeItem(item);
      player.getCurrentLocation().addItem(item);
    }

    player.getCurrentLocation().setItemContainer(item, sceneryContainer);

    // If item is a container with items, move contained items too
    if (item instanceof Container itemContainer) {
      final List<Item> containedItems = player.getContainedItems(itemContainer);
      for (final Item containedItem : containedItems) {
        if (player.getInventory().contains(containedItem)) {
          player.removeItem(containedItem);
          player.getCurrentLocation().addItem(containedItem);
        }
      }
    }
  }
}
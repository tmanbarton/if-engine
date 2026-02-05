package io.github.tmanbarton.ifengine.util;

import io.github.tmanbarton.ifengine.Container;
import io.github.tmanbarton.ifengine.ContainerType;
import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.LocationContainer;
import io.github.tmanbarton.ifengine.Openable;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.response.ResponseProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.github.tmanbarton.ifengine.constants.PrepositionConstants.IN;
import static io.github.tmanbarton.ifengine.constants.PrepositionConstants.INTO;
import static io.github.tmanbarton.ifengine.constants.PrepositionConstants.ON;
import static io.github.tmanbarton.ifengine.constants.PrepositionConstants.ONTO;

/**
 * Utility class for container operations.
 * <p>
 * Provides reusable logic for putting items into containers, used by both
 * {@code PutHandler} and {@code CommandContext.putItemInContainer()}.
 */
public final class ContainerOperations {

  private ContainerOperations() {
    // Utility class
  }

  /**
   * Puts an item into a container, handling all state management.
   *
   * @param player the player performing the action
   * @param itemName the name of the item to put
   * @param containerName the name of the container
   * @param preposition the preposition (in, on, into, onto)
   * @param responses the response provider for messages
   * @return a response message (success or error)
   */
  @Nonnull
  public static String putItemInContainer(
      @Nonnull final Player player,
      @Nonnull final String itemName,
      @Nonnull final String containerName,
      @Nonnull final String preposition,
      @Nonnull final ResponseProvider responses
  ) {
    Objects.requireNonNull(player, "player cannot be null");
    Objects.requireNonNull(itemName, "itemName cannot be null");
    Objects.requireNonNull(containerName, "containerName cannot be null");
    Objects.requireNonNull(preposition, "preposition cannot be null");
    Objects.requireNonNull(responses, "responses cannot be null");

    // Validate preposition is supported
    if (!isSupportedPreposition(preposition)) {
      return responses.getPutUnsupportedPreposition(preposition);
    }

    // Find container first to determine type
    final Container container = findContainer(containerName, player);
    if (container == null) {
      // Check if it's a scenery object that exists but isn't a container
      final Optional<io.github.tmanbarton.ifengine.SceneryObject> sceneryObject =
          player.getCurrentLocation().findSceneryObject(containerName);
      if (sceneryObject.isPresent()) {
        return responses.getPutNotAContainer(containerName);
      }
      return responses.getPutContainerNotFound(containerName);
    }

    // Check if container is an openable that is currently closed
    if (container instanceof Openable openable && !openable.isOpen()) {
      return responses.getPutContainerClosed(containerName);
    }

    // Validate preposition for this container
    if (!isValidPrepositionForContainer(container, preposition)) {
      return responses.getPutInvalidPreposition(
          containerName, container.getPreferredPrepositions().getFirst());
    }

    // Find item
    final Item item = findItem(itemName, player);
    if (item == null) {
      return responses.getPutItemNotPresent(itemName);
    }

    // Check if container accepts this item
    if (!container.canAccept(item)) {
      return responses.getPutItemNotAccepted(containerName, itemName);
    }

    // Check if container is full
    if (container.isFull()) {
      return responses.getPutContainerFull(containerName);
    }

    // Check for circular containment
    if (item instanceof Container && wouldCreateCircularReference((Container) item, container, player)) {
      return responses.getPutCircularContainment();
    }

    // Remove item from previous container if any
    removeFromPreviousContainer(item, player);

    // Attempt to insert item
    final boolean success = container.insertItem(item);
    if (!success) {
      return responses.getPutFailed(itemName, containerName);
    }

    // Handle item placement based on container type
    final ContainerType containerType = ContainerHelper.getContainerType(container);
    if (containerType == ContainerType.INVENTORY) {
      handleInventoryContainerInsertion(item, container, player);
    } else {
      handleLocationContainerInsertion(item, (LocationContainer) container, player);
    }

    return responses.getPutSuccess(itemName, preposition, containerName);
  }

  /**
   * Checks if a preposition is supported for put operations.
   */
  public static boolean isSupportedPreposition(@Nonnull final String preposition) {
    return IN.equals(preposition) || INTO.equals(preposition)
        || ON.equals(preposition) || ONTO.equals(preposition);
  }

  /**
   * Checks if a preposition is valid for a specific container.
   */
  public static boolean isValidPrepositionForContainer(
      @Nonnull final Container container,
      @Nonnull final String preposition
  ) {
    return container.getPreferredPrepositions().contains(preposition);
  }

  /**
   * Finds a container by name, searching inventory, location items, and scenery.
   */
  @Nullable
  public static Container findContainer(@Nonnull final String containerName, @Nonnull final Player player) {
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
    for (final LocationContainer locationContainer : location.getLocationContainers()) {
      if (locationContainer.getSceneryObject().matches(containerName)) {
        return locationContainer;
      }
    }

    return null;
  }

  /**
   * Finds an item by name, searching inventory and location.
   */
  @Nullable
  public static Item findItem(@Nonnull final String itemName, @Nonnull final Player player) {
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

  /**
   * Checks if placing itemContainer into targetContainer would create a circular reference.
   */
  public static boolean wouldCreateCircularReference(
      @Nonnull final Container itemContainer,
      @Nonnull final Container targetContainer,
      @Nonnull final Player player
  ) {
    if (itemContainer.equals(targetContainer)) {
      return true;
    }
    return isContainerInside(targetContainer, itemContainer, player);
  }

  private static boolean isContainerInside(
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

  /**
   * Removes an item from its previous container if any.
   */
  public static void removeFromPreviousContainer(@Nonnull final Item item, @Nonnull final Player player) {
    if (player.isItemContained(item)) {
      final Container oldContainer = player.getContainerForItem(item);
      player.removeContainment(item);
      if (oldContainer != null) {
        oldContainer.removeItem(item);
      }
    }

    if (player.getCurrentLocation().isItemInContainer(item)) {
      final LocationContainer oldLocationContainer = player.getCurrentLocation().getContainerForItem(item);
      if (oldLocationContainer != null) {
        oldLocationContainer.removeItem(item);
      }
      player.getCurrentLocation().removeItemFromContainer(item);
    }
  }

  /**
   * Handles insertion into an inventory container (bag, box, etc.).
   */
  public static void handleInventoryContainerInsertion(
      @Nonnull final Item item,
      @Nonnull final Container container,
      @Nonnull final Player player
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

  /**
   * Handles insertion into a location container (table, shelf, etc.).
   */
  public static void handleLocationContainerInsertion(
      @Nonnull final Item item,
      @Nonnull final LocationContainer locationContainer,
      @Nonnull final Player player
  ) {
    final boolean itemInInventory = player.getInventory().contains(item);

    if (itemInInventory) {
      player.removeItem(item);
      player.getCurrentLocation().addItem(item);
    }

    player.getCurrentLocation().setItemContainer(item, locationContainer);

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

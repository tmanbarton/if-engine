package com.ifengine.game;

import com.ifengine.Container;
import com.ifengine.Item;
import com.ifengine.SceneryContainer;
import com.ifengine.util.ContainerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralized manager for container state tracking.
 * Manages both inventory container containment (bags, jars) and location container
 * containment (tables, desks, shelves).
 *
 * <p>This manager provides a unified API for container operations while maintaining
 * the correct state ownership:
 * <ul>
 *   <li>Inventory containment: Owned by this manager (per-player state)</li>
 *   <li>Location containment: Delegated to Location instances (shared state)</li>
 * </ul>
 */
public class ContainerStateManager {

  private final Map<Item, Container> inventoryContainment;

  public ContainerStateManager() {
    this.inventoryContainment = new HashMap<>();
  }

  /**
   * Marks an item as contained within a container.
   * Handles both inventory containers and location containers based on container type.
   *
   * @param item the item being placed in the container
   * @param container the container holding the item
   * @param player the player context (needed for location container access)
   */
  public void markItemAsContained(@Nonnull final Item item, @Nonnull final Container container,
                                  @Nonnull final Player player) {
    if (ContainerHelper.isInventoryContainer(container)) {
      inventoryContainment.put(item, container);
    } else if (ContainerHelper.isLocationContainer(container)) {
      player.getCurrentLocation().setItemContainer(item, (SceneryContainer) container);
    }
  }

  /**
   * Removes containment tracking for an item from both inventory and location containers.
   * Checks both types of containment and removes from whichever applies.
   *
   * @param item the item to remove from containment tracking
   * @param player the player context (needed for location container access)
   */
  public void removeContainment(@Nonnull final Item item, @Nonnull final Player player) {
    // Remove from inventory containment
    inventoryContainment.remove(item);

    // Remove from location containment
    player.getCurrentLocation().removeItemFromContainer(item);
  }

  /**
   * Gets the container holding the specified item, searching both inventory and location containers.
   *
   * @param item the item to look up
   * @param player the player context (needed for location container access)
   * @return the container holding the item, or null if not contained
   */
  @Nullable
  public Container getContainerForItem(@Nonnull final Item item, @Nonnull final Player player) {
    // Check inventory containment first
    final Container inventoryContainer = inventoryContainment.get(item);
    if (inventoryContainer != null) {
      return inventoryContainer;
    }

    // Check location containment
    return player.getCurrentLocation().getContainerForItem(item);
  }

  /**
   * Checks if an item is contained in any container (inventory or location).
   *
   * @param item the item to check
   * @param player the player context (needed for location container access)
   * @return true if the item is in any container, false otherwise
   */
  public boolean isItemContained(@Nonnull final Item item, @Nonnull final Player player) {
    return inventoryContainment.containsKey(item) || player.getCurrentLocation().isItemInContainer(item);
  }

  /**
   * Gets all items contained within the specified container.
   * Works for both inventory containers and scenery containers.
   *
   * @param container the container to query
   * @param player the player context (needed for location container access)
   * @return list of items in the container
   */
  @Nonnull
  public List<Item> getContainedItems(@Nonnull final Container container, @Nonnull final Player player) {
    if (ContainerHelper.isInventoryContainer(container)) {
      // Search inventory containment map
      return inventoryContainment.entrySet().stream()
          .filter(entry -> entry.getValue().equals(container))
          .map(Map.Entry::getKey)
          .collect(Collectors.toList());
    } else {
      // Search location items for those in this scenery container
      return player.getCurrentLocation().getItems().stream()
          .filter(item -> {
            final SceneryContainer itemContainer = player.getCurrentLocation().getContainerForItem(item);
            return container.equals(itemContainer);
          })
          .collect(Collectors.toList());
    }
  }

  /**
   * Clears all inventory containment state.
   * Used when resetting the game. Location containment is cleared via Location.reset().
   */
  public void clearAllInventoryContainment() {
    inventoryContainment.clear();
  }
}
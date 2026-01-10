package io.github.tmanbarton.ifengine;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Interface for items that can contain other items.
 * Containers track which items have been inserted and enforce validation rules
 * for what can be placed inside them.
 */
public interface Container {

  /**
   * Checks if this container can accept the given item.
   *
   * @param item the item to check
   * @return true if the item can be inserted, false otherwise
   */
  boolean canAccept(@Nonnull Item item);

  /**
   * Inserts an item into this container.
   *
   * @param item the item to insert
   * @return true if insertion succeeded, false if item was already inserted or not allowed
   */
  boolean insertItem(@Nonnull Item item);

  /**
   * Removes an item from this container.
   *
   * @param item the item to remove
   * @return true if removal succeeded, false if item was not in container
   */
  boolean removeItem(@Nonnull Item item);

  /**
   * Checks if an item has already been inserted.
   *
   * @param itemName the name of the item to check
   * @return true if the item is currently in this container
   */
  boolean containsItem(@Nonnull String itemName);

  /**
   * Gets the set of names of all items currently in this container.
   *
   * @return immutable set of item names currently inserted
   */
  @Nonnull
  Set<String> getInsertedItemNames();

  /**
   * Gets the maximum capacity of this container.
   *
   * @return the maximum number of items this container can hold
   */
  int getCapacity();

  /**
   * Gets the current number of items in this container.
   *
   * @return the number of items currently inserted
   */
  int getCurrentCount();

  /**
   * Checks if this container is full.
   *
   * @return true if container is at maximum capacity
   */
  boolean isFull();

  /**
   * Gets a description of the container's current state.
   *
   * @return a human-readable description of container state
   */
  @Nonnull
  String getContainerStateDescription();

  /**
   * Gets the preposition that should be used with this container.
   *
   * @return the preferred prepositions for this container
   */
  @Nonnull
  List<String> getPreferredPrepositions();

  /**
   * Gets the type of this container (INVENTORY or LOCATION).
   *
   * @return the container type
   */
  @Nonnull
  default ContainerType getContainerType() {
    return ContainerType.INVENTORY;
  }
}
package io.github.tmanbarton.ifengine;

import io.github.tmanbarton.ifengine.constants.PrepositionConstants;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A location container implemented as a scenery object adapter.
 * When items are inserted into a scenery container, they are placed at the location
 * (not kept in inventory).
 * Examples: table, desk, shelf, counter
 *
 * Scenery containers have unlimited capacity (getCapacity() returns 0).
 */
public class LocationContainer implements Container {

  private final SceneryObject sceneryObject;
  private final Set<String> allowedItemNames;
  private final Set<String> insertedItemNames;

  /**
   * Creates a scenery container that wraps a scenery object.
   *
   * @param sceneryObject the scenery object this container represents
   * @param allowedItemNames names of items that can be placed on/in this container
   */
  public LocationContainer(@Nonnull final SceneryObject sceneryObject, @Nonnull final Set<String> allowedItemNames) {
    this.sceneryObject = sceneryObject;
    this.allowedItemNames = Set.copyOf(allowedItemNames);
    this.insertedItemNames = new HashSet<>();
  }

  /**
   * Gets the wrapped scenery object.
   *
   * @return the scenery object
   */
  @Nonnull
  public SceneryObject getSceneryObject() {
    return sceneryObject;
  }

  @Override
  @Nonnull
  public ContainerType getContainerType() {
    return ContainerType.LOCATION;
  }

  @Override
  public boolean canAccept(@Nonnull final Item item) {
    return allowedItemNames.isEmpty() || allowedItemNames.contains(item.getName());
  }

  @Override
  public boolean insertItem(@Nonnull final Item item) {
    if (!canAccept(item)) {
      return false;
    }

    if (containsItem(item.getName())) {
      return false;
    }

    if (isFull()) {
      return false;
    }

    insertedItemNames.add(item.getName());
    return true;
  }

  @Override
  public boolean removeItem(@Nonnull final Item item) {
    return insertedItemNames.remove(item.getName());
  }

  @Override
  public boolean containsItem(@Nonnull final String itemName) {
    return insertedItemNames.contains(itemName);
  }

  @Override
  @Nonnull
  public Set<String> getInsertedItemNames() {
    return Set.copyOf(insertedItemNames);
  }

  /**
   * Gets the maximum capacity of this container.
   * Scenery containers have unlimited capacity.
   *
   * @return 0, which represents unlimited capacity
   */
  @Override
  public int getCapacity() {
    return 0;
  }

  @Override
  public int getCurrentCount() {
    return insertedItemNames.size();
  }

  /**
   * Checks if this container is full.
   * Since scenery containers have unlimited capacity, this method always returns false.
   *
   * @return false, since scenery containers never reach capacity
   */
  @Override
  public boolean isFull() {
    return false;
  }

  @Override
  @Nonnull
  public String getContainerStateDescription() {
    final int count = getCurrentCount();
    final int cap = getCapacity();

    if (count == 0) {
      return String.format("The %s is empty.", sceneryObject.name());
    } else if (count == cap) {
      return String.format("The %s is full with %d items.", sceneryObject.name(), cap);
    } else {
      return String.format("The %s has %d of %d items on it.", sceneryObject.name(), count, cap);
    }
  }

  @Override
  @Nonnull
  public List<String> getPreferredPrepositions() {
    return List.of(PrepositionConstants.ON, PrepositionConstants.ONTO);
  }
}
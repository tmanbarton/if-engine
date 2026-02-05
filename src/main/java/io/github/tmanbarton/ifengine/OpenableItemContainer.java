package io.github.tmanbarton.ifengine;

import io.github.tmanbarton.ifengine.constants.PrepositionConstants;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract base class for openable items that can also contain other items.
 * Combines {@link OpenableItem} (unlock/open state) with {@link Container} (hold items).
 *
 * <p>Items can only be inserted when the container is open. The {@link #canAccept(Item)}
 * method checks open state before delegating to capacity and allowed-item checks.
 *
 * <p>Subclasses must implement the five abstract {@link Openable} methods:
 * <ul>
 *   <li>{@link #getInferredTargetNames()}</li>
 *   <li>{@link #matchesUnlockTarget(String)}</li>
 *   <li>{@link #matchesOpenTarget(String)}</li>
 *   <li>{@link #tryUnlock(io.github.tmanbarton.ifengine.game.Player, String, io.github.tmanbarton.ifengine.game.GameMapInterface)}</li>
 *   <li>{@link #tryOpen(io.github.tmanbarton.ifengine.game.Player, String, io.github.tmanbarton.ifengine.game.GameMapInterface)}</li>
 * </ul>
 */
public abstract class OpenableItemContainer extends OpenableItem implements Container {

  private final int capacity;
  private final Set<String> allowedItemNames;
  private final List<String> preferredPrepositions;
  private final Set<String> insertedItemNames;

  /**
   * Constructs an OpenableItemContainer with all properties.
   *
   * @param name the item name
   * @param inventoryDescription the description when in inventory
   * @param locationDescription the description when at a location
   * @param detailedDescription the detailed description for examine/look
   * @param aliases alternative names for the item
   * @param requiresUnlocking true if the item starts locked
   * @param capacity maximum items (0 = unlimited)
   * @param allowedItemNames names of items that can be inserted (empty = any)
   * @param preferredPrepositions prepositions for put commands
   */
  protected OpenableItemContainer(@Nonnull final String name,
                                  @Nonnull final String inventoryDescription,
                                  @Nonnull final String locationDescription,
                                  @Nonnull final String detailedDescription,
                                  @Nonnull final Set<String> aliases,
                                  final boolean requiresUnlocking,
                                  final int capacity,
                                  @Nonnull final Set<String> allowedItemNames,
                                  @Nonnull final List<String> preferredPrepositions) {
    super(name, inventoryDescription, locationDescription, detailedDescription,
        aliases, requiresUnlocking);
    this.capacity = capacity;
    this.allowedItemNames = Set.copyOf(allowedItemNames);
    this.preferredPrepositions = List.copyOf(preferredPrepositions);
    this.insertedItemNames = new HashSet<>();
  }

  @Override
  public boolean canAccept(@Nonnull final Item item) {
    if (!isOpen()) {
      return false;
    }
    if (isFull()) {
      return false;
    }
    if (allowedItemNames.isEmpty()) {
      return true;
    }
    return allowedItemNames.contains(item.getName().toLowerCase());
  }

  @Override
  public boolean insertItem(@Nonnull final Item item) {
    if (!canAccept(item)) {
      return false;
    }
    return insertedItemNames.add(item.getName().toLowerCase());
  }

  @Override
  public boolean removeItem(@Nonnull final Item item) {
    return insertedItemNames.remove(item.getName().toLowerCase());
  }

  @Override
  public boolean containsItem(@Nonnull final String itemName) {
    return insertedItemNames.contains(itemName.toLowerCase());
  }

  @Override
  @Nonnull
  public Set<String> getInsertedItemNames() {
    return Set.copyOf(insertedItemNames);
  }

  @Override
  public int getCapacity() {
    return capacity;
  }

  @Override
  public int getCurrentCount() {
    return insertedItemNames.size();
  }

  @Override
  public boolean isFull() {
    return capacity > 0 && insertedItemNames.size() >= capacity;
  }

  @Override
  @Nonnull
  public String getContainerStateDescription() {
    if (insertedItemNames.isEmpty()) {
      return String.format("The %s is empty.", getName());
    }
    return String.format("The %s contains: %s", getName(), String.join(", ", insertedItemNames));
  }

  @Override
  @Nonnull
  public List<String> getPreferredPrepositions() {
    return preferredPrepositions;
  }

  @Override
  @Nonnull
  public ContainerType getContainerType() {
    return ContainerType.INVENTORY;
  }
}
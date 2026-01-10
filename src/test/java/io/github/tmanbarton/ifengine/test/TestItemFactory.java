package io.github.tmanbarton.ifengine.test;

import io.github.tmanbarton.ifengine.Container;
import io.github.tmanbarton.ifengine.ContainerType;
import io.github.tmanbarton.ifengine.InteractionType;
import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.SceneryContainer;
import io.github.tmanbarton.ifengine.SceneryObject;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Factory for creating test items without production dependencies.
 * <p>
 * Provides methods to create various types of items for testing:
 * <ul>
 *   <li>Simple items with auto-generated descriptions</li>
 *   <li>Named items with custom descriptions</li>
 *   <li>Pre-built test items (key, rope, gem)</li>
 *   <li>Container items (inventory and scenery containers)</li>
 * </ul>
 */
public final class TestItemFactory {

  private TestItemFactory() {
    // Private constructor - static factory only
  }

  /**
   * Creates a simple test item with auto-generated descriptions.
   *
   * @param name the item name
   * @return a new test item
   */
  @Nonnull
  public static Item createSimpleItem(@Nonnull final String name) {
    return new Item(
        name,
        "A " + name,
        "There is a " + name + " here.",
        "A simple test " + name + "."
    );
  }

  /**
   * Creates a test item with custom descriptions.
   *
   * @param name the item name
   * @param inventoryDescription the description shown in inventory
   * @param locationDescription the description when at a location
   * @param detailedDescription the detailed description when examined
   * @return a new test item
   */
  @Nonnull
  public static Item createItem(@Nonnull final String name,
                                @Nonnull final String inventoryDescription,
                                @Nonnull final String locationDescription,
                                @Nonnull final String detailedDescription) {
    return new Item(name, inventoryDescription, locationDescription, detailedDescription);
  }

  /**
   * Creates a test item with aliases.
   *
   * @param name the item name
   * @param inventoryDescription the description shown in inventory
   * @param locationDescription the description when at a location
   * @param detailedDescription the detailed description when examined
   * @param aliases alternate names for the item
   * @return a new test item
   */
  @Nonnull
  public static Item createItemWithAliases(@Nonnull final String name,
                                           @Nonnull final String inventoryDescription,
                                           @Nonnull final String locationDescription,
                                           @Nonnull final String detailedDescription,
                                           @Nonnull final Set<String> aliases) {
    return new Item(name, inventoryDescription, locationDescription, detailedDescription, aliases);
  }

  /**
   * Creates a standard test key.
   *
   * @return a new test key item
   */
  @Nonnull
  public static Item createTestKey() {
    return new Item(
        "key",
        "A simple test key",
        "There is a key here.",
        "A brass key for testing lock mechanics."
    );
  }

  /**
   * Creates a standard test rope.
   *
   * @return a new test rope item
   */
  @Nonnull
  public static Item createTestRope() {
    return new Item(
        "rope",
        "A coiled test rope",
        "There is a rope here.",
        "A sturdy rope for testing."
    );
  }

  /**
   * Creates a standard test gem.
   *
   * @return a new test gem item
   */
  @Nonnull
  public static Item createTestGem() {
    return new Item(
        "gem",
        "A sparkling test gem",
        "There is a gem here.",
        "A precious gem for testing."
    );
  }

  /**
   * Creates a test container item that can hold other items.
   * This is an inventory-based container (like a bag, items stay with container).
   *
   * @param name the container name
   * @param capacity the maximum number of items
   * @param allowedItemNames names of items that can be inserted (empty = any item)
   * @return a new test container
   */
  @Nonnull
  public static TestContainer createTestContainer(@Nonnull final String name,
                                                  final int capacity,
                                                  @Nonnull final String... allowedItemNames) {
    return new TestContainer(name, capacity, Set.of(allowedItemNames));
  }

  /**
   * Creates a scenery container for testing put commands.
   *
   * @param containerName the name of the container
   * @param allowedItemNames names of items that can be inserted
   * @return a new scenery container
   */
  @Nonnull
  public static SceneryContainer createSceneryContainer(@Nonnull final String containerName,
                                                        @Nonnull final String... allowedItemNames) {
    final SceneryObject sceneryObject = SceneryObject.builder(containerName)
        .withInteraction(InteractionType.LOOK, "You see a " + containerName + ".")
        .build();
    return new SceneryContainer(sceneryObject, Set.of(allowedItemNames));
  }

  /**
   * A test container implementation that extends Item and implements Container.
   * Used for testing container mechanics.
   */
  public static class TestContainer extends Item implements Container {
    private final int capacity;
    private final Set<String> allowedItems;
    private final Set<String> insertedItems;

    public TestContainer(@Nonnull final String name,
                         final int capacity,
                         @Nonnull final Set<String> allowedItems) {
      super(name,
          "A test " + name,
          "There is a " + name + " here.",
          "A container for testing.");
      this.capacity = capacity;
      this.allowedItems = Set.copyOf(allowedItems);
      this.insertedItems = new HashSet<>();
    }

    @Override
    @Nonnull
    public ContainerType getContainerType() {
      return ContainerType.INVENTORY;
    }

    @Override
    public boolean canAccept(@Nonnull final Item item) {
      if (isFull()) {
        return false;
      }
      if (allowedItems.isEmpty()) {
        return true;
      }
      return allowedItems.contains(item.getName().toLowerCase());
    }

    @Override
    public boolean insertItem(@Nonnull final Item item) {
      if (!canAccept(item)) {
        return false;
      }
      return insertedItems.add(item.getName().toLowerCase());
    }

    @Override
    public boolean removeItem(@Nonnull final Item item) {
      return insertedItems.remove(item.getName().toLowerCase());
    }

    @Override
    public boolean containsItem(@Nonnull final String itemName) {
      return insertedItems.contains(itemName.toLowerCase());
    }

    @Override
    @Nonnull
    public Set<String> getInsertedItemNames() {
      return Set.copyOf(insertedItems);
    }

    @Override
    public int getCapacity() {
      return capacity;
    }

    @Override
    public int getCurrentCount() {
      return insertedItems.size();
    }

    @Override
    public boolean isFull() {
      return insertedItems.size() >= capacity;
    }

    @Override
    @Nonnull
    public String getContainerStateDescription() {
      if (insertedItems.isEmpty()) {
        return "The " + getName() + " is empty.";
      }
      return "The " + getName() + " contains: " + String.join(", ", insertedItems);
    }

    @Override
    @Nonnull
    public List<String> getPreferredPrepositions() {
      return List.of("in", "into");
    }
  }
}
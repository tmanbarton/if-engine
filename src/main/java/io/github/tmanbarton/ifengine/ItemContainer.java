package io.github.tmanbarton.ifengine;

import io.github.tmanbarton.ifengine.constants.PrepositionConstants;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A takeable item that can also contain other items.
 * Items inserted into an inventory container follow the container between
 * inventory and location (ContainerType.INVENTORY behavior).
 *
 * <p>Created via the builder:
 * <pre>
 * ItemContainer bag = ItemContainer.builder("bag")
 *     .withCapacity(5)
 *     .withAllowedItems(Set.of("key", "gem"))
 *     .build();
 * </pre>
 */
public class ItemContainer extends Item implements Container {

  private final int capacity;
  private final Set<String> allowedItemNames;
  private final List<String> preferredPrepositions;
  private final Set<String> insertedItemNames;

  private ItemContainer(@Nonnull final String name,
                        @Nonnull final String inventoryDescription,
                        @Nonnull final String locationDescription,
                        @Nonnull final String detailedDescription,
                        @Nonnull final Set<String> aliases,
                        final int capacity,
                        @Nonnull final Set<String> allowedItemNames,
                        @Nonnull final List<String> preferredPrepositions) {
    super(name, inventoryDescription, locationDescription, detailedDescription, aliases);
    this.capacity = capacity;
    this.allowedItemNames = Set.copyOf(allowedItemNames);
    this.preferredPrepositions = List.copyOf(preferredPrepositions);
    this.insertedItemNames = new HashSet<>();
  }

  /**
   * Creates a new builder for an ItemContainer.
   *
   * @param name the container's name
   * @return a new builder
   */
  @Nonnull
  public static Builder builder(@Nonnull final String name) {
    return new Builder(name);
  }

  @Override
  public boolean canAccept(@Nonnull final Item item) {
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

  /**
   * Builder for creating {@link ItemContainer} instances.
   */
  public static final class Builder {

    private final String name;
    private String inventoryDescription;
    private String locationDescription;
    private String detailedDescription;
    private Set<String> aliases = Set.of();
    private int capacity = 0;
    private Set<String> allowedItemNames = Set.of();
    private List<String> preferredPrepositions = List.of(PrepositionConstants.IN, PrepositionConstants.INTO);

    private Builder(@Nonnull final String name) {
      this.name = name;
    }

    @Nonnull
    public Builder withInventoryDescription(@Nonnull final String inventoryDescription) {
      this.inventoryDescription = inventoryDescription;
      return this;
    }

    @Nonnull
    public Builder withLocationDescription(@Nonnull final String locationDescription) {
      this.locationDescription = locationDescription;
      return this;
    }

    @Nonnull
    public Builder withDetailedDescription(@Nonnull final String detailedDescription) {
      this.detailedDescription = detailedDescription;
      return this;
    }

    @Nonnull
    public Builder withAliases(@Nonnull final Set<String> aliases) {
      this.aliases = aliases;
      return this;
    }

    @Nonnull
    public Builder withCapacity(final int capacity) {
      this.capacity = capacity;
      return this;
    }

    @Nonnull
    public Builder withAllowedItems(@Nonnull final Set<String> allowedItemNames) {
      this.allowedItemNames = allowedItemNames;
      return this;
    }

    @Nonnull
    public Builder withPrepositions(@Nonnull final List<String> prepositions) {
      this.preferredPrepositions = prepositions;
      return this;
    }

    /**
     * Builds the ItemContainer, using auto-generated descriptions for any not explicitly set.
     *
     * @return the new ItemContainer
     */
    @Nonnull
    public ItemContainer build() {
      final String invDesc = inventoryDescription != null
          ? inventoryDescription
          : "A " + name;
      final String locDesc = locationDescription != null
          ? locationDescription
          : "There is a " + name + " here.";
      final String detDesc = detailedDescription != null
          ? detailedDescription
          : "A " + name + ".";

      return new ItemContainer(name, invDesc, locDesc, detDesc,
          aliases, capacity, allowedItemNames, preferredPrepositions);
    }
  }
}
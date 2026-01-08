package com.ifengine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an item in the game world that players can interact with.
 */
public class Item {
  @Nonnull
  private final String name;
  @Nonnull
  private final String inventoryDescription;
  @Nonnull
  private final String locationDescription;
  @Nonnull
  private final String detailedDescription;
  @Nonnull
  private final Set<String> aliases;

  /**
   * Full constructor with all fields.
   */
  public Item(@Nonnull final String name,
              @Nonnull final String inventoryDescription,
              @Nonnull final String locationDescription,
              @Nonnull final String detailedDescription,
              @Nonnull final Set<String> aliases) {
    this.name = name;
    this.inventoryDescription = inventoryDescription;
    this.locationDescription = locationDescription;
    this.detailedDescription = detailedDescription;
    this.aliases = Set.copyOf(aliases);
  }

  /**
   * Convenience constructor with no aliases.
   */
  public Item(@Nonnull final String name,
              @Nonnull final String inventoryDescription,
              @Nonnull final String locationDescription,
              @Nonnull final String detailedDescription) {
    this(name, inventoryDescription, locationDescription, detailedDescription, Set.of());
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public String getInventoryDescription() {
    return inventoryDescription;
  }

  @Nonnull
  public String getLocationDescription() {
    return locationDescription;
  }

  @Nonnull
  public String getDetailedDescription() {
    return detailedDescription;
  }

  @Nonnull
  public Set<String> getAliases() {
    return aliases;
  }

  /**
   * Checks if this item matches the given name (case-insensitive).
   *
   * @param searchName the name to search for
   * @return true if the item matches the name or has it as an alias
   */
  public boolean matchesName(@Nonnull final String searchName) {
    final String normalizedSearch = searchName.toLowerCase().trim();
    final String itemName = name.toLowerCase();

    return itemName.equals(normalizedSearch) || hasAlias(normalizedSearch);
  }

  /**
   * Checks if this item has the given alias (case-insensitive).
   *
   * @param alias the alias to check
   * @return true if the item has this alias, false otherwise
   */
  public boolean hasAlias(@Nullable final String alias) {
    if (alias == null) {
      return false;
    }
    final String normalizedAlias = alias.toLowerCase().trim();
    return aliases.stream()
        .anyMatch(a -> a.toLowerCase().equals(normalizedAlias));
  }

  @Override
  @Nonnull
  public String toString() {
    return inventoryDescription;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Item item = (Item) o;
    return name.equals(item.name) &&
        inventoryDescription.equals(item.inventoryDescription) &&
        locationDescription.equals(item.locationDescription) &&
        detailedDescription.equals(item.detailedDescription) &&
        aliases.equals(item.aliases);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, inventoryDescription, locationDescription,
        detailedDescription, aliases);
  }
}
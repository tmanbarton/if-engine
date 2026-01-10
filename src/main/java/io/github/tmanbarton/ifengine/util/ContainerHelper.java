package io.github.tmanbarton.ifengine.util;

import io.github.tmanbarton.ifengine.Container;
import io.github.tmanbarton.ifengine.ContainerType;

import javax.annotation.Nonnull;

/**
 * Helper utility for working with containers and their types.
 * Provides centralized logic for container type checking and behavior determination.
 */
public final class ContainerHelper {

  // Private constructor to prevent instantiation
  private ContainerHelper() {
    throw new UnsupportedOperationException("Utility class should not be instantiated");
  }

  /**
   * Gets the type of the container.
   *
   * @param container the container
   * @return the container's type
   */
  @Nonnull
  public static ContainerType getContainerType(@Nonnull final Container container) {
    return container.getContainerType();
  }

  /**
   * Checks if a container is an inventory container.
   * Inventory containers keep items in player inventory when items are inserted.
   *
   * @param container the container to check
   * @return true if container is an inventory container
   */
  public static boolean isInventoryContainer(@Nonnull final Container container) {
    return getContainerType(container) == ContainerType.INVENTORY;
  }

  /**
   * Checks if a container is a location container.
   * Location containers place items at the location when items are inserted.
   *
   * @param container the container to check
   * @return true if container is a location container
   */
  public static boolean isLocationContainer(@Nonnull final Container container) {
    return getContainerType(container) == ContainerType.LOCATION;
  }
}
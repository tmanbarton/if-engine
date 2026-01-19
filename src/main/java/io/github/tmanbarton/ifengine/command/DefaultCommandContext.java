package io.github.tmanbarton.ifengine.command;

import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.parser.ObjectResolver;
import io.github.tmanbarton.ifengine.response.ResponseProvider;
import io.github.tmanbarton.ifengine.util.ContainerOperations;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * Default implementation of {@link CommandContext}.
 * <p>
 * Provides access to game utilities for custom command handlers.
 */
public final class DefaultCommandContext implements CommandContext {

  private final ResponseProvider responseProvider;
  private final ObjectResolver objectResolver;
  private final Player player;

  /**
   * Creates a new DefaultCommandContext.
   *
   * @param responseProvider the response provider for game messaging
   * @param objectResolver the object resolver for finding items (may be null)
   * @param player the player for this context
   */
  public DefaultCommandContext(@Nonnull final ResponseProvider responseProvider,
                               @Nullable final ObjectResolver objectResolver,
                               @Nonnull final Player player) {
    this.responseProvider = Objects.requireNonNull(responseProvider, "responseProvider cannot be null");
    this.objectResolver = objectResolver;
    this.player = Objects.requireNonNull(player, "player cannot be null");
  }

  @Override
  @Nonnull
  public ResponseProvider getResponseProvider() {
    return responseProvider;
  }

  @Override
  @Nullable
  public ObjectResolver getObjectResolver() {
    return objectResolver;
  }

  @Override
  @Nonnull
  public Location getCurrentLocation() {
    return player.getCurrentLocation();
  }

  @Override
  @Nonnull
  public Optional<Item> resolveItem(@Nonnull final String name, @Nonnull final Player player) {
    Objects.requireNonNull(name, "name cannot be null");
    Objects.requireNonNull(player, "player cannot be null");

    // Check inventory first
    for (final Item item : player.getInventory()) {
      if (item.matchesName(name)) {
        return Optional.of(item);
      }
    }

    // Check current location
    final Item locationItem = player.getCurrentLocation().getItemByName(name);
    if (locationItem != null) {
      return Optional.of(locationItem);
    }

    return Optional.empty();
  }

  @Override
  public boolean playerHasItem(@Nonnull final String itemName) {
    Objects.requireNonNull(itemName, "itemName cannot be null");
    return player.hasItem(itemName);
  }

  @Override
  @Nonnull
  public String putItemInContainer(
      @Nonnull final String itemName,
      @Nonnull final String containerName,
      @Nonnull final String preposition
  ) {
    return ContainerOperations.putItemInContainer(
        player, itemName, containerName, preposition, responseProvider);
  }

  @Override
  public boolean isItemInContainer(@Nonnull final String itemName) {
    Objects.requireNonNull(itemName, "itemName cannot be null");

    final Location location = player.getCurrentLocation();
    final Item item = location.getItemByName(itemName);
    if (item == null) {
      return false;
    }
    return location.isItemInContainer(item);
  }

  @Override
  public boolean isItemInContainer(@Nonnull final String itemName, @Nonnull final String containerName) {
    Objects.requireNonNull(itemName, "itemName cannot be null");
    Objects.requireNonNull(containerName, "containerName cannot be null");

    final Location location = player.getCurrentLocation();
    final Item item = location.getItemByName(itemName);
    if (item == null) {
      return false;
    }

    final var container = location.getContainerForItem(item);
    if (container == null) {
      return false;
    }

    return container.getSceneryObject().matches(containerName);
  }
}

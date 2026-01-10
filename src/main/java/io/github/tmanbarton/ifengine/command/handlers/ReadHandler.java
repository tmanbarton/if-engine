package io.github.tmanbarton.ifengine.command.handlers;

import io.github.tmanbarton.ifengine.InteractionType;
import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.response.ResponseProvider;
import io.github.tmanbarton.ifengine.game.AbstractInteractionHandler;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.parser.ContextManager;
import io.github.tmanbarton.ifengine.parser.ObjectResolver;
import io.github.tmanbarton.ifengine.parser.ParsedCommand;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles READ commands for both items (like letters) and scenery objects (like signs).
 * Checks for readable items first, then falls back to scenery interaction handling.
 */
public class ReadHandler extends AbstractInteractionHandler {

  /**
   * Set of item names that can be read.
   * Games should add readable item names to this set.
   */
  // todo add isReadable field to Item
  public static final Set<String> READABLE_ITEMS = new HashSet<>();

  private final ResponseProvider responseProvider;
  @Nullable
  private final ObjectResolver objectResolver;
  @Nullable
  private final ContextManager contextManager;

  /**
   * Creates a ReadHandler with dependencies for full item resolution.
   *
   * @param responseProvider the response provider for messages
   * @param objectResolver the resolver for finding items
   * @param contextManager the manager for pronoun resolution
   */
  public ReadHandler(
      @Nonnull final ResponseProvider responseProvider,
      @Nonnull final ObjectResolver objectResolver,
      @Nonnull final ContextManager contextManager
  ) {
    this.responseProvider = responseProvider;
    this.objectResolver = objectResolver;
    this.contextManager = contextManager;
  }

  /**
   * Creates a ReadHandler for scenery-only handling.
   *
   * @param responseProvider the response provider for messages
   */
  public ReadHandler(@Nonnull final ResponseProvider responseProvider) {
    this.responseProvider = responseProvider;
    this.objectResolver = null;
    this.contextManager = null;
  }

  @Override
  @Nonnull
  public List<String> getSupportedVerbs() {
    return List.of("read");
  }

  @Override
  @Nonnull
  public String handle(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    // If no dependencies, fall back to scenery-only handling
    if (objectResolver == null || contextManager == null) {
      return super.handle(player, command);
    }

    // Full item + scenery handling
    return handleRead(player, command);
  }

  @Nonnull
  private String handleRead(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    ObjectResolver.ResolutionResult result = null;

    if (command.getDirectObjects().isEmpty()) {
      // No object specified - try to infer readable item
      final Item inferredItem = inferReadableItem(player);
      if (inferredItem != null) {
        return inferredItem.getDetailedDescription();
      }
      // No readable items found - fall back to scenery handler
      return super.handle(player, command);
    } else {
      // Object specified - resolve it
      final String objectName = command.getFirstDirectObject();
      result = objectResolver.resolveObject(objectName, player);

      // Only fall back to inferred object resolution for pronouns, not for explicit object names
      if (!result.isSuccess()) {
        // Check if the object name is a pronoun (like "it", "that", etc.)
        if (contextManager.isPronoun(objectName)) {
          result = objectResolver.resolveImpliedObject("read", player);
        }
      }
    }

    if (result != null && result.isSuccess()) {
      final Item item = result.getItem();

      // Check if item is readable
      if (isReadable(item)) {
        return item.getDetailedDescription();
      } else {
        return responseProvider.getCantRead(item.getName());
      }
    } else {
      // No item found - fall back to scenery handler
      return super.handle(player, command);
    }
  }

  /**
   * Infers which readable item to use when no object is specified.
   * Checks inventory first, then location items.
   *
   * @param player the player performing the action
   * @return the readable item if exactly one is found, null otherwise
   */
  @Nullable
  private Item inferReadableItem(@Nonnull final Player player) {
    // Check inventory for readable items
    final List<Item> inventoryReadable = player.getInventory().stream()
        .filter(this::isReadable)
        .toList();

    if (inventoryReadable.size() == 1) {
      return inventoryReadable.get(0);
    }

    // Check location for readable items
    final List<Item> locationReadable = player.getCurrentLocation().getItems().stream()
        .filter(this::isReadable)
        .toList();

    if (locationReadable.size() == 1) {
      return locationReadable.get(0);
    }

    return null;
  }

  /**
   * Determines if an item is readable.
   *
   * @param item the item to check
   * @return true if the item is readable, false otherwise
   */
  private boolean isReadable(@Nonnull final Item item) {
    return READABLE_ITEMS.contains(item.getName());
  }

  @Override
  @Nonnull
  protected InteractionType getInteractionType() {
    return InteractionType.READ;
  }

  @Override
  @Nonnull
  protected String getWhatResponse(@Nonnull final Player player) {
    return responseProvider.getReadWhat();
  }

  @Override
  @Nonnull
  protected String getCantInteract(@Nonnull final Player player, @Nonnull final String itemName) {
    return responseProvider.getCantRead(itemName);
  }

  @Override
  @Nonnull
  protected String getNotPresentResponse(@Nonnull final Player player) {
    return responseProvider.getReadNotPresent();
  }
}
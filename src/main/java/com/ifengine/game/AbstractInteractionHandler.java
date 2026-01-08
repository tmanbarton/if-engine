package com.ifengine.game;

import com.ifengine.InteractionType;
import com.ifengine.Item;
import com.ifengine.SceneryObject;
import com.ifengine.parser.ParsedCommand;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * Abstract base class providing unified fallback logic for interaction handlers.
 * Implements InteractionHandler (which extends BaseCommandHandler) for unified command dispatch
 * and backward compatibility with InteractionDispatcher.
 *
 * Implements the standard pattern: check for object → resolve real items → check scenery → default response.
 */
public abstract class AbstractInteractionHandler implements InteractionHandler {

  private final SceneryInteractionHandler sceneryHandler = new SceneryInteractionHandler();

  @Override
  @Nonnull
  public String handle(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    // Step 1: Handle object inference when no object is specified
    if (command.getDirectObjects().isEmpty()) {
      // User typed verb without object (e.g., "climb") - try to infer from available scenery
      return handleObjectInference(player);
    }

    final String objectName = command.getFirstDirectObject();

    // Step 2: Try to resolve real items first
    final Optional<Item> realItem = findRealItem(player, objectName);
    if (realItem.isPresent()) {
      // Found a real item (in inventory or location) matching the object name - items can't be interacted with like scenery objects
      return getCantInteract(player, realItem.get().getName());
    }

    // Step 3: Fall back to scenery interactions
    final Optional<String> sceneryResponse = sceneryHandler.handleInteraction(
        player,
        getInteractionType(),
        objectName
    );
    if (sceneryResponse.isPresent()) {
      // Found scenery object matching the name that supports this interaction type
      return sceneryResponse.get();
    }

    // Step 4: Check if the object exists but doesn't support this interaction
    final List<SceneryObject> sceneryObject = player.getCurrentLocation().findSceneryObjectsByInteraction(getInteractionType());
    if (!sceneryObject.isEmpty()) {
      // Object exists but doesn't support this interaction type
      return getCantInteract(player, objectName);
    }

    // Step 5: Return default response if nothing matches
    // No real items, no scenery objects, no matching interactions - object doesn't exist
    return getNotPresentResponse(player);
  }

  /**
   * Handles object inference when no object is specified in the command.
   * Finds all scenery objects that support this interaction type and:
   * - If exactly one object exists, use it automatically
   * - If multiple objects exist, prompt for disambiguation
   * - If no objects exist, return the "what" response
   *
   * @param player the player performing the interaction
   * @return the response string
   */
  @Nonnull
  private String handleObjectInference(@Nonnull final Player player) {
    final List<SceneryObject> supportedObjects = player.getCurrentLocation()
        .findSceneryObjectsByInteraction(getInteractionType());

    if (supportedObjects.isEmpty()) {
      // No objects support this interaction
      return getNotPresentResponse(player);
    } else {
      // Size should only ever be 1. If there's ever a case later on where there's multiple things at a location that can be interacted with in the
      // same way, make this an else if size == 1 and the else is a disambiguation for the user to specify which thing they want to interact with
      final SceneryObject object = supportedObjects.get(0);
      final Optional<String> response = object.getResponse(getInteractionType());
      return response.orElseGet(() -> getWhatResponse(player));
    }
  }

  /**
   * Finds a real item (in inventory or current location) by name.
   *
   * @param player the player to search items for
   * @param objectName the name of the object to find
   * @return Optional containing the item if found, empty otherwise
   */
  @Nonnull
  private Optional<Item> findRealItem(@Nonnull final Player player, @Nonnull final String objectName) {
    // Check inventory first
    final Item inventoryItem = player.getInventoryItemByName(objectName);
    if (inventoryItem != null) {
      return Optional.of(inventoryItem);
    }

    // Check current location items
    final Optional<Item> locationItem = player.getCurrentLocation().getItems().stream()
        .filter(item -> item.getName().equalsIgnoreCase(objectName))
        .findFirst();

    return locationItem;
  }

  /**
   * Gets the InteractionType corresponding to this handler's verb.
   * Must be implemented by concrete handlers.
   *
   * @return the interaction type for scenery fallback
   */
  @Nonnull
  protected abstract InteractionType getInteractionType();

  /**
   * Gets the "what" response when no object is specified.
   * Must be implemented by concrete handlers.
   *
   * @param player the player performing the interaction
   * @return the response asking what to interact with
   */
  @Nonnull
  protected abstract String getWhatResponse(@Nonnull Player player);

  /**
   * Gets the response when attempting to interact with a real item that can't be interacted with.
   * Must be implemented by concrete handlers.
   *
   * @param player the player performing the interaction
   * @param itemName the name of the item
   * @return the response explaining why the interaction failed
   */
  @Nonnull
  protected abstract String getCantInteract(@Nonnull Player player, @Nonnull String itemName);

  /**
   * Gets the response when the specified object isn't present in the location.
   * Must be implemented by concrete handlers.
   *
   * @param player the player performing the interaction
   * @return the response explaining the object isn't present
   */
  @Nonnull
  protected abstract String getNotPresentResponse(@Nonnull Player player);
}
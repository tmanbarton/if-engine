package com.ifengine.game;

import com.ifengine.InteractionType;
import com.ifengine.SceneryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * Handles interactions between players and scenery objects in the game world.
 * This class provides the integration layer between the game engine and
 * the scenery object system.
 */
public class SceneryInteractionHandler {

  /**
   * Handles an interaction between a player and a scenery object.
   *
   * @param player the player attempting the interaction
   * @param interaction the type of interaction being attempted
   * @param objectName the name of the object to interact with
   * @return an Optional containing the response if a matching scenery object
   *         and interaction type are found, empty otherwise
   * @throws NullPointerException if player is null
   */
  @Nonnull
  public Optional<String> handleInteraction(@Nonnull final Player player,
                                          @Nullable final InteractionType interaction,
                                          @Nullable final String objectName) {
    Objects.requireNonNull(player, "Player cannot be null");

    // Return empty if interaction type or object name is null/empty
    if (interaction == null || objectName == null || objectName.trim().isEmpty()) {
      return Optional.empty();
    }

    // Find scenery object in player's current location
    final Optional<SceneryObject> sceneryObject = player.getCurrentLocation().findSceneryObject(objectName);

    if (sceneryObject.isEmpty()) {
      return Optional.empty();
    }

    // Get response for the interaction type
    return sceneryObject.get().getResponse(interaction);
  }
}
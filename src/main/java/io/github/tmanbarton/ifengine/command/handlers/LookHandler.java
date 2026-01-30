package io.github.tmanbarton.ifengine.command.handlers;

import io.github.tmanbarton.ifengine.InteractionType;
import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.command.BaseCommandHandler;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.game.SceneryInteractionHandler;
import io.github.tmanbarton.ifengine.parser.ContextManager;
import io.github.tmanbarton.ifengine.parser.ObjectResolver;
import io.github.tmanbarton.ifengine.parser.ParsedCommand;
import io.github.tmanbarton.ifengine.response.ResponseProvider;
import io.github.tmanbarton.ifengine.util.LocationItemFormatter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * Handles look/examine commands for examining surroundings or specific objects.
 */
public class LookHandler implements BaseCommandHandler {

  private final ObjectResolver objectResolver;
  private final SceneryInteractionHandler sceneryHandler;
  private final ContextManager contextManager;
  private final ResponseProvider responseProvider;

  public LookHandler(
    @Nonnull final ObjectResolver objectResolver,
    @Nonnull final SceneryInteractionHandler sceneryHandler,
    @Nonnull final ContextManager contextManager,
    @Nonnull final ResponseProvider responseProvider
  ) {
    this.objectResolver = objectResolver;
    this.sceneryHandler = sceneryHandler;
    this.contextManager = contextManager;
    this.responseProvider = responseProvider;
  }

  @Override
  @Nonnull
  public List<String> getSupportedVerbs() {
    return List.of("look", "l", "examine", "x");
  }

  @Override
  @Nonnull
  public String handle(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    return handleLook(player, command);
  }

  @Nonnull
  private String handleLook(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    // Handle "look around" as equivalent to "look" (show location description)
    if ("around".equals(command.getPreposition()) && command.getDirectObjects().isEmpty()
        && command.getIndirectObjects().isEmpty()) {
      return lookAtLocation(player);
    }

    // Check if we have an object to look at (either direct or indirect)
    String objectName = "";
    if (!command.getDirectObjects().isEmpty()) {
      objectName = command.getFirstDirectObject();
    } else if (!command.getIndirectObjects().isEmpty()) {
      // Handle "look at key" where "key" becomes an indirect object
      objectName = command.getFirstIndirectObject();
    }

    if (objectName.isEmpty()) {
      return lookAtLocation(player);
    } else {
      // Use object resolver for better object resolution
      ObjectResolver.ResolutionResult result = objectResolver.resolveObject(objectName, player);

      // Only fall back to inferred object resolution for pronouns
      if (!result.isSuccess() && contextManager.isPronoun(objectName)) {
        result = objectResolver.resolveImpliedObject("look", player);
      }

      if (result.isSuccess()) {
        final Item item = result.getItem();
        return item.getDetailedDescription();
      } else {
        // Check for scenery interaction
        final Optional<String> sceneryResponse = sceneryHandler.handleInteraction(
            player, InteractionType.LOOK, objectName);
        if (sceneryResponse.isPresent()) {
          return sceneryResponse.get();
        }

        // Fall back to looking for specific object by name
        return lookAtObject(player, objectName);
      }
    }
  }

  @Nonnull
  private String lookAtLocation(@Nonnull final Player player) {
    final Location location = player.getCurrentLocation();
    final StringBuilder sb = new StringBuilder();

    final String description = location.getLongDescription();
    sb.append(description);

    // Always show items when using explicit "look" command
    final List<Item> items = location.getItems();
    if (!items.isEmpty()) {
      sb.append("\n\n");
      sb.append(LocationItemFormatter.formatItems(items, player, location, true));
    }

    return sb.toString();
  }

  @Nonnull
  private String lookAtObject(@Nonnull final Player player, @Nonnull final String objectName) {
    // First check inventory
    final Item item = player.getInventoryItemByName(objectName);
    if (item != null) {
      return item.getDetailedDescription();
    }

    // Then check location items
    final Location location = player.getCurrentLocation();
    final Item locationItem = location.getItems().stream()
      .filter(i -> i.matchesName(objectName))
      .findFirst()
      .orElse(null);

    if (locationItem != null) {
      return locationItem.getDetailedDescription();
    }

    return responseProvider.getLookAtObjectNotPresent(objectName);
  }
}
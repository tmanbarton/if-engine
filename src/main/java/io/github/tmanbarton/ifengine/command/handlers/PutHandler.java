package io.github.tmanbarton.ifengine.command.handlers;

import io.github.tmanbarton.ifengine.command.BaseCommandHandler;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.parser.ObjectResolver;
import io.github.tmanbarton.ifengine.parser.ParsedCommand;
import io.github.tmanbarton.ifengine.response.ResponseProvider;
import io.github.tmanbarton.ifengine.util.ContainerOperations;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Handles put commands for placing items into containers.
 * <p>
 * Delegates to {@link ContainerOperations} for the core logic.
 */
public class PutHandler implements BaseCommandHandler {

  @SuppressWarnings("unused")
  private final ObjectResolver objectResolver;
  private final ResponseProvider responseProvider;

  public PutHandler(
      @Nonnull final ObjectResolver objectResolver,
      @Nonnull final ResponseProvider responseProvider
  ) {
    this.objectResolver = objectResolver;
    this.responseProvider = responseProvider;
  }

  @Override
  @Nonnull
  public List<String> getSupportedVerbs() {
    return List.of("put", "place", "insert");
  }

  @Override
  @Nonnull
  public String handle(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    // Check if item is specified
    if (command.getDirectObjects().isEmpty()) {
      return responseProvider.getPutWhat();
    }

    final String itemName = command.getFirstDirectObject();

    // Check if container is specified
    if (command.getIndirectObjects().isEmpty()) {
      return responseProvider.getPutWhere(itemName);
    }

    final String containerName = command.getFirstIndirectObject();
    final String preposition = command.getPreposition();

    // Check if preposition is present
    if (preposition == null || preposition.isEmpty()) {
      return responseProvider.getPutMissingPreposition(itemName);
    }

    // Delegate to ContainerOperations for the core logic
    return ContainerOperations.putItemInContainer(
        player, itemName, containerName, preposition, responseProvider);
  }
}
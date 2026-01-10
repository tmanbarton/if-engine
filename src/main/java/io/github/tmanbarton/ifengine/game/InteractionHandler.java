package io.github.tmanbarton.ifengine.game;

import io.github.tmanbarton.ifengine.command.BaseCommandHandler;

/**
 * Interface for handling specific player interactions with game objects.
 * Each implementation handles a specific verb or set of related verbs.
 *
 * This interface extends BaseCommandHandler for unified command dispatch.
 * The InteractionDispatcher wraps results in Optional for backward compatibility.
 */
public interface InteractionHandler extends BaseCommandHandler {
  // All methods inherited from BaseCommandHandler:
  // - getSupportedVerbs()
  // - handle(Player, ParsedCommand)
  // - canHandle(String) - default implementation
}
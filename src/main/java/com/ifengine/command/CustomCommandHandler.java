package com.ifengine.command;

import com.ifengine.game.Player;
import com.ifengine.parser.ParsedCommand;

import javax.annotation.Nonnull;

/**
 * Functional interface for custom command handlers.
 * <p>
 * Custom handlers are registered via {@code GameMap.Builder.withCommand()} and provide
 * a way for game creators to add new commands without implementing the full
 * {@link BaseCommandHandler} interface.
 * <p>
 * Example usage:
 * <pre>
 * new GameMap.Builder()
 *     .withCommand("xyzzy", (player, cmd, ctx) -> "Nothing happens.")
 *     .withCommand("search", (player, cmd, ctx) -> {
 *         String target = cmd.getFirstDirectObject();
 *         if (target.isEmpty()) {
 *             return "Search what?";
 *         }
 *         return "You search the " + target + " but find nothing.";
 *     })
 *     .build();
 * </pre>
 */
@FunctionalInterface
public interface CustomCommandHandler {

  /**
   * Handles a custom command.
   *
   * @param player the player issuing the command
   * @param command the parsed command with verb, objects, prepositions, etc.
   * @param context provides access to game utilities like ResponseProvider and ObjectResolver
   * @return the response message to show the player
   */
  @Nonnull
  String handle(@Nonnull Player player, @Nonnull ParsedCommand command,
                @Nonnull CommandContext context);
}

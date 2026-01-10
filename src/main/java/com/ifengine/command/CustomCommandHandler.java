package com.ifengine.command;

import com.ifengine.game.Player;
import com.ifengine.parser.ParsedCommand;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Functional interface for custom command handlers.
 * <p>
 * Custom handlers are registered via {@code GameMap.Builder.withCommand()} and provide
 * a way for game creators to add new commands without implementing the full
 * {@link BaseCommandHandler} interface.
 * <p>
 * <b>Delegation:</b> Return {@code null} to delegate to the built-in handler for this verb.
 * This allows custom handlers to handle specific cases while falling back to default behavior
 * for everything else.
 * <p>
 * Example usage:
 * <pre>
 * new GameMap.Builder()
 *     .withCommand("xyzzy", (player, cmd, ctx) -> "Nothing happens.")
 *     .withCommand("eat", (player, cmd, ctx) -> {
 *         // Handle special case
 *         if (cmd.getFirstDirectObject().equals("magic apple")) {
 *             return "You feel a surge of power!";
 *         }
 *         // Delegate to default eat behavior for everything else
 *         return null;
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
   * @return the response message to show the player, or {@code null} to delegate to the
   *         built-in handler for this verb
   */
  @Nullable
  String handle(@Nonnull Player player, @Nonnull ParsedCommand command,
                @Nonnull CommandContext context);
}

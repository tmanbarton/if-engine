package io.github.tmanbarton.ifengine.command.handlers;

import io.github.tmanbarton.ifengine.command.BaseCommandHandler;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.parser.ParsedCommand;
import io.github.tmanbarton.ifengine.response.ResponseProvider;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Handles system/meta commands that don't interact with game objects.
 *
 * This handler manages stateless commands related to player information and help.
 * These commands don't require object resolution or game state changes.
 *
 * <h3>Supported Commands:</h3>
 * <ul>
 *   <li><b>inventory, i</b> - Display player's inventory</li>
 *   <li><b>help, ?</b> - Display available commands</li>
 *   <li><b>info, information</b> - Display detailed game information</li>
 * </ul>
 *
 * NOTE: Stateful commands (quit, restart) remain in GameEngine as they require
 * session management and game state modification capabilities.
 */
public class SystemCommandHandler implements BaseCommandHandler {

  private final ResponseProvider responseProvider;

  public SystemCommandHandler(@Nonnull final ResponseProvider responseProvider) {
    this.responseProvider = responseProvider;
  }

  @Override
  @Nonnull
  public List<String> getSupportedVerbs() {
    return List.of("inventory", "i", "help", "info", "information");
  }

  @Override
  @Nonnull
  public String handle(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    final String verb = command.getVerb().toLowerCase().trim();

    return switch (verb) {
      case "inventory", "i" -> handleInventory(player);
      case "help", "?" -> handleHelp();
      case "info", "information" -> handleInfo();
      default -> responseProvider.getCommandNotUnderstood(command.getOriginalInput().trim());
    };
  }

  @Nonnull
  private String handleInventory(@Nonnull final Player player) {
    final String items = player.getFormattedInventoryItems();
    if (items.isEmpty()) {
      return responseProvider.getInventoryEmpty();
    }
    return responseProvider.getInventory(items);
  }

  @Nonnull
  private String handleHelp() {
    return responseProvider.getHelpMessage();
  }

  @Nonnull
  private String handleInfo() {
    return responseProvider.getInfoMessage();
  }
}
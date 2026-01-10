package com.ifengine.command;

import com.ifengine.game.Player;
import com.ifengine.parser.ParsedCommand;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Adapts a {@link CustomCommandHandler} to the {@link BaseCommandHandler} interface.
 * <p>
 * This adapter allows custom commands registered via {@code GameMap.Builder.withCommand()}
 * to work with the standard {@link CommandDispatcher} routing.
 */
public final class CustomCommandAdapter implements BaseCommandHandler {

  private final List<String> supportedVerbs;
  private final CustomCommandHandler handler;
  private final Function<Player, CommandContext> contextFactory;

  /**
   * Creates a new CustomCommandAdapter.
   *
   * @param primaryVerb the main verb for this command
   * @param aliases additional verbs that trigger this command
   * @param handler the custom command handler
   * @param contextFactory creates a CommandContext for each player at invocation time
   */
  public CustomCommandAdapter(@Nonnull final String primaryVerb,
                              @Nonnull final List<String> aliases,
                              @Nonnull final CustomCommandHandler handler,
                              @Nonnull final Function<Player, CommandContext> contextFactory) {
    Objects.requireNonNull(primaryVerb, "primaryVerb cannot be null");
    Objects.requireNonNull(aliases, "aliases cannot be null");
    Objects.requireNonNull(handler, "handler cannot be null");
    Objects.requireNonNull(contextFactory, "contextFactory cannot be null");

    final List<String> verbs = new ArrayList<>();
    verbs.add(primaryVerb.toLowerCase());
    for (final String alias : aliases) {
      verbs.add(alias.toLowerCase());
    }
    this.supportedVerbs = Collections.unmodifiableList(verbs);
    this.handler = handler;
    this.contextFactory = contextFactory;
  }

  @Override
  @Nonnull
  public List<String> getSupportedVerbs() {
    return supportedVerbs;
  }

  @Override
  @Nonnull
  public String handle(@Nonnull final Player player, @Nonnull final ParsedCommand command) {
    final CommandContext context = contextFactory.apply(player);
    return handler.handle(player, command, context);
  }
}

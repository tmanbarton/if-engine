package com.ifengine.command;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * Record holding custom command registration data.
 * <p>
 * Used internally by GameMap.Builder to store custom command registrations
 * before passing them to GameEngine for handler creation.
 *
 * @param verb the primary verb for the command
 * @param aliases additional verbs that trigger this command
 * @param handler the custom command handler
 */
public record CustomCommandRegistration(
    @Nonnull String verb,
    @Nonnull List<String> aliases,
    @Nonnull CustomCommandHandler handler
) {

  /**
   * Creates a CustomCommandRegistration with validation.
   */
  public CustomCommandRegistration {
    Objects.requireNonNull(verb, "verb cannot be null");
    Objects.requireNonNull(aliases, "aliases cannot be null");
    Objects.requireNonNull(handler, "handler cannot be null");
    aliases = List.copyOf(aliases);
  }
}

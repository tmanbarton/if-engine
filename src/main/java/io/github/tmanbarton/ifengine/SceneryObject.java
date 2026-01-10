package io.github.tmanbarton.ifengine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a scenery object in the game world that players can interact with but cannot take.
 * Scenery objects have names, aliases, and specific responses to different interaction types.
 * <p>
 * Supports both standard {@link InteractionType} responses and custom string-based interactions
 * for use with custom commands registered via {@code GameMap.Builder.withCommand()}.
 */
public record SceneryObject(
    @Nonnull String name,
    @Nonnull Set<String> aliases,
    @Nonnull Map<InteractionType, String> responses,
    @Nonnull Map<String, String> customResponses
) {

  /**
   * Creates a new builder for constructing SceneryObject instances.
   *
   * @param name the primary name of the scenery object (must not be null or blank)
   * @return a new Builder instance
   * @throws IllegalArgumentException if name is null, empty, or blank
   */
  @Nonnull
  public static Builder builder(@Nonnull final String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Name cannot be null or blank");
    }
    return new Builder(name.trim());
  }

  /**
   * Checks if this scenery object matches the given object name or any of its aliases.
   * The comparison is case-insensitive and handles null input safely.
   *
   * @param objectName the name to match against (can be null)
   * @return true if the name matches the scenery object's name or any alias, false otherwise
   */
  public boolean matches(@Nullable final String objectName) {
    if (objectName == null) {
      return false;
    }

    final String normalizedInput = objectName.trim().toLowerCase();

    if (name.toLowerCase().equals(normalizedInput)) {
      return true;
    }

    return aliases.stream()
        .anyMatch(alias -> alias.toLowerCase().equals(normalizedInput));
  }

  /**
   * Gets the response for a specific interaction type.
   *
   * @param interaction the interaction type to get response for (can be null)
   * @return an Optional containing the response if available, empty otherwise
   */
  @Nonnull
  public Optional<String> getResponse(@Nullable final InteractionType interaction) {
    if (interaction == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(responses.get(interaction));
  }

  /**
   * Gets the response for a custom interaction verb.
   * <p>
   * Custom interactions are defined via {@code withCustomInteraction()} and can be
   * used with custom commands registered via {@code GameMap.Builder.withCommand()}.
   *
   * @param verb the custom verb to get response for (can be null)
   * @return an Optional containing the response if available, empty otherwise
   */
  @Nonnull
  public Optional<String> getCustomResponse(@Nullable final String verb) {
    if (verb == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(customResponses.get(verb.toLowerCase()));
  }

  /**
   * Builder class for constructing SceneryObject instances with fluent API.
   */
  public static class Builder {
    private final String name;
    private final Set<String> aliases = new HashSet<>();
    private final Map<InteractionType, String> responses = new HashMap<>();
    private final Map<String, String> customResponses = new HashMap<>();

    private Builder(@Nonnull final String name) {
      this.name = name;
    }

    /**
     * Adds an alias for this scenery object.
     *
     * @param alias the alias to add (must not be null or blank)
     * @throws IllegalArgumentException if alias is null or blank
     */
    @Nonnull
    public void addAlias(@Nonnull final String alias) {
      if (alias == null || alias.trim().isEmpty()) {
        throw new IllegalArgumentException("Alias cannot be null or blank");
      }
      aliases.add(alias.trim());
    }

    /**
     * Adds one or multiple aliases for this scenery object.
     *
     * @param aliasArray the aliases to add (must not contain null or blank values)
     * @return this builder for method chaining
     * @throws IllegalArgumentException if any alias is null or blank
     */
    @Nonnull
    public Builder withAliases(@Nonnull final String... aliasArray) {
      for (final String alias : aliasArray) {
        addAlias(alias);
      }
      return this;
    }

    /**
     * Adds an interaction response for this scenery object.
     *
     * @param interaction the interaction type (must not be null)
     * @param response the response text (must not be null or blank)
     * @return this builder for method chaining
     * @throws IllegalArgumentException if interaction is null or response is null/blank
     */
    @Nonnull
    public Builder withInteraction(@Nonnull final InteractionType interaction, @Nonnull final String response) {
      if (interaction == null) {
        throw new IllegalArgumentException("Interaction type cannot be null");
      }
      if (response == null || response.trim().isEmpty()) {
        throw new IllegalArgumentException("Response cannot be null or blank");
      }
      responses.put(interaction, response.trim());
      return this;
    }

    /**
     * Adds a custom interaction response for this scenery object.
     * <p>
     * Custom interactions allow scenery to respond to verbs beyond the standard
     * {@link InteractionType} enum. Use with custom commands registered via
     * {@code GameMap.Builder.withCommand()}.
     * <p>
     * Example:
     * <pre>
     * SceneryObject.builder("flower")
     *     .withCustomInteraction("smell", "It smells lovely!")
     *     .build();
     * </pre>
     *
     * @param verb the custom verb (must not be null or blank)
     * @param response the response text (must not be null or blank)
     * @return this builder for method chaining
     * @throws IllegalArgumentException if verb or response is null/blank
     */
    @Nonnull
    public Builder withCustomInteraction(@Nonnull final String verb, @Nonnull final String response) {
      if (verb == null || verb.trim().isEmpty()) {
        throw new IllegalArgumentException("Verb cannot be null or blank");
      }
      if (response == null || response.trim().isEmpty()) {
        throw new IllegalArgumentException("Response cannot be null or blank");
      }
      customResponses.put(verb.trim().toLowerCase(), response.trim());
      return this;
    }

    /**
     * Builds the SceneryObject instance with the configured properties.
     *
     * @return a new SceneryObject instance
     * @throws IllegalStateException if no interactions (standard or custom) have been added
     */
    @Nonnull
    public SceneryObject build() {
      if (responses.isEmpty() && customResponses.isEmpty()) {
        throw new IllegalStateException("SceneryObject must have at least one interaction");
      }
      return new SceneryObject(name, Set.copyOf(aliases), Map.copyOf(responses), Map.copyOf(customResponses));
    }
  }
}
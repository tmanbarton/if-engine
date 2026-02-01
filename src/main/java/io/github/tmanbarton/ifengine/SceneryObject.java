package io.github.tmanbarton.ifengine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a scenery object in the game world that players can interact with but cannot take.
 * Scenery objects have names, aliases, and specific responses to different interaction types.
 * <p>
 * Supports both standard {@link InteractionType} responses and custom string-based interactions
 * for use with custom commands registered via {@code GameMap.Builder.withCommand()}.
 */
public class SceneryObject {

  @Nonnull
  private final String name;
  @Nonnull
  private final Set<String> aliases;
  @Nonnull
  private final Map<InteractionType, String> responses;
  @Nonnull
  private final Map<String, String> customResponses;
  private final boolean isContainer;
  @Nonnull
  private final Set<String> allowedItemNames;
  @Nonnull
  private final List<String> prepositions;

  /**
   * Constructs a SceneryObject with all properties.
   * Protected to allow subclass construction.
   *
   * @param name the primary name of the scenery object
   * @param aliases alternative names for the scenery object
   * @param responses standard interaction type responses
   * @param customResponses custom verb responses
   * @param isContainer whether this scenery object is a container
   * @param allowedItemNames names of items allowed in this container
   * @param prepositions valid prepositions for this container
   */
  protected SceneryObject(
      @Nonnull final String name,
      @Nonnull final Set<String> aliases,
      @Nonnull final Map<InteractionType, String> responses,
      @Nonnull final Map<String, String> customResponses,
      final boolean isContainer,
      @Nonnull final Set<String> allowedItemNames,
      @Nonnull final List<String> prepositions
  ) {
    this.name = name;
    this.aliases = aliases;
    this.responses = responses;
    this.customResponses = customResponses;
    this.isContainer = isContainer;
    this.allowedItemNames = allowedItemNames;
    this.prepositions = prepositions;
  }

  // ===== Accessor methods (matching record component names) =====

  @Nonnull
  public String name() {
    return name;
  }

  @Nonnull
  public Set<String> aliases() {
    return aliases;
  }

  @Nonnull
  public Map<InteractionType, String> responses() {
    return responses;
  }

  @Nonnull
  public Map<String, String> customResponses() {
    return customResponses;
  }

  public boolean isContainer() {
    return isContainer;
  }

  @Nonnull
  public Set<String> allowedItemNames() {
    return allowedItemNames;
  }

  @Nonnull
  public List<String> prepositions() {
    return prepositions;
  }

  // ===== Convenience accessor methods =====

  /**
   * Gets the allowed item names for this container.
   * Empty set means any item is allowed.
   *
   * @return the set of allowed item names
   */
  @Nonnull
  public Set<String> getAllowedItemNames() {
    return allowedItemNames;
  }

  /**
   * Gets the valid prepositions for this container.
   * Returns empty list for non-containers.
   *
   * @return the list of valid prepositions
   */
  @Nonnull
  public List<String> getPrepositions() {
    return prepositions;
  }

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

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SceneryObject that = (SceneryObject) o;
    return isContainer == that.isContainer
        && name.equals(that.name)
        && aliases.equals(that.aliases)
        && responses.equals(that.responses)
        && customResponses.equals(that.customResponses)
        && allowedItemNames.equals(that.allowedItemNames)
        && prepositions.equals(that.prepositions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, aliases, responses, customResponses, isContainer, allowedItemNames, prepositions);
  }

  @Override
  public String toString() {
    return "SceneryObject[" +
        "name=" + name +
        ", aliases=" + aliases +
        ", responses=" + responses +
        ", customResponses=" + customResponses +
        ", isContainer=" + isContainer +
        ", allowedItemNames=" + allowedItemNames +
        ", prepositions=" + prepositions +
        ']';
  }

  /**
   * Builder class for constructing SceneryObject instances with fluent API.
   */
  public static class Builder {
    private final String name;
    private final Set<String> aliases = new HashSet<>();
    private final Map<InteractionType, String> responses = new HashMap<>();
    private final Map<String, String> customResponses = new HashMap<>();
    private boolean isContainer = false;
    private final Set<String> allowedItemNames = new HashSet<>();
    private final List<String> prepositions = new ArrayList<>();

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
     * Marks this scenery object as a container that can hold items.
     * <p>
     * Containers can have items placed on/in them using the "put" command.
     * By default, containers use "on" and "onto" prepositions.
     *
     * @return this builder for method chaining
     */
    @Nonnull
    public Builder asContainer() {
      this.isContainer = true;
      return this;
    }

    /**
     * Sets which items can be placed in this container.
     * <p>
     * If not called or called with no arguments, any item can be placed.
     *
     * @param itemNames the names of items allowed in this container
     * @return this builder for method chaining
     */
    @Nonnull
    public Builder withAllowedItems(@Nonnull final String... itemNames) {
      for (final String itemName : itemNames) {
        if (itemName != null && !itemName.trim().isEmpty()) {
          allowedItemNames.add(itemName.trim());
        }
      }
      return this;
    }

    /**
     * Sets the valid prepositions for this container.
     * <p>
     * Common prepositions:
     * <ul>
     *   <li>"on", "onto" - for surfaces like tables, shelves</li>
     *   <li>"in", "into" - for enclosures like drawers, boxes</li>
     * </ul>
     * <p>
     * If not called, defaults to "on" and "onto" for containers.
     *
     * @param preps the valid prepositions for this container
     * @return this builder for method chaining
     */
    @Nonnull
    public Builder withPrepositions(@Nonnull final String... preps) {
      for (final String prep : preps) {
        if (prep != null && !prep.trim().isEmpty()) {
          prepositions.add(prep.trim());
        }
      }
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

      // Determine prepositions: use specified ones, or defaults for containers, or empty
      final List<String> finalPrepositions;
      if (!prepositions.isEmpty()) {
        finalPrepositions = List.copyOf(prepositions);
      } else if (isContainer) {
        finalPrepositions = List.of("on", "onto");
      } else {
        finalPrepositions = List.of();
      }

      return new SceneryObject(
          name,
          Set.copyOf(aliases),
          Map.copyOf(responses),
          Map.copyOf(customResponses),
          isContainer,
          Set.copyOf(allowedItemNames),
          finalPrepositions
      );
    }
  }
}
package com.ifengine.parser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Represents a parsed command from user input with normalized components.
 */
public final class ParsedCommand {
  private final String verb;
  private final List<String> directObjects;
  private final List<String> indirectObjects;
  private final String preposition;
  private final CommandType type;
  private final boolean impliedObject;
  private final String originalInput;
  private final List<String> sequenceCommands;

  public ParsedCommand(@Nonnull final String verb,
                       @Nonnull final List<String> directObjects,
                       @Nonnull final List<String> indirectObjects,
                       @Nullable final String preposition,
                       @Nonnull final CommandType type,
                       final boolean impliedObject,
                       @Nonnull final String originalInput) {
    this(verb, directObjects, indirectObjects, preposition, type, impliedObject, originalInput, Collections.emptyList());
  }

  public ParsedCommand(@Nonnull final String verb,
                       @Nonnull final List<String> directObjects,
                       @Nonnull final List<String> indirectObjects,
                       @Nullable final String preposition,
                       @Nonnull final CommandType type,
                       final boolean impliedObject,
                       @Nonnull final String originalInput,
                       @Nonnull final List<String> sequenceCommands) {
    this.verb = verb;
    this.directObjects = Collections.unmodifiableList(directObjects);
    this.indirectObjects = Collections.unmodifiableList(indirectObjects);
    this.preposition = preposition;
    this.type = type;
    this.impliedObject = impliedObject;
    this.originalInput = originalInput;
    this.sequenceCommands = Collections.unmodifiableList(sequenceCommands);
  }

  /**
   * @return the normalized verb (e.g., "take", "look", "go")
   */
  @Nonnull
  public String getVerb() {
    return verb;
  }

  /**
   * @return the direct objects (main targets of the command)
   */
  @Nonnull
  public List<String> getDirectObjects() {
    return directObjects;
  }

  /**
   * @return the first direct object, or empty string if none
   */
  @Nonnull
  public String getFirstDirectObject() {
    return directObjects.isEmpty() ? "" : directObjects.get(0);
  }

  /**
   * @return the indirect objects (objects after prepositions)
   */
  @Nonnull
  public List<String> getIndirectObjects() {
    return indirectObjects;
  }

  /**
   * @return the first indirect object, or empty string if none
   */
  @Nonnull
  public String getFirstIndirectObject() {
    return indirectObjects.isEmpty() ? "" : indirectObjects.get(0);
  }

  /**
   * @return the preposition connecting direct and indirect objects, or null if none
   */
  @Nullable
  public String getPreposition() {
    return preposition;
  }

  /**
   * @return the type of command structure
   */
  @Nonnull
  public CommandType getType() {
    return type;
  }

  /**
   * @return true if the object was inferred from context rather than explicitly stated
   */
  public boolean isImpliedObject() {
    return impliedObject;
  }

  /**
   * @return the original user input before processing
   */
  @Nonnull
  public String getOriginalInput() {
    return originalInput;
  }

  /**
   * @return true if this command has any direct objects
   */
  public boolean hasDirectObjects() {
    return !directObjects.isEmpty();
  }

  /**
   * @return true if this command has any indirect objects
   */
  public boolean hasIndirectObjects() {
    return !indirectObjects.isEmpty();
  }

  /**
   * @return true if this command has a preposition
   */
  public boolean hasPreposition() {
    return preposition != null && !preposition.trim().isEmpty();
  }

  /**
   * @return the list of additional commands in a sequence (for SEQUENCE type commands)
   */
  @Nonnull
  public List<String> getSequenceCommands() {
    return sequenceCommands;
  }

  /**
   * @return true if this is a sequence command with multiple parts
   */
  public boolean hasSequenceCommands() {
    return !sequenceCommands.isEmpty();
  }

  @Override
  public String toString() {
    return "ParsedCommand{" +
        "verb='" + verb + '\'' +
        ", directObjects=" + directObjects +
        ", indirectObjects=" + indirectObjects +
        ", preposition='" + preposition + '\'' +
        ", type=" + type +
        ", impliedObject=" + impliedObject +
        ", originalInput='" + originalInput + '\'' +
        '}';
  }
}
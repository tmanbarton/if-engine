package com.ifengine.command;

/**
 * Defines how object references in commands should be resolved.
 *
 * This enum describes the different ways players can specify objects in commands,
 * which determines how the command handler should interpret and resolve the object reference.
 *
 * <h3>Object Reference Types</h3>
 * Players can specify objects in three fundamentally different ways:
 * <ul>
 *   <li><b>EXPLICIT</b> - Direct object name: "take key", "climb tree"</li>
 *   <li><b>CONTEXTUAL</b> - Context reference: "take it", "climb that"</li>
 *   <li><b>INFERRED</b> - No object specified: "take", "climb"</li>
 * </ul>
 *
 * <h3>Resolution Priority and Precedence</h3>
 * The enum values are ordered by precedence for conflict resolution:
 * <ol>
 *   <li><b>EXPLICIT</b> - Highest precedence (most specific)</li>
 *   <li><b>CONTEXTUAL</b> - Medium precedence (reference-based)</li>
 *   <li><b>INFERRED</b> - Lowest precedence (fallback/automatic)</li>
 * </ol>
 *
 * <h3>Critical Rule: Inferred Objects Are Always Items</h3>
 * When a player types a command without specifying an object (INFERRED mode),
 * the system can only automatically select REAL ITEMS, never scenery objects.
 * Players must explicitly specify scenery objects to interact with them.
 *
 * <h4>Examples:</h4>
 * <ul>
 *   <li><b>"take"</b> (one item present) → INFERRED mode, takes the item automatically</li>
 *   <li><b>"take"</b> (tree present, no items) → INFERRED mode, "Take what?" (can't infer scenery)</li>
 *   <li><b>"climb tree"</b> → EXPLICIT mode, works (explicit scenery reference)</li>
 *   <li><b>"climb"</b> → INFERRED mode, "Climb what?" (even if tree is climbable)</li>
 *   <li><b>"take it"</b> → CONTEXTUAL mode, resolves "it" from recent context</li>
 * </ul>
 *
 * <h3>Handler Usage</h3>
 * Command handlers use this enum to determine:
 * <ul>
 *   <li>Whether to allow automatic object selection (INFERRED)</li>
 *   <li>Whether to resolve pronouns and references (CONTEXTUAL)</li>
 *   <li>Whether to perform direct object matching (EXPLICIT)</li>
 *   <li>What error messages to show when resolution fails</li>
 * </ul>
 *
 * @see com.ifengine.command.BaseCommandHandler
 * @see com.ifengine.parser.ObjectResolver
 * @see com.ifengine.parser.ParsedCommand
 */
public enum ResolutionMode {

  /**
   * Object is explicitly specified by name in the command.
   *
   * This mode represents commands where the player directly names the object
   * they want to interact with. This is the most common and straightforward
   * form of object specification.
   *
   * <h4>Examples:</h4>
   * <ul>
   *   <li>"take key" → object="key", mode=EXPLICIT</li>
   *   <li>"look book" → object="book", mode=EXPLICIT</li>
   *   <li>"climb tree" → object="tree", mode=EXPLICIT</li>
   *   <li>"unlock shed" → object="shed", mode=EXPLICIT</li>
   * </ul>
   *
   * <h4>Resolution Strategy:</h4>
   * <ol>
   *   <li>Match object name directly against available objects</li>
   *   <li>Case-insensitive matching typically used</li>
   *   <li>Can specify items, scenery objects, or location features</li>
   *   <li>Return specific error if object not found</li>
   * </ol>
   */
  EXPLICIT,

  /**
   * Object is specified using contextual references or pronouns.
   *
   * This mode represents commands where the player refers to objects using
   * pronouns or context-dependent references that must be resolved based on
   * recent conversation or game context.
   *
   * <h4>Examples:</h4>
   * <ul>
   *   <li>"take it" → object="it", mode=CONTEXTUAL</li>
   *   <li>"look at that" → object="that", mode=CONTEXTUAL</li>
   *   <li>"climb them" → object="them", mode=CONTEXTUAL</li>
   *   <li>"unlock it" → object="it", mode=CONTEXTUAL</li>
   * </ul>
   *
   * <h4>Resolution Strategy:</h4>
   * <ol>
   *   <li>Identify pronoun or contextual reference</li>
   *   <li>Resolve reference using conversation context</li>
   *   <li>Fall back to recent object mentions or interactions</li>
   *   <li>Apply same object type rules as explicit resolution</li>
   * </ol>
   */
  CONTEXTUAL,

  /**
   * No object specified - must infer appropriate object from game state.
   *
   * This mode represents commands where the player doesn't specify an object,
   * requiring the system to automatically determine the most appropriate object
   * based on available options and command-specific rules.
   *
   * <h4>Examples:</h4>
   * <ul>
   *   <li>"take" → no object, mode=INFERRED</li>
   *   <li>"drop" → no object, mode=INFERRED</li>
   *   <li>"unlock" → no object, mode=INFERRED</li>
   *   <li>"open" → no object, mode=INFERRED</li>
   * </ul>
   *
   * <h4>Critical Rule: Items Only for Auto-Inference</h4>
   * INFERRED mode can ONLY automatically select real game items, never scenery objects.
   * However, location-based commands (like unlock/open at special locations) can work
   * in INFERRED mode by targeting the current location's interactive features.
   *
   * <h4>Resolution Strategy:</h4>
   * <ol>
   *   <li>For item commands: Check if exactly one relevant item is available</li>
   *   <li>For location commands: Check if current location supports the action</li>
   *   <li>For scenery commands: Always return "what" message (no auto-inference)</li>
   *   <li>If multiple options: ask for clarification</li>
   * </ol>
   */
  INFERRED;

  /**
   * Checks if this resolution mode represents an explicit object specification.
   *
   * @return true if this is EXPLICIT mode
   */
  public boolean isExplicit() {
    return this == EXPLICIT;
  }

  /**
   * Checks if this resolution mode represents a contextual object reference.
   *
   * @return true if this is CONTEXTUAL mode
   */
  public boolean isContextual() {
    return this == CONTEXTUAL;
  }

  /**
   * Checks if this resolution mode represents an inferred object selection.
   *
   * @return true if this is INFERRED mode
   */
  public boolean isInferred() {
    return this == INFERRED;
  }

  /**
   * Determines the appropriate resolution mode based on object specification.
   *
   * This utility method analyzes how an object was specified in a command
   * and returns the corresponding resolution mode.
   *
   * @param objectName the object name from the command, may be null
   * @return the appropriate resolution mode for this object specification
   */
  public static ResolutionMode determineMode(final String objectName) {
    if (objectName == null || objectName.trim().isEmpty()) {
      return INFERRED;
    }

    final String trimmed = objectName.trim().toLowerCase();
    if ("it".equals(trimmed) || "them".equals(trimmed) || "that".equals(trimmed) ||
        "those".equals(trimmed) || "this".equals(trimmed)) {
      return CONTEXTUAL;
    }

    return EXPLICIT;
  }
}
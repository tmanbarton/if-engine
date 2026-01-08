package com.ifengine.parser;

import com.ifengine.game.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Main command parser that orchestrates all parsing components.
 * Handles input processing, grammar analysis, context integration, and error recovery.
 */
public final class CommandParser {

  private final VocabularyManager vocabularyManager;
  private final ContextManager contextManager;
  private final ObjectResolver objectResolver;

  public CommandParser() {
    this.vocabularyManager = new VocabularyManager();
    this.contextManager = new ContextManager();
    this.objectResolver = new ObjectResolver();
  }

  /**
   * Parses user input into a structured command with context resolution.
   *
   * @param input the raw user input
   * @param sessionId the player session ID
   * @param player the player for context
   * @return parsed command structure
   */
  @Nonnull
  public ParsedCommand parseCommand(@Nonnull final String input, @Nonnull final String sessionId, @Nonnull final Player player) {
    // Update context with current location
    contextManager.updateLocation(sessionId, player.getCurrentLocation());

    // Normalize and tokenize input
    final String normalized = normalizeInput(input);
    final String[] tokens = tokenize(normalized);

    if (tokens.length == 0) {
      return createEmptyCommand(input);
    }

    // Handle conjunctions and sequences
    final CommandType commandType = detectCommandType(tokens);
    if (commandType != CommandType.SINGLE) {
      return parseMultiCommand(tokens, commandType, input, sessionId, player);
    }

    // Parse single command
    return parseSingleCommand(tokens, input, sessionId, player);
  }

  /**
   * Clears all context for a session (used on restart).
   *
   * @param sessionId the session to clear
   */
  public void clearContext(@Nonnull final String sessionId) {
    contextManager.clearContext(sessionId);
  }


  /**
   * Gets the object resolver for external use.
   */
  @Nonnull
  public ObjectResolver getObjectResolver() {
    return objectResolver;
  }

  /**
   * Gets the vocabulary manager for external use.
   */
  @Nonnull
  public VocabularyManager getVocabularyManager() {
    return vocabularyManager;
  }

  /**
   * Normalizes input by trimming, lowercasing, and cleaning up whitespace.
   */
  @Nonnull
  private String normalizeInput(@Nonnull final String input) {
    return input.trim().toLowerCase().replaceAll("\\s+", " ");
  }

  /**
   * Tokenizes input into words, removing empty tokens and handling punctuation.
   */
  @Nonnull
  private String[] tokenize(@Nonnull final String input) {
    // Replace punctuation with spaced versions for proper tokenization
    final String preprocessed = input.replaceAll("([;&])", " $1 ");

    return Arrays.stream(preprocessed.split("\\s+"))
        .filter(token -> !token.isEmpty())
        .toArray(String[]::new);
  }

  /**
   * Detects the type of command structure.
   */
  @Nonnull
  private CommandType detectCommandType(@Nonnull final String[] tokens) {
    for (final String token : tokens) {
      switch (token) {
        case "and", "&" -> {
          return CommandType.CONJUNCTION;
        }
        case "then", ";" -> {
          return CommandType.SEQUENCE;
        }
      }
    }
    return CommandType.SINGLE;
  }

  /**
   * Parses a single command structure.
   */
  @Nonnull
  private ParsedCommand parseSingleCommand(@Nonnull final String[] tokens,
                                           @Nonnull final String originalInput,
                                           @Nonnull final String sessionId,
                                           @Nonnull final Player player) {
    return parseSingleCommand(tokens, originalInput, sessionId, player, CommandType.SINGLE);
  }

  @Nonnull
  private ParsedCommand parseSingleCommand(@Nonnull final String[] tokens,
                                           @Nonnull final String originalInput,
                                           @Nonnull final String sessionId,
                                           @Nonnull final Player player,
                                           @Nonnull final CommandType commandType) {
    if (tokens.length == 0) {
      return createEmptyCommand(originalInput);
    }

    // Extract and normalize verb
    final String verb = vocabularyManager.normalizeVerb(tokens[0]);

    // Find preposition position if any (context-sensitive for ambiguous words)
    int prepositionIndex = -1;
    String preposition = null;

    for (int i = 1; i < tokens.length; i++) {
      // Use context-sensitive resolution for ambiguous direction/preposition words
      if (vocabularyManager.shouldTreatAsDirection(tokens[i], verb)) {
        // This word should be treated as a direction, not a preposition
        continue;
      } else if (vocabularyManager.isPreposition(tokens[i])) {
        prepositionIndex = i;
        preposition = tokens[i];
        break;
      }
    }

    // Extract direct objects (before preposition)
    final List<String> directObjects = extractObjects(tokens, 1,
        prepositionIndex == -1 ? tokens.length : prepositionIndex, verb);

    // Extract indirect objects (after preposition)
    final List<String> indirectObjects = prepositionIndex == -1 ? new ArrayList<>() :
        extractObjects(tokens, prepositionIndex + 1, tokens.length, verb);

    // Object inference will be handled in GameEngine, not during parsing
    final boolean impliedObject = false;

    return new ParsedCommand(verb, directObjects, indirectObjects, preposition,
        commandType, impliedObject, originalInput);
  }

  /**
   * Parses commands with conjunctions or sequences.
   */
  @Nonnull
  private ParsedCommand parseMultiCommand(@Nonnull final String[] tokens,
                                          @Nonnull final CommandType type,
                                          @Nonnull final String originalInput,
                                          @Nonnull final String sessionId,
                                          @Nonnull final Player player) {
    if (type == CommandType.CONJUNCTION) {
      // Handle "take key and rope" - parse all objects including those after "and"
      return parseSingleCommand(tokens, originalInput, sessionId, player, type);
    }
    // Otherwise, type is SEQUENCE
    // For sequences, parse first command and store the rest for later execution
    final List<String> firstCommandTokens = new ArrayList<>();
    final List<String> remainingCommands = new ArrayList<>();
    boolean foundSequenceWord = false;
    final StringBuilder currentCommand = new StringBuilder();

    for (final String token : tokens) {
      if (isSequenceWord(token)) {
        foundSequenceWord = true;
        if (!currentCommand.isEmpty()) {
          remainingCommands.add(currentCommand.toString().trim());
          currentCommand.setLength(0);
        }
      } else if (foundSequenceWord) {
        if (!currentCommand.isEmpty()) {
          currentCommand.append(" ");
        }
        currentCommand.append(token);
      } else {
        firstCommandTokens.add(token);
      }
    }

    // Add final command if any
    if (!currentCommand.isEmpty()) {
      remainingCommands.add(currentCommand.toString().trim());
    }

    if (firstCommandTokens.isEmpty()) {
      return createEmptyCommand(originalInput);
    }

    final ParsedCommand singleCommand = parseSingleCommand(
        firstCommandTokens.toArray(new String[0]), originalInput, sessionId, player);

    // Return command with sequence information
    return new ParsedCommand(
        singleCommand.getVerb(),
        singleCommand.getDirectObjects(),
        singleCommand.getIndirectObjects(),
        singleCommand.getPreposition(),
        type,
        singleCommand.isImpliedObject(),
        originalInput,
        remainingCommands
    );
  }

  /**
   * Extracts object names from a token range, handling pronouns and articles.
   * Normalizes directions only for movement verbs.
   */
  @Nonnull
  private List<String> extractObjects(@Nonnull final String[] tokens, final int start, final int end, @Nonnull final String verb) {
    final List<String> objects = new ArrayList<>();
    final StringBuilder currentObject = new StringBuilder();

    for (int i = start; i < end; i++) {
      final String token = tokens[i];

      // Skip articles
      if (vocabularyManager.isArticle(token)) {
        continue;
      }

      // Pass through pronouns as-is for GameEngine to resolve later
      if (contextManager.isPronoun(token)) {
        if (currentObject.length() > 0) {
          final String objectName = currentObject.toString().trim();
          objects.add(objectName);
          currentObject.setLength(0);
        }
        objects.add(token); // Add pronoun as-is
        continue;
      }

      // Handle conjunction within object list ("key and rope")
      if (Objects.equals("and", token) || "&".equals(token)) {
        if (currentObject.length() > 0) {
          final String objectName = currentObject.toString().trim();
          // Only normalize directions for movement commands
          if (isMovementVerb(verb)) {
            final String normalizedObject = vocabularyManager.normalizeDirection(objectName);
            objects.add(normalizedObject);
          } else {
            objects.add(objectName);
          }
          currentObject.setLength(0);
        }
        continue;
      }

      // Build multi-word object names
      if (currentObject.length() > 0) {
        currentObject.append(" ");
      }
      currentObject.append(token);
    }

    // Add final object if any
    if (currentObject.length() > 0) {
      final String objectName = currentObject.toString().trim();
      // Only normalize directions for movement commands
      if (isMovementVerb(verb)) {
        final String normalizedObject = vocabularyManager.normalizeDirection(objectName);
        objects.add(normalizedObject);
      } else {
        objects.add(objectName);
      }
    }

    return objects;
  }

  /**
   * Checks if a verb is a movement command that should normalize direction abbreviations.
   */
  private boolean isMovementVerb(@Nonnull final String verb) {
    return "go".equals(verb) || "walk".equals(verb) || "run".equals(verb) ||
        vocabularyManager.isDirection(verb);
  }

  /**
   * Checks if a word is a conjunction word.
   */
  private boolean isConjunctionWord(@Nonnull final String word) {
    return Objects.equals("and", word) || Objects.equals("&", word) ||
        Objects.equals("then", word) || Objects.equals(";", word) ||
        Objects.equals("except", word) || Objects.equals("but", word);
  }

  private boolean isSequenceWord(@Nonnull final String word) {
    return Objects.equals("then", word) || Objects.equals(";", word);
  }

  /**
   * Creates an empty command for invalid input.
   */
  @Nonnull
  private ParsedCommand createEmptyCommand(@Nonnull final String originalInput) {
    return new ParsedCommand("", new ArrayList<>(), new ArrayList<>(), null,
        CommandType.SINGLE, false, originalInput);
  }
}
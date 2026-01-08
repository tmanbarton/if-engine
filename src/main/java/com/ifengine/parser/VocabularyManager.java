package com.ifengine.parser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.ifengine.constants.PrepositionConstants.IN;
import static com.ifengine.constants.PrepositionConstants.INTO;
import static com.ifengine.constants.PrepositionConstants.ON;
import static com.ifengine.constants.PrepositionConstants.ONTO;

/**
 * Manages vocabulary normalization, synonyms, and language understanding.
 * Provides extensible mapping from user input to canonical game commands.
 */
public final class VocabularyManager {

  // Verb synonyms - maps input verbs to canonical verbs
  private final Map<String, String> verbSynonyms = new HashMap<>();

  // Direction synonyms and abbreviations
  private final Map<String, String> directionMappings = new HashMap<>();

  // Articles to strip from input
  private final Set<String> articles = new HashSet<>();

  // Prepositions that we recognize and preserve
  private final Set<String> prepositions = new HashSet<>();

  public VocabularyManager() {
    initializeVerbSynonyms();
    initializeDirectionMappings();
    initializeArticles();
    initializePrepositions();
  }

  /**
   * Normalizes a verb to its canonical form.
   *
   * @param verb the input verb
   * @return the canonical verb form
   */
  @Nonnull
  public String normalizeVerb(@Nonnull final String verb) {
    final String lower = verb.toLowerCase().trim();
    return verbSynonyms.getOrDefault(lower, lower);
  }

  /**
   * Normalizes a direction to its canonical form.
   *
   * @param direction the input direction
   * @return the canonical direction form
   */
  @Nonnull
  public String normalizeDirection(@Nonnull final String direction) {
    final String lower = direction.toLowerCase().trim();
    return directionMappings.getOrDefault(lower, lower);
  }

  /**
   * Checks if a word is an article that should be stripped.
   *
   * @param word the word to check
   * @return true if it's an article
   */
  public boolean isArticle(@Nonnull final String word) {
    return articles.contains(word.toLowerCase());
  }

  /**
   * Checks if a word is a preposition.
   *
   * @param word the word to check
   * @return true if it's a preposition
   */
  public boolean isPreposition(@Nonnull final String word) {
    return prepositions.contains(word.toLowerCase());
  }

  /**
   * Checks if a word is a direction.
   *
   * @param word the word to check
   * @return true if it's a valid direction
   */
  public boolean isDirection(@Nonnull final String word) {
    return directionMappings.containsKey(word.toLowerCase());
  }

  /**
   * Resolves ambiguous words that can be both directions and prepositions.
   * Context: If preceded by movement verb, treat as direction. Otherwise, treat as preposition.
   *
   * @param word the ambiguous word
   * @param precedingVerb the verb that precedes this word (null if none)
   * @return true if should be treated as direction, false if preposition
   */
  public boolean shouldTreatAsDirection(@Nonnull final String word, @Nullable final String precedingVerb) {
    final String lower = word.toLowerCase();

    // Words that can be both directions and prepositions
    if (!(Objects.equals("up", lower) || Objects.equals("down", lower) || Objects.equals("to", lower) ||
        Objects.equals("in", lower) || Objects.equals("out", lower))) {
      // Not an ambiguous word - use default classification
      return isDirection(word);
    }

    // If preceded by movement verb, treat as direction
    if (precedingVerb != null) {
      final String normalizedVerb = normalizeVerb(precedingVerb);
      if (Objects.equals("go", normalizedVerb)) {
        return true;
      }
    }

    // Default: treat "to", "in", "out" as prepositions when used with non-movement verbs,
    // and "up"/"down" as directions when standalone
    return switch (lower) {
      case "up", "down" -> true;  // Always directions when standalone
      case "to", "in", "out" -> false;  // Prepositions when not preceded by movement verb
      default -> isDirection(word);
    };
  }

  /**
   * Removes articles from a phrase while preserving other words.
   *
   * @param phrase the input phrase
   * @return the phrase with articles removed
   */
  @Nonnull
  public String stripArticles(@Nonnull final String phrase) {
    final String[] words = phrase.trim().split("\\s+");
    final StringBuilder result = new StringBuilder();

    for (final String word : words) {
      if (!isArticle(word)) {
        if (!result.isEmpty()) {
          result.append(" ");
        }
        result.append(word);
      }
    }

    return result.toString();
  }

  /**
   * Adds a new verb synonym mapping.
   *
   * @param synonym the synonym to add
   * @param canonical the canonical form it maps to
   */
  public void addVerbSynonym(@Nonnull final String synonym, @Nonnull final String canonical) {
    verbSynonyms.put(synonym.toLowerCase(), canonical.toLowerCase());
  }

  /**
   * Adds a new direction mapping.
   *
   * @param input the input form
   * @param canonical the canonical direction
   */
  public void addDirectionMapping(@Nonnull final String input, @Nonnull final String canonical) {
    directionMappings.put(input.toLowerCase(), canonical.toLowerCase());
  }

  /**
   * Validates if a verb-preposition combination is semantically valid in English.
   * <p>
   * This method enforces natural language rules to prevent nonsensical commands.
   * For example, "put key on table" makes sense (put + on), but "take key on table"
   * doesn't (take + on). The validation maintains a whitelist of grammatically and
   * semantically correct combinations for known verbs.
   *
   * @param verb the verb to validate (will be normalized internally)
   * @param preposition the preposition to validate
   * @return true if the verb-preposition combination is semantically valid, false otherwise
   */
  public boolean isValidVerbPrepositionCombination(@Nonnull final String verb, @Nonnull final String preposition) {
    final String normalizedVerb = normalizeVerb(verb);
    final String normalizedPrep = preposition.toLowerCase();

    return switch (normalizedVerb) {
      case "put" ->
          // Put works with spatial prepositions for placement
          Objects.equals(IN, normalizedPrep) || Objects.equals(INTO, normalizedPrep) ||
              Objects.equals(ON, normalizedPrep) || Objects.equals(ONTO, normalizedPrep);
      case "take" ->
          // Take works with "from" for specifying source
          Objects.equals("from", normalizedPrep);
      case "look" ->
          // Look works with "at" for examining objects and "around" for surveying surroundings
          Objects.equals("at", normalizedPrep) || Objects.equals("around", normalizedPrep);
      case "open", "unlock" ->
          // These verbs work with "with" for tools
          Objects.equals("with", normalizedPrep) || Objects.equals("using", normalizedPrep);
      // Allow all combinations for other verbs (extensible design)
      default -> true;
    };
  }

  private void initializeVerbSynonyms() {
    // Movement synonyms
    verbSynonyms.put("go", "go");
    verbSynonyms.put("move", "go");
    verbSynonyms.put("walk", "go");
    verbSynonyms.put("run", "go");

    // Direction synonyms as verbs (for single direction commands)
    verbSynonyms.put("north", "north");
    verbSynonyms.put("n", "north");
    verbSynonyms.put("south", "south");
    verbSynonyms.put("s", "south");
    verbSynonyms.put("east", "east");
    verbSynonyms.put("e", "east");
    verbSynonyms.put("west", "west");
    verbSynonyms.put("w", "west");
    verbSynonyms.put("up", "up");
    verbSynonyms.put("u", "up");
    verbSynonyms.put("down", "down");
    verbSynonyms.put("d", "down");
    verbSynonyms.put("northeast", "northeast");
    verbSynonyms.put("ne", "northeast");
    verbSynonyms.put("northwest", "northwest");
    verbSynonyms.put("nw", "northwest");
    verbSynonyms.put("southeast", "southeast");
    verbSynonyms.put("se", "southeast");
    verbSynonyms.put("southwest", "southwest");
    verbSynonyms.put("sw", "southwest");
    verbSynonyms.put("in", "in");
    verbSynonyms.put("out", "out");

    // Looking synonyms
    verbSynonyms.put("look", "look");
    verbSynonyms.put("l", "look");
    verbSynonyms.put("x", "look");
    verbSynonyms.put("examine", "look");

    // Reading synonyms
    verbSynonyms.put("read", "read");

    // Taking synonyms
    verbSynonyms.put("take", "take");
    verbSynonyms.put("get", "take");
    verbSynonyms.put("grab", "take");
    verbSynonyms.put("pick", "take");
    verbSynonyms.put("pickup", "take");

    // Dropping synonyms
    verbSynonyms.put("drop", "drop");
    verbSynonyms.put("set", "drop");
    verbSynonyms.put("leave", "drop");

    // Put synonyms (different from drop when used with prepositions)
    verbSynonyms.put("put", "put");

    // Inventory synonyms
    verbSynonyms.put("inventory", "inventory");
    verbSynonyms.put("inv", "inventory");
    verbSynonyms.put("i", "inventory");

    // Opening synonyms
    verbSynonyms.put("open", "open");
    verbSynonyms.put("unlock", "unlock");
    verbSynonyms.put("turn", "turn");

    // Help synonyms
    verbSynonyms.put("help", "help");
    verbSynonyms.put("h", "help");

    // Info synonyms
    verbSynonyms.put("info", "info");
    verbSynonyms.put("information", "info");

    // Quit synonyms
    verbSynonyms.put("quit", "quit");
    verbSynonyms.put("exit", "quit");
    verbSynonyms.put("q", "quit");

    // Status synonyms
    verbSynonyms.put("status", "status");
    verbSynonyms.put("stats", "status");

    // Restart synonyms
    verbSynonyms.put("restart", "restart");
    verbSynonyms.put("reset", "restart");
  }

  private void initializeDirectionMappings() {
    // Cardinal directions
    directionMappings.put("north", "north");
    directionMappings.put("n", "north");
    directionMappings.put("south", "south");
    directionMappings.put("s", "south");
    directionMappings.put("east", "east");
    directionMappings.put("e", "east");
    directionMappings.put("west", "west");
    directionMappings.put("w", "west");
    // Diagonal directions
    directionMappings.put("northeast", "northeast");
    directionMappings.put("ne", "northeast");
    directionMappings.put("northwest", "northwest");
    directionMappings.put("nw", "northwest");
    directionMappings.put("southeast", "southeast");
    directionMappings.put("se", "southeast");
    directionMappings.put("southwest", "southwest");
    directionMappings.put("sw", "southwest");

    // Vertical directions
    directionMappings.put("up", "up");
    directionMappings.put("u", "up");
    directionMappings.put("down", "down");
    directionMappings.put("d", "down");

    // In/out
    directionMappings.put("in", "in");
    directionMappings.put("out", "out");
  }

  private void initializeArticles() {
    articles.addAll(Arrays.asList("a", "an", "the", "some", "any"));
  }

  private void initializePrepositions() {
    prepositions.addAll(Arrays.asList(
        // Spatial prepositions
        "in", "on", "under", "behind", "above", "below", "inside", "outside", "around",

        // Directional prepositions (context-sensitive ones: "up", "down", "to" handled separately)
        "from", "into", "onto", "toward", "towards",

        // Relational prepositions
        "with", "using", "by", "for", "against", "about", "at",

        // Ambiguous words that can be prepositions (resolved by context)
        "up", "down", "to"
    ));
  }
}
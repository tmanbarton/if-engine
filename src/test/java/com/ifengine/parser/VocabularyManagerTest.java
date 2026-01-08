package com.ifengine.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for VocabularyManager.
 */
@DisplayName("VocabularyManager Tests")
class VocabularyManagerTest {

  private VocabularyManager vocabularyManager;

  @BeforeEach
  void setUp() {
    vocabularyManager = new VocabularyManager();
  }

  @Nested
  @DisplayName("Verb Normalization")
  class VerbNormalization {

    @Test
    @DisplayName("Test normalizeVerb - get normalizes to take")
    void testNormalizeVerb_get() {
      final String result = vocabularyManager.normalizeVerb("get");

      assertEquals("take", result);
    }

    @Test
    @DisplayName("Test normalizeVerb - grab normalizes to take")
    void testNormalizeVerb_grab() {
      final String result = vocabularyManager.normalizeVerb("grab");

      assertEquals("take", result);
    }

    @Test
    @DisplayName("Test normalizeVerb - pick normalizes to take")
    void testNormalizeVerb_pick() {
      final String result = vocabularyManager.normalizeVerb("pick");

      assertEquals("take", result);
    }

    @Test
    @DisplayName("Test normalizeVerb - x normalizes to look")
    void testNormalizeVerb_x() {
      final String result = vocabularyManager.normalizeVerb("x");

      assertEquals("look", result);
    }

    @Test
    @DisplayName("Test normalizeVerb - examine normalizes to look")
    void testNormalizeVerb_examine() {
      final String result = vocabularyManager.normalizeVerb("examine");

      assertEquals("look", result);
    }

    @Test
    @DisplayName("Test normalizeVerb - l normalizes to look")
    void testNormalizeVerb_l() {
      final String result = vocabularyManager.normalizeVerb("l");

      assertEquals("look", result);
    }

    @Test
    @DisplayName("Test normalizeVerb - inv normalizes to inventory")
    void testNormalizeVerb_inv() {
      final String result = vocabularyManager.normalizeVerb("inv");

      assertEquals("inventory", result);
    }

    @Test
    @DisplayName("Test normalizeVerb - i normalizes to inventory")
    void testNormalizeVerb_i() {
      final String result = vocabularyManager.normalizeVerb("i");

      assertEquals("inventory", result);
    }

    @Test
    @DisplayName("Test normalizeVerb - already normalized verb")
    void testNormalizeVerb_alreadyNormalized() {
      final String result = vocabularyManager.normalizeVerb("take");

      assertEquals("take", result);
    }

    @Test
    @DisplayName("Test normalizeVerb - unknown verb passes through")
    void testNormalizeVerb_unknown() {
      final String result = vocabularyManager.normalizeVerb("xyzzy");

      assertEquals("xyzzy", result);
    }

    @Test
    @DisplayName("Test normalizeVerb - handles mixed case")
    void testNormalizeVerb_mixedCase() {
      final String result = vocabularyManager.normalizeVerb("GET");

      assertEquals("take", result);
    }

    @Test
    @DisplayName("Test normalizeVerb - move normalizes to go")
    void testNormalizeVerb_move() {
      final String result = vocabularyManager.normalizeVerb("move");

      assertEquals("go", result);
    }

    @Test
    @DisplayName("Test normalizeVerb - walk normalizes to go")
    void testNormalizeVerb_walk() {
      final String result = vocabularyManager.normalizeVerb("walk");

      assertEquals("go", result);
    }

    @Test
    @DisplayName("Test normalizeVerb - quit synonyms")
    void testNormalizeVerb_quitSynonyms() {
      assertEquals("quit", vocabularyManager.normalizeVerb("exit"));
      assertEquals("quit", vocabularyManager.normalizeVerb("q"));
    }
  }

  @Nested
  @DisplayName("Direction Normalization")
  class DirectionNormalization {

    @Test
    @DisplayName("Test normalizeDirection - full name passes through")
    void testNormalizeDirection_fullName() {
      final String result = vocabularyManager.normalizeDirection("north");

      assertEquals("north", result);
    }

    @Test
    @DisplayName("Test normalizeDirection - n abbreviation")
    void testNormalizeDirection_nAbbreviation() {
      final String result = vocabularyManager.normalizeDirection("n");

      assertEquals("north", result);
    }

    @Test
    @DisplayName("Test normalizeDirection - s abbreviation")
    void testNormalizeDirection_sAbbreviation() {
      final String result = vocabularyManager.normalizeDirection("s");

      assertEquals("south", result);
    }

    @Test
    @DisplayName("Test normalizeDirection - e abbreviation")
    void testNormalizeDirection_eAbbreviation() {
      final String result = vocabularyManager.normalizeDirection("e");

      assertEquals("east", result);
    }

    @Test
    @DisplayName("Test normalizeDirection - w abbreviation")
    void testNormalizeDirection_wAbbreviation() {
      final String result = vocabularyManager.normalizeDirection("w");

      assertEquals("west", result);
    }

    @Test
    @DisplayName("Test normalizeDirection - diagonal ne")
    void testNormalizeDirection_diagonalNe() {
      final String result = vocabularyManager.normalizeDirection("ne");

      assertEquals("northeast", result);
    }

    @Test
    @DisplayName("Test normalizeDirection - diagonal nw")
    void testNormalizeDirection_diagonalNw() {
      final String result = vocabularyManager.normalizeDirection("nw");

      assertEquals("northwest", result);
    }

    @Test
    @DisplayName("Test normalizeDirection - diagonal se")
    void testNormalizeDirection_diagonalSe() {
      final String result = vocabularyManager.normalizeDirection("se");

      assertEquals("southeast", result);
    }

    @Test
    @DisplayName("Test normalizeDirection - diagonal sw")
    void testNormalizeDirection_diagonalSw() {
      final String result = vocabularyManager.normalizeDirection("sw");

      assertEquals("southwest", result);
    }

    @Test
    @DisplayName("Test normalizeDirection - u abbreviation for up")
    void testNormalizeDirection_uAbbreviation() {
      final String result = vocabularyManager.normalizeDirection("u");

      assertEquals("up", result);
    }

    @Test
    @DisplayName("Test normalizeDirection - d abbreviation for down")
    void testNormalizeDirection_dAbbreviation() {
      final String result = vocabularyManager.normalizeDirection("d");

      assertEquals("down", result);
    }

    @Test
    @DisplayName("Test normalizeDirection - invalid direction passes through")
    void testNormalizeDirection_invalid() {
      final String result = vocabularyManager.normalizeDirection("sideways");

      assertEquals("sideways", result);
    }
  }

  @Nested
  @DisplayName("Direction Checking")
  class DirectionChecking {

    @Test
    @DisplayName("Test isDirection - cardinal directions")
    void testIsDirection_cardinal() {
      assertTrue(vocabularyManager.isDirection("north"));
      assertTrue(vocabularyManager.isDirection("south"));
      assertTrue(vocabularyManager.isDirection("east"));
      assertTrue(vocabularyManager.isDirection("west"));
    }

    @Test
    @DisplayName("Test isDirection - abbreviations")
    void testIsDirection_abbreviations() {
      assertTrue(vocabularyManager.isDirection("n"));
      assertTrue(vocabularyManager.isDirection("s"));
      assertTrue(vocabularyManager.isDirection("e"));
      assertTrue(vocabularyManager.isDirection("w"));
    }

    @Test
    @DisplayName("Test isDirection - diagonal directions")
    void testIsDirection_diagonal() {
      assertTrue(vocabularyManager.isDirection("northeast"));
      assertTrue(vocabularyManager.isDirection("ne"));
      assertTrue(vocabularyManager.isDirection("northwest"));
      assertTrue(vocabularyManager.isDirection("nw"));
    }

    @Test
    @DisplayName("Test isDirection - vertical directions")
    void testIsDirection_vertical() {
      assertTrue(vocabularyManager.isDirection("up"));
      assertTrue(vocabularyManager.isDirection("down"));
      assertTrue(vocabularyManager.isDirection("u"));
      assertTrue(vocabularyManager.isDirection("d"));
    }

    @Test
    @DisplayName("Test isDirection - in and out")
    void testIsDirection_inOut() {
      assertTrue(vocabularyManager.isDirection("in"));
      assertTrue(vocabularyManager.isDirection("out"));
    }

    @Test
    @DisplayName("Test isDirection - non-direction word")
    void testIsDirection_nonDirection() {
      assertFalse(vocabularyManager.isDirection("key"));
      assertFalse(vocabularyManager.isDirection("take"));
    }
  }

  @Nested
  @DisplayName("Ambiguous Word Resolution")
  class AmbiguousWordResolution {

    @Test
    @DisplayName("Test shouldTreatAsDirection - up with go verb")
    void testShouldTreatAsDirection_upWithGo() {
      final boolean result = vocabularyManager.shouldTreatAsDirection("up", "go");

      assertTrue(result);
    }

    @Test
    @DisplayName("Test shouldTreatAsDirection - up with non-movement verb")
    void testShouldTreatAsDirection_upWithNonMovementVerb() {
      // up should still be treated as direction by default when standalone
      final boolean result = vocabularyManager.shouldTreatAsDirection("up", "take");

      assertTrue(result);
    }

    @Test
    @DisplayName("Test shouldTreatAsDirection - in with go verb")
    void testShouldTreatAsDirection_inWithGo() {
      final boolean result = vocabularyManager.shouldTreatAsDirection("in", "go");

      assertTrue(result);
    }

    @Test
    @DisplayName("Test shouldTreatAsDirection - in with put verb")
    void testShouldTreatAsDirection_inWithPut() {
      final boolean result = vocabularyManager.shouldTreatAsDirection("in", "put");

      assertFalse(result);
    }

    @Test
    @DisplayName("Test shouldTreatAsDirection - down with go verb")
    void testShouldTreatAsDirection_downWithGo() {
      final boolean result = vocabularyManager.shouldTreatAsDirection("down", "go");

      assertTrue(result);
    }

    @Test
    @DisplayName("Test shouldTreatAsDirection - out with go verb")
    void testShouldTreatAsDirection_outWithGo() {
      final boolean result = vocabularyManager.shouldTreatAsDirection("out", "go");

      assertTrue(result);
    }

    @Test
    @DisplayName("Test shouldTreatAsDirection - to with go verb")
    void testShouldTreatAsDirection_toWithGo() {
      final boolean result = vocabularyManager.shouldTreatAsDirection("to", "go");

      assertTrue(result);
    }

    @Test
    @DisplayName("Test shouldTreatAsDirection - non-ambiguous direction")
    void testShouldTreatAsDirection_nonAmbiguous() {
      final boolean result = vocabularyManager.shouldTreatAsDirection("north", "take");

      assertTrue(result);
    }
  }

  @Nested
  @DisplayName("Article and Preposition Detection")
  class ArticlePrepositionDetection {

    @Test
    @DisplayName("Test isArticle - the")
    void testIsArticle_the() {
      assertTrue(vocabularyManager.isArticle("the"));
    }

    @Test
    @DisplayName("Test isArticle - a")
    void testIsArticle_a() {
      assertTrue(vocabularyManager.isArticle("a"));
    }

    @Test
    @DisplayName("Test isArticle - an")
    void testIsArticle_an() {
      assertTrue(vocabularyManager.isArticle("an"));
    }

    @Test
    @DisplayName("Test isArticle - some")
    void testIsArticle_some() {
      assertTrue(vocabularyManager.isArticle("some"));
    }

    @Test
    @DisplayName("Test isArticle - non-article word")
    void testIsArticle_nonArticle() {
      assertFalse(vocabularyManager.isArticle("key"));
    }

    @Test
    @DisplayName("Test isPreposition - in")
    void testIsPreposition_in() {
      assertTrue(vocabularyManager.isPreposition("in"));
    }

    @Test
    @DisplayName("Test isPreposition - on")
    void testIsPreposition_on() {
      assertTrue(vocabularyManager.isPreposition("on"));
    }

    @Test
    @DisplayName("Test isPreposition - with")
    void testIsPreposition_with() {
      assertTrue(vocabularyManager.isPreposition("with"));
    }

    @Test
    @DisplayName("Test isPreposition - at")
    void testIsPreposition_at() {
      assertTrue(vocabularyManager.isPreposition("at"));
    }

    @Test
    @DisplayName("Test isPreposition - non-preposition word")
    void testIsPreposition_nonPreposition() {
      assertFalse(vocabularyManager.isPreposition("key"));
    }
  }

  @Nested
  @DisplayName("Semantic Validation")
  class SemanticValidation {

    @Test
    @DisplayName("Test isValidVerbPreposition - put on is valid")
    void testIsValidVerbPreposition_putOn() {
      final boolean result = vocabularyManager.isValidVerbPrepositionCombination("put", "on");

      assertTrue(result);
    }

    @Test
    @DisplayName("Test isValidVerbPreposition - put in is valid")
    void testIsValidVerbPreposition_putIn() {
      final boolean result = vocabularyManager.isValidVerbPrepositionCombination("put", "in");

      assertTrue(result);
    }

    @Test
    @DisplayName("Test isValidVerbPreposition - put into is valid")
    void testIsValidVerbPreposition_putInto() {
      final boolean result = vocabularyManager.isValidVerbPrepositionCombination("put", "into");

      assertTrue(result);
    }

    @Test
    @DisplayName("Test isValidVerbPreposition - put onto is valid")
    void testIsValidVerbPreposition_putOnto() {
      final boolean result = vocabularyManager.isValidVerbPrepositionCombination("put", "onto");

      assertTrue(result);
    }

    @Test
    @DisplayName("Test isValidVerbPreposition - look at is valid")
    void testIsValidVerbPreposition_lookAt() {
      final boolean result = vocabularyManager.isValidVerbPrepositionCombination("look", "at");

      assertTrue(result);
    }

    @Test
    @DisplayName("Test isValidVerbPreposition - look around is valid")
    void testIsValidVerbPreposition_lookAround() {
      final boolean result = vocabularyManager.isValidVerbPrepositionCombination("look", "around");

      assertTrue(result);
    }

    @Test
    @DisplayName("Test isValidVerbPreposition - look in is invalid")
    void testIsValidVerbPreposition_lookIn() {
      final boolean result = vocabularyManager.isValidVerbPrepositionCombination("look", "in");

      assertFalse(result);
    }

    @Test
    @DisplayName("Test isValidVerbPreposition - open with is valid")
    void testIsValidVerbPreposition_openWith() {
      final boolean result = vocabularyManager.isValidVerbPrepositionCombination("open", "with");

      assertTrue(result);
    }

    @Test
    @DisplayName("Test isValidVerbPreposition - unlock with is valid")
    void testIsValidVerbPreposition_unlockWith() {
      final boolean result = vocabularyManager.isValidVerbPrepositionCombination("unlock", "with");

      assertTrue(result);
    }

    @Test
    @DisplayName("Test isValidVerbPreposition - unknown verb allows any preposition")
    void testIsValidVerbPreposition_unknownVerb() {
      final boolean result = vocabularyManager.isValidVerbPrepositionCombination("dance", "around");

      assertTrue(result);
    }
  }

  @Nested
  @DisplayName("Strip Articles")
  class StripArticles {

    @Test
    @DisplayName("Test stripArticles - removes the")
    void testStripArticles_removesThe() {
      final String result = vocabularyManager.stripArticles("the key");

      assertEquals("key", result);
    }

    @Test
    @DisplayName("Test stripArticles - removes a")
    void testStripArticles_removesA() {
      final String result = vocabularyManager.stripArticles("a key");

      assertEquals("key", result);
    }

    @Test
    @DisplayName("Test stripArticles - removes multiple articles")
    void testStripArticles_removesMultiple() {
      final String result = vocabularyManager.stripArticles("the old key");

      assertEquals("old key", result);
    }

    @Test
    @DisplayName("Test stripArticles - no articles to remove")
    void testStripArticles_noArticles() {
      final String result = vocabularyManager.stripArticles("rusty key");

      assertEquals("rusty key", result);
    }
  }

  @Nested
  @DisplayName("Custom Vocabulary")
  class CustomVocabulary {

    @Test
    @DisplayName("Test addVerbSynonym - adds custom synonym")
    void testAddVerbSynonym() {
      vocabularyManager.addVerbSynonym("snatch", "take");

      final String result = vocabularyManager.normalizeVerb("snatch");
      assertEquals("take", result);
    }

    @Test
    @DisplayName("Test addDirectionMapping - adds custom direction")
    void testAddDirectionMapping() {
      vocabularyManager.addDirectionMapping("fore", "north");

      final String result = vocabularyManager.normalizeDirection("fore");
      assertEquals("north", result);
    }
  }
}
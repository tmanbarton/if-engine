package io.github.tmanbarton.ifengine.parser;

import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.test.TestItemFactory;
import io.github.tmanbarton.ifengine.test.TestLocationFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for CommandParser.
 */
@DisplayName("CommandParser Tests")
class CommandParserTest {

  private CommandParser parser;
  private Player player;
  private static final String SESSION_ID = "test-session";

  @BeforeEach
  void setUp() {
    parser = new CommandParser();
    final Location location = TestLocationFactory.createDefaultLocation();
    player = new Player(location);
  }

  @Nested
  @DisplayName("Simple Command Parsing")
  class SimpleCommandParsing {

    @Test
    @DisplayName("Test parse - single verb command")
    void testParse_singleVerbCommand() {
      final ParsedCommand result = parser.parseCommand("look", SESSION_ID, player);

      assertEquals("look", result.getVerb());
      assertFalse(result.hasDirectObjects());
      assertFalse(result.hasIndirectObjects());
      assertNull(result.getPreposition());
      assertEquals(CommandType.SINGLE, result.getType());
    }

    @Test
    @DisplayName("Test parse - inventory verb")
    void testParse_inventoryVerb() {
      final ParsedCommand result = parser.parseCommand("inventory", SESSION_ID, player);

      assertEquals("inventory", result.getVerb());
      assertFalse(result.hasDirectObjects());
    }

    @Test
    @DisplayName("Test parse - verb and object")
    void testParse_verbAndObject() {
      final ParsedCommand result = parser.parseCommand("take key", SESSION_ID, player);

      assertEquals("take", result.getVerb());
      assertTrue(result.hasDirectObjects());
      assertEquals("key", result.getFirstDirectObject());
    }

    @Test
    @DisplayName("Test parse - drop command with object")
    void testParse_dropCommand() {
      final ParsedCommand result = parser.parseCommand("drop rope", SESSION_ID, player);

      assertEquals("drop", result.getVerb());
      assertEquals("rope", result.getFirstDirectObject());
    }

    @Test
    @DisplayName("Test parse - verb with multi-word object")
    void testParse_verbWithMultiWordObject() {
      final ParsedCommand result = parser.parseCommand("take rusty key", SESSION_ID, player);

      assertEquals("take", result.getVerb());
      assertEquals("rusty key", result.getFirstDirectObject());
    }

    @Test
    @DisplayName("Test parse - empty input")
    void testParse_emptyInput() {
      final ParsedCommand result = parser.parseCommand("", SESSION_ID, player);

      assertEquals("", result.getVerb());
      assertFalse(result.hasDirectObjects());
    }

    @Test
    @DisplayName("Test parse - whitespace only input")
    void testParse_whitespaceOnlyInput() {
      final ParsedCommand result = parser.parseCommand("   ", SESSION_ID, player);

      assertEquals("", result.getVerb());
      assertFalse(result.hasDirectObjects());
    }

    @Test
    @DisplayName("Test parse - mixed case input")
    void testParse_mixedCaseInput() {
      final ParsedCommand result = parser.parseCommand("TaKe KeY", SESSION_ID, player);

      assertEquals("take", result.getVerb());
      assertEquals("key", result.getFirstDirectObject());
    }

    @Test
    @DisplayName("Test parse - strips articles from input")
    void testParse_stripsArticles() {
      final ParsedCommand result = parser.parseCommand("take the key", SESSION_ID, player);

      assertEquals("take", result.getVerb());
      assertEquals("key", result.getFirstDirectObject());
    }
  }

  @Nested
  @DisplayName("Direction Parsing")
  class DirectionParsing {

    @Test
    @DisplayName("Test parse - full direction with go")
    void testParse_fullDirectionWithGo() {
      final ParsedCommand result = parser.parseCommand("go north", SESSION_ID, player);

      assertEquals("go", result.getVerb());
      assertEquals("north", result.getFirstDirectObject());
    }

    @Test
    @DisplayName("Test parse - go south")
    void testParse_goSouth() {
      final ParsedCommand result = parser.parseCommand("go south", SESSION_ID, player);

      assertEquals("go", result.getVerb());
      assertEquals("south", result.getFirstDirectObject());
    }

    @Test
    @DisplayName("Test parse - direction abbreviation")
    void testParse_directionAbbreviation() {
      final ParsedCommand result = parser.parseCommand("go n", SESSION_ID, player);

      assertEquals("go", result.getVerb());
      assertEquals("north", result.getFirstDirectObject());
    }

    @Test
    @DisplayName("Test parse - east abbreviation")
    void testParse_eastAbbreviation() {
      final ParsedCommand result = parser.parseCommand("go e", SESSION_ID, player);

      assertEquals("go", result.getVerb());
      assertEquals("east", result.getFirstDirectObject());
    }

    @Test
    @DisplayName("Test parse - implicit movement command")
    void testParse_implicitMovement() {
      final ParsedCommand result = parser.parseCommand("north", SESSION_ID, player);

      assertEquals("north", result.getVerb());
    }

    @Test
    @DisplayName("Test parse - implicit south")
    void testParse_implicitSouth() {
      final ParsedCommand result = parser.parseCommand("south", SESSION_ID, player);

      assertEquals("south", result.getVerb());
    }

    @Test
    @DisplayName("Test parse - diagonal directions full name")
    void testParse_diagonalDirectionsFull() {
      final ParsedCommand result = parser.parseCommand("go northeast", SESSION_ID, player);

      assertEquals("go", result.getVerb());
      assertEquals("northeast", result.getFirstDirectObject());
    }

    @Test
    @DisplayName("Test parse - diagonal direction abbreviation")
    void testParse_diagonalAbbreviation() {
      final ParsedCommand result = parser.parseCommand("go ne", SESSION_ID, player);

      assertEquals("go", result.getVerb());
      assertEquals("northeast", result.getFirstDirectObject());
    }

    @Test
    @DisplayName("Test parse - up direction")
    void testParse_upDirection() {
      final ParsedCommand result = parser.parseCommand("go up", SESSION_ID, player);

      assertEquals("go", result.getVerb());
      assertEquals("up", result.getFirstDirectObject());
    }

    @Test
    @DisplayName("Test parse - down direction")
    void testParse_downDirection() {
      final ParsedCommand result = parser.parseCommand("go down", SESSION_ID, player);

      assertEquals("go", result.getVerb());
      assertEquals("down", result.getFirstDirectObject());
    }
  }

  @Nested
  @DisplayName("Preposition Handling")
  class PrepositionHandling {

    @Test
    @DisplayName("Test parse - verb object preposition target")
    void testParse_verbObjectPrepositionTarget() {
      final ParsedCommand result = parser.parseCommand("put key in box", SESSION_ID, player);

      assertEquals("put", result.getVerb());
      assertEquals("key", result.getFirstDirectObject());
      assertEquals("in", result.getPreposition());
      assertEquals("box", result.getFirstIndirectObject());
    }

    @Test
    @DisplayName("Test parse - preposition on")
    void testParse_prepositionOn() {
      final ParsedCommand result = parser.parseCommand("put key on table", SESSION_ID, player);

      assertEquals("put", result.getVerb());
      assertEquals("key", result.getFirstDirectObject());
      assertEquals("on", result.getPreposition());
      assertEquals("table", result.getFirstIndirectObject());
    }

    @Test
    @DisplayName("Test parse - look at object")
    void testParse_lookAtObject() {
      final ParsedCommand result = parser.parseCommand("look at painting", SESSION_ID, player);

      assertEquals("look", result.getVerb());
      assertTrue(result.hasPreposition());
      assertEquals("at", result.getPreposition());
      assertEquals("painting", result.getFirstIndirectObject());
    }

    @Test
    @DisplayName("Test parse - up as direction with go")
    void testParse_upAsDirectionWithGo() {
      final ParsedCommand result = parser.parseCommand("go up", SESSION_ID, player);

      assertEquals("go", result.getVerb());
      assertEquals("up", result.getFirstDirectObject());
      // up should be treated as direction, not preposition
      assertFalse(result.hasPreposition());
    }

    @Test
    @DisplayName("Test parse - in as direction with go")
    void testParse_inAsDirectionWithGo() {
      final ParsedCommand result = parser.parseCommand("go in", SESSION_ID, player);

      assertEquals("go", result.getVerb());
      assertEquals("in", result.getFirstDirectObject());
    }
  }

  @Nested
  @DisplayName("Command Type Classification")
  class CommandTypeClassification {

    @Test
    @DisplayName("Test parse - single command type")
    void testParse_singleCommandType() {
      final ParsedCommand result = parser.parseCommand("take key", SESSION_ID, player);

      assertEquals(CommandType.SINGLE, result.getType());
    }

    @Test
    @DisplayName("Test parse - conjunction command type")
    void testParse_conjunctionCommandType() {
      final ParsedCommand result = parser.parseCommand("take key and rope", SESSION_ID, player);

      assertEquals(CommandType.CONJUNCTION, result.getType());
    }

    @Test
    @DisplayName("Test parse - sequence command type")
    void testParse_sequenceCommandType() {
      final ParsedCommand result = parser.parseCommand("go north then take key", SESSION_ID, player);

      assertEquals(CommandType.SEQUENCE, result.getType());
    }
  }

  @Nested
  @DisplayName("Conjunction Parsing")
  class ConjunctionParsing {

    @Test
    @DisplayName("Test parse - and conjunction")
    void testParse_andConjunction() {
      final ParsedCommand result = parser.parseCommand("take key and rope", SESSION_ID, player);

      assertEquals("take", result.getVerb());
      assertEquals(CommandType.CONJUNCTION, result.getType());
      assertEquals(2, result.getDirectObjects().size());
      assertTrue(result.getDirectObjects().contains("key"));
      assertTrue(result.getDirectObjects().contains("rope"));
    }

    @Test
    @DisplayName("Test parse - multiple conjunctions")
    void testParse_multipleConjunctions() {
      final ParsedCommand result = parser.parseCommand("take key and rope and gem", SESSION_ID, player);

      assertEquals("take", result.getVerb());
      assertEquals(3, result.getDirectObjects().size());
      assertTrue(result.getDirectObjects().contains("key"));
      assertTrue(result.getDirectObjects().contains("rope"));
      assertTrue(result.getDirectObjects().contains("gem"));
    }

    @Test
    @DisplayName("Test parse - ampersand as conjunction")
    void testParse_ampersandConjunction() {
      final ParsedCommand result = parser.parseCommand("take key & rope", SESSION_ID, player);

      assertEquals("take", result.getVerb());
      assertEquals(CommandType.CONJUNCTION, result.getType());
      assertEquals(2, result.getDirectObjects().size());
    }
  }

  @Nested
  @DisplayName("Sequence Parsing")
  class SequenceParsing {

    @Test
    @DisplayName("Test parse - then sequence")
    void testParse_thenSequence() {
      final ParsedCommand result = parser.parseCommand("go north then take key", SESSION_ID, player);

      assertEquals("go", result.getVerb());
      assertEquals("north", result.getFirstDirectObject());
      assertEquals(CommandType.SEQUENCE, result.getType());
      assertTrue(result.hasSequenceCommands());
      assertEquals(1, result.getSequenceCommands().size());
      assertEquals("take key", result.getSequenceCommands().get(0));
    }

    @Test
    @DisplayName("Test parse - complex sequence")
    void testParse_complexSequence() {
      final ParsedCommand result = parser.parseCommand("go north then take key then go south", SESSION_ID, player);

      assertEquals("go", result.getVerb());
      assertEquals(CommandType.SEQUENCE, result.getType());
      assertEquals(2, result.getSequenceCommands().size());
      assertEquals("take key", result.getSequenceCommands().get(0));
      assertEquals("go south", result.getSequenceCommands().get(1));
    }

    @Test
    @DisplayName("Test parse - semicolon as sequence")
    void testParse_semicolonSequence() {
      final ParsedCommand result = parser.parseCommand("go north ; take key", SESSION_ID, player);

      assertEquals("go", result.getVerb());
      assertEquals(CommandType.SEQUENCE, result.getType());
      assertTrue(result.hasSequenceCommands());
    }
  }

  @Nested
  @DisplayName("Edge Cases")
  class EdgeCases {

    @Test
    @DisplayName("Test parse - unknown verb passes through")
    void testParse_unknownVerb() {
      final ParsedCommand result = parser.parseCommand("xyzzy", SESSION_ID, player);

      assertEquals("xyzzy", result.getVerb());
      assertFalse(result.hasDirectObjects());
    }

    @Test
    @DisplayName("Test parse - multiple consecutive spaces")
    void testParse_multipleConsecutiveSpaces() {
      final ParsedCommand result = parser.parseCommand("take   key", SESSION_ID, player);

      assertEquals("take", result.getVerb());
      assertEquals("key", result.getFirstDirectObject());
    }

    @Test
    @DisplayName("Test parse - very long input")
    void testParse_veryLongInput() {
      final String longInput = "take " + "a".repeat(1000);
      final ParsedCommand result = parser.parseCommand(longInput, SESSION_ID, player);

      assertEquals("take", result.getVerb());
      assertTrue(result.hasDirectObjects());
    }

    @Test
    @DisplayName("Test parse - preserves original input")
    void testParse_preservesOriginalInput() {
      final String input = "TaKe ThE KeY";
      final ParsedCommand result = parser.parseCommand(input, SESSION_ID, player);

      assertEquals(input, result.getOriginalInput());
    }

    @Test
    @DisplayName("Test parse - pronoun it passes through")
    void testParse_pronounItPassesThrough() {
      final ParsedCommand result = parser.parseCommand("take it", SESSION_ID, player);

      assertEquals("take", result.getVerb());
      assertEquals("it", result.getFirstDirectObject());
    }

    @Test
    @DisplayName("Test parse - pronoun that passes through")
    void testParse_pronounThatPassesThrough() {
      final ParsedCommand result = parser.parseCommand("take that", SESSION_ID, player);

      assertEquals("take", result.getVerb());
      assertEquals("that", result.getFirstDirectObject());
    }
  }

  @Nested
  @DisplayName("Verb Synonyms")
  class VerbSynonyms {

    @Test
    @DisplayName("Test parse - get normalizes to take")
    void testParse_getNormalizesToTake() {
      final ParsedCommand result = parser.parseCommand("get key", SESSION_ID, player);

      assertEquals("take", result.getVerb());
    }

    @Test
    @DisplayName("Test parse - grab normalizes to take")
    void testParse_grabNormalizesToTake() {
      final ParsedCommand result = parser.parseCommand("grab key", SESSION_ID, player);

      assertEquals("take", result.getVerb());
    }

    @Test
    @DisplayName("Test parse - examine normalizes to look")
    void testParse_examineNormalizesToLook() {
      final ParsedCommand result = parser.parseCommand("examine key", SESSION_ID, player);

      assertEquals("look", result.getVerb());
    }

    @Test
    @DisplayName("Test parse - x normalizes to look")
    void testParse_xNormalizesToLook() {
      final ParsedCommand result = parser.parseCommand("x key", SESSION_ID, player);

      assertEquals("look", result.getVerb());
    }

    @Test
    @DisplayName("Test parse - i normalizes to inventory")
    void testParse_iNormalizesToInventory() {
      final ParsedCommand result = parser.parseCommand("i", SESSION_ID, player);

      assertEquals("inventory", result.getVerb());
    }
  }
}
package io.github.tmanbarton.ifengine.command.handlers;

import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.game.GameState;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.parser.CommandType;
import io.github.tmanbarton.ifengine.parser.ContextManager;
import io.github.tmanbarton.ifengine.parser.ObjectResolver;
import io.github.tmanbarton.ifengine.parser.ParsedCommand;
import io.github.tmanbarton.ifengine.response.DefaultResponses;
import io.github.tmanbarton.ifengine.response.ResponseProvider;
import io.github.tmanbarton.ifengine.test.TestGameMap;
import io.github.tmanbarton.ifengine.test.TestItemFactory;
import io.github.tmanbarton.ifengine.test.TestLocationFactory;
import io.github.tmanbarton.ifengine.test.TestOpenableItem;
import io.github.tmanbarton.ifengine.test.TestOpenableLocation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for OpenHandler.
 */
@DisplayName("OpenHandler Tests")
class OpenHandlerTest {

  private OpenHandler handler;
  private Player player;
  private TestOpenableLocation openableLocation;
  private TestGameMap gameMap;
  private ResponseProvider responses;

  @BeforeEach
  void setUp() {
    final ObjectResolver objectResolver = new ObjectResolver();
    final ContextManager contextManager = new ContextManager();
    responses = new DefaultResponses();

    // Create an OpenableLocation
    openableLocation = TestOpenableLocation.builder("vault-room")
        .withUnlockTargets("door", "vault", "vault door")
        .withOpenTargets("door", "vault", "vault door")
        .withRequiredKey("key")
        .build();

    gameMap = TestGameMap.createWithLocations(openableLocation);
    handler = new OpenHandler(objectResolver, contextManager, gameMap, responses);
    player = new Player(openableLocation);
  }

  @Nested
  @DisplayName("Open Unlocked Door")
  class OpenUnlockedDoor {

    @Test
    @DisplayName("Test handle - open already unlocked door")
    void testHandle_openUnlockedDoor() {
      openableLocation.setUnlocked(true);
      final ParsedCommand command = createOpenCommand("door");

      final String result = handler.handle(player, command);

      assertEquals(TestOpenableLocation.DEFAULT_OPEN_MESSAGE, result);
      assertTrue(openableLocation.isOpen());
    }

    @Test
    @DisplayName("Test handle - open already unlocked vault")
    void testHandle_openUnlockedVault() {
      openableLocation.setUnlocked(true);
      final ParsedCommand command = createOpenCommand("vault");

      final String result = handler.handle(player, command);

      assertEquals(TestOpenableLocation.DEFAULT_OPEN_MESSAGE, result);
      assertTrue(openableLocation.isOpen());
    }
  }

  @Nested
  @DisplayName("Open Locked Door With Key")
  class OpenLockedWithKey {

    @Test
    @DisplayName("Test handle - open locked door with key auto-unlocks and opens")
    void testHandle_openLockedWithKey() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      final ParsedCommand command = createOpenCommand("door");

      final String result = handler.handle(player, command);

      assertEquals(TestOpenableLocation.DEFAULT_UNLOCK_AND_OPEN_MESSAGE, result);
      assertTrue(openableLocation.isUnlocked());
      assertTrue(openableLocation.isOpen());
    }
  }

  @Nested
  @DisplayName("Open Locked Door Without Key")
  class OpenLockedWithoutKey {

    @Test
    @DisplayName("Test handle - open locked door without key shows locked message")
    void testHandle_openLockedWithoutKey() {
      // Door is locked (not unlocked) and player has no key
      assertFalse(openableLocation.isUnlocked());
      assertFalse(player.hasItem("key"));
      final ParsedCommand command = createOpenCommand("door");

      final String result = handler.handle(player, command);

      assertEquals(TestOpenableLocation.DEFAULT_OPEN_LOCKED_NO_KEY_MESSAGE, result);
      assertFalse(openableLocation.isUnlocked());
      assertFalse(openableLocation.isOpen());
    }

    @Test
    @DisplayName("Test handle - open locked vault without key")
    void testHandle_openLockedVaultWithoutKey() {
      // Vault is locked and player has no key
      final ParsedCommand command = createOpenCommand("vault");

      final String result = handler.handle(player, command);

      assertEquals(TestOpenableLocation.DEFAULT_OPEN_LOCKED_NO_KEY_MESSAGE, result);
    }
  }

  @Nested
  @DisplayName("Already Open")
  class AlreadyOpen {

    @Test
    @DisplayName("Test handle - open already open door")
    void testHandle_alreadyOpen() {
      openableLocation.setUnlocked(true);
      openableLocation.setOpen(true);
      final ParsedCommand command = createOpenCommand("door");

      final String result = handler.handle(player, command);

      assertEquals(TestOpenableLocation.DEFAULT_ALREADY_OPEN_MESSAGE, result);
    }
  }

  @Nested
  @DisplayName("Nothing To Open")
  class NothingToOpen {

    @Test
    @DisplayName("Test handle - open at regular location")
    void testHandle_openAtRegularLocation() {
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final Player regularPlayer = new Player(regularLocation);
      final ParsedCommand command = createOpenCommand("door");

      final String result = handler.handle(regularPlayer, command);

      assertEquals(responses.getOpenCantOpen("door"), result);
    }

    @Test
    @DisplayName("Test handle - open with no object and no openable present")
    void testHandle_noObjectNoOpenable() {
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final Player regularPlayer = new Player(regularLocation);
      final ParsedCommand command = createOpenCommand();

      final String result = handler.handle(regularPlayer, command);

      assertEquals(responses.getOpenNothingToOpen(), result);
    }

    @Test
    @DisplayName("Test handle - open something that doesn't match open targets")
    void testHandle_openNonMatchingTarget() {
      final ParsedCommand command = createOpenCommand("window");

      final String result = handler.handle(player, command);

      assertEquals(responses.getOpenCantOpen("window"), result);
    }
  }

  @Nested
  @DisplayName("Supported Verbs")
  class SupportedVerbs {

    @Test
    @DisplayName("Test getSupportedVerbs - includes open")
    void testGetSupportedVerbs_includesOpen() {
      final List<String> verbs = handler.getSupportedVerbs();

      assertTrue(verbs.contains("open"));
    }

    @Test
    @DisplayName("Test canHandle - returns true for open")
    void testCanHandle_open() {
      assertTrue(handler.canHandle("open"));
    }

    @Test
    @DisplayName("Test canHandle - returns false for unsupported verb")
    void testCanHandle_unsupportedVerb() {
      assertFalse(handler.canHandle("unlock"));
      assertFalse(handler.canHandle("close"));
    }
  }

  @Nested
  @DisplayName("Case Sensitivity")
  class CaseSensitivity {

    @Test
    @DisplayName("Test handle - open with uppercase target")
    void testHandle_openUppercaseTarget() {
      openableLocation.setUnlocked(true);
      final ParsedCommand command = createOpenCommand("DOOR");

      final String result = handler.handle(player, command);

      assertEquals(TestOpenableLocation.DEFAULT_OPEN_MESSAGE, result);
    }

    @Test
    @DisplayName("Test handle - open with mixed case target")
    void testHandle_openMixedCaseTarget() {
      openableLocation.setUnlocked(true);
      final ParsedCommand command = createOpenCommand("DoOr");

      final String result = handler.handle(player, command);

      assertEquals(TestOpenableLocation.DEFAULT_OPEN_MESSAGE, result);
    }
  }

  @Nested
  @DisplayName("State Transitions")
  class StateTransitions {

    @Test
    @DisplayName("Test handle - open locked with key sets both unlocked and open")
    void testHandle_stateAfterOpenLockedWithKey() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      assertFalse(openableLocation.isUnlocked());
      assertFalse(openableLocation.isOpen());
      final ParsedCommand command = createOpenCommand("door");

      handler.handle(player, command);

      assertTrue(openableLocation.isUnlocked());
      assertTrue(openableLocation.isOpen());
    }

    @Test
    @DisplayName("Test handle - open unlocked only sets open")
    void testHandle_stateAfterOpenUnlocked() {
      openableLocation.setUnlocked(true);
      assertFalse(openableLocation.isOpen());
      final ParsedCommand command = createOpenCommand("door");

      handler.handle(player, command);

      assertTrue(openableLocation.isUnlocked());
      assertTrue(openableLocation.isOpen());
    }
  }

  @Nested
  @DisplayName("OpenableItem In Inventory")
  class OpenableItemInInventory {

    @Test
    @DisplayName("Test handle - open unlocked item in inventory")
    void testHandle_openUnlockedItemInInventory() {
      // Given
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final Player testPlayer = new Player(regularLocation);
      final TestOpenableItem chest = TestOpenableItem.builder("chest")
          .withOpenTargets("chest")
          .withRequiresUnlocking(false)
          .build();
      testPlayer.addItem(chest);
      final ParsedCommand command = createOpenCommand("chest");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then
      assertEquals(TestOpenableItem.DEFAULT_OPEN_MESSAGE, result);
      assertTrue(chest.isOpen());
    }

    @Test
    @DisplayName("Test handle - open locked item in inventory with key")
    void testHandle_openLockedItemWithKey() {
      // Given
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final Player testPlayer = new Player(regularLocation);
      final TestOpenableItem chest = TestOpenableItem.builder("chest")
          .withOpenTargets("chest")
          .withRequiresUnlocking(true)
          .withRequiredKey("chest-key")
          .build();
      testPlayer.addItem(chest);
      final Item key = new Item("chest-key", "a small key", "A small key.", "A small brass key.");
      testPlayer.addItem(key);
      final ParsedCommand command = createOpenCommand("chest");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then
      assertTrue(chest.isUnlocked());
      assertTrue(chest.isOpen());
    }

    @Test
    @DisplayName("Test handle - open locked item in inventory without key")
    void testHandle_openLockedItemWithoutKey() {
      // Given
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final Player testPlayer = new Player(regularLocation);
      final TestOpenableItem chest = TestOpenableItem.builder("chest")
          .withOpenTargets("chest")
          .withRequiresUnlocking(true)
          .build();
      testPlayer.addItem(chest);
      final ParsedCommand command = createOpenCommand("chest");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then
      assertEquals(TestOpenableItem.DEFAULT_OPEN_LOCKED_MESSAGE, result);
      assertFalse(chest.isOpen());
    }

    @Test
    @DisplayName("Test handle - inventory item takes priority over location")
    void testHandle_inventoryItemPriorityOverLocation() {
      // Given - player at OpenableLocation with chest in inventory
      final TestOpenableItem chest = TestOpenableItem.builder("chest")
          .withOpenTargets("chest", "door")
          .withRequiresUnlocking(false)
          .build();
      player.addItem(chest);
      openableLocation.setUnlocked(true);
      final ParsedCommand command = createOpenCommand("door");

      // When - "open door" should find chest first (inventory priority)
      final String result = handler.handle(player, command);

      // Then - chest opens, not the door
      assertEquals(TestOpenableItem.DEFAULT_OPEN_MESSAGE, result);
      assertTrue(chest.isOpen());
      assertFalse(openableLocation.isOpen());
    }
  }

  @Nested
  @DisplayName("Extract Provided Answer")
  class ExtractProvidedAnswer {

    @Test
    @DisplayName("Test handle - open with code via 'with' preposition")
    void testHandle_openWithCodeViaWithPreposition() {
      // Given - "open lockbox with 1, 2, 3, 4"
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withOpenTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1 2 3 4")
          .build();
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      final ParsedCommand command = createOpenCommandWithCode("lockbox", "1, 2, 3, 4");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then - code should be extracted and passed to tryOpen
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_MESSAGE + " " + TestOpenableItem.DEFAULT_OPEN_MESSAGE, result);
      assertTrue(lockbox.isUnlocked());
      assertTrue(lockbox.isOpen());
    }

    @Test
    @DisplayName("Test handle - open with multi-word answer via 'with' preposition")
    void testHandle_openWithMultiWordAnswer() {
      // Given - "open cryptex with secret phrase"
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem cryptex = TestOpenableItem.builder("cryptex")
          .withOpenTargets("cryptex")
          .withRequiresUnlocking(true)
          .withExpectedCode("secret phrase")
          .build();
      regularLocation.addItem(cryptex);
      final Player testPlayer = new Player(regularLocation);
      final ParsedCommand command = createOpenCommandWithCode("cryptex", "secret", "phrase");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then - multi-word answer should be joined with spaces
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_MESSAGE + " " + TestOpenableItem.DEFAULT_OPEN_MESSAGE, result);
      assertTrue(cryptex.isUnlocked());
      assertTrue(cryptex.isOpen());
    }

    @Test
    @DisplayName("Test handle - open without code triggers prompt")
    void testHandle_openWithoutCodeTriggersPrompt() {
      // Given - "open lockbox" (no code provided)
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withOpenTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1234")
          .build();
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      final ParsedCommand command = createOpenCommand("lockbox");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then - no code extracted, tryOpen receives null → prompt message
      assertEquals(TestOpenableItem.DEFAULT_PROMPT_MESSAGE, result);
      assertFalse(lockbox.isOpen());
    }

    @Test
    @DisplayName("Test handle - open with wrong code returns failure message")
    void testHandle_openWithWrongCode() {
      // Given - "open lockbox with 9, 9, 9, 9" (wrong code)
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withOpenTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1 2 3 4")
          .build();
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      final ParsedCommand command = createOpenCommandWithCode("lockbox", "9, 9, 9, 9");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then - wrong code returns failure message
      assertEquals(TestOpenableItem.DEFAULT_WRONG_CODE_MESSAGE, result);
      assertFalse(lockbox.isOpen());
    }

    @Test
    @DisplayName("Test handle - 'open X code' without 'with' treats as single object name")
    void testHandle_openWithInlineCodeTreatedAsSingleObject() {
      // Given - "open lockbox 1234" is parsed as directObjects=["lockbox 1234"]
      final ParsedCommand command = createOpenCommand("lockbox 1234");

      // When
      final String result = handler.handle(player, command);

      // Then - returns "can't open" because no object named "lockbox 1234" exists
      assertEquals(responses.getOpenCantOpen("lockbox 1234"), result);
    }
  }

  @Nested
  @DisplayName("Implied Object With Code")
  class ImpliedObjectWithCode {

    @Test
    @DisplayName("Test handle - 'open with CODE' uses implied object and passes code")
    void testHandle_openWithCodeImpliedObject() {
      // Given - "open with 1234" with single openable at location
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withOpenTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1234")
          .build();
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      final ParsedCommand command = createOpenCommandWithCodeOnly("1234");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then - implied object resolved AND code passed to tryOpen
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_MESSAGE + " " + TestOpenableItem.DEFAULT_OPEN_MESSAGE, result);
      assertTrue(lockbox.isUnlocked());
      assertTrue(lockbox.isOpen());
    }

    @Test
    @DisplayName("Test handle - 'open with WRONG_CODE' uses implied object and returns wrong code message")
    void testHandle_openWithWrongCodeImpliedObject() {
      // Given - "open with 9999" with single openable at location
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withOpenTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1234")
          .build();
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      final ParsedCommand command = createOpenCommandWithCodeOnly("9999");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then - implied object resolved, wrong code returns failure message
      assertEquals(TestOpenableItem.DEFAULT_WRONG_CODE_MESSAGE, result);
      assertFalse(lockbox.isOpen());
    }
  }

  @Nested
  @DisplayName("Prompt State Handling")
  class PromptStateHandling {

    @Test
    @DisplayName("Test handle - open code-based item without code sets WAITING_FOR_OPEN_CODE state")
    void testHandle_openWithoutCodeSetsWaitingState() {
      // Given - code-based lockbox, player in PLAYING state
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withOpenTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1234")
          .build();
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      testPlayer.setGameState(GameState.PLAYING);
      final ParsedCommand command = createOpenCommand("lockbox");

      // When - open without providing code
      handler.handle(testPlayer, command);

      // Then - state should change to WAITING_FOR_OPEN_CODE
      assertEquals(GameState.WAITING_FOR_OPEN_CODE, testPlayer.getGameState());
    }

    @Test
    @DisplayName("Test handle - open code-based item without code sets pending openable")
    void testHandle_openWithoutCodeSetsPendingOpenable() {
      // Given - code-based lockbox, player in PLAYING state
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withOpenTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1234")
          .build();
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      testPlayer.setGameState(GameState.PLAYING);
      final ParsedCommand command = createOpenCommand("lockbox");

      // When - open without providing code
      handler.handle(testPlayer, command);

      // Then - pending openable should be set
      assertEquals(lockbox, testPlayer.getPendingOpenable());
    }

    @Test
    @DisplayName("Test handle - open with correct code does not set prompt state")
    void testHandle_openWithCorrectCodeNoPromptState() {
      // Given - code-based lockbox, player in PLAYING state
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withOpenTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1234")
          .build();
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      testPlayer.setGameState(GameState.PLAYING);
      final ParsedCommand command = createOpenCommandWithCode("lockbox", "1234");

      // When - open with correct code
      handler.handle(testPlayer, command);

      // Then - state should remain PLAYING, no pending openable
      assertEquals(GameState.PLAYING, testPlayer.getGameState());
      assertNull(testPlayer.getPendingOpenable());
    }

    @Test
    @DisplayName("Test handle - open with wrong code does not set prompt state")
    void testHandle_openWithWrongCodeNoPromptState() {
      // Given - code-based lockbox, player in PLAYING state
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withOpenTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1234")
          .build();
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      testPlayer.setGameState(GameState.PLAYING);
      final ParsedCommand command = createOpenCommandWithCode("lockbox", "9999");

      // When - open with wrong code
      handler.handle(testPlayer, command);

      // Then - state should remain PLAYING, no pending openable
      assertEquals(GameState.PLAYING, testPlayer.getGameState());
      assertNull(testPlayer.getPendingOpenable());
    }

    @Test
    @DisplayName("Test handle - open already open item does not set prompt state")
    void testHandle_openAlreadyOpenNoPromptState() {
      // Given - already open lockbox
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withOpenTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1234")
          .build();
      lockbox.setUnlocked(true);
      lockbox.setOpen(true);
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      testPlayer.setGameState(GameState.PLAYING);
      final ParsedCommand command = createOpenCommand("lockbox");

      // When - try to open already open item
      handler.handle(testPlayer, command);

      // Then - state should remain PLAYING, no pending openable
      assertEquals(GameState.PLAYING, testPlayer.getGameState());
      assertNull(testPlayer.getPendingOpenable());
    }
  }

  @Nested
  @DisplayName("OpenableItem At Location")
  class OpenableItemAtLocation {

    @Test
    @DisplayName("Test handle - open unlocked item at location")
    void testHandle_openUnlockedItemAtLocation() {
      // Given
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem chest = TestOpenableItem.builder("chest")
          .withOpenTargets("chest")
          .withRequiresUnlocking(false)
          .build();
      regularLocation.addItem(chest);
      final Player testPlayer = new Player(regularLocation);
      final ParsedCommand command = createOpenCommand("chest");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then
      assertEquals(TestOpenableItem.DEFAULT_OPEN_MESSAGE, result);
      assertTrue(chest.isOpen());
    }

    @Test
    @DisplayName("Test handle - open locked item at location without key")
    void testHandle_openLockedItemAtLocationWithoutKey() {
      // Given
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem chest = TestOpenableItem.builder("chest")
          .withOpenTargets("chest")
          .withRequiresUnlocking(true)
          .build();
      regularLocation.addItem(chest);
      final Player testPlayer = new Player(regularLocation);
      final ParsedCommand command = createOpenCommand("chest");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then
      assertEquals(TestOpenableItem.DEFAULT_OPEN_LOCKED_MESSAGE, result);
      assertFalse(chest.isOpen());
    }

    @Test
    @DisplayName("Test handle - location item takes priority over OpenableLocation")
    void testHandle_locationItemPriorityOverOpenableLocation() {
      // Given - chest at OpenableLocation that also matches "door"
      final TestOpenableItem chest = TestOpenableItem.builder("chest")
          .withOpenTargets("chest", "door")
          .withRequiresUnlocking(false)
          .build();
      openableLocation.addItem(chest);
      openableLocation.setUnlocked(true);
      final ParsedCommand command = createOpenCommand("door");

      // When
      final String result = handler.handle(player, command);

      // Then - chest opens, not the location door
      assertEquals(TestOpenableItem.DEFAULT_OPEN_MESSAGE, result);
      assertTrue(chest.isOpen());
      assertFalse(openableLocation.isOpen());
    }
  }

  /**
   * Helper method to create an open command with a specified object.
   */
  private ParsedCommand createOpenCommand(final String objectName) {
    final List<String> directObjects = objectName != null && !objectName.isEmpty()
        ? List.of(objectName)
        : new ArrayList<>();
    return new ParsedCommand("open", directObjects, new ArrayList<>(), null,
        CommandType.SINGLE, false, "open " + objectName);
  }

  /**
   * Helper method to create an open command with no object.
   */
  private ParsedCommand createOpenCommand() {
    return new ParsedCommand("open", new ArrayList<>(), new ArrayList<>(), null,
        CommandType.SINGLE, false, "open");
  }

  /**
   * Helper method to create an open command with a code/word via "with" preposition.
   * Simulates "open lockbox with 1, 2, 3, 4" by passing indirect objects.
   */
  private ParsedCommand createOpenCommandWithCode(final String objectName, final String... codeWords) {
    final List<String> directObjects = List.of(objectName);
    final List<String> indirectObjects = List.of(codeWords);
    return new ParsedCommand("open", directObjects, indirectObjects, "with",
        CommandType.SINGLE, false, "open " + objectName + " with " + String.join(" ", codeWords));
  }

  /**
   * Helper method to create an open command with code only (no direct object).
   * Simulates "open with 1, 2, 3, 4" for implied object resolution.
   */
  private ParsedCommand createOpenCommandWithCodeOnly(final String... codeWords) {
    final List<String> directObjects = new ArrayList<>();
    final List<String> indirectObjects = List.of(codeWords);
    return new ParsedCommand("open", directObjects, indirectObjects, "with",
        CommandType.SINGLE, false, "open with " + String.join(" ", codeWords));
  }
}
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
import io.github.tmanbarton.ifengine.test.TestOpenableSceneryObject;

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
 * Unit tests for UnlockHandler.
 */
@DisplayName("UnlockHandler Tests")
class UnlockHandlerTest {

  private UnlockHandler handler;
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
    handler = new UnlockHandler(objectResolver, contextManager, gameMap, responses);
    player = new Player(openableLocation);
  }

  @Nested
  class UnlockWithKey {

    @Test
    @DisplayName("Test handle - unlock door with key in inventory")
    void testHandle_unlockWithKey() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      final ParsedCommand command = createUnlockCommand("door");

      final String result = handler.handle(player, command);

      assertEquals(TestOpenableLocation.DEFAULT_UNLOCK_MESSAGE, result);
      assertTrue(openableLocation.isUnlocked());
    }

    @Test
    @DisplayName("Test handle - unlock vault with key")
    void testHandle_unlockVaultWithKey() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      final ParsedCommand command = createUnlockCommand("vault");

      final String result = handler.handle(player, command);

      assertEquals(TestOpenableLocation.DEFAULT_UNLOCK_MESSAGE, result);
      assertTrue(openableLocation.isUnlocked());
    }
  }

  @Nested
  class UnlockWithoutKey {

    @Test
    @DisplayName("Test handle - unlock door without key")
    void testHandle_unlockWithoutKey() {
      final ParsedCommand command = createUnlockCommand("door");

      final String result = handler.handle(player, command);

      assertEquals(TestOpenableLocation.DEFAULT_UNLOCK_NO_KEY_MESSAGE, result);
      assertFalse(openableLocation.isUnlocked());
    }
  }

  @Nested
  class AlreadyUnlocked {

    @Test
    @DisplayName("Test handle - unlock already unlocked door")
    void testHandle_alreadyUnlocked() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      openableLocation.setUnlocked(true);
      final ParsedCommand command = createUnlockCommand("door");

      final String result = handler.handle(player, command);

      assertEquals(TestOpenableLocation.DEFAULT_ALREADY_UNLOCKED_MESSAGE, result);
    }
  }

  @Nested
  class NothingToUnlock {

    @Test
    @DisplayName("Test handle - unlock at regular location")
    void testHandle_unlockAtRegularLocation() {
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final Player regularPlayer = new Player(regularLocation);
      final ParsedCommand command = createUnlockCommand("door");

      final String result = handler.handle(regularPlayer, command);

      assertEquals(responses.getUnlockCantUnlock("door"), result);
    }

    @Test
    @DisplayName("Test handle - unlock with no object and no unlockable present")
    void testHandle_noObjectNoUnlockable() {
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final Player regularPlayer = new Player(regularLocation);
      final ParsedCommand command = createUnlockCommand();

      final String result = handler.handle(regularPlayer, command);

      assertEquals(responses.getUnlockNothingToUnlock(), result);
    }

    @Test
    @DisplayName("Test handle - unlock something that doesn't match unlock targets")
    void testHandle_unlockNonMatchingTarget() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      final ParsedCommand command = createUnlockCommand("window");

      final String result = handler.handle(player, command);

      assertEquals(responses.getUnlockCantUnlock("window"), result);
    }
  }

  @Nested
  class SupportedVerbs {

    @Test
    @DisplayName("Test getSupportedVerbs - includes unlock")
    void testGetSupportedVerbs_includesUnlock() {
      final List<String> verbs = handler.getSupportedVerbs();

      assertTrue(verbs.contains("unlock"));
    }

    @Test
    @DisplayName("Test canHandle - returns true for unlock")
    void testCanHandle_unlock() {
      assertTrue(handler.canHandle("unlock"));
    }

    @Test
    @DisplayName("Test canHandle - returns false for unsupported verb")
    void testCanHandle_unsupportedVerb() {
      assertFalse(handler.canHandle("open"));
      assertFalse(handler.canHandle("lock"));
    }
  }

  @Nested
  class CaseSensitivity {

    @Test
    @DisplayName("Test handle - unlock with uppercase target")
    void testHandle_unlockUppercaseTarget() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      final ParsedCommand command = createUnlockCommand("DOOR");

      final String result = handler.handle(player, command);

      assertEquals(TestOpenableLocation.DEFAULT_UNLOCK_MESSAGE, result);
    }

    @Test
    @DisplayName("Test handle - unlock with mixed case target")
    void testHandle_unlockMixedCaseTarget() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      final ParsedCommand command = createUnlockCommand("DoOr");

      final String result = handler.handle(player, command);

      assertEquals(TestOpenableLocation.DEFAULT_UNLOCK_MESSAGE, result);
    }
  }

  @Nested
  class OpenableItemInInventory {

    @Test
    @DisplayName("Test handle - unlock item in inventory with key")
    void testHandle_unlockItemWithKey() {
      // Given
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final Player testPlayer = new Player(regularLocation);
      final TestOpenableItem chest = TestOpenableItem.builder("chest")
          .withUnlockTargets("chest")
          .withRequiresUnlocking(true)
          .withRequiredKey("chest-key")
          .build();
      testPlayer.addItem(chest);
      final Item key = new Item("chest-key", "a small key", "A small key.", "A small brass key.");
      testPlayer.addItem(key);
      final ParsedCommand command = createUnlockCommand("chest");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_MESSAGE, result);
      assertTrue(chest.isUnlocked());
    }

    @Test
    @DisplayName("Test handle - unlock item in inventory without key")
    void testHandle_unlockItemWithoutKey() {
      // Given
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final Player testPlayer = new Player(regularLocation);
      final TestOpenableItem chest = TestOpenableItem.builder("chest")
          .withUnlockTargets("chest")
          .withRequiresUnlocking(true)
          .build();
      testPlayer.addItem(chest);
      final ParsedCommand command = createUnlockCommand("chest");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_NO_KEY_MESSAGE, result);
      assertFalse(chest.isUnlocked());
    }

    @Test
    @DisplayName("Test handle - inventory item takes priority over location")
    void testHandle_inventoryItemPriorityOverLocation() {
      // Given - player at OpenableLocation with chest in inventory
      final TestOpenableItem chest = TestOpenableItem.builder("chest")
          .withUnlockTargets("chest", "door")
          .withRequiresUnlocking(true)
          .withRequiredKey("chest-key")
          .build();
      player.addItem(chest);
      final Item chestKey = new Item("chest-key", "a small key", "A small key.", "A small brass key.");
      player.addItem(chestKey);
      final Item doorKey = TestItemFactory.createTestKey();
      player.addItem(doorKey);
      final ParsedCommand command = createUnlockCommand("door");

      // When - "unlock door" should find chest first (inventory priority)
      final String result = handler.handle(player, command);

      // Then - chest unlocks, not the door
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_MESSAGE, result);
      assertTrue(chest.isUnlocked());
      assertFalse(openableLocation.isUnlocked());
    }
  }

  @Nested
  class ExtractProvidedAnswer {

    @Test
    @DisplayName("Test handle - unlock with code via 'with' preposition")
    void testHandle_unlockWithCodeViaWithPreposition() {
      // Given - "unlock lockbox with 1, 2, 3, 4"
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withUnlockTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1 2 3 4")
          .build();
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      final ParsedCommand command = createUnlockCommandWithCode("lockbox", "1, 2, 3, 4");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then - code should be extracted and passed to tryUnlock
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_MESSAGE, result);
      assertTrue(lockbox.isUnlocked());
    }

    @Test
    @DisplayName("Test handle - unlock with multi-word answer via 'with' preposition")
    void testHandle_unlockWithMultiWordAnswer() {
      // Given - "unlock cryptex with secret phrase"
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem cryptex = TestOpenableItem.builder("cryptex")
          .withUnlockTargets("cryptex")
          .withRequiresUnlocking(true)
          .withExpectedCode("secret phrase")
          .build();
      regularLocation.addItem(cryptex);
      final Player testPlayer = new Player(regularLocation);
      final ParsedCommand command = createUnlockCommandWithCode("cryptex", "secret", "phrase");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then - multi-word answer should be joined with spaces
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_MESSAGE, result);
      assertTrue(cryptex.isUnlocked());
    }

    @Test
    @DisplayName("Test handle - unlock without code triggers prompt")
    void testHandle_unlockWithoutCodeTriggersPrompt() {
      // Given - "unlock lockbox" (no code provided)
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withUnlockTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1234")
          .build();
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      final ParsedCommand command = createUnlockCommand("lockbox");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then - no code extracted, tryUnlock receives null → prompt message
      assertEquals(TestOpenableItem.DEFAULT_PROMPT_MESSAGE, result);
      assertFalse(lockbox.isUnlocked());
    }

    @Test
    @DisplayName("Test handle - unlock with wrong code returns failure message")
    void testHandle_unlockWithWrongCode() {
      // Given - "unlock lockbox with 9, 9, 9, 9" (wrong code)
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withUnlockTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1 2 3 4")
          .build();
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      final ParsedCommand command = createUnlockCommandWithCode("lockbox", "9, 9, 9, 9");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then - wrong code returns failure message
      assertEquals(TestOpenableItem.DEFAULT_WRONG_CODE_MESSAGE, result);
      assertFalse(lockbox.isUnlocked());
    }

    @Test
    @DisplayName("Test handle - 'unlock X code' without 'with' treats as single object name")
    void testHandle_unlockWithInlineCodeTreatedAsSingleObject() {
      // Given - "unlock lockbox 1234" is parsed as directObjects=["lockbox 1234"]
      // because the parser treats "lockbox 1234" as a multi-word object name
      final ParsedCommand command = createUnlockCommand("lockbox 1234");

      // When
      final String result = handler.handle(player, command);

      // Then - returns "can't unlock" because no object named "lockbox 1234" exists
      // This documents that "unlock X code" format does NOT work - use "unlock X with code"
      assertEquals(responses.getUnlockCantUnlock("lockbox 1234"), result);
    }
  }

  @Nested
  class ImpliedObjectWithCode {

    @Test
    @DisplayName("Test handle - 'unlock with CODE' uses implied object and passes code")
    void testHandle_unlockWithCodeImpliedObject() {
      // Given - "unlock with 1234" with single unlockable at location
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withUnlockTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1234")
          .build();
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      final ParsedCommand command = createUnlockCommandWithCodeOnly("1234");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then - implied object resolved AND code passed to tryUnlock
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_MESSAGE, result);
      assertTrue(lockbox.isUnlocked());
    }

    @Test
    @DisplayName("Test handle - 'unlock with WRONG_CODE' uses implied object and returns wrong code message")
    void testHandle_unlockWithWrongCodeImpliedObject() {
      // Given - "unlock with 9999" with single unlockable at location
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withUnlockTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1234")
          .build();
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      final ParsedCommand command = createUnlockCommandWithCodeOnly("9999");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then - implied object resolved, wrong code returns failure message
      assertEquals(TestOpenableItem.DEFAULT_WRONG_CODE_MESSAGE, result);
      assertFalse(lockbox.isUnlocked());
    }
  }

  @Nested
  class PromptStateHandling {

    @Test
    @DisplayName("Test handle - unlock code-based item without code sets WAITING_FOR_UNLOCK_CODE state")
    void testHandle_unlockWithoutCodeSetsWaitingState() {
      // Given - code-based lockbox, player in PLAYING state
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withUnlockTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1234")
          .build();
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      testPlayer.setGameState(GameState.PLAYING);
      final ParsedCommand command = createUnlockCommand("lockbox");

      // When - unlock without providing code
      handler.handle(testPlayer, command);

      // Then - state should change to WAITING_FOR_UNLOCK_CODE
      assertEquals(GameState.WAITING_FOR_UNLOCK_CODE, testPlayer.getGameState());
    }

    @Test
    @DisplayName("Test handle - unlock code-based item without code sets pending openable")
    void testHandle_unlockWithoutCodeSetsPendingOpenable() {
      // Given - code-based lockbox, player in PLAYING state
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withUnlockTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1234")
          .build();
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      testPlayer.setGameState(GameState.PLAYING);
      final ParsedCommand command = createUnlockCommand("lockbox");

      // When - unlock without providing code
      handler.handle(testPlayer, command);

      // Then - pending openable should be set
      assertEquals(lockbox, testPlayer.getPendingOpenable());
    }

    @Test
    @DisplayName("Test handle - unlock with correct code does not set prompt state")
    void testHandle_unlockWithCorrectCodeNoPromptState() {
      // Given - code-based lockbox, player in PLAYING state
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withUnlockTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1234")
          .build();
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      testPlayer.setGameState(GameState.PLAYING);
      final ParsedCommand command = createUnlockCommandWithCode("lockbox", "1234");

      // When - unlock with correct code
      handler.handle(testPlayer, command);

      // Then - state should remain PLAYING, no pending openable
      assertEquals(GameState.PLAYING, testPlayer.getGameState());
      assertNull(testPlayer.getPendingOpenable());
    }

    @Test
    @DisplayName("Test handle - unlock with wrong code does not set prompt state")
    void testHandle_unlockWithWrongCodeNoPromptState() {
      // Given - code-based lockbox, player in PLAYING state
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withUnlockTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1234")
          .build();
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      testPlayer.setGameState(GameState.PLAYING);
      final ParsedCommand command = createUnlockCommandWithCode("lockbox", "9999");

      // When - unlock with wrong code
      handler.handle(testPlayer, command);

      // Then - state should remain PLAYING, no pending openable
      assertEquals(GameState.PLAYING, testPlayer.getGameState());
      assertNull(testPlayer.getPendingOpenable());
    }

    @Test
    @DisplayName("Test handle - unlock already unlocked item does not set prompt state")
    void testHandle_unlockAlreadyUnlockedNoPromptState() {
      // Given - already unlocked lockbox
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem lockbox = TestOpenableItem.builder("lockbox")
          .withUnlockTargets("lockbox")
          .withRequiresUnlocking(true)
          .withExpectedCode("1234")
          .build();
      lockbox.setUnlocked(true);
      regularLocation.addItem(lockbox);
      final Player testPlayer = new Player(regularLocation);
      testPlayer.setGameState(GameState.PLAYING);
      final ParsedCommand command = createUnlockCommand("lockbox");

      // When - try to unlock already unlocked item
      handler.handle(testPlayer, command);

      // Then - state should remain PLAYING, no pending openable
      assertEquals(GameState.PLAYING, testPlayer.getGameState());
      assertNull(testPlayer.getPendingOpenable());
    }
  }

  @Nested
  class OpenableItemAtLocation {

    @Test
    @DisplayName("Test handle - unlock item at location with key")
    void testHandle_unlockItemAtLocationWithKey() {
      // Given
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem chest = TestOpenableItem.builder("chest")
          .withUnlockTargets("chest")
          .withRequiresUnlocking(true)
          .withRequiredKey("chest-key")
          .build();
      regularLocation.addItem(chest);
      final Player testPlayer = new Player(regularLocation);
      final Item key = new Item("chest-key", "a small key", "A small key.", "A small brass key.");
      testPlayer.addItem(key);
      final ParsedCommand command = createUnlockCommand("chest");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_MESSAGE, result);
      assertTrue(chest.isUnlocked());
    }

    @Test
    @DisplayName("Test handle - unlock item at location without key")
    void testHandle_unlockItemAtLocationWithoutKey() {
      // Given
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem chest = TestOpenableItem.builder("chest")
          .withUnlockTargets("chest")
          .withRequiresUnlocking(true)
          .build();
      regularLocation.addItem(chest);
      final Player testPlayer = new Player(regularLocation);
      final ParsedCommand command = createUnlockCommand("chest");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_NO_KEY_MESSAGE, result);
      assertFalse(chest.isUnlocked());
    }

    @Test
    @DisplayName("Test handle - location item takes priority over OpenableLocation")
    void testHandle_locationItemPriorityOverOpenableLocation() {
      // Given - chest at OpenableLocation that also matches "door"
      final TestOpenableItem chest = TestOpenableItem.builder("chest")
          .withUnlockTargets("chest", "door")
          .withRequiresUnlocking(true)
          .withRequiredKey("chest-key")
          .build();
      openableLocation.addItem(chest);
      final Item chestKey = new Item("chest-key", "a small key", "A small key.", "A small brass key.");
      player.addItem(chestKey);
      final Item doorKey = TestItemFactory.createTestKey();
      player.addItem(doorKey);
      final ParsedCommand command = createUnlockCommand("door");

      // When
      final String result = handler.handle(player, command);

      // Then - chest unlocks, not the location door
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_MESSAGE, result);
      assertTrue(chest.isUnlocked());
      assertFalse(openableLocation.isUnlocked());
    }
  }

  @Nested
  class OpenableSceneryAtLocation {

    @Test
    @DisplayName("unlocks scenery with key in inventory")
    void testHandle_unlockSceneryWithKey() {
      // Given
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableSceneryObject safe = TestOpenableSceneryObject.openableBuilder("safe")
          .withUnlockTargets("safe")
          .withRequiresUnlocking(true)
          .withRequiredKey("safe-key")
          .build();
      regularLocation.addSceneryObject(safe);
      final Player testPlayer = new Player(regularLocation);
      final Item safeKey = new Item("safe-key", "a safe key", "A safe key.", "A small key for the safe.");
      testPlayer.addItem(safeKey);
      final ParsedCommand command = createUnlockCommand("safe");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then
      assertEquals(TestOpenableSceneryObject.DEFAULT_UNLOCK_MESSAGE, result);
      assertTrue(safe.isUnlocked());
    }

    @Test
    @DisplayName("location items take priority over openable scenery")
    void testHandle_locationItemPriorityOverScenery() {
      // Given - both an openable item and openable scenery named "chest"
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem chest = TestOpenableItem.builder("chest")
          .withUnlockTargets("chest")
          .withRequiresUnlocking(true)
          .withRequiredKey("chest-key")
          .build();
      regularLocation.addItem(chest);
      final TestOpenableSceneryObject sceneryChest = TestOpenableSceneryObject.openableBuilder("chest")
          .withUnlockTargets("chest")
          .withRequiresUnlocking(true)
          .withRequiredKey("chest-key")
          .build();
      regularLocation.addSceneryObject(sceneryChest);
      final Player testPlayer = new Player(regularLocation);
      final Item chestKey = new Item("chest-key", "a small key", "A small key.", "A small brass key.");
      testPlayer.addItem(chestKey);
      final ParsedCommand command = createUnlockCommand("chest");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then - item unlocks, not scenery
      assertEquals(TestOpenableItem.DEFAULT_UNLOCK_MESSAGE, result);
      assertTrue(chest.isUnlocked());
      assertFalse(sceneryChest.isUnlocked());
    }

    @Test
    @DisplayName("openable scenery takes priority over OpenableLocation")
    void testHandle_sceneryPriorityOverOpenableLocation() {
      // Given - openable scenery at an OpenableLocation, both matching "door"
      final TestOpenableSceneryObject sceneryDoor = TestOpenableSceneryObject.openableBuilder("door")
          .withUnlockTargets("door")
          .withRequiresUnlocking(true)
          .withRequiredKey("key")
          .build();
      openableLocation.addSceneryObject(sceneryDoor);
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);
      final ParsedCommand command = createUnlockCommand("door");

      // When
      final String result = handler.handle(player, command);

      // Then - scenery unlocks, not the location
      assertEquals(TestOpenableSceneryObject.DEFAULT_UNLOCK_MESSAGE, result);
      assertTrue(sceneryDoor.isUnlocked());
      assertFalse(openableLocation.isUnlocked());
    }
  }

  @Nested
  class Disambiguation {

    @Test
    @DisplayName("two openable items in inventory with same name returns disambiguation")
    void testHandle_disambiguateInventoryItems() {
      // Given - two openable items in inventory matching "box"
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final Player testPlayer = new Player(regularLocation);
      final TestOpenableItem box1 = TestOpenableItem.builder("red box")
          .withUnlockTargets("box", "red box")
          .withRequiresUnlocking(true)
          .build();
      final TestOpenableItem box2 = TestOpenableItem.builder("blue box")
          .withUnlockTargets("box", "blue box")
          .withRequiresUnlocking(true)
          .build();
      testPlayer.addItem(box1);
      testPlayer.addItem(box2);
      final ParsedCommand command = createUnlockCommand("box");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then
      assertEquals(responses.getUnlockNeedToSpecify("box"), result);
      assertFalse(box1.isUnlocked());
      assertFalse(box2.isUnlocked());
    }

    @Test
    @DisplayName("two openable items at location with same name returns disambiguation")
    void testHandle_disambiguateLocationItems() {
      // Given - two openable items at location matching "chest"
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableItem chest1 = TestOpenableItem.builder("wooden chest")
          .withUnlockTargets("chest", "wooden chest")
          .withRequiresUnlocking(true)
          .build();
      final TestOpenableItem chest2 = TestOpenableItem.builder("iron chest")
          .withUnlockTargets("chest", "iron chest")
          .withRequiresUnlocking(true)
          .build();
      regularLocation.addItem(chest1);
      regularLocation.addItem(chest2);
      final Player testPlayer = new Player(regularLocation);
      final ParsedCommand command = createUnlockCommand("chest");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then
      assertEquals(responses.getUnlockNeedToSpecify("chest"), result);
      assertFalse(chest1.isUnlocked());
      assertFalse(chest2.isUnlocked());
    }

    @Test
    @DisplayName("two openable scenery objects with same name returns disambiguation")
    void testHandle_disambiguateSceneryObjects() {
      // Given - two openable scenery at location matching "safe"
      final Location regularLocation = TestLocationFactory.createDefaultLocation();
      final TestOpenableSceneryObject safe1 = TestOpenableSceneryObject.openableBuilder("wall safe")
          .withUnlockTargets("safe", "wall safe")
          .withRequiresUnlocking(true)
          .build();
      final TestOpenableSceneryObject safe2 = TestOpenableSceneryObject.openableBuilder("floor safe")
          .withUnlockTargets("safe", "floor safe")
          .withRequiresUnlocking(true)
          .build();
      regularLocation.addSceneryObject(safe1);
      regularLocation.addSceneryObject(safe2);
      final Player testPlayer = new Player(regularLocation);
      final ParsedCommand command = createUnlockCommand("safe");

      // When
      final String result = handler.handle(testPlayer, command);

      // Then
      assertEquals(responses.getUnlockNeedToSpecify("safe"), result);
      assertFalse(safe1.isUnlocked());
      assertFalse(safe2.isUnlocked());
    }
  }

  /**
   * Helper method to create an unlock command with a specified object.
   */
  private ParsedCommand createUnlockCommand(final String objectName) {
    final List<String> directObjects = objectName != null && !objectName.isEmpty()
        ? List.of(objectName)
        : new ArrayList<>();
    return new ParsedCommand("unlock", directObjects, new ArrayList<>(), null,
        CommandType.SINGLE, false, "unlock " + objectName);
  }

  /**
   * Helper method to create an unlock command with no object.
   */
  private ParsedCommand createUnlockCommand() {
    return new ParsedCommand("unlock", new ArrayList<>(), new ArrayList<>(), null,
        CommandType.SINGLE, false, "unlock");
  }

  /**
   * Helper method to create an unlock command with a code/word via "with" preposition.
   * Simulates "unlock lockbox with 1, 2, 3, 4" by passing indirect objects.
   */
  private ParsedCommand createUnlockCommandWithCode(final String objectName, final String... codeWords) {
    final List<String> directObjects = List.of(objectName);
    final List<String> indirectObjects = List.of(codeWords);
    return new ParsedCommand("unlock", directObjects, indirectObjects, "with",
        CommandType.SINGLE, false, "unlock " + objectName + " with " + String.join(" ", codeWords));
  }

  /**
   * Helper method to create an unlock command with code only (no direct object).
   * Simulates "unlock with 1, 2, 3, 4" for implied object resolution.
   */
  private ParsedCommand createUnlockCommandWithCodeOnly(final String... codeWords) {
    final List<String> directObjects = new ArrayList<>();
    final List<String> indirectObjects = List.of(codeWords);
    return new ParsedCommand("unlock", directObjects, indirectObjects, "with",
        CommandType.SINGLE, false, "unlock with " + String.join(" ", codeWords));
  }
}
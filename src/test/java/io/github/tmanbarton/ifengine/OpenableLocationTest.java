package io.github.tmanbarton.ifengine;

import io.github.tmanbarton.ifengine.game.GameState;
import io.github.tmanbarton.ifengine.test.TestGameEngine;
import io.github.tmanbarton.ifengine.test.TestGameEngineBuilder;
import io.github.tmanbarton.ifengine.test.TestGameMap;
import io.github.tmanbarton.ifengine.test.TestItemFactory;
import io.github.tmanbarton.ifengine.test.TestOpenableLocation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for OpenableLocation abstract class.
 * Uses TestOpenableLocation as concrete implementation.
 */
@DisplayName("OpenableLocation")
class OpenableLocationTest {

  private static final String REQUIRED_KEY = "key";

  private TestOpenableLocation location;
  private TestGameMap gameMap;
  private TestGameEngine engine;
  private io.github.tmanbarton.ifengine.game.Player player;
  private Item testKey;

  @BeforeEach
  void setUp() {
    location = TestOpenableLocation.builder("test-vault")
        .withUnlockTargets("door", "vault")
        .withOpenTargets("door", "vault")
        .withRequiredKey(REQUIRED_KEY)
        .build();

    testKey = TestItemFactory.createTestKey();
    location.addItem(testKey);

    gameMap = TestGameMap.createWithLocations(location);

    engine = TestGameEngineBuilder.withCustomMap(gameMap)
        .withInitialPlayerState(GameState.PLAYING)
        .build();

    player = new io.github.tmanbarton.ifengine.game.Player(location);
  }

  @Nested
  @DisplayName("tryUnlock with providedAnswer")
  class TryUnlockWithProvidedAnswer {

    @Test
    @DisplayName("succeeds when providedAnswer matches required key name and player has key")
    void testTryUnlock_withKeyNameAsProvidedAnswer() {
      // Given - player has the required key
      player.addItem(testKey);

      // When - "unlock door with key" parses as providedAnswer = "key"
      final UnlockResult result = location.tryUnlock(player, REQUIRED_KEY, gameMap);

      // Then - should succeed because the answer matches the required key name
      assertTrue(result.success());
      assertEquals(TestOpenableLocation.DEFAULT_UNLOCK_MESSAGE, result.message());
      assertTrue(location.isUnlocked());
    }

    @Test
    @DisplayName("fails when providedAnswer matches key name but player doesn't have key")
    void testTryUnlock_withKeyNameButNoKey() {
      // Given - player does NOT have the key (it's at location, not in inventory)

      // When - "unlock door with key" but player doesn't have the key
      final UnlockResult result = location.tryUnlock(player, REQUIRED_KEY, gameMap);

      // Then - should fail because player doesn't have the key
      assertFalse(result.success());
      assertEquals(TestOpenableLocation.DEFAULT_UNLOCK_NO_KEY_MESSAGE, result.message());
      assertFalse(location.isUnlocked());
    }

    @Test
    @DisplayName("fails when providedAnswer is a code (not the key name)")
    void testTryUnlock_withCodeRejectsNonKey() {
      // Given - player has the key
      player.addItem(testKey);

      // When - "unlock door with 1234" (code, not key name)
      final UnlockResult result = location.tryUnlock(player, "1234", gameMap);

      // Then - should fail because key-based locks don't accept codes
      assertFalse(result.success());
      assertEquals(TestOpenableLocation.DEFAULT_UNLOCK_NO_KEY_MESSAGE, result.message());
      assertFalse(location.isUnlocked());
    }
  }

  @Nested
  @DisplayName("tryOpen with providedAnswer")
  class TryOpenWithProvidedAnswer {

    @Test
    @DisplayName("succeeds when providedAnswer matches required key name and player has key")
    void testTryOpen_withKeyNameAsProvidedAnswer() {
      // Given - player has the required key
      player.addItem(testKey);

      // When - "open door with key" parses as providedAnswer = "key"
      final OpenResult result = location.tryOpen(player, REQUIRED_KEY, gameMap);

      // Then - should auto-unlock and open
      assertTrue(result.success());
      assertEquals(TestOpenableLocation.DEFAULT_UNLOCK_AND_OPEN_MESSAGE, result.message());
      assertTrue(location.isUnlocked());
      assertTrue(location.isOpen());
    }

    @Test
    @DisplayName("fails when providedAnswer matches key name but player doesn't have key")
    void testTryOpen_withKeyNameButNoKey() {
      // Given - player does NOT have the key

      // When - "open door with key" but player doesn't have the key
      final OpenResult result = location.tryOpen(player, REQUIRED_KEY, gameMap);

      // Then - should fail because player doesn't have the key
      assertFalse(result.success());
      assertEquals(TestOpenableLocation.DEFAULT_OPEN_LOCKED_NO_KEY_MESSAGE, result.message());
      assertFalse(location.isUnlocked());
      assertFalse(location.isOpen());
    }

    @Test
    @DisplayName("fails when providedAnswer is a code (not the key name)")
    void testTryOpen_withCodeRejectsNonKey() {
      // Given - player has the key
      player.addItem(testKey);

      // When - "open door with 1234" (code, not key name)
      final OpenResult result = location.tryOpen(player, "1234", gameMap);

      // Then - should fail because key-based locks don't accept codes
      assertFalse(result.success());
      assertEquals(TestOpenableLocation.DEFAULT_OPEN_LOCKED_NO_KEY_MESSAGE, result.message());
      assertFalse(location.isUnlocked());
      assertFalse(location.isOpen());
    }
  }
}
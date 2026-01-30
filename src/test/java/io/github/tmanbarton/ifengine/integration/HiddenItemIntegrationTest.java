package io.github.tmanbarton.ifengine.integration;

import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.response.DefaultResponses;
import io.github.tmanbarton.ifengine.response.ResponseProvider;
import io.github.tmanbarton.ifengine.test.JsonTestUtils;
import io.github.tmanbarton.ifengine.test.TestFixtures;
import io.github.tmanbarton.ifengine.test.TestGameEngine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for hidden item functionality.
 * <p>
 * Verifies that hidden items are invisible until revealed, can be interacted
 * with after reveal, use revealed descriptions, and are properly restored
 * on game restart.
 */
@DisplayName("Hidden Item Integration Tests")
class HiddenItemIntegrationTest {

  private static final String SESSION_ID = "test-session";
  private static final ResponseProvider RESPONSES = new DefaultResponses();

  @Nested
  class HiddenItemVisibility {

    @Test
    @DisplayName("hidden item is not visible in location description")
    void testLook_hiddenItemNotVisibleInLocationDescription() {
      // Given
      final TestGameEngine engine = TestFixtures.hiddenItemScenario();
      engine.createPlayer(SESSION_ID);
      final Location location = engine.getPlayer(SESSION_ID).getCurrentLocation();
      final Item key = engine.getTestGameMap().getItem("key");

      // When
      final String response = engine.processCommand(SESSION_ID, "look");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(location.getLongDescription() + "\n\n", message);
      assertTrue(location.isItemHidden(key));
      assertFalse(location.hasItem("key"));
    }

    @Test
    @DisplayName("hidden item cannot be taken")
    void testTake_hiddenItemCannotBeTaken() {
      // Given
      final TestGameEngine engine = TestFixtures.hiddenItemScenario();
      engine.createPlayer(SESSION_ID);
      final Location location = engine.getPlayer(SESSION_ID).getCurrentLocation();
      final Item key = engine.getTestGameMap().getItem("key");
      final Player player = engine.getPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "take key");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getItemNotPresent("key") + "\n\n", message);
      assertTrue(location.isItemHidden(key));
      assertFalse(player.hasItem("key"));
    }
  }

  @Nested
  class RevealAndInteract {

    @Test
    @DisplayName("revealed item shows revealed description in look output")
    void testRevealThenLook_showsRevealedDescription() {
      // Given
      final TestGameEngine engine = TestFixtures.hiddenItemScenario();
      engine.createPlayer(SESSION_ID);
      final Location location = engine.getPlayer(SESSION_ID).getCurrentLocation();
      final Item key = engine.getTestGameMap().getItem("key");
      location.revealHiddenItemByName("key");

      // When
      final String response = engine.processCommand(SESSION_ID, "look");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      final String revealedDesc = location.getRevealedLocationDescription(key);
      assertEquals(
          location.getLongDescription() + "\n\n" + revealedDesc + "\n\n",
          message
      );
      assertFalse(location.isItemHidden(key));
      assertTrue(location.hasItem("key"));
    }

    @Test
    @DisplayName("revealed item can be taken")
    void testRevealThenTake_itemTaken() {
      // Given
      final TestGameEngine engine = TestFixtures.hiddenItemScenario();
      engine.createPlayer(SESSION_ID);
      final Location location = engine.getPlayer(SESSION_ID).getCurrentLocation();
      final Player player = engine.getPlayer(SESSION_ID);
      location.revealHiddenItemByName("key");

      // When
      final String response = engine.processCommand(SESSION_ID, "take key");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(RESPONSES.getTakeSuccess() + "\n\n", message);
      assertTrue(player.hasItem("key"));
      assertFalse(location.hasItem("key"));
    }

    @Test
    @DisplayName("dropped item after reveal uses normal location description")
    void testTakeThenDrop_usesNormalLocationDescription() {
      // Given
      final TestGameEngine engine = TestFixtures.hiddenItemScenario();
      engine.createPlayer(SESSION_ID);
      final Location location = engine.getPlayer(SESSION_ID).getCurrentLocation();
      final Item key = engine.getTestGameMap().getItem("key");
      location.revealHiddenItemByName("key");
      engine.processCommand(SESSION_ID, "take key");
      engine.processCommand(SESSION_ID, "drop key");

      // When
      final String response = engine.processCommand(SESSION_ID, "look");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals(
          location.getLongDescription() + "\n\n" + key.getLocationDescription() + "\n\n",
          message
      );
      assertTrue(location.hasItem("key"));
      assertNull(location.getRevealedLocationDescription(key));
    }
  }

  @Nested
  class ResetBehavior {

    @Test
    @DisplayName("restart restores hidden item state")
    void testRestart_restoresHiddenState() {
      // Given
      final TestGameEngine engine = TestFixtures.hiddenItemScenario();
      engine.createPlayer(SESSION_ID);
      final Location location = engine.getPlayer(SESSION_ID).getCurrentLocation();
      final Item key = engine.getTestGameMap().getItem("key");
      location.revealHiddenItemByName("key");
      assertFalse(location.isItemHidden(key));
      assertTrue(location.hasItem("key"));

      // When
      engine.processCommand(SESSION_ID, "restart");
      engine.processCommand(SESSION_ID, "yes");

      // Then
      assertTrue(location.isItemHidden(key));
      assertFalse(location.hasItem("key"));

      final String lookResponse = engine.processCommand(SESSION_ID, "look");
      final String lookMessage = JsonTestUtils.extractMessage(lookResponse);
      assertEquals(location.getLongDescription() + "\n\n", lookMessage);
    }
  }
}

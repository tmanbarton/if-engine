package io.github.tmanbarton.ifengine.integration;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for command sequence functionality.
 * <p>
 * These tests verify that "then" sequences work correctly.
 * Unit tests don't cover this because they test handlers with single ParsedCommand objects.
 */
@DisplayName("Command Sequence Integration Tests")
class CommandSequenceIntegrationTest {

  private static final String SESSION_ID = "test-session";
  private static final ResponseProvider RESPONSES = new DefaultResponses();

  @Nested
  class ThenSequences {

    @Test
    @DisplayName("'go north then take rope' executes both commands")
    void testThenSequence_moveThenTake() {
      // Given
      final TestGameEngine engine = TestFixtures.adventureScenario();
      engine.createPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "go north then take rope");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertEquals("forest", player.getCurrentLocation().getName());
      assertTrue(player.hasItem("rope"));
    }

    @Test
    @DisplayName("three command sequence executes all commands")
    void testThenSequence_threeCommands() {
      // Given
      final TestGameEngine engine = TestFixtures.adventureScenario();
      engine.createPlayer(SESSION_ID);

      // When
      engine.processCommand(SESSION_ID, "take key then go north then take rope");

      // Then
      final Player player = engine.getPlayer(SESSION_ID);
      assertTrue(player.hasItem("key"));
      assertTrue(player.hasItem("rope"));
      assertEquals("forest", player.getCurrentLocation().getName());
    }
  }

  @Nested
  class ResponseFormat {

    @Test
    @DisplayName("'then' sequence returns combined response")
    void testThenSequence_responseFormat() {
      // Given
      final TestGameEngine engine = TestFixtures.adventureScenario();
      engine.createPlayer(SESSION_ID);

      // When
      final String response = engine.processCommand(SESSION_ID, "take key then go north");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      // Response should contain both take success and location description
      assertTrue(message.startsWith(RESPONSES.getTakeSuccess()));
    }
  }
}
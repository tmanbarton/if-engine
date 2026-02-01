package io.github.tmanbarton.ifengine.integration;

import io.github.tmanbarton.ifengine.test.JsonTestUtils;
import io.github.tmanbarton.ifengine.test.TestFixtures;
import io.github.tmanbarton.ifengine.test.TestGameEngine;
import io.github.tmanbarton.ifengine.test.TestOpenableSceneryObject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for openable scenery object functionality.
 * <p>
 * Verifies the full processCommand stack for openable scenery:
 * parser → dispatcher → handler → OpenableSceneryObject → JSON response.
 * <p>
 * Basic routing and priority is covered by OpenHandlerTest and UnlockHandlerTest.
 */
@DisplayName("Openable Scenery Integration Tests")
class OpenableSceneryIntegrationTest {

  private static final String SESSION_ID = "test-session";

  @Nested
  class LockedScenery {

    @Test
    @DisplayName("take key, unlock safe, open safe - full sequence")
    void testOpenableScenery_fullUnlockOpenSequence() {
      // Given
      final TestGameEngine engine = TestFixtures.openableSceneryScenario();
      engine.createPlayer(SESSION_ID);

      // When - take the key
      engine.processCommand(SESSION_ID, "take safe-key");

      // When - unlock the safe
      final String unlockResponse = engine.processCommand(SESSION_ID, "unlock safe");

      // Then - safe unlocks
      final String unlockMessage = JsonTestUtils.extractMessage(unlockResponse);
      assertEquals(TestOpenableSceneryObject.DEFAULT_UNLOCK_MESSAGE + "\n\n", unlockMessage);

      // When - open the safe
      final String openResponse = engine.processCommand(SESSION_ID, "open safe");

      // Then - safe opens
      final String openMessage = JsonTestUtils.extractMessage(openResponse);
      assertEquals(TestOpenableSceneryObject.DEFAULT_OPEN_MESSAGE + "\n\n", openMessage);
    }
  }

  @Nested
  class NoLockScenery {

    @Test
    @DisplayName("open cabinet directly without unlocking")
    void testOpenableScenery_directOpen() {
      // Given
      final TestGameEngine engine = TestFixtures.openableSceneryNoLockScenario();
      engine.createPlayer(SESSION_ID);

      // When - open cabinet directly
      final String openResponse = engine.processCommand(SESSION_ID, "open cabinet");

      // Then
      final String openMessage = JsonTestUtils.extractMessage(openResponse);
      assertEquals(TestOpenableSceneryObject.DEFAULT_OPEN_MESSAGE + "\n\n", openMessage);
    }
  }
}
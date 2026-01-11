package io.github.tmanbarton.ifengine.command;

import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.game.GameEngine;
import io.github.tmanbarton.ifengine.game.GameMap;
import io.github.tmanbarton.ifengine.test.JsonTestUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for custom command registration via GameMap.Builder.
 * <p>
 * These tests verify the full flow of registering custom commands through the
 * builder API and executing them through the game engine.
 */
@DisplayName("Custom Command Integration Tests")
class CustomCommandIntegrationTest {

  private static final String SESSION_ID = "test-session";

  @Nested
  class CustomCommandRegistration {

    @Test
    @DisplayName("executes simple custom command")
    void testWithCommand_simpleCommand() {
      // Given
      final GameMap map = new GameMap.Builder()
              .addLocation(new Location("room", "A test room.", "Test room."))
              .setStartingLocation("room")
              .skipIntro()
              .withCommand("xyzzy", (player, cmd, ctx) -> "Nothing happens.")
              .build();
      final GameEngine engine = new GameEngine(map);

      // When
      final String response = engine.processCommand(SESSION_ID, "xyzzy");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals("Nothing happens.\n\n", message);
    }

    @Test
    @DisplayName("executes custom command with aliases")
    void testWithCommand_withAliases() {
      // Given
      final GameMap map = new GameMap.Builder()
              .addLocation(new Location("room", "A test room.", "Test room."))
              .setStartingLocation("room")
              .skipIntro()
              .withCommand("search", List.of("find", "look for"), (player, cmd, ctx) ->
                      "You search carefully but find nothing.")
              .build();
      final GameEngine engine = new GameEngine(map);

      // When - use the alias instead of primary verb
      final String response = engine.processCommand(SESSION_ID, "find");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals("You search carefully but find nothing.\n\n", message);
    }

    @Test
    @DisplayName("custom command receives parsed command with objects")
    void testWithCommand_receivesDirectObject() {
      // Given
      final GameMap map = new GameMap.Builder()
              .addLocation(new Location("room", "A test room.", "Test room."))
              .setStartingLocation("room")
              .skipIntro()
              .withCommand("poke", (player, cmd, ctx) -> {
                final String target = cmd.getFirstDirectObject();
                if (target.isEmpty()) {
                  return "Poke what?";
                }
                return "You poke the " + target + ".";
              })
              .build();
      final GameEngine engine = new GameEngine(map);

      // When
      final String response = engine.processCommand(SESSION_ID, "poke lamp");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals("You poke the lamp.\n\n", message);
    }

    @Test
    @DisplayName("custom command can override built-in command")
    void testWithCommand_overridesBuiltIn() {
      // Given
      final GameMap map = new GameMap.Builder()
              .addLocation(new Location("room", "A test room.", "Test room."))
              .setStartingLocation("room")
              .skipIntro()
              .withCommand("look", (player, cmd, ctx) -> "You see nothing special.")
              .build();
      final GameEngine engine = new GameEngine(map);

      // When
      final String response = engine.processCommand(SESSION_ID, "look");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals("You see nothing special.\n\n", message);
    }

    @Test
    @DisplayName("custom command handler receives context with utilities")
    void testWithCommand_contextProvidesUtilities() {
      // Given
      final Item key = new Item("key", "a brass key", "A key lies here.", "An old brass key.");
      final GameMap map = new GameMap.Builder()
              .addLocation(new Location("room", "A test room.", "Test room."))
              .placeItem(key, "room")
              .setStartingLocation("room")
              .skipIntro()
              .withCommand("locate", (player, cmd, ctx) -> {
                final String target = cmd.getFirstDirectObject();
                final var resolved = ctx.resolveItem(target, player);
                if (resolved.isPresent()) {
                  return "Found: " + resolved.get().getName();
                }
                return "Not found: " + target;
              })
              .build();
      final GameEngine engine = new GameEngine(map);

      // When
      final String response = engine.processCommand(SESSION_ID, "locate key");

      // Then
      final String message = JsonTestUtils.extractMessage(response);
      assertEquals("Found: key\n\n", message);
    }

    @Test
    @DisplayName("custom command returning null delegates to built-in handler")
    void testWithCommand_returnsNullDelegatesToBuiltIn() {
      // Given - custom "look" that always returns null to delegate
      final GameMap map = new GameMap.Builder()
              .addLocation(new Location("room", "A test room.", "Test room."))
              .setStartingLocation("room")
              .skipIntro()
              .withCommand("look", (player, cmd, ctx) -> null)
              .build();
      final GameEngine engine = new GameEngine(map);

      // When
      final String response = engine.processCommand(SESSION_ID, "look");

      // Then - should get built-in look response (location description)
      final String message = JsonTestUtils.extractMessage(response);
      assertTrue(message.contains("A test room."),
              "Expected built-in look behavior with location description, got: " + message);
    }

    @Test
    @DisplayName("custom command handles specific case and delegates otherwise")
    void testWithCommand_partialOverride() {
      // Given - custom "look" that handles "look mirror" specially, delegates otherwise
      final GameMap map = new GameMap.Builder()
              .addLocation(new Location("room", "A test room.", "Test room."))
              .setStartingLocation("room")
              .skipIntro()
              .withCommand("look", (player, cmd, ctx) -> {
                final String target = cmd.getFirstDirectObject();
                if ("mirror".equals(target)) {
                  return "You see your reflection staring back at you.";
                }
                // Delegate to default look for everything else
                return null;
              })
              .build();
      final GameEngine engine = new GameEngine(map);

      // When - special case
      final String mirrorResponse = engine.processCommand(SESSION_ID, "look mirror");

      // Then - custom handler response
      final String mirrorMessage = JsonTestUtils.extractMessage(mirrorResponse);
      assertEquals("You see your reflection staring back at you.\n\n", mirrorMessage);

      // When - default case (just "look")
      final String lookResponse = engine.processCommand(SESSION_ID, "look");

      // Then - built-in look response
      final String lookMessage = JsonTestUtils.extractMessage(lookResponse);
      assertTrue(lookMessage.contains("A test room."),
              "Expected built-in look behavior, got: " + lookMessage);
    }
  }
}
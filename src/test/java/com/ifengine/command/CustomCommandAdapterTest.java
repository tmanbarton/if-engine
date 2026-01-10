package com.ifengine.command;

import com.ifengine.Location;
import com.ifengine.game.Player;
import com.ifengine.parser.CommandType;
import com.ifengine.parser.ParsedCommand;
import com.ifengine.response.DefaultResponses;
import com.ifengine.response.ResponseProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for CustomCommandAdapter.
 */
@DisplayName("CustomCommandAdapter")
class CustomCommandAdapterTest {

  private Player player;
  private ResponseProvider responses;

  @BeforeEach
  void setUp() {
    final Location location = new Location("test", "Test location.", "Test.");
    player = new Player(location);
    responses = new DefaultResponses();
  }

  private CommandContext createContext(final Player p) {
    return new DefaultCommandContext(responses, null, p);
  }

  private ParsedCommand createCommand(final String verb) {
    return new ParsedCommand(
        verb,
        Collections.emptyList(),
        Collections.emptyList(),
        null,
        CommandType.SINGLE,
        false,
        verb
    );
  }

  @Nested
  @DisplayName("getSupportedVerbs")
  class GetSupportedVerbs {

    @Test
    @DisplayName("returns primary verb when no aliases")
    void testGetSupportedVerbs_primaryVerbOnly() {
      // Given
      final CustomCommandHandler handler = (p, cmd, ctx) -> "response";
      final CustomCommandAdapter adapter = new CustomCommandAdapter(
          "dance", List.of(), handler, CustomCommandAdapterTest.this::createContext, null);

      // When
      final List<String> verbs = adapter.getSupportedVerbs();

      // Then
      assertEquals(1, verbs.size());
      assertTrue(verbs.contains("dance"));
    }

    @Test
    @DisplayName("returns primary verb and all aliases")
    void testGetSupportedVerbs_includesAliases() {
      // Given
      final CustomCommandHandler handler = (p, cmd, ctx) -> "response";
      final CustomCommandAdapter adapter = new CustomCommandAdapter(
          "search", List.of("find", "look for"), handler, CustomCommandAdapterTest.this::createContext, null);

      // When
      final List<String> verbs = adapter.getSupportedVerbs();

      // Then
      assertEquals(3, verbs.size());
      assertTrue(verbs.contains("search"));
      assertTrue(verbs.contains("find"));
      assertTrue(verbs.contains("look for"));
    }
  }

  @Nested
  @DisplayName("handle")
  class Handle {

    @Test
    @DisplayName("delegates to custom handler with context")
    void testHandle_delegatesToHandler() {
      // Given
      final CustomCommandHandler handler = (p, cmd, ctx) -> "custom response";
      final CustomCommandAdapter adapter = new CustomCommandAdapter(
          "test", List.of(), handler, CustomCommandAdapterTest.this::createContext, null);
      final ParsedCommand command = createCommand("test");

      // When
      final String result = adapter.handle(player, command);

      // Then
      assertEquals("custom response", result);
    }

    @Test
    @DisplayName("passes player to handler")
    void testHandle_passesPlayer() {
      // Given
      final CustomCommandHandler handler = (p, cmd, ctx) ->
          "Location: " + p.getCurrentLocation().getName();
      final CustomCommandAdapter adapter = new CustomCommandAdapter(
          "test", List.of(), handler, CustomCommandAdapterTest.this::createContext, null);
      final ParsedCommand command = createCommand("test");

      // When
      final String result = adapter.handle(player, command);

      // Then
      assertEquals("Location: test", result);
    }

    @Test
    @DisplayName("passes parsed command to handler")
    void testHandle_passesParsedCommand() {
      // Given
      final CustomCommandHandler handler = (p, cmd, ctx) ->
          "Verb: " + cmd.getVerb();
      final CustomCommandAdapter adapter = new CustomCommandAdapter(
          "poke", List.of(), handler, CustomCommandAdapterTest.this::createContext, null);
      final ParsedCommand command = createCommand("poke");

      // When
      final String result = adapter.handle(player, command);

      // Then
      assertEquals("Verb: poke", result);
    }
  }
}
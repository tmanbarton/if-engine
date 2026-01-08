package com.ifengine.command;

import com.ifengine.game.Player;
import com.ifengine.parser.CommandType;
import com.ifengine.parser.ParsedCommand;
import com.ifengine.test.TestLocationFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for CommandDispatcher.
 * Tests verb to handler routing
 */
@DisplayName("CommandDispatcher Tests")
class CommandDispatcherTest {

  private CommandDispatcher dispatcher;
  private Player player;

  @BeforeEach
  void setUp() {
    dispatcher = new CommandDispatcher();
    player = new Player(TestLocationFactory.createDefaultLocation());
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

  private ParsedCommand createCommand(final String verb, final String directObject) {
    return new ParsedCommand(
        verb,
        List.of(directObject),
        Collections.emptyList(),
        null,
        CommandType.SINGLE,
        false,
        verb + " " + directObject
    );
  }

  private static class TestHandler implements BaseCommandHandler {
    private final List<String> verbs;
    private final String response;

    TestHandler(final List<String> verbs, final String response) {
      this.verbs = verbs;
      this.response = response;
    }

    @Override
    public List<String> getSupportedVerbs() {
      return verbs;
    }

    @Override
    public String handle(final Player player, final ParsedCommand command) {
      return response;
    }
  }

  @Nested
  @DisplayName("Handler Registration")
  class HandlerRegistration {

    @Test
    @DisplayName("Test registerHandler - multiple verbs for same handler")
    void testRegisterHandler_multipleVerbsSameHandler() {
      final TestHandler handler = new TestHandler(List.of("take", "get", "grab"), "taken");

      dispatcher.registerHandler(handler);

      assertTrue(dispatcher.hasHandlerForVerb("take"));
      assertTrue(dispatcher.hasHandlerForVerb("get"));
      assertTrue(dispatcher.hasHandlerForVerb("grab"));
      assertEquals(handler, dispatcher.getHandlerForVerb("take"));
    }

    @Test
    @DisplayName("Test registerHandler - case insensitive verb lookup")
    void testRegisterHandler_caseInsensitive() {
      final TestHandler handler = new TestHandler(List.of("TEST"), "response");

      dispatcher.registerHandler("TEST", handler);

      assertTrue(dispatcher.hasHandlerForVerb("test"));
      assertTrue(dispatcher.hasHandlerForVerb("TEST"));
      assertTrue(dispatcher.hasHandlerForVerb("Test"));
    }
  }

  @Nested
  @DisplayName("Command Dispatch")
  class CommandDispatch {

    @Test
    @DisplayName("Test handle - routes to correct handler")
    void testHandle_routesToCorrectHandler() {
      final TestHandler takeHandler = new TestHandler(List.of("take"), "taken");
      final TestHandler lookHandler = new TestHandler(List.of("look"), "looked");
      dispatcher.registerHandler(takeHandler);
      dispatcher.registerHandler(lookHandler);

      final Optional<String> takeResult = dispatcher.handle(player, createCommand("take", "key"));
      final Optional<String> lookResult = dispatcher.handle(player, createCommand("look"));

      assertTrue(takeResult.isPresent());
      assertEquals("taken", takeResult.get());
      assertTrue(lookResult.isPresent());
      assertEquals("looked", lookResult.get());
    }

    @Test
    @DisplayName("Test handle - returns empty for unregistered verb")
    void testHandle_emptyForUnregisteredVerb() {
      final Optional<String> result = dispatcher.handle(player, createCommand("unknown"));

      assertFalse(result.isPresent());
    }
  }

  @Nested
  @DisplayName("Handler Unregistration")
  class HandlerUnregistration {

    @Test
    @DisplayName("Test unregisterHandler by verb - removes verb mapping")
    void testUnregisterHandler_byVerb() {
      final TestHandler handler = new TestHandler(List.of("test"), "response");
      dispatcher.registerHandler(handler);

      final boolean removed = dispatcher.unregisterHandler("test");

      assertTrue(removed);
      assertFalse(dispatcher.hasHandlerForVerb("test"));
    }

    @Test
    @DisplayName("Test unregisterHandler by handler - removes all verb mappings")
    void testUnregisterHandler_byHandler() {
      final TestHandler handler = new TestHandler(List.of("test1", "test2"), "response");
      dispatcher.registerHandler(handler);

      final boolean removed = dispatcher.unregisterHandler(handler);

      assertTrue(removed);
      assertFalse(dispatcher.hasHandlerForVerb("test1"));
      assertFalse(dispatcher.hasHandlerForVerb("test2"));
    }
  }

  @Nested
  @DisplayName("Query Methods")
  class QueryMethods {

    @Test
    @DisplayName("Test getRegisteredVerbs - returns all registered verbs")
    void testGetRegisteredVerbs_returnsAllVerbs() {
      final TestHandler handler = new TestHandler(List.of("take", "get"), "response");
      dispatcher.registerHandler(handler);

      final Set<String> verbs = dispatcher.getRegisteredVerbs();

      assertEquals(2, verbs.size());
      assertTrue(verbs.contains("take"));
      assertTrue(verbs.contains("get"));
    }

    @Test
    @DisplayName("Test getRegisteredHandlers - returns all handlers")
    void testGetRegisteredHandlers_returnsAllHandlers() {
      final TestHandler handler1 = new TestHandler(List.of("test1"), "response1");
      final TestHandler handler2 = new TestHandler(List.of("test2"), "response2");
      dispatcher.registerHandler(handler1);
      dispatcher.registerHandler(handler2);

      final Set<BaseCommandHandler> handlers = dispatcher.getRegisteredHandlers();

      assertEquals(2, handlers.size());
      assertTrue(handlers.contains(handler1));
      assertTrue(handlers.contains(handler2));
    }
  }
}
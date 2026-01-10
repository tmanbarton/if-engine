package com.ifengine.command.handlers;

import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.game.HintConfiguration;
import com.ifengine.game.HintConfigurationBuilder;
import com.ifengine.game.Player;
import com.ifengine.parser.CommandType;
import com.ifengine.parser.ParsedCommand;
import com.ifengine.test.TestLocationFactory;
import com.ifengine.test.TestGameMap;
import com.ifengine.test.TestGameMapBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("HintHandler")
class HintHandlerTest {

  private Location location;
  private Player player;
  private TestGameMap gameMap;

  @BeforeEach
  void setUp() {
    location = TestLocationFactory.createSimpleLocation("test-room");
    player = new Player(location);
    gameMap = TestGameMapBuilder.singleLocation().build();
  }

  private ParsedCommand createHintCommand() {
    return new ParsedCommand(
        "hint",
        Collections.emptyList(),
        Collections.emptyList(),
        null,
        CommandType.SINGLE,
        false,
        "hint"
    );
  }

  @Nested
  @DisplayName("Without configuration")
  class WithoutConfiguration {

    @Test
    @DisplayName("returns default message when no HintConfiguration provided")
    void testHint_withNoConfig_returnsDefaultMessage() {
      // Given
      final var handler = new HintHandler(null, gameMap);

      // When
      final String result = handler.handle(player, createHintCommand());

      // Then
      assertEquals(HintConfiguration.DEFAULT_HINT, result);
    }
  }

  @Nested
  @DisplayName("With configuration")
  class WithConfiguration {

    private HintHandler handler;
    private HintConfiguration config;

    @BeforeEach
    void setUp() {
      config = new HintConfigurationBuilder()
          .addPhase("PHASE_ONE",
              "Phase one hint 1",
              "Phase one hint 2",
              "Phase one hint 3")
          .addPhase("PHASE_TWO",
              "Phase two hint 1",
              "Phase two hint 2",
              "Phase two hint 3")
          .determiner((p, gm) -> p.hasItem("key") ? "PHASE_TWO" : "PHASE_ONE")
          .build();
      handler = new HintHandler(config, gameMap);
    }

    @Test
    @DisplayName("progressive hints return level 1, then 2, then 3")
    void testHint_progressiveHints_level1Then2Then3() {
      // Given - player starts in PHASE_ONE (no key)

      // When/Then - first hint is level 1
      final String hint1 = handler.handle(player, createHintCommand());
      assertEquals("Phase one hint 1", hint1);

      // When/Then - second hint is level 2
      final String hint2 = handler.handle(player, createHintCommand());
      assertEquals("Phase one hint 2", hint2);

      // When/Then - third hint is level 3
      final String hint3 = handler.handle(player, createHintCommand());
      assertEquals("Phase one hint 3", hint3);
    }

    @Test
    @DisplayName("fourth request stays at level 3")
    void testHint_fourthRequest_staysAtLevel3() {
      // Given - request 3 hints to get to level 3
      handler.handle(player, createHintCommand());
      handler.handle(player, createHintCommand());
      handler.handle(player, createHintCommand());

      // When - request a fourth hint
      final String hint4 = handler.handle(player, createHintCommand());

      // Then - still returns level 3
      assertEquals("Phase one hint 3", hint4);
    }

    @Test
    @DisplayName("phase change resets to level 1")
    void testHint_phaseChange_resetsToLevel1() {
      // Given - get to level 2 in PHASE_ONE
      handler.handle(player, createHintCommand());
      handler.handle(player, createHintCommand());

      // When - change to PHASE_TWO by giving player the key
      player.addItem(new Item("key", "a key", "A key.", "A key."));
      final String hint = handler.handle(player, createHintCommand());

      // Then - returns level 1 of PHASE_TWO
      assertEquals("Phase two hint 1", hint);
    }
  }
}

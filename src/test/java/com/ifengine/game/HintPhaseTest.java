package com.ifengine.game;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("HintPhase")
class HintPhaseTest {

  @Test
  @DisplayName("getHint caps at level 3 for levels above 3")
  void testGetHint_levelAbove3_capsAtHint3() {
    // Given
    final var phase = new HintPhase(
        "TEST_PHASE",
        "Level 1 hint",
        "Level 2 hint",
        "Level 3 hint"
    );

    // When/Then
    assertEquals("Level 3 hint", phase.getHint(4));
    assertEquals("Level 3 hint", phase.getHint(5));
    assertEquals("Level 3 hint", phase.getHint(100));
  }

  @Test
  @DisplayName("getHint returns correct hint for each level")
  void testGetHint_returnsCorrectLevel() {
    // Given
    final var phase = new HintPhase(
        "TEST_PHASE",
        "Level 1 hint",
        "Level 2 hint",
        "Level 3 hint"
    );

    // When/Then
    assertEquals("Level 1 hint", phase.getHint(1));
    assertEquals("Level 2 hint", phase.getHint(2));
    assertEquals("Level 3 hint", phase.getHint(3));
  }
}

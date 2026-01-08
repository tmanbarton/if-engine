package com.ifengine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for SceneryObject class.
 * Tests the builder pattern, name matching, and interaction responses.
 */
@DisplayName("SceneryObject Tests")
class SceneryObjectTest {

  @Nested
  @DisplayName("Builder")
  class Builder {

    @Test
    @DisplayName("Test builder - creates scenery with name and interaction")
    void testBuilder_createsWithNameAndInteraction() {
      final SceneryObject scenery = SceneryObject.builder("tree")
          .withInteraction(InteractionType.CLIMB, "You climb the tree.")
          .build();

      assertEquals("tree", scenery.name());
      assertTrue(scenery.aliases().isEmpty());
      assertEquals(1, scenery.responses().size());
    }

    @Test
    @DisplayName("Test builder - no interactions throws exception")
    void testBuilder_noInteractionsThrows() {
      assertThrows(IllegalStateException.class, () ->
          SceneryObject.builder("tree").build()
      );
    }

    @Test
    @DisplayName("Test withAliases - adds multiple aliases")
    void testWithAliases_multipleAliases() {
      final SceneryObject scenery = SceneryObject.builder("tree")
          .withAliases("oak", "big tree", "tall tree")
          .withInteraction(InteractionType.CLIMB, "You climb.")
          .build();

      assertEquals(3, scenery.aliases().size());
      assertTrue(scenery.aliases().contains("oak"));
      assertTrue(scenery.aliases().contains("big tree"));
      assertTrue(scenery.aliases().contains("tall tree"));
    }

    @Test
    @DisplayName("Test withInteraction - multiple interactions")
    void testWithInteraction_multipleInteractions() {
      final SceneryObject scenery = SceneryObject.builder("tree")
          .withInteraction(InteractionType.CLIMB, "You climb the tree.")
          .withInteraction(InteractionType.PUNCH, "You punch the tree. Ouch!")
          .build();

      assertEquals(2, scenery.responses().size());
      assertEquals("You climb the tree.", scenery.responses().get(InteractionType.CLIMB));
      assertEquals("You punch the tree. Ouch!", scenery.responses().get(InteractionType.PUNCH));
    }
  }

  @Nested
  @DisplayName("Name Matching")
  class NameMatching {

    @Test
    @DisplayName("Test matches - exact name match")
    void testMatches_exactNameMatch() {
      final SceneryObject scenery = SceneryObject.builder("tree")
          .withInteraction(InteractionType.CLIMB, "You climb.")
          .build();

      assertTrue(scenery.matches("tree"));
    }

    @Test
    @DisplayName("Test matches - case insensitive")
    void testMatches_caseInsensitive() {
      final SceneryObject scenery = SceneryObject.builder("tree")
          .withInteraction(InteractionType.CLIMB, "You climb.")
          .build();

      assertTrue(scenery.matches("TREE"));
      assertTrue(scenery.matches("Tree"));
    }

    @Test
    @DisplayName("Test matches - alias match")
    void testMatches_aliasMatch() {
      final SceneryObject scenery = SceneryObject.builder("tree")
          .withAliases("oak", "big tree")
          .withInteraction(InteractionType.CLIMB, "You climb.")
          .build();

      assertTrue(scenery.matches("oak"));
      assertTrue(scenery.matches("big tree"));
    }

    @Test
    @DisplayName("Test matches - no match")
    void testMatches_noMatch() {
      final SceneryObject scenery = SceneryObject.builder("tree")
          .withInteraction(InteractionType.CLIMB, "You climb.")
          .build();

      assertFalse(scenery.matches("rock"));
      assertFalse(scenery.matches("trees"));
    }
  }

  @Nested
  @DisplayName("Interaction Responses")
  class InteractionResponses {

    @Test
    @DisplayName("Test getResponse - returns response for supported interaction")
    void testGetResponse_returnsResponse() {
      final SceneryObject scenery = SceneryObject.builder("tree")
          .withInteraction(InteractionType.CLIMB, "You climb the tree.")
          .build();

      final Optional<String> result = scenery.getResponse(InteractionType.CLIMB);

      assertTrue(result.isPresent());
      assertEquals("You climb the tree.", result.get());
    }

    @Test
    @DisplayName("Test getResponse - returns correct response when multiple interactions exist")
    void testGetResponse_multipleInteractions() {
      final SceneryObject scenery = SceneryObject.builder("tree")
          .withInteraction(InteractionType.CLIMB, "You climb the tree.")
          .withInteraction(InteractionType.PUNCH, "You punch the tree.")
          .withInteraction(InteractionType.KICK, "You kick the tree.")
          .build();

      assertEquals("You climb the tree.", scenery.getResponse(InteractionType.CLIMB).get());
      assertEquals("You punch the tree.", scenery.getResponse(InteractionType.PUNCH).get());
      assertEquals("You kick the tree.", scenery.getResponse(InteractionType.KICK).get());
    }
  }
}
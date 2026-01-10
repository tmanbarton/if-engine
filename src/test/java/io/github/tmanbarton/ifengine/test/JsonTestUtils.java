package io.github.tmanbarton.ifengine.test;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing JSON responses in tests.
 * Handles the JSON response format from the game engine.
 */
public final class JsonTestUtils {

  private JsonTestUtils() {
    throw new UnsupportedOperationException("Utility class should not be instantiated");
  }

  /**
   * Extracts the message content from a JSON game response.
   *
   * @param jsonResponse the JSON response string from the game engine
   * @return the message content, or the original string if not valid JSON
   */
  @Nonnull
  public static String extractMessage(@Nonnull final String jsonResponse) {
    if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
      return jsonResponse;
    }

    final String trimmed = jsonResponse.trim();
    if (!trimmed.startsWith("{")) {
      return jsonResponse;
    }

    try {
      final JSONObject json = new JSONObject(trimmed);
      return json.optString("message", jsonResponse);
    } catch (final Exception e) {
      return jsonResponse;
    }
  }

  /**
   * Extracts the game state from a JSON game response.
   *
   * @param jsonResponse the JSON response string from the game engine
   * @return the game state, or null if not found or not valid JSON
   */
  public static String extractGameState(@Nonnull final String jsonResponse) {
    if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
      return null;
    }

    final String trimmed = jsonResponse.trim();
    if (!trimmed.startsWith("{")) {
      return null;
    }

    try {
      final JSONObject json = new JSONObject(trimmed);
      return json.optString("gameState", null);
    } catch (final Exception e) {
      return null;
    }
  }

  /**
   * Extracts the boldableText from a JSON game response.
   *
   * @param jsonResponse the JSON response string from the game engine
   * @return the boldable text, or null if not present or not valid JSON
   */
  public static String extractBoldableText(@Nonnull final String jsonResponse) {
    if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
      return null;
    }

    final String trimmed = jsonResponse.trim();
    if (!trimmed.startsWith("{")) {
      return null;
    }

    try {
      final JSONObject json = new JSONObject(trimmed);
      if (json.isNull("boldableText")) {
        return null;
      }
      return json.optString("boldableText", null);
    } catch (final Exception e) {
      return null;
    }
  }

  /**
   * Extracts the valid directions array from a JSON game response.
   *
   * @param jsonResponse the JSON response string from the game engine
   * @return list of valid direction strings, or empty list if not found
   */
  @Nonnull
  public static List<String> extractValidDirections(@Nonnull final String jsonResponse) {
    if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
      return List.of();
    }

    final String trimmed = jsonResponse.trim();
    if (!trimmed.startsWith("{")) {
      return List.of();
    }

    try {
      final JSONObject json = new JSONObject(trimmed);
      if (!json.has("validDirections")) {
        return List.of();
      }

      final JSONArray directionsArray = json.getJSONArray("validDirections");
      final List<String> directions = new ArrayList<>();
      for (int i = 0; i < directionsArray.length(); i++) {
        directions.add(directionsArray.getString(i));
      }
      return directions;
    } catch (final Exception e) {
      return List.of();
    }
  }

  /**
   * Checks if the message contains the expected text.
   * Use this only when you need to verify partial content (e.g., location description
   * is part of a larger response).
   *
   * @param jsonResponse the JSON response string from the game engine
   * @param expectedText the text expected to be in the message
   * @return true if the message contains the expected text
   */
  public static boolean messageContains(@Nonnull final String jsonResponse,
                                         @Nonnull final String expectedText) {
    final String message = extractMessage(jsonResponse);
    return message.contains(expectedText);
  }
}
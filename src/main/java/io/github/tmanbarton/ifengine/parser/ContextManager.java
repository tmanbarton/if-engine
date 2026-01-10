package io.github.tmanbarton.ifengine.parser;

import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages conversational context for pronoun resolution and implied objects.
 * Tracks recently mentioned objects and maintains session-specific state.
 */
public final class ContextManager {
  // Session-based context storage
  private final Map<String, SessionContext> sessionContexts = new ConcurrentHashMap<>();

  /**
   * Session-specific context data
   */
  private static final class SessionContext {
    private final List<Item> lastDirectObjects = new ArrayList<>();
    private final Map<String, Item> possessiveReferences = new HashMap<>();
    private Location lastLocation;

    void updateLocation(@Nonnull final Location location) {
      if (lastLocation != location) {
        // Clear pronouns when location changes
        lastDirectObjects.clear();
        possessiveReferences.clear();
      }
      lastLocation = location;
    }
  }

  /**
   * Updates the context when a player changes location.
   *
   * @param sessionId the player session ID
   * @param location the new location
   */
  public void updateLocation(@Nonnull final String sessionId, @Nonnull final Location location) {
    final SessionContext context = getOrCreateContext(sessionId);
    context.updateLocation(location);
  }

  /**
   * Checks if a word is a pronoun that this manager can resolve.
   *
   * @param word the word to check
   * @return true if it's a resolvable pronoun
   */
  public boolean isPronoun(@Nonnull final String word) {
    final String lower = word.toLowerCase();
    return Objects.equals("it", lower) || Objects.equals("them", lower) || Objects.equals("they", lower) ||
        Objects.equals("its", lower) || Objects.equals("their", lower);
  }

  /**
   * Clears all context for a session (useful on game restart).
   *
   * @param sessionId the player session ID
   */
  public void clearContext(@Nonnull final String sessionId) {
    sessionContexts.remove(sessionId);
  }

  /**
   * Gets or creates a session context.
   *
   * @param sessionId the session ID
   * @return the session context
   */
  @Nonnull
  private SessionContext getOrCreateContext(@Nonnull final String sessionId) {
    return sessionContexts.computeIfAbsent(sessionId, id -> new SessionContext());
  }
}
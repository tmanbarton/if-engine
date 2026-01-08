package com.ifengine.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import com.ifengine.game.GameEngine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for game client connections.
 * <p>
 * Manages WebSocket sessions and routes messages between clients and the GameEngine.
 * Each client connection gets a unique session ID used to track their game state.
 * <p>
 * Usage in your application:
 * <pre>
 * GameEngine engine = new GameEngine(gameMap, responseProvider);
 * wsContainer.addMapping("/game", (req, resp) -> new GameWebSocketHandler(engine));
 * </pre>
 */
@WebSocket
public class ExampleGameWebSocketHandler extends WebSocketAdapter {

  private final GameEngine gameEngine;
  private static final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

  /**
   * Creates a WebSocket handler with the given game engine.
   *
   * @param gameEngine the game engine to process commands
   */
  public ExampleGameWebSocketHandler(@Nonnull final GameEngine gameEngine) {
    this.gameEngine = gameEngine;
  }

  @Override
  public void onWebSocketConnect(@Nonnull final Session session) {
    super.onWebSocketConnect(session);
    final String sessionId = getSessionId(session);
    sessions.put(sessionId, session);

    System.out.println("New connection established: " + sessionId);
  }

  @Override
  public void onWebSocketText(@Nonnull final String message) {
    final Session session = getSession();
    final String sessionId = getSessionId(session);
    final String command = message.trim();

    if (command.isEmpty()) {
      return;
    }

    try {
      // Process command through game engine
      final String response = gameEngine.processCommand(sessionId, command);

      // Send response back to client
      if (!response.isEmpty()) {
        session.getRemote().sendString(response);
      }

    } catch (final Exception e) {
      // Send error message to client
      try {
        final String errorMsg = "Error processing command: " + e.getMessage();
        session.getRemote().sendString(errorMsg);
        System.err.println("Error processing command '" + command + "' for session " + sessionId + ": " + e.getMessage());
      } catch (final IOException ioException) {
        System.err.println("Failed to send error message: " + ioException.getMessage());
      }
    }
  }

  @Override
  public void onWebSocketClose(final int statusCode, @Nullable final String reason) {
    final Session session = getSession();
    final String sessionId = getSessionId(session);

    sessions.remove(sessionId);
    gameEngine.cleanupSession(sessionId);
    System.out.println("Connection closed: " + sessionId + " (" + statusCode + ": " + reason + ")");

    super.onWebSocketClose(statusCode, reason);
  }

  @Override
  public void onWebSocketError(@Nonnull final Throwable cause) {
    final Session session = getSession();
    final String sessionId = getSessionId(session);

    System.err.println("WebSocket error for session " + sessionId + ": " + cause.getMessage());
    sessions.remove(sessionId);
    gameEngine.cleanupSession(sessionId);

    super.onWebSocketError(cause);
  }

  /**
   * Sends a message to a specific session.
   * Useful for timed events, notifications, etc.
   *
   * @param sessionId the session to send to
   * @param message the message to send
   */
  public static void sendMessageToSession(@Nonnull final String sessionId, @Nonnull final String message) {
    final Session session = sessions.get(sessionId);
    if (session != null && session.isOpen()) {
      try {
        session.getRemote().sendString(message);
      } catch (final IOException e) {
        System.err.println("Failed to send message to session " + sessionId + ": " + e.getMessage());
      }
    }
  }

  /**
   * Broadcasts a message to all connected sessions.
   * Useful for server-wide announcements.
   *
   * @param message the message to broadcast
   */
  public static void broadcastMessage(@Nonnull final String message) {
    sessions.values().forEach(session -> {
      if (session.isOpen()) {
        try {
          session.getRemote().sendString(message);
        } catch (final IOException e) {
          System.err.println("Failed to broadcast to session: " + e.getMessage());
        }
      }
    });
  }

  @Nonnull
  private String getSessionId(@Nullable final Session session) {
    return session != null ? session.toString() : "unknown";
  }
}
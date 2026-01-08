package com.ifengine;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import com.ifengine.content.ExampleGameContent;
import com.ifengine.content.GameContent;
import com.ifengine.game.GameEngine;
import com.ifengine.game.GameMap;
import com.ifengine.websocket.ExampleGameWebSocketHandler;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * Example application showing how to start an IFEngine game server.
 * <p>
 * This demonstrates the standard pattern for wiring up a game:
 * <ol>
 *   <li>Create your GameContent implementation</li>
 *   <li>Wrap it in a GameMap</li>
 *   <li>Create a GameEngine with the map and response provider</li>
 *   <li>Start the WebSocket server</li>
 * </ol>
 * <p>
 * To create your own game, replace ExampleGameContent with your implementation:
 * <pre>
 * GameContent content = new MyAdventureContent();
 * </pre>
 */
public class IFEngineApplication {

  private static final int DEFAULT_PORT = 8080;

  public static void main(@Nonnull final String[] args) {
    final int port = getPort(args);
    final Server server = new Server(port);

    final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    server.setHandler(context);

    // ============================================================
    // GAME SETUP - Replace ExampleGameContent with your own game
    // ============================================================
    final GameContent content = new ExampleGameContent();
    final GameMap gameMap = new GameMap(content);
    final GameEngine gameEngine = new GameEngine(gameMap, content.getResponseProvider());
    // ============================================================

    // Configure WebSocket
    JettyWebSocketServletContainerInitializer.configure(context, (servletContext, wsContainer) -> {
      // Disable idle timeout for uninterrupted gameplay
      wsContainer.setIdleTimeout(Duration.ofSeconds(0));

      // Map WebSocket endpoint
      wsContainer.addMapping("/game", (req, resp) -> new ExampleGameWebSocketHandler(gameEngine));
    });

    try {
      server.start();
      System.out.println("===========================================");
      System.out.println("  IFEngine Game Server Started");
      System.out.println("===========================================");
      System.out.println("  Port: " + port);
      System.out.println("  WebSocket: ws://localhost:" + port + "/game");
      System.out.println("===========================================");
      server.join();
    } catch (final Exception e) {
      System.err.println("Failed to start server: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Gets the port from command line args or returns the default.
   *
   * @param args command line arguments
   * @return the port to use
   */
  private static int getPort(@Nonnull final String[] args) {
    if (args.length > 0) {
      try {
        return Integer.parseInt(args[0]);
      } catch (final NumberFormatException e) {
        System.err.println("Invalid port '" + args[0] + "', using default: " + DEFAULT_PORT);
      }
    }
    return DEFAULT_PORT;
  }
}
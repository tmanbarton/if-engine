package com.ifengine.content;

import com.ifengine.Direction;
import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.game.GameEngine;
import com.ifengine.game.GameMap;
import java.util.Set;

/**
 * Example demonstrating the builder-style GameMap API.
 * <p>
 * This is the simplest way to create a game - no need to implement GameContent.
 * Just build the map directly and pass it to GameEngine.
 *
 * <h2>Usage</h2>
 * <pre>
 * GameMap map = SimpleGameExample.createMap();
 * GameEngine engine = new GameEngine(map);
 * String response = engine.processCommand("player1", "look");
 * </pre>
 */
public final class SimpleGameExample {

  private SimpleGameExample() {
    // Utility class
  }

  /**
   * Creates a simple 3-location game map using the builder API.
   *
   * @return a configured GameMap ready for use with GameEngine
   */
  public static GameMap createMap() {
    return new GameMap.Builder()
        // Add locations
        .addLocation(new Location(
            "cottage",
            "You are inside a small, cozy cottage. Sunlight streams through a dusty window. "
                + "A wooden door leads north to the outside.",
            "Inside the cottage."))
        .addLocation(new Location(
            "forest",
            "You stand on a narrow forest path. Ancient trees tower above you. "
                + "The path continues east. The cottage lies to the south.",
            "On the forest path."))
        .addLocation(new Location(
            "clearing",
            "You emerge into a sun-dappled clearing. Wildflowers dot the grass. "
                + "The forest path leads west.",
            "In a sunny clearing."))

        // Add items
        .addItem(new Item(
            "lantern",
            "a brass lantern",
            "A brass lantern sits on a wooden shelf.",
            "An old brass lantern, slightly tarnished but still functional.",
            Set.of("lamp", "light")))
        .addItem(new Item(
            "key",
            "a rusty key",
            "A rusty key lies in the grass.",
            "A small iron key, covered in rust.",
            Set.of("rusty key", "iron key")))

        // Connect locations (bidirectional)
        .connect("cottage", Direction.NORTH, "forest")
        .connect("forest", Direction.EAST, "clearing")

        // Place items in locations
        .placeItem("lantern", "cottage")
        .placeItem("key", "clearing")

        // Set where players start
        .setStartingLocation("cottage")

        // Custom intro responses (question is displayed in HTML/frontend)
        .withIntroResponses(
            "Excellent! Your adventure begins...",
            "Take your time. Type 'yes' when you're ready.")

        // Progressive hint system
        .withHints(hints -> hints
            .addPhase("GET_LANTERN",
                "There might be something useful in this cottage...",
                "Check around for a light source. You may need it later.",
                "Take the brass lantern from the shelf.")
            .addPhase("FIND_KEY",
                "The forest beckons. Perhaps there's something to discover.",
                "Explore the clearing to the east. Something glints in the grass.",
                "Go north, then east. Take the rusty key from the clearing.")
            .addPhase("ADVENTURE_CONTINUES",
                "You have everything you need for now.",
                "Explore freely. Who knows what you might find?",
                "The adventure continues... try different commands!")
            .determiner((player, gameMap) -> {
              if (!player.hasItem("lantern")) {
                return "GET_LANTERN";
              }
              if (!player.hasItem("key")) {
                return "FIND_KEY";
              }
              return "ADVENTURE_CONTINUES";
            }))

        .build();
  }

  /**
   * Example of creating and using the game engine.
   */
  public static void main(final String[] args) {
    final GameMap map = createMap();
    final GameEngine engine = new GameEngine(map);

    // Process some example commands
    System.out.println("=== Simple Game Example ===\n");

    final String sessionId = "player1";
    System.out.println("> look");
    System.out.println(extractMessage(engine.processCommand(sessionId, "yes")));
    System.out.println(extractMessage(engine.processCommand(sessionId, "look")));

    System.out.println("> take lantern");
    System.out.println(extractMessage(engine.processCommand(sessionId, "take lantern")));

    System.out.println("> north");
    System.out.println(extractMessage(engine.processCommand(sessionId, "north")));

    System.out.println("> inventory");
    System.out.println(extractMessage(engine.processCommand(sessionId, "inventory")));
  }

  private static String extractMessage(final String json) {
    final int start = json.indexOf("\"message\": \"") + 12;
    if (start < 12) {
      return json;
    }
    final StringBuilder sb = new StringBuilder();
    boolean escaped = false;
    for (int i = start; i < json.length(); i++) {
      final char c = json.charAt(i);
      if (escaped) {
        if (c == 'n') {
          sb.append('\n');
        } else {
          sb.append(c);
        }
        escaped = false;
      } else if (c == '\\') {
        escaped = true;
      } else if (c == '"') {
        break;
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }
}
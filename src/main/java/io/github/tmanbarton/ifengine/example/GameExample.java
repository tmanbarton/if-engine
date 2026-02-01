package io.github.tmanbarton.ifengine.example;

import io.github.tmanbarton.ifengine.Direction;
import io.github.tmanbarton.ifengine.InteractionType;
import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.OpenableSceneryObject;
import io.github.tmanbarton.ifengine.SceneryObject;
import io.github.tmanbarton.ifengine.game.GameEngine;
import io.github.tmanbarton.ifengine.game.GameMap;

import java.util.List;
import java.util.Set;

/**
 * Example demonstrating the builder-style GameMap API.
 * <p>
 * This is the simplest way to create a game - no need to implement GameContent.
 * Just build the map directly and pass it to GameEngine.
 *
 * <h2>Usage</h2>
 * <pre>
 * GameMap map = GameExample.createMap();
 * GameEngine engine = new GameEngine(map);
 * String response = engine.processCommand("player1", "look");
 * </pre>
 */
public final class GameExample {

  private GameExample() {
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
        .addLocation(createCottage())
        .addLocation(new Location(
            "forest",
            "You stand on a narrow forest path. Ancient trees tower above you. "
                + "The path continues east. The cottage lies to the south.",
            "On the forest path."))
        .addLocation(createClearing())

        // Connect locations (bidirectional)
        .connect("cottage", Direction.NORTH, "forest")
        .connect("forest", Direction.EAST, "clearing")

        // Add and place items
        .placeItem(new Item(
            "lantern",
            "a brass lantern",
            "A brass lantern sits on a wooden shelf.",
            "An old brass lantern, slightly tarnished but still functional.",
            Set.of("lamp", "light")), "cottage")
        .placeItem(new Item(
            "key",
            "a rusty key",
            "A rusty key lies in the grass.",
            "A small iron key, covered in rust.",
            Set.of("rusty key", "iron key")), "clearing")

        // A locked chest that requires the key to open
        .placeItem(new Lockbox(
            "chest",
            "a wooden chest",
            "A sturdy wooden chest sits in the corner.",
            "An old chest with iron bands and a rusty lock.",
            Set.of("box", "wooden chest"),
            "key"), "cottage")

        // Set where players start
        .setStartingLocation("cottage")

        // Custom intro responses (question is displayed in HTML/frontend)
        .withIntroResponses(
            "Excellent! Let's begin...",
            "No worries. Let's begin anyway...")

        // Intro message shown before the first location description
        .withIntroMessage("You find yourself at the edge of a mysterious forest. "
            + "A small cottage catches your eye...")

        // Progressive hint system
        .withHints(hints -> hints
            .addPhase("get-lantern",
                "There might be something useful in this cottage...",
                "Check around for a light source. You may need it later.",
                "Take the brass lantern from the shelf.")
            .addPhase("find-key",
                "The forest beckons. Perhaps there's something to discover.",
                "Explore the clearing to the east. Something glints in the grass.",
                "Go north, then east. Take the rusty key from the clearing.")
            .addPhase("open-chest",
                "That key must fit something...",
                "Remember the chest in the cottage? Return and try the key.",
                "Go back to the cottage and type 'open chest'.")
            // Check later-game states first; earliest hint goes in else clause.
            // This ensures new players get the starting hint when they first type "hint".
            .determiner((player, gameMap) -> {
              final var cottage = gameMap.getLocation("cottage");
              final var chest = cottage.getItems().stream()
                  .filter(item -> item.getName().equals("chest"))
                  .findFirst();
              if (player.hasItem("key")) {
                return "open-chest";
              }
              if (player.hasItem("lantern")) {
                return "find-key";
              }
              return "get-lantern";
            }))

        // Custom command: "listen" - hear ambient sounds based on location
        .withCommand("listen", List.of("hear"), (player, cmd, ctx) -> {
          final String locationName = ctx.getCurrentLocation().getName();
          return switch (locationName) {
            case "cottage" -> "You hear the creak of old floorboards settling and "
                + "the gentle whisper of wind through gaps in the window frame.";
            case "forest" -> "Birdsong echoes through the canopy above. "
                + "Leaves rustle softly in the breeze.";
            case "clearing" -> "Bees hum lazily among the wildflowers. "
                + "A distant woodpecker taps rhythmically.";
            default -> "You pause and listen, but hear nothing unusual.";
          };
        })

        // Custom command: "knock on <object>" - demonstrates ParsedCommand usage
        // Supports: "knock on chest", "knock chest", "knock on door", "rap on chest"
        .withCommand("knock", List.of("rap", "tap"), (player, cmd, ctx) -> {
          // Get the target - could be direct object or indirect object after "on"
          String target = cmd.getFirstDirectObject();
          if (target.isEmpty()) {
            target = cmd.getFirstIndirectObject();
          }
          if (target.isEmpty()) {
            return "Knock on what?";
          }

          // Check what the player is trying to knock on
          final String normalizedTarget = target.toLowerCase();
          if (normalizedTarget.contains("chest") || normalizedTarget.contains("box")) {
            // Check if chest is at current location
            final boolean chestHere = ctx.getCurrentLocation().getItems().stream()
                .anyMatch(item -> item.getName().equals("chest"));
            if (!chestHere) {
              return "There's no chest here.";
            }
            return "You rap your knuckles on the wooden chest. A hollow thud suggests there might be something inside.";
          }

          if (normalizedTarget.contains("door")) {
            if (ctx.getCurrentLocation().getName().equals("cottage")) {
              return "You knock on the wooden door. No one answers, but it creaks slightly ajar.";
            }
            return "There's no door here.";
          }

          if (normalizedTarget.contains("tree")) {
            if (!ctx.getCurrentLocation().getName().equals("cottage")) {
              return "You knock on the tree trunk. It feels solid and ancient.";
            }
            return "There are no trees inside the cottage.";
          }

          return "You can't knock on that.";
        })

        // Custom command using scenery custom interactions
        // Works with SceneryObject.withCustomInteraction("smell", response)
        .withCommand("smell", List.of("sniff"), (player, cmd, ctx) -> {
          final String target = cmd.getFirstDirectObject();
          if (target.isEmpty()) {
            return "Smell what?";
          }
          return ctx.getCurrentLocation().findSceneryObject(target)
              .flatMap(s -> s.getCustomResponse("smell"))
              .orElse("You can't smell that.");
        })

        // Custom "put" command demonstrating putItemInContainer with conditional logic.
        // Handles a special case (lantern in stump), otherwise falls through to default.
        .withCommand("put", (player, cmd, ctx) -> {
          final String item = cmd.getFirstDirectObject();
          final String container = cmd.getFirstIndirectObject();

          // Special case: putting the lantern in the stump illuminates it
          if (item.equalsIgnoreCase("lantern") && container.equalsIgnoreCase("stump")) {
            // Use putItemInContainer to handle the mechanics
            final String result = ctx.putItemInContainer(item, container, "in");

            // If it succeeded (lantern is now in the stump), add flavor text
            if (result.contains("put") || result.contains("place")) {
              return "You place the lantern in the hollow stump. Its brass surface catches "
                  + "the light, illuminating a hidden compartment you hadn't noticed before!";
            }
            // Otherwise return the error message (item not found, wrong preposition, etc.)
            return result;
          }

          // For all other put commands, return null to use the default PutHandler
          return null;
        })

        .build();
  }

  /**
   * Creates the cottage location with an openable window scenery object.
   */
  private static Location createCottage() {
    final Location cottage = new Location(
        "cottage",
        "You are inside a small, cozy cottage. Sunlight streams through a dusty window. "
            + "A wooden door leads north to the outside.",
        "Inside the cottage.");

    final OpenableSceneryObject window = new CottageWindow();
    cottage.addSceneryObject(window);

    return cottage;
  }

  /**
   * Creates the clearing location with scenery demonstrating custom interactions
   * and the new scenery container API.
   */
  private static Location createClearing() {
    final Location clearing = new Location(
        "clearing",
        "You emerge into a sun-dappled clearing. Wildflowers dot the grass. "
            + "A weathered stone bench sits beneath an old oak. The forest path leads west.",
        "In a sunny clearing.");

    // Scenery with standard interaction + custom interaction for use with custom commands
    final SceneryObject wildflowers = SceneryObject.builder("wildflowers")
        .withAliases("flowers", "flower")
        .withInteraction(InteractionType.LOOK, "A colorful mix of daisies, buttercups, and violets.")
        .withCustomInteraction("smell", "The sweet fragrance of wildflowers fills your nose.")
        .build();
    clearing.addSceneryObject(wildflowers);

    // Scenery container using the simplified builder API

    // Container example: a hollow tree stump that uses "in" preposition - "put X in stump"
    final SceneryObject stump = SceneryObject.builder("stump")
        .withAliases("tree stump", "hollow stump")
        .withInteraction(InteractionType.LOOK, "A hollow tree stump with a dark cavity inside.")
        .asContainer()
        .withPrepositions("in", "into", "inside")  // Custom prepositions for enclosures
        .withAllowedItems("key", "lantern")  // Items that fit in the stump
        .build();
    clearing.addSceneryObject(stump);

    return clearing;
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
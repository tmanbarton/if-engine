# IF-Engine

A Java library for creating text adventure (interactive fiction) games with a fluent builder API.

## Requirements

- Java 17+

## Quick Start

```java
import com.ifengine.Direction;
import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.game.GameEngine;
import com.ifengine.game.GameMap;

// Build your game world
GameMap map = new GameMap.Builder()
    .addLocation(new Location("cottage", "A cozy cottage.", "In the cottage."))
    .addLocation(new Location("forest", "A dark forest.", "In the forest."))
    .addItem(new Item("key", "a key", "A key lies here.", "A rusty key."))
    .connect("cottage", Direction.NORTH, "forest")
    .placeItem("key", "cottage")
    .setStartingLocation("cottage")
    .build();  // Validates configuration

// Create engine and process commands
GameEngine engine = new GameEngine(map);
String response = engine.processCommand("player1", "look");
```

The engine returns JSON responses:

```json
{
  "message": "A cozy cottage.\n\nA key lies here.",
  "boldableText": "A cozy cottage.",
  "gameState": "PLAYING",
  "validDirections": ["north"]
}
```

## Core Concepts

### GameMap

Use `GameMap.Builder` to construct your game world with fluent method chaining. The `build()` method validates your configuration:

```java
GameMap map = new GameMap.Builder()
    .addLocation(location)       // Add a location
    .addItem(item)               // Register an item
    .placeItem("key", "room")    // Place item in location
    .connect("a", Direction.NORTH, "b")  // Bidirectional connection
    .connectOneWay("a", Direction.DOWN, "b")  // One-way connection
    .setStartingLocation("room") // Required: where players start
    .skipIntro()                 // Optional: skip intro question
    .withIntroResponses(yesResponse, noResponse)  // Custom yes/no responses
    .build();  // Validates and returns GameMap
```

The `build()` method throws `IllegalStateException` if:
- No locations have been added
- Starting location has not been set

### Location

Locations represent places in your game world:

```java
new Location(
    "cottage",                              // name (used as key)
    "You are in a cozy cottage...",         // long description (first visit)
    "In the cottage."                       // short description (subsequent visits)
)
```

Locations automatically track:
- Connections to other locations
- Items present at the location
- Scenery objects (non-takeable)
- Whether the player has visited

### Item

Items are objects players can pick up and interact with:

```java
new Item(
    "key",                    // name (used as key)
    "a rusty key",            // inventory description ("You have: a rusty key")
    "A key lies here.",       // location description (shown when at location)
    "A small iron key.",      // detailed description (shown on examine)
    Set.of("rusty key")       // aliases (optional alternate names)
)
```

Items support aliases for flexible player input - "take rusty key" works if "rusty key" is an alias.

### Direction

The `Direction` enum defines all valid movement directions:

```
NORTH, SOUTH, EAST, WEST
NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST
UP, DOWN, IN, OUT
```

Players can use abbreviations: `n`, `s`, `e`, `w`, `ne`, `nw`, `se`, `sw`, `u`, `d`.

## Intro Configuration

By default, games start with "Have you played this adventure before? (yes/no)".

**Note:** The intro question itself should be displayed in your HTML/frontend layer, not stored in the game map. The engine only handles processing the player's response.

### Skip the intro

```java
GameMap map = new GameMap.Builder()
    .addLocation(...)
    .setStartingLocation("start")
    .skipIntro()
    .build();
```

### Custom yes/no responses

```java
GameMap map = new GameMap.Builder()
    .addLocation(...)
    .setStartingLocation("start")
    .withIntroResponses(
        "Let's begin!",           // shown on "yes"
        "Take your time."         // shown on "no"
    )
    .build();
```

### Custom intro handler

For full control over intro logic:

```java
GameMap map = new GameMap.Builder()
    .addLocation(...)
    .setStartingLocation("start")
    .withIntroHandler((player, response, gameMap) -> {
        if ("easy".equalsIgnoreCase(response)) {
            return IntroResult.playing("Easy mode selected!");
        } else if ("hard".equalsIgnoreCase(response)) {
            return IntroResult.playing("Hard mode selected!");
        }
        return IntroResult.waiting("Please choose easy or hard.");
    })
    .build();
```

## Built-in Commands

The engine handles these commands automatically:

| Category | Commands |
|----------|----------|
| Navigation | `north`/`n`, `south`/`s`, `east`/`e`, `west`/`w`, `up`/`u`, `down`/`d`, `northeast`/`ne`, `northwest`/`nw`, `southeast`/`se`, `southwest`/`sw`, `in`, `out`, `go <direction>` |
| Items | `take <item>`, `drop <item>`, `inventory`/`i`, `put <item> in/on <container>` |
| Interaction | `look`/`l`, `examine <object>`, `read <item>`, `eat <item>`, `drink <item>`, `climb <object>`, `kick <object>`, `punch <object>`, `swim`, `unlock <object>`, `open <object>` |
| System | `quit`, `restart`, `help`, `hint` |

## Customizing Response Text

Implement `ResponseProvider` to customize all game messages:

```java
public class CustomResponses implements ResponseProvider {
    @Override
    public String getTakeSuccess(String itemName) {
        return "You acquire the " + itemName + ".";
    }
    // ... implement other methods
}

GameEngine engine = new GameEngine(map, new CustomResponses());
```

## Session Management

The engine supports multiple concurrent players, each with their own game state:

```java
// Each session ID gets independent game state
engine.processCommand("player1", "north");
engine.processCommand("player2", "take key");

// Clean up when done
engine.cleanupSession("player1");
```

## Advanced Features

### SceneryObject

Non-takeable objects that add atmosphere and can respond to interactions:

```java
SceneryObject painting = SceneryObject.builder("painting")
    .withAliases("portrait", "picture")
    .withInteraction(InteractionType.LOOK, "A faded portrait of someone important.")
    .withInteraction(InteractionType.TAKE, "It's firmly attached to the wall.")
    .build();

location.addSceneryObject(painting);
```

### SceneryContainer

Scenery that can hold items:

```java
SceneryContainer table = new SceneryContainer(
    tableSceneryObject,
    Set.of("book", "key")  // items allowed on this surface
);
location.addSceneryContainer(table);
```

### Openable Interface

For implementing locked doors, containers, and puzzles:

```java
public interface Openable {
    UnlockResult tryUnlock(Player player, String code, GameMapInterface gameMap);
    OpenResult tryOpen(Player player, String code, GameMapInterface gameMap);
}
```

### Hint System

The engine supports progressive 3-level hints that adapt based on game state:

```java
GameMap map = new GameMap.Builder()
    .addLocation(...)
    .addItem(new Item("key", "a key", "A key lies here.", "A brass key."))
    .placeItem("key", "garden")
    .withHints(hints -> hints
        .addPhase("FIND_KEY",
            "Something important might be nearby...",           // Level 1: subtle nudge
            "Check around the old tree. Something brass...",    // Level 2: more direct
            "Take the brass key from the Lightning Tree.")      // Level 3: explicit answer
        .addPhase("UNLOCK_SHED",
            "That key must be for something...",
            "Try using the key on the shed's lock.",
            "Type 'unlock shed' to use your key.")
        .determiner((player, gameMap) -> {
            if (player.hasItem("key")) {
                return "UNLOCK_SHED";
            }
            return "FIND_KEY";
        })
    )
    .build();
```

**How hints work:**
- Each `hint` command returns progressively more direct hints (1 → 2 → 3)
- Level 3 stays as the maximum (repeated `hint` commands keep returning level 3)
- When the player advances to a new phase, hints reset to level 1 for the new phase
- If no hint configuration is set, returns a default "No hints available" message

**Determining hint phase:**

The `determiner` function receives `Player` and `GameMap`, allowing you to check game state directly:

```java
.determiner((player, gameMap) -> {
    // Check inventory
    if (player.hasItem("key")) {
        return "UNLOCK_SHED";
    }
    // Check location
    if (player.getCurrentLocation().getName().equals("garden")) {
        return "EXPLORE_GARDEN";
    }
    // Check if location was visited
    Location cave = gameMap.getLocation("cave");
    if (cave != null && cave.isVisited()) {
        return "AFTER_CAVE";
    }
    return "FIND_KEY";
})
```

## Building

```bash
./gradlew build          # Compile and run tests
./gradlew test           # Run tests only
```

## Example

See `SimpleGameExample.java` for a complete working example:

```java
public static void main(String[] args) {
    GameMap map = SimpleGameExample.createMap();
    GameEngine engine = new GameEngine(map);

    String sessionId = "player1";
    System.out.println(engine.processCommand(sessionId, "yes"));   // Start game
    System.out.println(engine.processCommand(sessionId, "look"));  // Look around
    System.out.println(engine.processCommand(sessionId, "take lantern"));
    System.out.println(engine.processCommand(sessionId, "north"));
    System.out.println(engine.processCommand(sessionId, "inventory"));
}
```

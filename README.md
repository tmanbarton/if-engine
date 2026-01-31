# IF-Engine

A Java library for creating text adventure (interactive fiction) games with a fluent builder API.

## Requirements

- Java 17+

## Quick Start

```java
import io.github.tmanbarton.ifengine.Direction;
import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.game.GameEngine;
import io.github.tmanbarton.ifengine.game.GameMap;

// Build your game world
GameMap map = new GameMap.Builder()
    .addLocation(new Location("cottage", "A cozy cottage.", "In the cottage."))
    .addLocation(new Location("forest", "A dark forest.", "In the forest."))
    .connect("cottage", Direction.NORTH, "forest")
    .placeItem(new Item("key", "a key", "A key lies here.", "A rusty key."), "cottage")
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
    .placeItem(item, "room")     // Add and place item in location
    .placeHiddenItem(item, "room", "revealed desc")  // Hidden until revealed
    .connect("a", Direction.NORTH, "b")  // Bidirectional connection
    .connectOneWay("a", Direction.DOWN, "b")  // One-way connection
    .setStartingLocation("room") // Required: where players start
    .skipIntro()                 // Optional: skip intro question
    .withIntroResponses(yes, no) // Custom yes/no responses
    .withIntroMessage(message)   // Story intro before location
    .withCommand("verb", handler)  // Register custom command
    .withHints(configurer)       // Configure hint system
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

### Custom Item Subclasses

Extend `Item` to add custom properties for your game:

```java
public class TreasureItem extends Item {
  private final int pointValue;
  private final boolean cursed;

  public TreasureItem(String name, String invDesc, String locDesc,
                      String detailDesc, int pointValue, boolean cursed) {
    super(name, invDesc, locDesc, detailDesc);
    this.pointValue = pointValue;
    this.cursed = cursed;
  }

  public int getPointValue() { return pointValue; }
  public boolean isCursed() { return cursed; }
}
```

Use your custom properties in game logic:

```java
// Calculate score from inventory
int score = player.getInventory().stream()
    .filter(item -> item instanceof TreasureItem)
    .mapToInt(item -> ((TreasureItem) item).getPointValue())
    .sum();
```

Common patterns for custom item properties:
- **Point values** - Track score/treasure value
- **Readability/Edibility** - Item-level behavior flags
- **Game state** - Track phases, quests, or progress
- **Custom attributes** - Color, weight, material, etc.

### Direction

The `Direction` enum defines all valid movement directions:

```
NORTH, SOUTH, EAST, WEST
NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST
UP, DOWN, IN, OUT
```

Players can use abbreviations: `n`, `s`, `e`, `w`, `ne`, `nw`, `se`, `sw`, `u`, `d`.

## Intro Configuration

By default, games start in `WAITING_FOR_START_ANSWER` state, expecting a response to an intro question like "Have you played this adventure before?".

**Important:** The engine does **not** display the intro question - your frontend must handle this. Display the question when:
- The user first connects (e.g., on WebSocket connect)
- The page loads in a web app
- The session starts in a CLI application

The engine only processes the player's response and returns the appropriate message. Your frontend is responsible for showing the initial question to the user.

### Skip the intro

```java
GameMap map = new GameMap.Builder()
    .addLocation(...)
    .setStartingLocation("start")
    .skipIntro()
    .build();
```

### Custom yes/no responses

For yes/no questions where both answers start the game (e.g., "Have you played IF before?" or "Ready to begin?"):

```java
GameMap map = new GameMap.Builder()
    .addLocation(...)
    .setStartingLocation("start")
    .withIntroResponses(
        "Great! Let's begin...",     // shown on "yes"
        "No problem! Here we go..."  // shown on "no"
    )
    .build();
```

Both "yes" and "no" answers transition to PLAYING state. The custom response is shown followed by the starting location description.

**Accepted variants:**
- Yes: `yes`, `y`, `yeah`, `yep`, `sure`, `yup`, `yuh`, `yeppers`, `ya`, `heck yeah`, `oh yeah`, `uh hu`, `yes sir`, `yes maam`, `yes ma'am`
- No: `no`, `n`, `nah`, `nope`, `no thanks`, `no way`, `no way jose`, `nah fam`, `heck no`, `no sir`, `no maam`, `no ma'am`

### Custom intro message

Use `withIntroMessage()` to add story context before the first location description:

```java
GameMap map = new GameMap.Builder()
    .addLocation(...)
    .setStartingLocation("cottage")
    .withIntroMessage("You find yourself at the edge of a mysterious forest...")
    .build();
```

When the player answers yes/no, they'll see: intro message → location description.

### Combining intro responses and message

You can use both together for full customization:

```java
GameMap map = new GameMap.Builder()
    .addLocation(...)
    .setStartingLocation("cottage")
    .withIntroResponses(
        "Excellent! Let's begin...",
        "No worries. Let's begin anyway...")
    .withIntroMessage("You find yourself at the edge of a mysterious forest. "
        + "A small cottage catches your eye...")
    .build();
```

Output on "yes":
```
Excellent! Let's begin...

You find yourself at the edge of a mysterious forest. A small cottage catches your eye...

You are in a cozy cottage...
```

### Custom intro handler

For full control over intro logic (including keeping player in intro state on certain answers):

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

## Custom Commands

Register custom commands via `GameMap.Builder.withCommand()`:

### Simple custom command

```java
GameMap map = new GameMap.Builder()
    .addLocation(new Location("room", "A test room.", "Test room."))
    .setStartingLocation("room")
    .withCommand("xyzzy", (player, cmd, ctx) -> "Nothing happens.")
    .build();
```

### Custom command with aliases

```java
.withCommand("search", List.of("find", "look for"), (player, cmd, ctx) -> {
    String target = cmd.getFirstDirectObject();
    if (target.isEmpty()) {
        return "Search what?";
    }
    return "You search the " + target + " but find nothing.";
})
```

### Using CommandContext

The `ctx` parameter provides access to game utilities:

```java
.withCommand("locate", (player, cmd, ctx) -> {
    String target = cmd.getFirstDirectObject();

    // Resolve items from inventory or location
    Optional<Item> item = ctx.resolveItem(target, player);
    if (item.isPresent()) {
        return "Found: " + item.get().getName();
    }

    // Check player inventory
    if (player.hasItem("key")) {
        return "You have the key!";
    }

    // Get current location
    Location loc = ctx.getCurrentLocation();

    // Access response provider for consistent messaging
    ResponseProvider responses = ctx.getResponseProvider();

    return "Not found.";
})
```

### Container Operations in Custom Commands

Use `putItemInContainer` to perform container operations (like the built-in "put" command):

```java
// Create a wall as a container that accepts a ladder
SceneryObject wall = SceneryObject.builder("wall")
    .withInteraction(InteractionType.LOOK, "A tall stone wall.")
    .asContainer()
    .withAllowedItems("ladder")
    .withPrepositions("on", "against")
    .build();

// Create custom commands that behave like "put ladder on wall"
.withCommand("lean", (player, cmd, ctx) -> {
    String item = cmd.getFirstDirectObject();
    String target = cmd.getFirstIndirectObject();
    return ctx.putItemInContainer(item, target, "on");
})

.withCommand("climb", (player, cmd, ctx) -> {
    // "climb ladder" could put the ladder on a wall
    String item = cmd.getFirstDirectObject();
    return ctx.putItemInContainer(item, "wall", "on");
})
```

The `putItemInContainer` method handles all aspects of container insertion:
- Finding the item in inventory or at the current location
- Finding the container (inventory container or scenery container)
- Validating the preposition matches the container's accepted prepositions
- Removing the item from inventory
- Inserting the item into the container
- Tracking containment state

**Checking container state** - Use `isItemInContainer` to check if an item is in a container:

```java
.withCommand("climb", (player, cmd, ctx) -> {
    // Check if ladder is already on the wall
    if (ctx.isItemInContainer("ladder", "wall")) {
        return "You climb the ladder and peek over the wall.";
    }
    return "You need to put the ladder against the wall first.";
})

// Check if item is in any container (no container name)
if (ctx.isItemInContainer("lantern")) {
    return "The lantern is placed somewhere.";
}
```

Both item names and container names support aliases.

### Delegating to built-in commands

Return `null` from your handler to delegate to the built-in command. This allows you to handle specific cases while using default behavior for everything else:

```java
.withCommand("eat", (player, cmd, ctx) -> {
    // Handle special case
    if (cmd.getFirstDirectObject().equals("magic apple")) {
        return "You feel a surge of power!";
    }
    // Delegate to default eat behavior for all other items
    return null;
})
```

### Fully overriding built-in commands

To completely replace a built-in command (never delegate), simply always return a non-null response:

```java
.withCommand("look", (player, cmd, ctx) -> "You see nothing special.")
```

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

#### Custom interactions

Scenery can also respond to custom verbs for use with custom commands:

```java
SceneryObject flower = SceneryObject.builder("flower")
    .withAliases("rose", "red flower")
    .withInteraction(InteractionType.LOOK, "A beautiful red rose.")
    .withCustomInteraction("smell", "It smells lovely!")
    .build();

location.addSceneryObject(flower);
```

Then register a custom command to use it:

```java
.withCommand("smell", (player, cmd, ctx) -> {
    final String target = cmd.getFirstDirectObject();
    return ctx.getCurrentLocation().findSceneryObject(target)
        .flatMap(s -> s.getCustomResponse("smell"))
        .orElse("You can't smell that.");
})
```

### Scenery Containers

Scenery objects can be configured as containers that hold items. The simplest approach uses the SceneryObject builder:

```java
// Surface container using default "on"/"onto" prepositions
SceneryObject table = SceneryObject.builder("table")
    .withInteraction(InteractionType.LOOK, "A wooden table.")
    .asContainer()  // Marks as container - items can be placed here
    .build();

location.addSceneryObject(table);  // Automatically registers as container
```

**Custom prepositions** - For enclosures like drawers, boxes, or hollow spaces:

```java
SceneryObject drawer = SceneryObject.builder("drawer")
    .withInteraction(InteractionType.LOOK, "A wooden drawer.")
    .asContainer()
    .withPrepositions("in", "into")  // Use "put X in drawer" instead of "on"
    .build();
```

**Item restrictions** - Limit what can be placed in the container:

```java
SceneryObject shelf = SceneryObject.builder("shelf")
    .withInteraction(InteractionType.LOOK, "A dusty bookshelf.")
    .asContainer()
    .withAllowedItems("book", "scroll")  // Only these items accepted
    .build();
```

### Hidden Items

Items can be placed at a location but hidden from the player until revealed by game logic. This is useful for items concealed under furniture, behind scenery, or in secret compartments.

**Placing a hidden item during map construction:**

```java
Item key = new Item("key", "a brass key", "A key lies here.", "A small brass key.");

GameMap map = new GameMap.Builder()
    .addLocation(new Location("room", "A dusty room.", "The room."))
    .placeHiddenItem(key, "room", "There's a key under the table.")
    .setStartingLocation("room")
    .build();
```

The third argument is the **revealed location description** — shown in the location after the item is revealed but before the player takes it. Once taken and dropped, the item uses its normal location description (`"A key lies here."`).

**Revealing a hidden item:**

Call `revealHiddenItemByName()` on the location to make the item visible. A typical trigger is examining scenery — for example, overriding `look` to handle "look under table":

```java
.withCommand("look", (player, cmd, ctx) -> {
    final String preposition = cmd.getPreposition();
    final String target = cmd.getFirstIndirectObject();
    final Location currentLocation = ctx.getCurrentLocation();
    if ("bedroom".equals(currentLocation.getName()) && "under".equals(preposition) && "table".equals(target)) {
        if (currentLocation.revealHiddenItemByName("key")) {
            return "You look under the table and find a key!";
        }
        return "You don't find anything else under the table.";
    }
    // Delegate to built-in look for everything else
    return null;
})
```

`revealHiddenItemByName(String)` looks up the hidden item by name (case-insensitive, supports aliases) and moves it to the visible items list. It returns `true` if the item was found and revealed, `false` otherwise.

There is also `revealItem(Item)` which does the same thing but requires an `Item` reference directly. `revealHiddenItemByName` is almost always what you want since command handlers work with string input. `revealItem` exists for cases where you already have the `Item` object (e.g., from iterating `location.getHiddenItems()`).

**Querying hidden items:**

- `isItemHiddenByName(String)` — returns `true` if a hidden item matching the name exists at the location (case-insensitive, supports aliases)
- `isItemHidden(Item)` — checks by `Item` reference
- `getHiddenItemByName(String)` — returns `Optional<Item>` for the matching hidden item, useful when you need the `Item` reference without revealing it
- `getHiddenItems()` — returns a defensive copy of all hidden items at the location

**Hidden item lifecycle:**

1. `placeHiddenItem()` — item is invisible; not shown in `look`, cannot be taken
2. `revealHiddenItemByName()` — item becomes visible with its revealed location description
3. Player takes the item — revealed description is cleared
4. Player drops the item — item uses its normal `locationDescription`
5. On `restart` — item is restored to its original hidden state

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
    .placeItem(new Item("key", "a key", "A key lies here.", "A brass key."), "garden")
    .withHints(hints -> hints
        .addPhase("find-key",
            "Something important might be nearby...",           // Level 1: subtle nudge
            "Check around the old tree. Something brass...",    // Level 2: more direct
            "Take the brass key from the table.")      // Level 3: explicit answer
        .addPhase("unlock-shed",
            "That key must be for something...",
            "Try using the key on the shed's lock.",
            "Type 'unlock shed' to use your key.")
        .determiner((player, gameMap) -> {
            if (player.hasItem("key")) {
                return "unlock-shed";
            }
            return "find-key";
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

The `determiner` function receives `Player` and `GameMap`, allowing you to check game state directly.

**Important:** Check later-game states first and put the earliest hint in the `else` clause. This ensures new players get the starting hint when they first type `hint`:

```java
.determiner((player, gameMap) -> {
    // Check later-game states first
    if (player.hasItem("treasure")) {
        return "GAME_COMPLETE";
    }
    if (player.hasItem("key")) {
        return "unlock-shed";
    }
    // Earliest hint goes in the else
    return "find-key";
})
```

## Building

```bash
./gradlew build          # Compile and run tests
./gradlew test           # Run tests only
```

## Example

See `GameExample.java` for a complete working example:

```java
public static void main(String[] args) {
    GameMap map = GameExample.createMap();
    GameEngine engine = new GameEngine(map);

    String sessionId = "player1";
    System.out.println(engine.processCommand(sessionId, "yes"));   // Start game
    System.out.println(engine.processCommand(sessionId, "look"));  // Look around
    System.out.println(engine.processCommand(sessionId, "take lantern"));
    System.out.println(engine.processCommand(sessionId, "north"));
    System.out.println(engine.processCommand(sessionId, "inventory"));
}
```

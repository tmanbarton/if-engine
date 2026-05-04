# IF-Engine

A Java library for creating parser/text-based interactive fiction games.
(WIP but can be used as-is)

Use the Python-based CLI [chatbot](https://github.com/tmanbarton/if-engineREADME_RAG) I made to answer questions about how to use the library using AI instead of directly referencing the docs.
(You'll need an Anthropic API key)

## Overview

IF-Engine is a reusable game engine for parser-based interactive fiction written in Java. Instead of rebuilding the engine from scratch for every game, you define your game world — locations, items, connections, puzzles, etc. — and the engine handles command parsing, state management, and most game world interaction.

**What it provides:**

- **Command parsing** — Handles all common interactive fiction commands: "take key", "go north", "put gem in chest", and "unlock the door", etc. Handles verb synonyms, abbreviations, and prepositions out of the box. (NPCs "talk", "ask", "tell" work in progress)
- **World building** — A builder-based API for defining locations, connections, items, scenery, containers, locked objects, and hidden objects. Wire up a complete game map without writing engine logic.
- **Built-in commands** — Navigation, inventory management, item interaction (take, drop, examine, read, eat, drink, and more), unlock/open mechanics, and a progressive hint system are all included.
- **Custom commands** — Register your own verbs with full access to game state. Override or extend built-in commands as needed.
- **Session management** — Supports multiple concurrent players, each with independent game state.
- **Customizable responses** — All player-facing text is routed through a `ResponseProvider` interface, so you can restyle every message without touching engine internals.

**What you provide:**

- The game world: locations, items, scenery, and how they connect
- Any custom commands or puzzle logic specific to your game
- A frontend (CLI, web, etc.) that sends player input to the engine and displays its JSON responses

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
    .connect("forest", Direction.SOUTH, "cottage")
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
  "locationDescription": "A cozy cottage.",
  "gameState": "PLAYING",
  "validDirections": ["north"]
}
```
- **"message"** is what the game returns in response to the user's input.
- **"locationDescription"** is for when the location's long description is returned (first visit or "look" command) and you want to do something with it such as bold the directions. "locationDescription" doesn't include the item list - my initial intent here was for bolding the directions - which include "in" and "out" - but avoid bolding anything in the items list e.g. "gold flakes in jar."
- **"gameState"** is the state of the game. Possible values:
  - `PLAYING`: The user has started the game and is playing.
  - `WAITING_FOR_START_ANSWER`: The game hasn't started yet, the user hasn't answered the intro question at the beginning of the game if it exists or hasn't entered a valid answer. e.g. "Have you played interactive fiction before?"
  - `WAITING_FOR_QUIT_CONFIRMATION`: Similar to `WAITING_FOR_START_ANSWER`, this is for when the game is waiting for a yes/no answer for "Are you sure you want to quit?" or a similar yes/no question.
  - `WAITING_FOR_RESTART_CONFIRMATION`: Same as above, but for restarting: "Are you sure you want to restart?"
    - Note on the difference between quitting and restarting: quitting goes back to the intro question, restarting stays in the current game and resets the game state.
  - `WAITING_FOR_UNLOCK_CODE`: This is for when something requires some sort of code to unlock and the user says something like
    - User: "unlock the vault"
    - Game: "Enter the code" (State goes to `WAITING_FOR_UNLOCK_CODE`)
    - User: "1, 2, 3, 4"
    - The vault unlocks and the state goes back to `PLAYING`
  - `WAITING_FOR_OPEN_CODE`: same as above, but for when the user says "open".

## Core Concepts

### GameMap

Use `GameMap.Builder` to construct your game world with method chaining. The `build()` method validates and builds your configuration:

```java
GameMap map = new GameMap.Builder()
        .addLocation(location)       // Add a location
        .placeItem(item, "room")     // Add and place item in location
        .placeHiddenItem(item, "room", "revealed desc")  // Hidden until revealed
        .connect("a", Direction.DOWN, "b")  // One-way connection
        .connectBidirectional("a", Direction.NORTH, "b")  // Bidirectional connection
        .setStartingLocation("room") // Required: where players start
        .skipIntroQuestion()         // Optional: When the frontend doesn't show an intro question, skip intro question handling and go straight to gameplay
        .withIntroResponses(yes, no) // Custom yes/no responses
        .withGameIntro(message)   // Story intro before location
        .withCommand("verb", handler)  // Register custom command
        .withHints(configurer)       // Configure hint system
        .build();  // Validates and returns GameMap
```

The `build()` method throws `IllegalStateException` if:
- No locations have been added
- Starting location has not been set
- Intro question handling hasn't been set. One of these must be configured:
  - Yes/no responses set if using yes/no question: `.withIntroResponses("response on 'yes'", "response on 'no'")`
  - Custom intro handler: `.withIntroHandler((player, response, map) -> {// Custom intro handling`
  - Skip intro question if going straight to the start location without asking an intro question: `.skipIntroQuestion()`

### Location

Locations represent places in your game world:

```java
new Location(
    "cottage",                              // name (used as key)
    "You are in a cozy cottage...",         // long description (first visit and "look" command)
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
    "A small iron key.",      // detailed description (shown on "examine" or "look at" command)
    Set.of("rusty key")       // aliases (optional alternate names)
)
```

Items support aliases for flexible player input - "take rusty key" works if "rusty key" is an alias of the "key" item.

### Custom Item Subclasses

Extend `Item` to add custom properties for your game:

```java
public class TreasureItem extends Item {
  private final int pointValue;
  private final boolean cursed;

  public TreasureItem(String name, String invDesc, String locDesc,
                      String detailDesc, int pointValue, boolean cursed) {
    super(name, invDesc, locDesc, detailDesc);
    this.cursed = cursed;
    if (cursed) {
      pointValue *= -1;
    }
    this.pointValue = pointValue;
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

### Direction

The `Direction` enum defines all valid movement directions:

```
NORTH, SOUTH, EAST, WEST
NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST
UP, DOWN, IN, OUT
```

Players can use abbreviations: `n`, `s`, `e`, `w`, `ne`, `nw`, `se`, `sw`, `u`, `d`.

## Intro Configuration

### Default behavior
Games start in `WAITING_FOR_START_ANSWER` state by default, expecting a response to an intro question like "Have you played this adventure before?".
The engine automatically handles yes/no questions, assuming the initial question will be something like "Have you played before?"

Both "yes" and "no" answers transition to PLAYING state. After, it shows the location description of your first location or a custom intro message if one is set then the location description.

**Accepted variants:**
- Yes: `yes`, `y`, `yeah`, `yep`, `sure`, `yup`, `yuh`, `yeppers`, `yah`, `ya`, `heck yeah`, `oh yeah`, `uh hu`, `yes sir`, `yes maam`, `yes ma'am`
- No: `no`, `n`, `nah`, `nope`, `no thanks`, `no way`, `no way jose`, `nah fam`, `heck no`, `no sir`, `no maam`, `no ma'am`

**Important:** The engine does **not** display the intro question - your front end must handle this. Display the question when:
- The user first connects (e.g., on WebSocket connect)
- The page loads in a web app
- The session starts in a CLI application

The engine only processes the player's response and returns the appropriate message. Your frontend is responsible for showing the initial question to the user.

### Skip the intro

```java
GameMap map = new GameMap.Builder()
    .addLocation(...)
    .setStartingLocation("start")
    .skipIntroQuestion()
    .build();
```

### Set yes/no responses

For yes/no questions where both answers start the game (e.g., "Have you played IF before?" or "Would you like help?"):

```java
GameMap map = new GameMap.Builder()
    .addLocation(...)
    .setStartingLocation("start")
    .withIntroResponses(
        "Great! Let's begin.",     // shown on "yes"
        "Ok, here's more info: (blah blah blah, how IF works). Here we go."  // shown on "no"
    )
    .build();
```

### Fully custom intro handler

If you want answers besides "yes" and "no" for a different intro question and for full control over intro logic, use `withIntroHandler()`. This allows you to set the custom answers with custom responses and also change game state when the user answers the question. For example, you can change the start location based on whether the user chooses "easy" or "hard" mode.

```java
GameMap map = new GameMap.Builder()
        .addLocation(new Location("cave", "A cave.", "Cave"))
        .addLocation(new Location("cottage", "A cozy cottage.", "In a cottage."))
        // create the rest of the map and game.
        .setStartingLocation("cottage")
        .withIntroHandler((player, response, gameMap) -> {
          if ("easy".equalsIgnoreCase(response)) {
            return IntroResult.playing("Easy mode selected!");
          } else if ("hard".equalsIgnoreCase(response)) {
            gameMap.setStartingLocation("cave");
            return IntroResult.playing("Hard mode selected!");
          }
          return IntroResult.waiting("Please choose easy or hard.");
        })
        .build();
```
You can also use this for custom logic on yes/no answers. Use `IntroHandler.isYesAnswer(response)` and `IntroHandler.isNoAnswer(response)` to check if the input is yes/no.

### Game intro message

Use `withGameIntro()` to add story context before the first location description:

```java
GameMap map = new GameMap.Builder()
    .addLocation(...)
    .setStartingLocation("cottage")
    .withGameIntro("On the outskirts of Smalltown, a petite cottage sits along a wall of trees. Smalltown " +
                           "residents try to ignore the screams from the forest, but you with your adventurous ways are intrigued. Chilled, but still intrigued.")
    .build();
```

When the player answers yes/no, they'll see the intro message followed by the description of the starting location.

### Combining intro responses and message

Use both together for a full game introduction:

```java
GameMap map = new GameMap.Builder()
        .addLocation(new Location("cottage", "You're in a cozy cottage along a line of trees.", "In the cottage."))
        .setStartingLocation("cottage")
        .withIntroResponses(
                "Excellent! Let's begin.",
                "Ok, here's more info: (blah blah blah, how IF works). Here we go.")
        .withGameIntro("On the outskirts of Smalltown, a petite cottage sits along a wall of trees. Smalltown " +
                "residents try to ignore the screams from the forest, but you with your adventurous ways are intrigued. Chilled, but still intrigued.")
        .build();
```

Question in the UI: "Have you played before?" 
```
User: Yes

Game: Excellent! Let's begin.
On the outskirts of Smalltown, a petite cottage sits along a wall of trees. Smalltown residents try to ignore the screams from the forest, but adventurous you is intrigued. Chilled, but still intrigued.
You're in a cozy cottage along a line of trees.
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
        .addLocation(new Location("street", "You're on an unnamed street in an unnamed town.", "You're on the street."))
        .setStartingLocation("street")
        .withCommand("xyzzy", (player, cmd, ctx) -> "Nothing happens.")
        .withCommane("beg", (player, cmd, ctx) -> "You get on your knees with cupped hands extended and give your best puppy eyes. " +
                "No one's around for you to look at with those pitiful puppy eyes or put anything in your grubby hands, so you dust yourself off and stand back up.")
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

### Item Containers

Takeable items that can hold other items. When items are put inside an item container, they follow the container between inventory and location.

```java
ItemContainer satchel = ItemContainer.builder("satchel")
    .withInventoryDescription("a leather satchel")
    .withLocationDescription("A leather satchel lies on the floor.")
    .withDetailedDescription("A worn leather satchel with a wide opening.")
    .withAliases(Set.of("bag", "pouch"))
    .withCapacity(3)                        // Max 3 items (0 = unlimited)
    .withAllowedItems(Set.of("key", "gem")) // Only these items (empty = any)
    .withPrepositions(List.of("in", "into")) // Default prepositions
    .build();

location.addItem(satchel);
```

Players interact with item containers using `put <item> in <container>`. Items inside follow the container when it is taken, dropped, or moved between containers.

### Openable Item Containers

For lockable item containers that hold other items — chests, lockboxes, etc. Extend `OpenableItemContainer` to combine `OpenableItem` (unlock/open state) with `Container` (hold items). Items can only be inserted when the container is open.

```java
public class LockableChest extends OpenableItemContainer {

  private final String requiredKeyName;

  public LockableChest(String name, String invDesc, String locDesc,
                       String detailDesc, Set<String> aliases,
                       String requiredKeyName, int capacity) {
    super(name, invDesc, locDesc, detailDesc, aliases,
        true, capacity, Set.of(), List.of("in", "into"));
    this.requiredKeyName = requiredKeyName;
  }

  @Override
  public UnlockResult tryUnlock(Player player, String code, GameMapInterface gameMap) {
    if (isUnlocked()) return new UnlockResult(false, "Already unlocked.");
    if (!player.hasItem(requiredKeyName)) return new UnlockResult(false, "You need a key.");
    setUnlocked(true);
    return new UnlockResult(true, "You unlock the " + getName() + ".");
  }

  @Override
  public OpenResult tryOpen(Player player, String code, GameMapInterface gameMap) {
    if (isOpen()) return new OpenResult(false, "Already open.");
    if (!isUnlocked()) return new OpenResult(false, "It's locked.");
    setOpen(true);
    return new OpenResult(true, "You open the " + getName() + ".");
  }

  // ... matchesUnlockTarget, matchesOpenTarget, getTargetNames
}
```

Usage:

```java
LockableChest chest = new LockableChest(
    "chest", "a wooden chest",
    "A sturdy wooden chest sits in the corner.",
    "An old chest with iron bands and a rusty lock.",
    Set.of("box"), "key", 5);

location.addItem(chest);
```

The player must `unlock chest` then `open chest` before `put gem in chest` works. Attempting to put items in a closed container returns "The chest is closed."
Hidden items can also be in an item container. Once the item container is open, the hidden item is revealed and can be taken.

### Hidden Items

Items can be placed at a location but hidden from the player until revealed by game logic. This is useful for items concealed under furniture, behind scenery, or in secret compartments.

#### Placing a hidden item during map construction

```java
Item key = new Item("key", "a brass key", "A key lies here.", "A small brass key.");

GameMap map = new GameMap.Builder()
    .addLocation(new Location("room", "A dusty room.", "The room."))
    .placeHiddenItem(key, "room", "There's a key under the table.")
    .setStartingLocation("room")
    .build();
```

The third argument is the **revealed location description** — shown in the location after the item is revealed but before the player takes it. Once taken and dropped, the item uses its normal location description (`"A key lies here."`).

#### Revealing a hidden item

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

#### Querying hidden items

- `isItemHiddenByName(String)` — returns `true` if a hidden item matching the name exists at the location (case-insensitive, supports aliases)
- `isItemHidden(Item)` — checks by `Item` reference
- `getHiddenItemByName(String)` — returns `Optional<Item>` for the matching hidden item, useful when you need the `Item` reference without revealing it
- `getHiddenItems()` — returns a defensive copy of all hidden items at the location

#### Hidden item lifecycle

1. `placeHiddenItem()` — item is invisible; not shown in `look`, cannot be taken
2. `revealHiddenItemByName()` — item becomes visible with its revealed location description
3. Player takes the item — revealed description is cleared
4. Player drops the item — item uses its normal `locationDescription`
5. On `restart` — item is restored to its original hidden state

### Openable Interface

For implementing locked doors, containers, and puzzles. The `Openable` interface supports both key-based and code-based locking mechanisms:

```java
public interface Openable {
    UnlockResult tryUnlock(Player player, String code, GameMapInterface gameMap);
    OpenResult tryOpen(Player player, String code, GameMapInterface gameMap);
    boolean matchesUnlockTarget(String name);
    boolean matchesOpenTarget(String name);
    boolean isUnlocked();
    boolean isOpen();
    boolean requiresUnlocking();
    boolean usesCodeBasedUnlocking();
}
```

Three abstract base classes provide `Openable` support for different object types:

| Base class | Extends | Use case |
|------------|---------|----------|
| `OpenableItem` | `Item` | Takeable objects (lockbox, chest) |
| `OpenableItemContainer` | `OpenableItem` + `Container` | Lockable containers that hold items |
| `OpenableLocation` | `Location` | The location itself (locked room, vault) |
| `OpenableSceneryObject` | `SceneryObject` | Non-takeable scenery (wall safe, cabinet) |

Each manages unlock/open state and delegates the abstract methods (`tryUnlock`, `tryOpen`, `matchesUnlockTarget`, `matchesOpenTarget`, `getTargetNames`) to your subclass.

When the player types `unlock` or `open` without specifying an object, the engine checks in priority order:
1. `OpenableItem` in player inventory
2. `OpenableItem` at current location
3. `OpenableSceneryObject` at current location
4. `OpenableLocation` (the location itself)

#### OpenableSceneryObject

For scenery that can be unlocked and opened but cannot be picked up — wall safes, cabinets, gates bolted to the floor:

```java
public class WallSafe extends OpenableSceneryObject {

  public WallSafe() {
    super(
        "safe",                                         // name
        Set.of("wall safe"),                            // aliases
        Map.of(InteractionType.LOOK, "A wall safe."),   // interactions
        Map.of(),                                       // custom interactions
        false,                                          // isContainer
        Set.of(),                                       // allowedItemNames
        List.of(),                                      // prepositions
        true                                            // requiresUnlocking
    );
  }

  @Override
  public Set<String> getTargetNames() {
    return Set.of("safe", "wall safe");
  }

  @Override
  public boolean matchesUnlockTarget(String name) {
    return "safe".equalsIgnoreCase(name) || "wall safe".equalsIgnoreCase(name);
  }

  @Override
  public boolean matchesOpenTarget(String name) {
    return matchesUnlockTarget(name);
  }

  @Override
  public UnlockResult tryUnlock(Player player, String code, GameMapInterface gameMap) {
    if (isUnlocked()) {
      return new UnlockResult(false, "Already unlocked.");
    }
    if (!player.hasItem("safe-key")) {
      return new UnlockResult(false, "You need a key.");
    }
    setUnlocked(true);
    return new UnlockResult(true, "You unlock the safe.");
  }

  @Override
  public OpenResult tryOpen(Player player, String code, GameMapInterface gameMap) {
    if (isOpen()) {
      return new OpenResult(false, "Already open.");
    }
    if (!isUnlocked()) {
      return new OpenResult(false, "It's locked.");
    }
    setOpen(true);
    return new OpenResult(true, "You open the safe and find treasure inside!");
  }
}
```

Add it to a location like any scenery object:

```java
location.addSceneryObject(new WallSafe());
```

The player can then `unlock safe` and `open safe`. If `requiresUnlocking` is `false` (e.g., a cabinet with no lock), the object starts unlocked and the player can `open` it directly.

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

#### How hints work
- Each `hint` command returns progressively more direct hints (1 → 2 → 3)
- Level 3 stays as the maximum (repeated `hint` commands keep returning level 3)
- When the player advances to a new phase, hints reset to level 1 for the new phase
- If no hint configuration is set, returns a default "No hints available" message

#### Determining hint phase

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

## API Reference

### Player
Represents the player in the game.

- `Location getCurrentLocation()`: Returns the location object that the player is currently at.
- `void setCurrentLocation(Location location)`: Sets the players current location to the provided location.
- `List<Item> getInventory()`: Returns the player's inventory.
- `void addItem(Item item)`: Adds the provided item to the player's inventory.
- `boolean removeItem(Item item)`: Attempts to remove the provided item from the player's inventory. Return true if it was removed, false otherwise.
- `Item getInventoryItemByName(String itemName)`: Finds an item in inventory by name or alias (case-insensitive). Returns the first matching item or null if not found.
- `boolean hasItem(String itemName)`: Checks if the player has an item with the given name or alias. Case-insensitive search through inventory. Returns true if an item with a matching name or alias is in the inventory.
- `GameState getGameState()`: Returns the GameState object.
- `void setGameState(GameState gameState)`: Sets the player's game state.
- `void reset(Location startingLocation)`: Resets player to initial state. Clears the inventory, sets the current location to the provided Location (assumed to be the starting location), and sets the state to PLAYING.
- `void markItemAsContained(Item item, Container container)`: Marks the provided item as being contained within the provided container.
- `boolean isItemContained(Item item)`: Checks if the provided item is currently contained within a container. Returns true if so, false if not.
- `Container getContainerForItem(Item item)`: Gets the container that holds the provided item. 
- `void removeContainment(Item item)`: Removes containment tracking for the given item.
- `List<Item> getContainedItems(Container container)`: Gets all items that are contained within the given container.
- `String getFormattedInventoryItems()`: Generates a formatted list of inventory items with the items' inventory descriptions. Shows contained items with " - in [container]" suffix.
- `String getSessionId()`: Gets the session ID for this player. Returns the session ID or null if it hasn't been set.
- `void setSessionId(String sessionId)`: Sets the session ID for this player. (Called at the start of command processing to track which session is active.)

### Location
Represents a location in the game world.

- `void addConnection(Direction direction, Location location)`: Connects this location to another location with the specified direction
- `void replaceConnection(Direction direction, Location location)`: Replaces the current location that's connected to this location in the given direction with the provided location. If no location is connected in that direction, a new connection is added.
- `Location getConnection(Direction direction)`: Returns the `Connection` connected to this Location in the provided direction or null if nothing is connected.
- `Set<Direction> getAvailableDirections()`: Returns all directions that connect to this location
- `void addItem(Item item)`: Adds an item to this location. (Frequently used when dropping an item from the player's inventory to the location.)
- `boolean removeItem(Item item)`: Removes an item from this location. Returns true if successful, false otherwise. (Frequently used when the user picks up an item from the location.)
- `List<Item> getItems()`: Returns all `Items` at this `Location`
- `Item getItemByName(String itemName)`: Finds an item at this location by name (case-insensitive).
- `boolean hasItem(String itemName)`: Checks if this location has an item with the given name.
- `void addSceneryObject(SceneryObject sceneryObject)`: Adds the given scenery object to this location. If the scenery object is configured as a container (via `asContainer()` on its builder), a `SceneryContainer` is automatically created and registered.
- `public void removeSceneryObject(SceneryObject sceneryObject)`: Removes a scenery object from this location.
- `List<SceneryObject> getSceneryObjects()`: Returns all SceneryObjects at this location.
- `List<SceneryContainer> getSceneryContainers()`: Gets all scenery containers at this location.
- `Optional<SceneryObject> findSceneryObject(String objectName)`: Finds and returns an Optional SceneryObject by name or an empty Optional if it can't be found. 
- `List<SceneryObject> findSceneryObjectsByInteraction(InteractionType interactionType)`: Finds scenery objects that support a given interaction type.
- `String getLongDescription()`: Returns this location's long description
- `String getShortDescription()`: Returns this location's short description
- `String getName()`: Returns this location's name
- `boolean isVisited()`: Returns whether this location has been visited or not
- `void setVisited(boolean visited)`: sets the `visited` field to track if the player has visited this location or not.
- `void setItemContainer(Item item, SceneryContainer container)`: Records that an item is contained in a scenery container.
- `setLongDescription(String longDescription)`: Sets the long description of the location
- `setShortDescription(String shortDescription)`: Sets the short description of the location
- `boolean isItemInContainer(Item item)`: Checks if an item is in a scenery container.
- `SceneryContainer getContainerForItem(Item item)`: Gets the scenery container that contains an item, or null if not contained.
- `void removeItemFromContainer(Item item)`: Removes item from container tracking (when item is taken).
- `addHiddenItem(Item item, String revealedLocationDescription)`: Adds a hidden item to this location. Hidden items are not visible to the player until revealed. The revealed location description is what the player sees when they look at it after the item is revealed until they player takes it.
- `boolean revealHiddenItem(Item item)`: Reveals a hidden item, making it visible and takeable. The item now shows the revealed location description when the player looks at it until the item is taken.
- `boolean revealHiddenItemByName(String itemName)`: Reveals a hidden item by name (case-insensitive, supports aliases).
- `boolean isItemHidden(Item item)`: Checks if an item is hidden at this location.
- `boolean isItemHiddenByName(String itemName)`: Checks if an item is hidden at this location by name (case-insensitive, supports aliases).
- `Optional<Item> getHiddenItemByName(String itemName)`: Finds a hidden item by name (case-insensitive, supports aliases).
- `Set<Item> getHiddenItems()`: Gets all hidden items at this location.
- `void cliearHiddenItems()`: Removes all hidden items and their revealed descriptions. Used during game reset to restore initial state.
- `String getRevealedLocationDescription(Item item)`: Gets the revealed location description for an item, if one exists. This description is used instead of the item's default location description when the item was revealed from a hidden state and has not yet been taken.

### SceneryObject
Represents a scenery object in the game world that players can interact with but cannot take/isn't part of the game and can't affect the world. Scenery objects have names, aliases, and specific responses to different interaction types.

Supports both standard `InteractionType` responses and custom string-based interactions for use with custom commands registered via `GameMap.Builder.withCommand()`.
- `String getName()`: Scenery object name
- `Set<String> getAliases()`: Scenery object aliases
- `Map<InteractionType, String> getResponses()`: Returns all defined responses to interaction types for this scenery object
- `Map<String, String> getCustomResponses()`: Returns all defined responses to custom verbs
- `boolean isContainer()`: Flag for if this scenery object is a container or not
- `Set<String> getAllowedItemNames()`: Gets the allowed item names for this container. Empty set means any item is allowed.
- `List<String> getPrepositions()`: Gets the valid prepositions for this container (i.e. are things put **in** the container, **on** it, etc.). Returns empty list for non-containers.
- `boolean matches(String objectName)`: Checks if this scenery object matches the given object name or any of its aliases. The comparison is case-insensitive and handles null input safely.
- `Optional<String> getResponse(InteractionType interaction)`: Gets the response for a specific interaction type.
- `Optional<String> getCustomResponse(String verb)`: Gets the response for a custom interaction verb. Custom interactions are defined via `withCustomInteraction()` and can be used with custom commands registered via `GameMap.Builder.withCommand()`.
- `static Builder builder(String name)`: Creates a new builder for constructing SceneryObject instances.

### SceneryObject.Builder
- `void addAlias(String alias)`: Adds an alias for this scenery object.
- `Builder withAliases(String... aliasArray)`: Adds one or multiple aliases for this scenery object.
- `Builder withInteraction(InteractionType interaction, String response)`: Adds an interaction response for this scenery object. This is a hard-coded response for when the user tries to interact with the scenery object.
- `Builder withCustomInteraction(String verb, String response)`: Adds a custom interaction response for this scenery object. Custom interactions allow scenery to respond to verbs beyond the standard `InteractionType` enum. Use with custom commands registered via code `GameMap.Builder.withCommand()`. Example:
  ``` java
  SceneryObject.builder("flower")
    .withCustomInteraction("smell", "It smells lovely!")
    .build();
  ```
- `Builder asContainer()`: Marks this scenery object as a container that can hold items. Containers can have items placed on/in them using the "put" command. By default, containers use "on" and "onto" prepositions. Other prepositions like "under" can be added with `withPrepositions(String... preps)`
- `Builder withAllowedItems(String... itemNames)`: Sets which items can be placed in this container by name (alias not included). If not called or called with no arguments, any item can be placed.
- `Builder withPrepositions(String... preps)`: Sets the valid prepositions for this container. Default prepositions: "on" and "in".
- `SceneryOject build()`: Builds the SceneryObject instance with the configured properties and runs some validations. If no interactions are defined, throw `IllegalStateException`

### ParsedCommand
Represents a parsed command from user input with normalized components.
- `String getVerb()`: Return the normalized verb (e.g., "take", "look", "go").
- `List<String> getDirectObjects()`: Return the direct objects (main targets of the command).
- `String getFirstDirectObject()`: Return the first direct object, or empty string if none.
- `List<String> getIndirectObjects()`: Return the indirect objects (objects after prepositions).
- `String getFirstIndirectObject()`: Return the first indirect object, or empty string if none.
- `String getPreposition()`: Return the preposition connecting direct and indirect objects, or null if none.
- `CommandType getType()`: Return the type of command structure.
- `boolean isImpliedObject()`: Return true if the object was inferred from context rather than explicitly stated.
- `String getOriginalInput()`: Return the original user input before processing.
- `boolean hasDirectObjects()`: Return true if this command has any direct objects.
- `boolean hasIndirectObjects()`: Return true if this command has any indirect objects.
- `boolean hasPreposition()`: Return true if this command has a preposition.
- `List<String> getSequenceCommands()`: Return the list of additional commands in a sequence (for SEQUENCE type commands).
- `boolean hasSequenceCommands()`: Return true if this is a sequence command with multiple parts.

### CommandContext
Context provided to custom command handlers. Provides access to game utilities that custom handlers may need, including:
- ResponseProvider for consistent game messaging
- ObjectResolver for finding items by name
- Current location information
- Convenience methods for common operations
---
Methods
- `ResponseProvider getResponseProvider()`: Returns the response provider for consistent game messaging.
- `ObjectResolver getObjectResolver()`: Returns the object resolver for finding items by name.
- `Location getCurrentLocation()`: Returns the player's current location.
- `Optional<Item> resolveItem(String name, Player player)`: Resolves an item by name from the player's inventory or current location. This is a convenience method that handles the common case of looking up an item that may be in inventory or at the current location.
- `String putItemInContainer(String itemName, String containerName, String preposition)`: Puts an item into a container, returning a response message. Handles all aspects of container insertion:
  - Finding the item in inventory or at the current location
  - Finding the container (inventory container or scenery container)
  - Validating the preposition matches the container's accepted prepositions
  - Checking if the container accepts the item
  - Removing the item from inventory if needed
  - Inserting the item into the container
  - Tracking containment state
  - Example usage in a custom command handler:

```java
.withCommand("lean", (player, cmd, ctx) -> {
    return ctx.putItemInContainer("ladder", "wall", "on");
})
```
- `boolean isItemInContainer(String itemName)`: Checks if an item is in any container at the current location.

### ItemContainer
A takeable item that can also contain other items. Items inserted into an inventory container follow the container between inventory and location (ContainerType.INVENTORY behavior).

- `static Builder builder(@Nonnull final String name)`: Creates a new builder for an ItemContainer.
- `boolean canAccept(@Nonnull final Item item)`: Checks whether the given item is allowed in this item container. 
- `boolean insertItem(@Nonnull final Item item)`: Inserts the given item into the item container. Returns true if inserted, false if not or if the container doesn't allow the given item.
- `boolean removeItem(@Nonnull final Item item)`: Removes the given item from this item container. Returns true if successfully removed, false otherwise.
- `boolean containsItem(@Nonnull final String itemName)`: Checks if this item container contains the given item.
- `Set<String> getInsertedItemNames()`: Returns all items in this container.
- `int getCapacity()`: Returns the capacity of this item container.
- `int getCurrentCount()`: Returns the number of items in this item container.
- `boolean isFull()`: Checks for if this container is full.
- `String getContainerStateDescription()`: Returns a string describing the state of this item container - if it's empty or what items it contains.
- `List<String> getPreferredPrepositions()`: Returns a list of the preferred prepositions.
- `ContainerType getContainerType()`: ContainerType.ITEM.

### ItemContainer.Builder
Builder for creating `ItemContainer` instances.
(See `Item` and `ItemContainer` API docs for method details.)

- `Builder withInventoryDescription(String inventoryDescription)`
- `Builder withLocationDescription(String locationDescription)`
- `Builder withDetailedDescription(String detailedDescription)`
- `Builder withAliases(Set<String> aliases)`
- `Builder withCapacity(int capacity)`
- `Builder withAllowedItems(Set<String> allowedItemNames)`
- `Builder withPrepositions(List<String> prepositions)`
- `ItemContainer build()`: Builds the ItemContainer, using auto-generated descriptions for any not explicitly set.

### SceneryContainer
A scenery container implemented as a scenery object adapter. When items are inserted into a scenery container, they are placed at the location (not kept in inventory). Examples: table, desk, shelf, counter Scenery containers have unlimited capacity by default (getCapacity() returns 0).
Creates a scenery container that wraps a scenery object.

- `SceneryObject getSceneryObject()`: Gets the wrapped scenery object.
- `ContainerType getContainerType()`: ContainerType.SCENERY
- `boolean canAccept(Item item)`: Checks if this container accepts the given item.
- `boolean insertItem(Item item)`: Inserts an item into this scenery container. Returns true if successfully inserted, false if the item isn't allowed, the container is full, or otherwise didn't insert.
- `boolean removeItem(Item item)`: Removes the given item from this container. Returns true if successfully removed, false otherwise.
- `boolean containsItem(String itemName)`: Checks if this scenery container contains the given item.
- `Set<String> getInsertedItemNames()`: Returns all items currently in this scenery container.
- `int getCapacity()`: Gets the maximum capacity of this container. Returns 0, representing unlimited capacity.
- `int getCurrentCount()`: Get the current number of items in this scenery container.
- `boolean isFull()`: Checks if this container is full. Since scenery containers have unlimited capacity, this method always returns false.
- `String getContainerStateDescription()`: Returns a string describing the state of this scenery container - if it's empty or what items it contains.
- `List<String> getPreferredPrepositions()`: Returns a list of the preferred prepositions.

### OpenableItem
Abstract base class for items that can be unlocked and opened. Implements the `Openable` interface for unified unlock/open handling. Extends `Item to inherit all item properties.

Openable Interface - State Management
- `boolean isUnlocked()`: Checks whether the openable object is unlocked or not.
- `void setUnlocked(boolean unlocked)`: Sets the unlocked flag on this openable object.
- `boolean isOpen()`: Checks whether the openable object is open or not.
- `void setOpen(boolean open)`: Sets the open flag on this openable object.
Openable Interface - Configuration
- `boolean requiresUnlocking()`: Checks whether this openable object requires unlocking.
Abstract Methods for Subclass Implementation
- `abstract Set<String> getTargetNames()`: Get all defined taget names for this openable object for command inference. This is the name and any defined aliases.
- `abstract boolean matchesUnlockTarget(String name)`: Check for if the given String matches the object to unlock.
- `abstract boolean matchesOpenTarget(String name)`: Check for if the given String matches the object to open.
- `abstract UnlockResult tryUnlock(Player player, String providedAnswer, GameMapInterface gameMap)`: Implementation for the action that happens when the user says to unlock the object.
- `abstract OpenResult tryOpen(Player player, String providedAnswer, GameMapInterface gameMap)`: Implementation for the action that happens when the user says to open the object.

### OpenableItemContainer
Abstract base class for openable items that can also contain other items. Combines `OpenableItem` (unlock/open state) with `Container` (hold items). Items can only be inserted when the container is open. Subclasses must implement the five abstract `Openable` methods (`getTargetNames`, `matchesUnlockTarget`, `matchesOpenTarget`, `tryUnlock`, `tryOpen`).

Inherited from OpenableItem (see `OpenableItem` API docs for details):
- State management: `isUnlocked()`, `setUnlocked(boolean)`, `isOpen()`, `setOpen(boolean)`
- Configuration: `requiresUnlocking()`

Container Methods:
- `boolean canAccept(Item item)`: Checks whether the given item can be inserted. Returns false if the container is not open, is full, or the item is not in the allowed items list (when restrictions are set). Returns true if the container is open, not full, and the item is allowed (or no restrictions are set).
- `boolean insertItem(Item item)`: Inserts the given item into this container. Returns true if inserted, false if `canAccept` fails.
- `boolean removeItem(Item item)`: Removes the given item from this container. Returns true if successfully removed, false otherwise.
- `boolean containsItem(String itemName)`: Checks if this container contains an item with the given name (case-insensitive).
- `Set<String> getInsertedItemNames()`: Returns all item names currently in this container.
- `int getCapacity()`: Returns the maximum capacity of this container. 0 means unlimited.
- `int getCurrentCount()`: Returns the number of items currently in this container.
- `boolean isFull()`: Checks if this container is full.
- `String getContainerStateDescription()`: Returns a formatted string describing the container's contents — "The [name] is empty." or "The [name] contains: item1, item2".
- `List<String> getPreferredPrepositions()`: Returns the list of preferred prepositions for put commands.
- `ContainerType getContainerType()`: Returns `ContainerType.INVENTORY`.

### OpenableLocation
Abstract base class for locations that can be unlocked and opened. Implements the `Openable` interface for unified unlock/open handling. Extends `Location` to inherit all location properties. Provides key-based unlocking by default with auto-unlock behavior (if a player has the key and tries to `open`, it unlocks and opens in one action).

Descriptions change based on state — the `getLongDescription()` and `getShortDescription()` methods return different text depending on whether the location is locked, unlocked, or open.

Openable Interface - State Management
- `boolean isUnlocked()`: Checks whether the location is unlocked.
- `void setUnlocked(boolean unlocked)`: Sets the unlocked state.
- `boolean isOpen()`: Checks whether the location is open.
- `void setOpen(boolean open)`: Sets the open state.

Openable Interface - Configuration
- `boolean requiresUnlocking()`: Returns true by default. Subclasses can override if needed.

Openable Interface - Default Implementations
- `UnlockResult tryUnlock(Player player, String providedAnswer, GameMapInterface gameMap)`: Logic for when the user tries to unlock the object. Defaults to simple key-based unlocking. (Checks if the player has the required key (from `getRequiredKeyName()`). If `providedAnswer` is provided, validates it refers to the required key. On success, sets unlocked to true and calls `onUnlock(gameMap)`).
- `OpenResult tryOpen(Player player, String providedAnswer, GameMapInterface gameMap)`: Logic for when the user tries to open the object. Handles opening with auto-unlock. Defaults to simple key-based opening. (If locked and player has the key, automatically unlocks and opens (calls `onUnlockAndOpen(gameMap)`). If already unlocked, just opens (calls `onOpen(gameMap)`). If locked without key, returns failure.)

Template Methods (override `Location` behavior)
- `String getLongDescription()`: Returns `getOpenLongDescription()` if open, `getUnlockedLongDescription()` if unlocked, or the base long description if locked.
- `String getShortDescription()`: Returns `getOpenShortDescription()` if open, `getUnlockedShortDescription()` if unlocked, or the base short description if locked.

Abstract Methods for Subclass Implementation
- `abstract Set<String> getTargetNames()`: Get all target names for command inference (name and aliases).
- `abstract boolean matchesUnlockTarget(String name)`: Check if the given string matches the object to unlock.
- `abstract boolean matchesOpenTarget(String name)`: Check if the given string matches the object to open.
- `abstract String getRequiredKeyName()`: Returns the name of the key item required to unlock this location if using key-based locking.
- `abstract String onUnlock(GameMapInterface gameMap)`: Called on successful unlock. Returns the message to display.
- `abstract String onOpen(GameMapInterface gameMap)`: Called on successful open (when already unlocked). Returns the message to display.
- `abstract String onUnlockAndOpen(GameMapInterface gameMap)`: Called when unlocked and opened in one action (player has key and tries to open). Returns the message to display.
- `abstract String getUnlockedLongDescription()`: Long description when unlocked but not open.
- `abstract String getUnlockedShortDescription()`: Short description when unlocked but not open.
- `abstract String getOpenLongDescription()`: Long description when open.
- `abstract String getOpenShortDescription()`: Short description when open.
- `abstract String getAlreadyUnlockedMessage()`: Message when player tries to unlock but it's already unlocked.
- `abstract String getUnlockNoKeyMessage()`: Message when player tries to unlock without the key.
- `abstract String getAlreadyOpenMessage()`: Message when player tries to open but it's already open.
- `abstract String getOpenLockedNoKeyMessage()`: Message when player tries to open but it's locked and they don't have the key.

### OpenableSceneryObject
Abstract base class for scenery objects at a location that can be opened and optionally unlocked but cannot be picked up. Implements the `Openable` interface for unified unlock/open handling. Extends `SceneryObject` to inherit all scenery properties. Use for wall safes, cabinets, gates, or any non-takeable object that con be opened.

If `requiresUnlocking` is false, the object starts unlocked and the player can `open` it directly.

Openable Interface - State Management
- `boolean isUnlocked()`: Checks whether the object is unlocked.
- `void setUnlocked(boolean unlocked)`: Sets the unlocked state.
- `boolean isOpen()`: Checks whether the object is open.
- `void setOpen(boolean open)`: Sets the open state.

Openable Interface - Configuration
- `boolean requiresUnlocking()`: Returns whether this object requires unlocking before it can be opened.

Abstract Methods for Subclass Implementation
- `abstract Set<String> getTargetNames()`: Get all target names for command inference (name and aliases).
- `abstract boolean matchesUnlockTarget(String name)`: Check if the given string matches the object to unlock.
- `abstract boolean matchesOpenTarget(String name)`: Check if the given string matches the object to open.
- `abstract UnlockResult tryUnlock(Player player, String providedAnswer, GameMapInterface gameMap)`: Implementation for the unlock action.
- `abstract OpenResult tryOpen(Player player, String providedAnswer, GameMapInterface gameMap)`: Implementation for the open action.

### HintConfigurationBuilder
Builder for creating hint configurations. Used via `GameMap.Builder.withHints()` to define progressive hint phases and a determiner that selects the current phase based on game state.

- `HintConfigurationBuilder addPhase(String phaseKey, String hint1, String hint2, String hint3)`: Adds a hint phase with three progressive hints — a subtle nudge (level 1), a more direct hint (level 2), and an explicit answer (level 3). The `phaseKey` is a unique identifier used by the determiner to select this phase.
- `HintConfigurationBuilder determiner(HintPhaseDeterminer determiner)`: Sets the phase determiner that identifies the current puzzle phase. The determiner receives `Player` and `GameMap` and returns the phase key string for the current hint phase.
- `HintConfiguration build()`: Builds the hint configuration. Throws `IllegalStateException` if no determiner was set.

### GameMapInterface
Interface for game maps that provide locations and items for the game world. Passed to `tryUnlock` and `tryOpen` methods so openable objects can access game state during unlock/open logic.

- `Location getLocation(String locationKey)`: Gets a location by its unique key/name. Returns the location or null if not found.
- `Item getItem(String itemKey)`: Gets an item by its unique key/name. Returns the item or null if not found.
- `Collection<Location> getAllLocations()`: Gets all locations in this game map.
- `Collection<Item> getAllItems()`: Gets all items in this game map.
- `Location getStartingLocation()`: Gets the starting location for new players.
- `void resetMap()`: Resets the game map to its initial state. Restores item locations, clears visited flags, and restores any other initial state.

### ContainerType
Enum representing the type of container and its behavior.

- `INVENTORY`: Items inserted into this container stay in the player's inventory and follow the container when taken or dropped.
- `SCENERY`: Items inserted into this container are placed at the current location (not kept in inventory).

### Direction
Enum defining all valid movement directions. Used with `GameMap.Builder.connect()` and `GameMap.Builder.connectBidirectional()` to wire up location connections.

Values: `NORTH`, `SOUTH`, `EAST`, `WEST`, `NORTHEAST`, `NORTHWEST`, `SOUTHEAST`, `SOUTHWEST`, `UP`, `DOWN`, `IN`, `OUT`

Players can use abbreviations: `n`, `s`, `e`, `w`, `ne`, `nw`, `se`, `sw`, `u`, `d`.

### InteractionType
Enum representing the types of interactions that can be performed on scenery objects. Used with `SceneryObject.Builder.withInteraction()` to define how scenery responds to built-in commands.

Values: `CLIMB`, `DRINK`, `EAT`, `KICK`, `LOOK`, `PUNCH`, `READ`, `SWIM`, `TAKE`

For interactions beyond these built-in types, use `SceneryObject.Builder.withCustomInteraction(String verb, String response)` with a custom command.


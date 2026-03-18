# Core Concepts

## GameMap

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

## Location

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

## Item

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

## Custom Item Subclasses

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

## Direction

The `Direction` enum defines all valid movement directions:

```
NORTH, SOUTH, EAST, WEST
NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST
UP, DOWN, IN, OUT
```

Players can use abbreviations: `n`, `s`, `e`, `w`, `ne`, `nw`, `se`, `sw`, `u`, `d`.
# Hidden Items

Items can be placed at a location but hidden from the player until revealed by game logic. This is useful for items concealed under furniture, behind scenery, or in secret compartments.

## Placing a hidden item during map construction

```java
Item key = new Item("key", "a brass key", "A key lies here.", "A small brass key.");

GameMap map = new GameMap.Builder()
    .addLocation(new Location("room", "A dusty room.", "The room."))
    .placeHiddenItem(key, "room", "There's a key under the table.")
    .setStartingLocation("room")
    .build();
```

The third argument is the **revealed location description** — shown in the location after the item is revealed but before the player takes it. Once taken and dropped, the item uses its normal location description (`"A key lies here."`).

## Revealing a hidden item

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

## Querying hidden items

- `isItemHiddenByName(String)` — returns `true` if a hidden item matching the name exists at the location (case-insensitive, supports aliases)
- `isItemHidden(Item)` — checks by `Item` reference
- `getHiddenItemByName(String)` — returns `Optional<Item>` for the matching hidden item, useful when you need the `Item` reference without revealing it
- `getHiddenItems()` — returns a defensive copy of all hidden items at the location

## Hidden item lifecycle

1. `placeHiddenItem()` — item is invisible; not shown in `look`, cannot be taken
2. `revealHiddenItemByName()` — item becomes visible with its revealed location description
3. Player takes the item — revealed description is cleared
4. Player drops the item — item uses its normal `locationDescription`
5. On `restart` — item is restored to its original hidden state
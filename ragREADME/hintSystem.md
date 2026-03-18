# Hint System

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

## How hints work
- Each `hint` command returns progressively more direct hints (1 → 2 → 3)
- Level 3 stays as the maximum (repeated `hint` commands keep returning level 3)
- When the player advances to a new phase, hints reset to level 1 for the new phase
- If no hint configuration is set, returns a default "No hints available" message

## Determining hint phase

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
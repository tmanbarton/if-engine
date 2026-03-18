# Openable Interface

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

Each manages unlock/open state and delegates the abstract methods (`tryUnlock`, `tryOpen`, `matchesUnlockTarget`, `matchesOpenTarget`, `getInferredTargetNames`) to your subclass.

When the player types `unlock` or `open` without specifying an object, the engine checks in priority order:
1. `OpenableItem` in player inventory
2. `OpenableItem` at current location
3. `OpenableSceneryObject` at current location
4. `OpenableLocation` (the location itself)

## OpenableSceneryObject

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
  public Set<String> getInferredTargetNames() {
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
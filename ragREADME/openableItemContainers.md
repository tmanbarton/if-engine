# Openable Item Containers

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

  // ... matchesUnlockTarget, matchesOpenTarget, getInferredTargetNames
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
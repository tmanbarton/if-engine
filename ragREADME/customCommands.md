# Custom Commands

Register custom commands via `GameMap.Builder.withCommand()`:

## Simple custom command

```java
GameMap map = new GameMap.Builder()
    .addLocation(new Location("room", "A test room.", "Test room."))
    .setStartingLocation("room")
    .withCommand("xyzzy", (player, cmd, ctx) -> "Nothing happens.")
    .build();
```

## Custom command with aliases

```java
.withCommand("search", List.of("find", "look for"), (player, cmd, ctx) -> {
    String target = cmd.getFirstDirectObject();
    if (target.isEmpty()) {
        return "Search what?";
    }
    return "You search the " + target + " but find nothing.";
})
```

## Using CommandContext

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

## Container Operations in Custom Commands

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

## Delegating to built-in commands

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

## Fully overriding built-in commands

To completely replace a built-in command (never delegate), simply always return a non-null response:

```java
.withCommand("look", (player, cmd, ctx) -> "You see nothing special.")
```

# Customizing Response Text

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
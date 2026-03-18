# Quick Start

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
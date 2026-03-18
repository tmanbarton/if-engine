# Session Management

The engine supports multiple concurrent players, each with their own game state:

```java
// Each session ID gets independent game state
engine.processCommand("player1", "north");
engine.processCommand("player2", "take key");

// Clean up when done
engine.cleanupSession("player1");
```
# Intro Configuration

By default, games start in `WAITING_FOR_START_ANSWER` state, expecting a response to an intro question like "Have you played this adventure before?".

**Important:** The engine does **not** display the intro question - your frontend must handle this. Display the question when:
- The user first connects (e.g., on WebSocket connect)
- The page loads in a web app
- The session starts in a CLI application

The engine only processes the player's response and returns the appropriate message. Your frontend is responsible for showing the initial question to the user.

## Skip the intro

```java
GameMap map = new GameMap.Builder()
    .addLocation(...)
    .setStartingLocation("start")
    .skipIntro()
    .build();
```

## Custom yes/no responses

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
- Yes: `yes`, `y`, `yeah`, `yep`, `sure`, `yup`, `yuh`, `yeppers`, `yah`, `ya`, `heck yeah`, `oh yeah`, `uh hu`, `yes sir`, `yes maam`, `yes ma'am`
- No: `no`, `n`, `nah`, `nope`, `no thanks`, `no way`, `no way jose`, `nah fam`, `heck no`, `no sir`, `no maam`, `no ma'am`

## Custom intro message

Use `withIntroMessage()` to add story context before the first location description:

```java
GameMap map = new GameMap.Builder()
    .addLocation(...)
    .setStartingLocation("cottage")
    .withIntroMessage("You find yourself at the edge of a mysterious forest...")
    .build();
```

When the player answers yes/no, they'll see: intro message → location description.

## Combining intro responses and message

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

## Custom intro handler

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
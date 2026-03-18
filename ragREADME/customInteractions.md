# Custom interactions

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
# SceneryObject

Non-takeable objects that add atmosphere and can respond to interactions:

```java
SceneryObject painting = SceneryObject.builder("painting")
    .withAliases("portrait", "picture")
    .withInteraction(InteractionType.LOOK, "A faded portrait of someone important.")
    .withInteraction(InteractionType.TAKE, "It's firmly attached to the wall.")
    .build();

location.addSceneryObject(painting);
```
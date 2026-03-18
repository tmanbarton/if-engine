# Scenery Containers

Scenery objects can be configured as containers that hold items. The simplest approach uses the SceneryObject builder:

```java
// Surface container using default "on"/"onto" prepositions
SceneryObject table = SceneryObject.builder("table")
    .withInteraction(InteractionType.LOOK, "A wooden table.")
    .asContainer()  // Marks as container - items can be placed here
    .build();

location.addSceneryObject(table);  // Automatically registers as container
```

## Custom prepositions
For enclosures like drawers, boxes, or hollow spaces:

```java
SceneryObject drawer = SceneryObject.builder("drawer")
    .withInteraction(InteractionType.LOOK, "A wooden drawer.")
    .asContainer()
    .withPrepositions("in", "into")  // Use "put X in drawer" instead of "on"
    .build();
```

## Item restrictions
Limit what can be placed in the container:

```java
SceneryObject shelf = SceneryObject.builder("shelf")
    .withInteraction(InteractionType.LOOK, "A dusty bookshelf.")
    .asContainer()
    .withAllowedItems("book", "scroll")  // Only these items accepted
    .build();
```
# Item Containers

Takeable items that can hold other items. When items are put inside an item container, they follow the container between inventory and location.

```java
ItemContainer satchel = ItemContainer.builder("satchel")
    .withInventoryDescription("a leather satchel")
    .withLocationDescription("A leather satchel lies on the floor.")
    .withDetailedDescription("A worn leather satchel with a wide opening.")
    .withAliases(Set.of("bag", "pouch"))
    .withCapacity(3)                        // Max 3 items (0 = unlimited)
    .withAllowedItems(Set.of("key", "gem")) // Only these items (empty = any)
    .withPrepositions(List.of("in", "into")) // Default prepositions
    .build();

location.addItem(satchel);
```

Players interact with item containers using `put <item> in <container>`. Items inside follow the container when it is taken, dropped, or moved between containers.
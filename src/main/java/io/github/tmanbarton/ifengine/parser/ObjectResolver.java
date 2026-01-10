package io.github.tmanbarton.ifengine.parser;

import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.OpenableItem;
import io.github.tmanbarton.ifengine.OpenableLocation;
import io.github.tmanbarton.ifengine.game.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Resolves object names to actual items based on scope and context.
 * Uses exact name or alias matching only.
 */
public final class ObjectResolver {

  /**
   * Result of object resolution attempt.
   */
  public static final class ResolutionResult {
    private final Item item;
    private final boolean success;

    private ResolutionResult(@Nullable final Item item, final boolean success) {
      this.item = item;
      this.success = success;
    }

    public static ResolutionResult success(@Nonnull final Item item) {
      return new ResolutionResult(item, true);
    }

    public static ResolutionResult notFound() {
      return new ResolutionResult(null, false);
    }

    @Nullable
    public Item getItem() {
      return item;
    }

    public boolean isSuccess() {
      return success;
    }
  }

  /**
   * Resolves an object name to an item using scope-based priority.
   * Priority: inventory → current location
   *
   * @param objectName the name to resolve
   * @param player the player for context
   * @return resolution result
   */
  @Nonnull
  public ResolutionResult resolveObject(@Nonnull final String objectName, @Nonnull final Player player) {
    final String normalizedName = objectName.toLowerCase().trim();

    // Handle pronouns like "it" by treating them as implied objects
    if (isPronoun(normalizedName)) {
      return ResolutionResult.notFound(); // Signal that this should use implied object resolution
    }

    // Priority 1: Player inventory
    final List<Item> inventoryMatches = findMatches(normalizedName, player.getInventory());
    if (!inventoryMatches.isEmpty()) {
      return ResolutionResult.success(inventoryMatches.getFirst());
    }

    // Priority 2: Current location items
    final List<Item> locationMatches = findMatches(normalizedName, player.getCurrentLocation().getItems());
    if (!locationMatches.isEmpty()) {
      return ResolutionResult.success(locationMatches.getFirst());
    }

    return ResolutionResult.notFound();
  }

  /**
   * Resolves an implied object when context makes it obvious.
   * For example, "open" when only one openable thing is visible,
   * or "drop" when only one item in inventory.
   *
   * @param verb the command verb for context
   * @param player the player for context
   * @return resolution result, or not found if multiple/no candidates
   */
  @Nonnull
  public ResolutionResult resolveImpliedObject(@Nonnull final String verb, @Nonnull final Player player) {
    final Location location = player.getCurrentLocation();

    return switch (verb.toLowerCase()) {
      case "open" -> {
        // Look for openable items (doors, containers, etc.)
        final List<Item> openableItems = location.getItems().stream()
            .filter(this::isOpenable).toList();

        // If no openable items found, check if current location itself is openable (like a shed)
        if (openableItems.isEmpty() && isLocationOpenable(location)) {
          final Item locationProxy = createLocationProxy((OpenableLocation) location);
          yield ResolutionResult.success(locationProxy);
        }

        if (openableItems.size() == 1) {
          yield ResolutionResult.success(openableItems.get(0));
        }
        yield ResolutionResult.notFound();
      }
      case "unlock" -> {
        // Look for unlockable items
        final List<Item> unlockableItems = location.getItems().stream()
            .filter(this::isUnlockable).toList();

        // If no unlockable items found, check if current location itself is unlockable (like a shed)
        if (unlockableItems.isEmpty() && isLocationUnlockable(location)) {
          final Item locationProxy = createLocationProxy((OpenableLocation) location);
          yield ResolutionResult.success(locationProxy);
        }

        if (unlockableItems.size() == 1) {
          yield ResolutionResult.success(unlockableItems.getFirst());
        }
        yield ResolutionResult.notFound();
      }
      case "take" -> {
        final List<Item> locationItems = location.getItems();

        if (locationItems.size() == 1) {
          yield ResolutionResult.success(locationItems.getFirst());
        }
        yield ResolutionResult.notFound();
      }
      case "drop" -> {
        final List<Item> inventory = player.getInventory();

        if (inventory.size() == 1) {
          yield ResolutionResult.success(inventory.getFirst());
        }
        yield ResolutionResult.notFound();
      }
      case "look" -> {
        // For look, check both inventory and location items, prioritizing inventory
        final List<Item> allAvailable = new ArrayList<>(player.getInventory());
        allAvailable.addAll(location.getItems());

        // If only one item available total, use it
        if (allAvailable.size() == 1) {
          yield ResolutionResult.success(allAvailable.get(0));
        }

        // If multiple items, prioritize inventory items (things player just interacted with)
        final List<Item> inventory = player.getInventory();
        if (inventory.size() == 1) {
          yield ResolutionResult.success(inventory.get(0));
        }

        yield ResolutionResult.notFound();
      }
      default -> ResolutionResult.notFound();
    };
  }

  /**
   * Finds items matching the given name using exact name or alias matching only.
   * No partial or substring matching is performed.
   *
   * @param objectName the normalized object name
   * @param items the items to search
   * @return list of matching items
   */
  @Nonnull
  private List<Item> findMatches(@Nonnull final String objectName, @Nonnull final List<Item> items) {
    final List<Item> matches = new ArrayList<>();

    // Handle empty search term
    if (objectName.isEmpty()) {
      return matches;
    }

    for (final Item item : items) {
      if (item.matchesName(objectName)) {
        matches.add(item);
      }
    }

    return matches;
  }

  /**
   * Determines if an item can be opened.
   * An item is openable if it is an OpenableItem instance.
   */
  private boolean isOpenable(@Nullable final Item item) {
    return item instanceof OpenableItem;
  }

  /**
   * Determines if an item can be unlocked.
   * An item is unlockable if it is an OpenableItem that requires unlocking.
   */
  private boolean isUnlockable(@Nullable final Item item) {
    if (item instanceof OpenableItem openableItem) {
      return openableItem.requiresUnlocking() && !openableItem.isUnlocked();
    }
    return false;
  }

  /**
   * Determines if the given string is a pronoun that should trigger inferred object resolution.
   */
  private boolean isPronoun(@Nonnull final String word) {
    return word.equals("it") || word.equals("that") || word.equals("this");
  }

  /**
   * Checks if the current location can be opened (like a shed or vault).
   */
  private boolean isLocationOpenable(@Nonnull final Location location) {
    return location instanceof OpenableLocation;
  }

  /**
   * Checks if the current location can be unlocked (like a shed or vault).
   */
  private boolean isLocationUnlockable(@Nonnull final Location location) {
    return location instanceof OpenableLocation;
  }

  /**
   * Creates a proxy item representing the location for inferred object resolution.
   * This allows locations like sheds to be treated as objects for commands like "open" or "unlock".
   */
  @Nonnull
  private Item createLocationProxy(@Nonnull final OpenableLocation location) {
    final Set<String> inferredNames = location.getInferredTargetNames();
    final String name = inferredNames.iterator().next();
    return new Item(name, name, name, name, inferredNames);
  }
}
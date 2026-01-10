package io.github.tmanbarton.ifengine.util;

import io.github.tmanbarton.ifengine.Container;
import io.github.tmanbarton.ifengine.Item;
import io.github.tmanbarton.ifengine.LocationContainer;
import io.github.tmanbarton.ifengine.game.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for formatting location item descriptions with container status.
 * Provides consistent item display across look and movement commands.
 */
public final class LocationItemFormatter {

  private LocationItemFormatter() {
    // Utility class - prevent instantiation
  }

  /**
   * Formats a list of items showing containment status.
   * Items are separated into non-contained and contained, with non-contained items first.
   *
   * @param items the items to format
   * @param player the player context (for containment lookups)
   * @param useEraAwareNames whether to use era-aware names for inventory containers
   * @return formatted string with items and containment status
   */
  @Nonnull
  public static String formatItems(
      @Nonnull final List<Item> items,
      @Nonnull final Player player,
      final boolean useEraAwareNames
  ) {
    final List<String> nonContainedDescriptions = new ArrayList<>();
    final List<String> containedDescriptions = new ArrayList<>();

    for (final Item item : items) {
      // Get era-aware description for the item
      final String itemDescription = item.getLocationDescription();

      // Check if item is contained in any container
      if (player.isItemContained(item)) {
        final Container container = player.getContainerForItem(item);
        // Inventory container (bag, jar, etc.)
        if (container instanceof Item containerItem) {
          final String containerName = containerItem.getName();
          containedDescriptions.add(itemDescription + " - in " + containerName);
        }
        // Location container (table, desk, etc.)
        else if (container instanceof LocationContainer locationContainer) {
          final List<String> prepositions = locationContainer.getPreferredPrepositions();
          containedDescriptions.add(itemDescription + " - " + prepositions.getFirst() + " " + locationContainer.getSceneryObject().name());
        } else {
          // Shouldn't happen, but fallback to plain description
          containedDescriptions.add(itemDescription);
        }
      } else {
        // Add non-contained items
        nonContainedDescriptions.add(itemDescription);
      }
    }

    // Combine: non-contained first, then contained at the end
    final List<String> allDescriptions = new ArrayList<>();
    allDescriptions.addAll(nonContainedDescriptions);
    allDescriptions.addAll(containedDescriptions);

    // Join with newlines (no trailing newline after last item)
    return String.join("\n", allDescriptions);
  }
}
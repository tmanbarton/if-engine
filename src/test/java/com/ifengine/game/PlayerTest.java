package com.ifengine.game;

import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.Openable;
import com.ifengine.OpenResult;
import com.ifengine.UnlockResult;
import com.ifengine.test.TestItemFactory;
import com.ifengine.test.TestLocationFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for Player class.
 * Tests inventory, containment, location, reset, and hint tracking.
 */
@DisplayName("Player Tests")
class PlayerTest {

  private Player player;
  private Location startingLocation;

  @BeforeEach
  void setUp() {
    startingLocation = TestLocationFactory.createDefaultLocation();
    player = new Player(startingLocation);
  }

  @Nested
  @DisplayName("Inventory Management")
  class InventoryManagement {

    @Test
    @DisplayName("Test addItem - multiple items")
    void testAddItem_multipleItems() {
      final Item key = TestItemFactory.createTestKey();
      final Item rope = TestItemFactory.createTestRope();

      player.addItem(key);
      player.addItem(rope);

      assertEquals(2, player.getInventory().size());
      assertTrue(player.getInventory().contains(key));
      assertTrue(player.getInventory().contains(rope));
    }

    @Test
    @DisplayName("Test removeItem - removes item from inventory")
    void testRemoveItem_removesFromInventory() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);

      final boolean result = player.removeItem(key);

      assertTrue(result);
      assertFalse(player.getInventory().contains(key));
    }

    @Test
    @DisplayName("Test hasItem - returns true for item in inventory")
    void testHasItem_itemInInventory() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);

      assertTrue(player.hasItem("key"));
    }

    @Test
    @DisplayName("Test getInventoryItemByName - returns item when present")
    void testGetInventoryItemByName_itemPresent() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);

      final Item result = player.getInventoryItemByName("key");

      assertNotNull(result);
      assertEquals(key, result);
    }
  }

  @Nested
  @DisplayName("Container State Tracking")
  class ContainerStateTracking {

    @Test
    @DisplayName("Test markItemAsContained - tracks containment")
    void testMarkItemAsContained_tracksContainment() {
      final Item key = TestItemFactory.createTestKey();
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 5);
      player.addItem(key);
      player.addItem(bag);

      player.markItemAsContained(key, bag);

      assertTrue(player.isItemContained(key));
      assertEquals(bag, player.getContainerForItem(key));
    }

    @Test
    @DisplayName("Test getContainedItems - returns items in container")
    void testGetContainedItems_returnsContainedItems() {
      final Item key = TestItemFactory.createTestKey();
      final Item gem = TestItemFactory.createTestGem();
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 5);
      player.addItem(key);
      player.addItem(gem);
      player.addItem(bag);
      player.markItemAsContained(key, bag);
      player.markItemAsContained(gem, bag);

      final List<Item> containedItems = player.getContainedItems(bag);

      assertEquals(2, containedItems.size());
      assertTrue(containedItems.contains(key));
      assertTrue(containedItems.contains(gem));
    }

    @Test
    @DisplayName("Test removeContainment - removes containment tracking")
    void testRemoveContainment_removesTracking() {
      final Item key = TestItemFactory.createTestKey();
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 5);
      player.addItem(key);
      player.addItem(bag);
      player.markItemAsContained(key, bag);

      player.removeContainment(key);

      assertFalse(player.isItemContained(key));
      assertNull(player.getContainerForItem(key));
    }
  }

  @Nested
  @DisplayName("Reset Functionality")
  class ResetFunctionality {

    @Test
    @DisplayName("Test reset - clears inventory")
    void testReset_clearsInventory() {
      final Item key = TestItemFactory.createTestKey();
      player.addItem(key);

      player.reset(startingLocation);

      assertTrue(player.getInventory().isEmpty());
    }

    @Test
    @DisplayName("Test reset - sets location to starting location")
    void testReset_setsLocation() {
      final Location newLocation = TestLocationFactory.createTestForest();
      player.setCurrentLocation(newLocation);

      player.reset(startingLocation);

      assertEquals(startingLocation, player.getCurrentLocation());
    }

    @Test
    @DisplayName("Test reset - sets game state to PLAYING")
    void testReset_setsGameStatePlaying() {
      player.setGameState(GameState.WAITING_FOR_START_ANSWER);

      player.reset(startingLocation);

      assertEquals(GameState.PLAYING, player.getGameState());
    }
  }

  @Nested
  @DisplayName("Hint Tracking")
  class HintTracking {

    @Test
    @DisplayName("Test incrementHintCount - multiple increments")
    void testIncrementHintCount_multipleIncrements() {
      player.incrementHintCount("puzzle1");
      player.incrementHintCount("puzzle1");
      player.incrementHintCount("puzzle1");

      assertEquals(3, player.getHintCount("puzzle1"));
    }

    @Test
    @DisplayName("Test incrementHintCount - tracks multiple phases independently")
    void testIncrementHintCount_independentPhases() {
      player.incrementHintCount("puzzle1");
      player.incrementHintCount("puzzle1");
      player.incrementHintCount("puzzle2");

      assertEquals(2, player.getHintCount("puzzle1"));
      assertEquals(1, player.getHintCount("puzzle2"));
    }
  }

  @Nested
  @DisplayName("Formatted Inventory")
  class FormattedInventory {

    @Test
    @DisplayName("Test getFormattedInventoryItems - multiple items")
    void testGetFormattedInventoryItems_multipleItems() {
      final Item key = TestItemFactory.createTestKey();
      final Item rope = TestItemFactory.createTestRope();
      player.addItem(key);
      player.addItem(rope);

      final String result = player.getFormattedInventoryItems();

      assertEquals("A simple test key\nA coiled test rope", result);
    }

    @Test
    @DisplayName("Test getFormattedInventoryItems - contained item shows container name")
    void testGetFormattedInventoryItems_containerAnnotation() {
      final Item key = TestItemFactory.createTestKey();
      final TestItemFactory.TestContainer bag = TestItemFactory.createTestContainer("bag", 5);
      player.addItem(key);
      player.addItem(bag);
      player.markItemAsContained(key, bag);

      final String result = player.getFormattedInventoryItems();

      assertEquals("A simple test key - in bag\nA test bag", result);
    }
  }

  @Nested
  @DisplayName("Pending Openable")
  class PendingOpenable {

    @Test
    @DisplayName("Test clearPendingOpenable - clears pending openable")
    void testClearPendingOpenable_clearsPendingOpenable() {
      final Openable openable = createTestOpenable();
      player.setPendingOpenable(openable);

      player.clearPendingOpenable();

      assertNull(player.getPendingOpenable());
    }

    private Openable createTestOpenable() {
      return new Openable() {
        private boolean unlocked = false;
        private boolean open = false;

        @Override
        public boolean isUnlocked() {
          return unlocked;
        }

        @Override
        public void setUnlocked(final boolean unlocked) {
          this.unlocked = unlocked;
        }

        @Override
        public boolean isOpen() {
          return open;
        }

        @Override
        public void setOpen(final boolean open) {
          this.open = open;
        }

        @Override
        public boolean requiresUnlocking() {
          return true;
        }

        @Override
        @Nonnull
        public Set<String> getInferredTargetNames() {
          return Set.of("test");
        }

        @Override
        public boolean matchesUnlockTarget(@Nonnull final String name) {
          return "test".equalsIgnoreCase(name);
        }

        @Override
        public boolean matchesOpenTarget(@Nonnull final String name) {
          return "test".equalsIgnoreCase(name);
        }

        @Override
        @Nonnull
        public UnlockResult tryUnlock(
            @Nonnull final Player player,
            @Nullable final String providedAnswer,
            @Nonnull final GameMapInterface gameMap) {
          return new UnlockResult(false, "Not implemented");
        }

        @Override
        @Nonnull
        public OpenResult tryOpen(
            @Nonnull final Player player,
            @Nullable final String providedAnswer,
            @Nonnull final GameMapInterface gameMap) {
          return new OpenResult(false, "Not implemented");
        }
      };
    }
  }
}
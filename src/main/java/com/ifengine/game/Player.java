package com.ifengine.game;

import com.ifengine.Container;
import com.ifengine.Item;
import com.ifengine.Location;
import com.ifengine.Openable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a player in the game, tracking their location, inventory, and game state.
 */
public class Player {

  private Location currentLocation;
  private final List<Item> inventory;
  private GameState gameState;
  private final ContainerStateManager containerStateManager;
  private boolean isExperiencedPlayer;
  private String sessionId;
  private final Map<String, Integer> hintCounts;
  private String lastHintPhase;
  private Openable pendingOpenable;

  public Player(@Nonnull final Location startingLocation) {
    this.currentLocation = startingLocation;
    this.inventory = new ArrayList<>();
    this.gameState = GameState.WAITING_FOR_START_ANSWER;
    this.containerStateManager = new ContainerStateManager();
    this.isExperiencedPlayer = false;
    this.hintCounts = new HashMap<>();
  }

  @Nonnull
  public Location getCurrentLocation() {
    return currentLocation;
  }

  /**
   * Sets the player's current location.
   * @param location the new location for the player
   */
  public void setCurrentLocation(@Nonnull final Location location) {
    this.currentLocation = location;
  }

  /**
   * Returns a copy of the player's inventory.
   * Returns a defensive copy to prevent external modification of the inventory list.
   */
  @Nonnull
  public List<Item> getInventory() {
    return new ArrayList<>(inventory);
  }

  /**
   * Adds an item to the player's inventory.
   * @param item the item to add to inventory
   */
  public void addItem(@Nonnull final Item item) {
    inventory.add(item);
  }

  /**
   * Removes an item from the player's inventory.
   * @param item the item to remove from inventory
   * @return true if item was removed, false if item wasn't in inventory
   */
  public boolean removeItem(@Nonnull final Item item) {
    return inventory.remove(item);
  }

  /**
   * Finds an item in inventory by name (case-insensitive).
   * @param itemName the name of the item to find
   * @return the first matching item, or null if not found
   */
  @Nullable
  public Item getInventoryItemByName(@Nonnull final String itemName) {
    return inventory.stream()
        .filter(item -> item.getName().equalsIgnoreCase(itemName))
        .findFirst()
        .orElse(null);
  }

  /**
   * Checks if player has an item with the given name.
   * Case-insensitive search through inventory.
   * @param itemName the name of the item to check for
   */
  public boolean hasItem(@Nonnull final String itemName) {
    return getInventoryItemByName(itemName) != null;
  }

  @Nonnull
  public GameState getGameState() {
    return gameState;
  }

  /**
   * Sets the player's game state.
   * @param gameState the new game state
   */
  public void setGameState(@Nonnull final GameState gameState) {
    this.gameState = gameState;
  }

  /**
   * Resets player to initial state.
   * Clears inventory, moves to starting location, sets state to PLAYING.
   * Used when restarting the game.
   * @param startingLocation the location to move the player to
   */
  public void reset(@Nonnull final Location startingLocation) {
    this.currentLocation = startingLocation;
    this.inventory.clear();
    this.containerStateManager.clearAllInventoryContainment();
    this.gameState = GameState.PLAYING;
    this.isExperiencedPlayer = false;
    this.pendingOpenable = null;
    this.lastHintPhase = null;
    resetHintCounts();
  }

  /**
   * Marks an item as being contained within a container.
   * Delegates to ContainerStateManager for centralized state management.
   * @param item the item that is contained
   * @param container the container holding the item
   */
  public void markItemAsContained(@Nonnull final Item item, @Nonnull final Container container) {
    containerStateManager.markItemAsContained(item, container, this);
  }

  /**
   * Checks if an item is currently contained within a container.
   * Delegates to ContainerStateManager for centralized state management.
   * @param item the item to check
   * @return true if the item is contained, false otherwise
   */
  public boolean isItemContained(@Nonnull final Item item) {
    return containerStateManager.isItemContained(item, this);
  }

  /**
   * Gets the container that holds the given item.
   * Delegates to ContainerStateManager for centralized state management.
   * @param item the item to look up
   * @return the container holding the item, or null if not contained
   */
  @Nullable
  public Container getContainerForItem(@Nonnull final Item item) {
    return containerStateManager.getContainerForItem(item, this);
  }

  /**
   * Removes containment tracking for an item.
   * Delegates to ContainerStateManager for centralized state management.
   * @param item the item to remove containment for
   */
  public void removeContainment(@Nonnull final Item item) {
    containerStateManager.removeContainment(item, this);
  }

  /**
   * Gets all items that are contained within the given container.
   * Delegates to ContainerStateManager for centralized state management.
   * @param container the container to check
   * @return list of items contained in the container
   */
  @Nonnull
  public List<Item> getContainedItems(@Nonnull final Container container) {
    return containerStateManager.getContainedItems(container, this);
  }

  /**
   * Generates a formatted list of inventory items.
   * Shows contained items with " - in [container]" suffix.
   * Does not include header text - use ResponseProvider for that.
   * @return formatted string of inventory items, or empty string if no items
   */
  @Nonnull
  public String getFormattedInventoryItems() {
    if (inventory.isEmpty()) {
      return "";
    }

    return inventory.stream()
        .map(item -> {
          final String description = item.getInventoryDescription();
          if (isItemContained(item)) {
            final Container container = getContainerForItem(item);
            if (container instanceof Item containerItem) {
              return description + " - in " + containerItem.getName();
            }
          }
          return description;
        })
        .collect(Collectors.joining("\n"));
  }

  /**
   * Gets whether the player is experienced with text adventures.
   * @return true if experienced, false if new player
   */
  public boolean isExperiencedPlayer() {
    return isExperiencedPlayer;
  }

  /**
   * Sets whether the player is experienced with text adventures.
   * @param isExperiencedPlayer true if experienced, false if new
   */
  public void setExperiencedPlayer(final boolean isExperiencedPlayer) {
    this.isExperiencedPlayer = isExperiencedPlayer;
  }

  /**
   * Gets the session ID for this player.
   * @return the session ID, or null if not set
   */
  @Nullable
  public String getSessionId() {
    return sessionId;
  }

  /**
   * Sets the session ID for this player.
   * Called at the start of command processing to track which session is active.
   * @param sessionId the session ID
   */
  public void setSessionId(@Nullable final String sessionId) {
    this.sessionId = sessionId;
  }

  /**
   * Gets the hint count for a specific puzzle phase.
   * Used for progressive hint system where hints get more specific with each request.
   * @param phaseKey the unique identifier for the puzzle phase
   * @return the number of times hints have been requested for this phase (0 if never requested)
   */
  public int getHintCount(@Nonnull final String phaseKey) {
    return hintCounts.getOrDefault(phaseKey, 0);
  }

  /**
   * Increments the hint count for a specific puzzle phase.
   * Called each time the player requests a hint to progress through hint levels.
   * @param phaseKey the unique identifier for the puzzle phase
   */
  public void incrementHintCount(@Nonnull final String phaseKey) {
    hintCounts.merge(phaseKey, 1, Integer::sum);
  }

  /**
   * Resets all hint counts to zero.
   * Called when the game is restarted to give players fresh hints.
   */
  public void resetHintCounts() {
    hintCounts.clear();
  }

  /**
   * Gets the last hint phase the player was in.
   * Used to detect phase changes for hint count reset.
   *
   * @return the last hint phase key, or null if no hints requested yet
   */
  @Nullable
  public String getLastHintPhase() {
    return lastHintPhase;
  }

  /**
   * Sets the last hint phase the player was in.
   *
   * @param phaseKey the phase key to set
   */
  public void setLastHintPhase(@Nullable final String phaseKey) {
    this.lastHintPhase = phaseKey;
  }

  /**
   * Gets the openable object awaiting code/word input from the player.
   * Used when the player is in WAITING_FOR_UNLOCK_CODE or WAITING_FOR_OPEN_CODE state.
   * @return the pending openable, or null if none
   */
  @Nullable
  public Openable getPendingOpenable() {
    return pendingOpenable;
  }

  /**
   * Sets the openable object awaiting code/word input from the player.
   * Called when the player attempts to unlock/open without providing a code.
   * @param openable the openable awaiting input, or null to clear
   */
  public void setPendingOpenable(@Nullable final Openable openable) {
    this.pendingOpenable = openable;
  }

  /**
   * Clears the pending openable.
   * Called after the player provides input or when the interaction is cancelled.
   */
  public void clearPendingOpenable() {
    this.pendingOpenable = null;
  }
}
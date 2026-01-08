package com.ifengine.constants;

import java.util.List;

/**
 * Game-wide constants. Populate with your game's intro text, messages, etc.
 */
public class GameConstants {

  private GameConstants() {
    // Prevent instantiation
  }

  /**
   * All valid direction words for movement commands.
   */
  public static final List<String> DIRECTIONS = List.of(
      "north", "south", "east", "west", "up", "down",
      "northeast", "northwest", "southeast", "southwest",
      "in", "out"
  );

  /**
   * Default restart message format. Games can override via ResponseProvider.
   * The %s placeholder is replaced with the current location description.
   */
  public static final String RESTART_MESSAGE = "You're back where it all began.\n\n%s";
}
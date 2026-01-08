package com.ifengine.util;

import com.ifengine.Direction;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Utility class for converting Direction enums to their textual representations.
 * Supports direction word extraction for UI highlighting and command parsing.
 */
public final class DirectionHelper {

  private DirectionHelper() {
    // Utility class - prevent instantiation
  }

  /**
   * Gets all textual representations of a direction (full name and abbreviation).
   *
   * @param direction the direction to convert
   * @return list of all words representing this direction
   */
  @Nonnull
  public static List<String> getAllDirectionWords(@Nonnull final Direction direction) {
    return switch (direction) {
      case NORTH -> List.of("north");
      case SOUTH -> List.of("south");
      case EAST -> List.of("east");
      case WEST -> List.of("west");
      case UP -> List.of("up");
      case DOWN -> List.of("down");
      case NORTHEAST -> List.of("northeast");
      case NORTHWEST -> List.of("northwest");
      case SOUTHEAST -> List.of("southeast");
      case SOUTHWEST -> List.of("southwest");
      case IN -> List.of("in");
      case OUT -> List.of("out");
    };
  }

  /**
   * Converts a set of directions to all their textual representations.
   * Useful for getting all valid direction words for a location.
   *
   * @param directions set of directions to convert
   * @return list of all words representing these directions (no duplicates)
   */
  @Nonnull
  public static List<String> getDirectionWordsFromSet(@Nonnull final Set<Direction> directions) {
    return directions.stream()
        .flatMap(dir -> getAllDirectionWords(dir).stream())
        .distinct()
        .toList();
  }
}
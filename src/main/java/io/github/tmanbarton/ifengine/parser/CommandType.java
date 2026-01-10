package io.github.tmanbarton.ifengine.parser;

/**
 * Represents the type of command structure parsed from user input.
 */
public enum CommandType {
  /**
   * Single command: "take key"
   */
  SINGLE,

  /**
   * Multiple objects with conjunction: "take key and rope"
   */
  CONJUNCTION,

  /**
   * Sequential commands: "go north then east"
   */
  SEQUENCE,

  /**
   * Exclusion command: "drop all except key"
   */
  EXCLUSION
}
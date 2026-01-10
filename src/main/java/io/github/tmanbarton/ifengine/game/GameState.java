package io.github.tmanbarton.ifengine.game;

/**
 * Represents the current state of the game for a player session.
 */
public enum GameState {
  /**
   * Normal gameplay - player is actively playing.
   */
  PLAYING,

  /**
   * Waiting for player to answer the initial "have you played before?" question.
   */
  WAITING_FOR_START_ANSWER,

  /**
   * Waiting for player to confirm quit command.
   */
  WAITING_FOR_QUIT_CONFIRMATION,

  /**
   * Waiting for player to confirm restart command.
   */
  WAITING_FOR_RESTART_CONFIRMATION,

  /**
   * Waiting for player to enter a code/word to unlock an openable object.
   */
  WAITING_FOR_UNLOCK_CODE,

  /**
   * Waiting for player to enter a code/word to open an openable object.
   */
  WAITING_FOR_OPEN_CODE
}
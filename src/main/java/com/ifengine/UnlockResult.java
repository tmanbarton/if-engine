package com.ifengine;

import javax.annotation.Nonnull;

/**
 * Represents the result of an unlock attempt on an Openable object.
 * Contains a success flag and a message describing the outcome.
 *
 * @param success true if the unlock was successful, false otherwise
 * @param message the message to display to the player
 */
public record UnlockResult(boolean success, @Nonnull String message) {
}
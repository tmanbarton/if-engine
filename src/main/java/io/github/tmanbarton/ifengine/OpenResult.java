package io.github.tmanbarton.ifengine;

import javax.annotation.Nonnull;

/**
 * Represents the result of an open attempt on an Openable object.
 * Contains a success flag and a message describing the outcome.
 *
 * @param success true if the open was successful, false otherwise
 * @param message the message to display to the player
 */
public record OpenResult(boolean success, @Nonnull String message) {
}
package io.github.tmanbarton.ifengine.response;

import javax.annotation.Nonnull;

/**
 * Interface for providing response text in the game.
 * Games implement this interface to customize the narrator voice and responses.
 */
public interface ResponseProvider {

  // Movement responses

  @Nonnull
  String getCantGoThatWay();

  @Nonnull
  String getDirectionNotUnderstood(@Nonnull String direction);

  @Nonnull
  String getGoNoDirectionSpecified();

  // Look responses

  @Nonnull
  String getLookAtObjectNotPresent(@Nonnull String objectName);

  // Take responses

  @Nonnull
  String getTakeWhat();

  @Nonnull
  String getTakeSuccess();

  @Nonnull
  String getTakeNoItemsAvailable();

  @Nonnull
  String getTakeNeedToSpecify();

  @Nonnull
  String getTakeAlreadyHave();

  @Nonnull
  String getTakeAllSuccess();

  // Drop responses

  @Nonnull
  String getDropWhat();

  @Nonnull
  String getDropSuccess();

  @Nonnull
  String getDropNotCarryingAnything();

  @Nonnull
  String getDropNeedToSpecify();

  @Nonnull
  String getDropDontHave(@Nonnull String itemName);

  @Nonnull
  String getDropAllSuccess();

  // Put responses

  @Nonnull
  String getPutWhat();

  @Nonnull
  String getPutWhere(@Nonnull String itemName);

  @Nonnull
  String getPutSuccess(@Nonnull String itemName, @Nonnull String preposition, @Nonnull String containerName);

  @Nonnull
  String getPutItemNotPresent(@Nonnull String itemName);

  @Nonnull
  String getPutContainerNotFound(@Nonnull String containerName);

  @Nonnull
  String getPutNotAContainer(@Nonnull String objectName);

  @Nonnull
  String getPutItemNotAccepted(@Nonnull String containerName, @Nonnull String itemName);

  @Nonnull
  String getPutContainerFull(@Nonnull String containerName);

  @Nonnull
  String getPutFailed(@Nonnull String itemName, @Nonnull String containerName);

  @Nonnull
  String getPutCircularContainment();

  @Nonnull
  String getPutMissingPreposition(@Nonnull String itemName);

  @Nonnull
  String getPutUnsupportedPreposition(@Nonnull String preposition);

  @Nonnull
  String getPutInvalidPreposition(@Nonnull String containerName, @Nonnull String preferredPreposition);

  // Unlock responses

  @Nonnull
  String getUnlockNothingToUnlock();

  @Nonnull
  String getUnlockCantUnlock(@Nonnull String itemName);

  // Open responses

  @Nonnull
  String getOpenNothingToOpen();

  @Nonnull
  String getOpenCantOpen(@Nonnull String itemName);

  @Nonnull
  String getOpenNeedToSpecify(@Nonnull String objectName);

  @Nonnull
  String getUnlockNeedToSpecify(@Nonnull String objectName);

  // Climb responses

  @Nonnull
  String getClimbWhat();

  @Nonnull
  String getCantClimb();

  @Nonnull
  String getClimbNotPresent();

  // Punch responses

  @Nonnull
  String getPunchWhat();

  @Nonnull
  String getCantPunch(@Nonnull String itemName);

  @Nonnull
  String getPunchNotPresent();

  // Kick responses

  @Nonnull
  String getKickWhat();

  @Nonnull
  String getCantKick(@Nonnull String itemName);

  @Nonnull
  String getKickNotPresent();

  // Drink responses

  @Nonnull
  String getDrinkWhat();

  @Nonnull
  String getCantDrink(@Nonnull String itemName);

  @Nonnull
  String getDrinkNotPresent();

  // Swim responses

  @Nonnull
  String getSwimWhat();

  @Nonnull
  String getCantSwim(@Nonnull String itemName);

  @Nonnull
  String getSwimNotPresent();

  // Read responses

  @Nonnull
  String getReadWhat();

  @Nonnull
  String getCantRead(@Nonnull String itemName);

  @Nonnull
  String getReadNotPresent();

  // Eat responses

  @Nonnull
  String getEatWhat();

  @Nonnull
  String getEatNothingAvailable();

  @Nonnull
  String getEatDontHave(@Nonnull String itemName);

  @Nonnull
  String getEatNotEdible();

  @Nonnull
  String getEatSuccess();

  // Inventory responses

  @Nonnull
  String getInventoryEmpty();

  @Nonnull
  String getInventory(@Nonnull String inventoryItems);

  // System responses

  @Nonnull
  String getCommandNotUnderstood(@Nonnull String command);

  @Nonnull
  String getVerbPrepositionInvalid();

  @Nonnull
  String getItemNotPresent(@Nonnull String itemName);

  @Nonnull
  String getHelpMessage();

  @Nonnull
  String getInfoMessage();

  @Nonnull
  String getQuitConfirmation();

  @Nonnull
  String getQuitCancelled();

  @Nonnull
  String getRestartConfirmation();

  @Nonnull
  String getRestartCancelled();

  @Nonnull
  String getPleaseAnswerQuestion();

  // Intro/Start responses

  /**
   * Gets the initial question asking if the player has played before.
   * @return the "have you played before?" question text
   */
  @Nonnull
  String getHaveYouPlayedBeforeQuestion();

  /**
   * Gets the intro text for new players.
   * This is shown when they answer "no" to the played before question.
   * @return the intro text for new players
   */
  @Nonnull
  String getNewPlayerIntro();

  /**
   * Gets the intro text for experienced players.
   * This is shown when they answer "yes" to the played before question.
   * @return the intro text for experienced players
   */
  @Nonnull
  String getExperiencedPlayerIntro();

  /**
   * Gets the restart message format.
   * Should contain a %s placeholder for the location description.
   * @return the restart message format string
   */
  @Nonnull
  String getRestartMessage();
}
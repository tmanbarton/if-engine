 package io.github.tmanbarton.ifengine.response;

import javax.annotation.Nonnull;

/**
 * Default implementation of ResponseProvider with neutral English responses.
 * Games can extend this class or implement ResponseProvider directly for custom responses.
 */
public class DefaultResponses implements ResponseProvider {

  // Movement responses

  @Override
  @Nonnull
  public String getCantGoThatWay() {
    return "You can't go that way.";
  }

  @Override
  @Nonnull
  public String getDirectionNotUnderstood(@Nonnull final String direction) {
    return String.format("'%s' is not a direction I understand.", direction);
  }

  @Override
  @Nonnull
  public String getGoNoDirectionSpecified() {
    return "Which direction do you want to go?";
  }

  // Look responses

  @Override
  @Nonnull
  public String getLookAtObjectNotPresent(@Nonnull final String objectName) {
    return String.format("You don't see a %s here.", objectName);
  }

  // Take responses

  @Override
  @Nonnull
  public String getTakeWhat() {
    return "Take what?";
  }

  @Override
  @Nonnull
  public String getTakeSuccess() {
    return "Taken.";
  }

  @Override
  @Nonnull
  public String getTakeNoItemsAvailable() {
    return "There's nothing here to take.";
  }

  @Override
  @Nonnull
  public String getTakeNeedToSpecify() {
    return "You'll need to be more specific about what you want to take.";
  }

  @Override
  @Nonnull
  public String getTakeAlreadyHave() {
    return "You already have that.";
  }

  @Override
  @Nonnull
  public String getTakeAllSuccess() {
    return "Taken.";
  }

  // Drop responses

  @Override
  @Nonnull
  public String getDropWhat() {
    return "Drop what?";
  }

  @Override
  @Nonnull
  public String getDropSuccess() {
    return "Dropped.";
  }

  @Override
  @Nonnull
  public String getDropNotCarryingAnything() {
    return "You're not carrying anything.";
  }

  @Override
  @Nonnull
  public String getDropNeedToSpecify() {
    return "You'll need to be more specific about what you want to drop.";
  }

  @Override
  @Nonnull
  public String getDropDontHave(@Nonnull final String itemName) {
    return String.format("You're not carrying a '%s'.", itemName);
  }

  @Override
  @Nonnull
  public String getDropAllSuccess() {
    return "Dropped.";
  }

  // Put responses

  @Override
  @Nonnull
  public String getPutWhat() {
    return "Put what?";
  }

  @Override
  @Nonnull
  public String getPutWhere(@Nonnull final String itemName) {
    return String.format("Where do you want to put the %s?", itemName);
  }

  @Override
  @Nonnull
  public String getPutSuccess(@Nonnull final String itemName, @Nonnull final String preposition,
                              @Nonnull final String containerName) {
    return "Done.";
  }

  @Override
  @Nonnull
  public String getPutItemNotPresent(@Nonnull final String itemName) {
    return String.format("You don't have a %s and there isn't one here.", itemName);
  }

  @Override
  @Nonnull
  public String getPutContainerNotFound(@Nonnull final String containerName) {
    return String.format("You don't see a %s here.", containerName);
  }

  @Override
  @Nonnull
  public String getPutNotAContainer(@Nonnull final String objectName) {
    return String.format("The %s isn't something you can put things in or on.", objectName);
  }

  @Override
  @Nonnull
  public String getPutItemNotAccepted(@Nonnull final String containerName, @Nonnull final String itemName) {
    return String.format("The %s won't hold a %s.", containerName, itemName);
  }

  @Override
  @Nonnull
  public String getPutContainerFull(@Nonnull final String containerName) {
    return String.format("The %s is full.", containerName);
  }

  @Override
  @Nonnull
  public String getPutFailed(@Nonnull final String itemName, @Nonnull final String containerName) {
    return String.format("You can't put the %s in the %s.", itemName, containerName);
  }

  @Override
  @Nonnull
  public String getPutCircularContainment() {
    return "You can't put something inside itself.";
  }

  @Override
  @Nonnull
  public String getPutMissingPreposition(@Nonnull final String itemName) {
    return String.format("What do you want to do with the %s?", itemName);
  }

  @Override
  @Nonnull
  public String getPutUnsupportedPreposition(@Nonnull final String preposition) {
    return String.format("You can only put things 'in' or 'on' other things, not '%s'.", preposition);
  }

  @Override
  @Nonnull
  public String getPutInvalidPreposition(@Nonnull final String containerName,
                                         @Nonnull final String preferredPreposition) {
    return String.format("You can only put things %s the %s.", preferredPreposition, containerName);
  }

  // Unlock responses

  @Override
  @Nonnull
  public String getUnlockNothingToUnlock() {
    return "There's nothing here that needs unlocking.";
  }

  @Override
  @Nonnull
  public String getUnlockCantUnlock(@Nonnull final String itemName) {
    return String.format("The %s isn't something you can unlock.", itemName);
  }

  // Open responses

  @Override
  @Nonnull
  public String getOpenNothingToOpen() {
    return "There's nothing here to open.";
  }

  @Override
  @Nonnull
  public String getOpenCantOpen(@Nonnull final String itemName) {
    return String.format("The %s isn't something you can open.", itemName);
  }

  // Climb responses

  @Override
  @Nonnull
  public String getClimbWhat() {
    return "Climb what?";
  }

  @Override
  @Nonnull
  public String getCantClimb() {
    return "That's not something you can climb.";
  }

  @Override
  @Nonnull
  public String getClimbNotPresent() {
    return "There's nothing here to climb.";
  }

  // Punch responses

  @Override
  @Nonnull
  public String getPunchWhat() {
    return "Punch what?";
  }

  @Override
  @Nonnull
  public String getCantPunch(@Nonnull final String itemName) {
    return String.format("Punching the %s won't accomplish anything.", itemName);
  }

  @Override
  @Nonnull
  public String getPunchNotPresent() {
    return "There's nothing here to punch.";
  }

  // Kick responses

  @Override
  @Nonnull
  public String getKickWhat() {
    return "Kick what?";
  }

  @Override
  @Nonnull
  public String getCantKick(@Nonnull final String itemName) {
    return String.format("Kicking the %s won't accomplish anything.", itemName);
  }

  @Override
  @Nonnull
  public String getKickNotPresent() {
    return "There's nothing here to kick.";
  }

  // Drink responses

  @Override
  @Nonnull
  public String getDrinkWhat() {
    return "Drink what?";
  }

  @Override
  @Nonnull
  public String getCantDrink(@Nonnull final String itemName) {
    return String.format("The %s isn't something you can drink.", itemName);
  }

  @Override
  @Nonnull
  public String getDrinkNotPresent() {
    return "There's nothing here to drink.";
  }

  // Swim responses

  @Override
  @Nonnull
  public String getSwimWhat() {
    return "Swim where?";
  }

  @Override
  @Nonnull
  public String getCantSwim(@Nonnull final String itemName) {
    return String.format("You can't swim in the %s.", itemName);
  }

  @Override
  @Nonnull
  public String getSwimNotPresent() {
    return "There's nothing here to swim in.";
  }

  // Read responses

  @Override
  @Nonnull
  public String getReadWhat() {
    return "Read what?";
  }

  @Override
  @Nonnull
  public String getCantRead(@Nonnull final String itemName) {
    return String.format("There's nothing written on the %s.", itemName);
  }

  @Override
  @Nonnull
  public String getReadNotPresent() {
    return "There's nothing here to read.";
  }

  // Eat responses

  @Override
  @Nonnull
  public String getEatWhat() {
    return "Eat what?";
  }

  @Override
  @Nonnull
  public String getEatNothingAvailable() {
    return "There's nothing here to eat.";
  }

  @Override
  @Nonnull
  public String getEatDontHave(@Nonnull final String itemName) {
    return String.format("You don't have a '%s' to eat.", itemName);
  }

  @Override
  @Nonnull
  public String getEatNotEdible() {
    return "That's not something you can eat.";
  }

  @Override
  @Nonnull
  public String getEatSuccess() {
    return "Eaten.";
  }

  // Inventory responses

  @Override
  @Nonnull
  public String getInventoryEmpty() {
    return "You're not carrying anything.";
  }

  @Override
  @Nonnull
  public String getInventory(@Nonnull final String inventoryItems) {
    return String.format("You are carrying:\n%s", inventoryItems);
  }

  // System responses

  @Override
  @Nonnull
  public String getCommandNotUnderstood(@Nonnull final String command) {
    return String.format("I don't understand '%s'.", command);
  }

  @Override
  @Nonnull
  public String getVerbPrepositionInvalid() {
    return "That doesn't make sense.";
  }

  @Override
  @Nonnull
  public String getItemNotPresent(@Nonnull final String itemName) {
    return String.format("You don't see a '%s' here.", itemName);
  }

  private static final String DEFAULT_HELP_MESSAGE = """
      Movement: NORTH, SOUTH, EAST, WEST (or N, S, E, W). Also UP, DOWN, IN, OUT where applicable.

      Items: TAKE to pick up, DROP to put down, INVENTORY (or I) to see what you're carrying.

      Looking: LOOK (or L) to see where you are. LOOK AT something to examine it.

      Actions: OPEN, UNLOCK, CLIMB, READ, PUT, and more. Try what seems natural.

      System: HELP for this message. INFO for game tips. QUIT to exit. RESTART to start over.
      """;

  @Override
  @Nonnull
  public String getHelpMessage() {
    return DEFAULT_HELP_MESSAGE;
  }

  private static final String DEFAULT_INFO_MESSAGE = """
      This is an interactive fiction game. Explore, solve puzzles, and uncover the story.

      Take your time. Look at things carefully. Try different approaches.

      You can't die or get permanently stuck. Experiment freely.

      Type HELP for a list of commands.
      """;

  @Override
  @Nonnull
  public String getInfoMessage() {
    return DEFAULT_INFO_MESSAGE;
  }

  @Override
  @Nonnull
  public String getQuitConfirmation() {
    return "Are you sure you want to quit?";
  }

  @Override
  @Nonnull
  public String getQuitCancelled() {
    return "Okay, continuing.";
  }

  @Override
  @Nonnull
  public String getRestartConfirmation() {
    return "Are you sure you want to restart?";
  }

  @Override
  @Nonnull
  public String getRestartCancelled() {
    return "Okay, continuing.";
  }

  @Override
  @Nonnull
  public String getPleaseAnswerQuestion() {
    return "Please answer the question.";
  }

  // Intro/Start responses

  private static final String DEFAULT_HAVE_YOU_PLAYED_BEFORE = """
      Have you played interactive fiction before?
      """;

  @Override
  @Nonnull
  public String getHaveYouPlayedBeforeQuestion() {
    return DEFAULT_HAVE_YOU_PLAYED_BEFORE;
  }

  private static final String DEFAULT_NEW_PLAYER_INTRO = """
      Welcome! In this interactive fiction, you type commands to interact with the world.

      Try commands like LOOK, TAKE, GO NORTH, or INVENTORY.
      Type HELP for a list of commands.
      """;

  @Override
  @Nonnull
  public String getNewPlayerIntro() {
    return DEFAULT_NEW_PLAYER_INTRO;
  }

  private static final String DEFAULT_EXPERIENCED_PLAYER_INTRO = """
      Welcome back! Let's begin.
      """;

  @Override
  @Nonnull
  public String getExperiencedPlayerIntro() {
    return DEFAULT_EXPERIENCED_PLAYER_INTRO;
  }

  private static final String DEFAULT_RESTART_MESSAGE = "You're back where it all began.\n\n%s";

  @Override
  @Nonnull
  public String getRestartMessage() {
    return DEFAULT_RESTART_MESSAGE;
  }
}
package io.github.tmanbarton.ifengine.command.handlers;

import io.github.tmanbarton.ifengine.InteractionType;
import io.github.tmanbarton.ifengine.game.AbstractInteractionHandler;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.response.ResponseProvider;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Handles PUNCH interaction commands.
 * Follows the unified fallback pattern: real items → scenery objects → default response.
 */
public class PunchHandler extends AbstractInteractionHandler {

  private final ResponseProvider responseProvider;

  public PunchHandler(@Nonnull final ResponseProvider responseProvider) {
    this.responseProvider = responseProvider;
  }

  @Override
  @Nonnull
  public List<String> getSupportedVerbs() {
    return List.of("punch", "hit");
  }

  @Override
  @Nonnull
  protected InteractionType getInteractionType() {
    return InteractionType.PUNCH;
  }

  @Override
  @Nonnull
  protected String getWhatResponse(@Nonnull final Player player) {
    return responseProvider.getPunchWhat();
  }

  @Override
  @Nonnull
  protected String getCantInteract(@Nonnull final Player player, @Nonnull final String itemName) {
    return responseProvider.getCantPunch(itemName);
  }

  @Override
  @Nonnull
  protected String getNotPresentResponse(@Nonnull final Player player) {
    return responseProvider.getPunchNotPresent();
  }
}
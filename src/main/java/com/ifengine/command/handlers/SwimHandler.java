package com.ifengine.command.handlers;

import com.ifengine.InteractionType;
import com.ifengine.game.AbstractInteractionHandler;
import com.ifengine.game.Player;
import com.ifengine.response.ResponseProvider;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Handles SWIM interaction commands.
 * Follows the unified fallback pattern: real items → scenery objects → default response.
 */
public class SwimHandler extends AbstractInteractionHandler {

  private final ResponseProvider responseProvider;

  public SwimHandler(@Nonnull final ResponseProvider responseProvider) {
    this.responseProvider = responseProvider;
  }

  @Override
  @Nonnull
  public List<String> getSupportedVerbs() {
    return List.of("swim");
  }

  @Override
  @Nonnull
  protected InteractionType getInteractionType() {
    return InteractionType.SWIM;
  }

  @Override
  @Nonnull
  protected String getWhatResponse(@Nonnull final Player player) {
    return responseProvider.getSwimWhat();
  }

  @Override
  @Nonnull
  protected String getCantInteract(@Nonnull final Player player, @Nonnull final String itemName) {
    return responseProvider.getCantSwim(itemName);
  }

  @Override
  @Nonnull
  protected String getNotPresentResponse(@Nonnull final Player player) {
    return responseProvider.getSwimNotPresent();
  }
}
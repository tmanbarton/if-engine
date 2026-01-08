package com.ifengine.command.handlers;

import com.ifengine.InteractionType;
import com.ifengine.game.AbstractInteractionHandler;
import com.ifengine.game.Player;
import com.ifengine.response.ResponseProvider;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Handles DRINK interaction commands.
 * Follows the unified fallback pattern: real items → scenery objects → default response.
 */
public class DrinkHandler extends AbstractInteractionHandler {

  private final ResponseProvider responseProvider;

  public DrinkHandler(@Nonnull final ResponseProvider responseProvider) {
    this.responseProvider = responseProvider;
  }

  @Override
  @Nonnull
  public List<String> getSupportedVerbs() {
    return List.of("drink");
  }

  @Override
  @Nonnull
  protected InteractionType getInteractionType() {
    return InteractionType.DRINK;
  }

  @Override
  @Nonnull
  protected String getWhatResponse(@Nonnull final Player player) {
    return responseProvider.getDrinkWhat();
  }

  @Override
  @Nonnull
  protected String getCantInteract(@Nonnull final Player player, @Nonnull final String itemName) {
    return responseProvider.getCantDrink(itemName);
  }

  @Override
  @Nonnull
  protected String getNotPresentResponse(@Nonnull final Player player) {
    return responseProvider.getDrinkNotPresent();
  }
}
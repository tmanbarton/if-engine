package io.github.tmanbarton.ifengine.command.handlers;

import io.github.tmanbarton.ifengine.InteractionType;
import io.github.tmanbarton.ifengine.game.AbstractInteractionHandler;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.response.ResponseProvider;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Handles KICK interaction commands.
 * Follows the unified fallback pattern: real items → scenery objects → default response.
 */
public class KickHandler extends AbstractInteractionHandler {

  private final ResponseProvider responseProvider;

  public KickHandler(@Nonnull final ResponseProvider responseProvider) {
    this.responseProvider = responseProvider;
  }

  @Override
  @Nonnull
  public List<String> getSupportedVerbs() {
    return List.of("kick");
  }

  @Override
  @Nonnull
  protected InteractionType getInteractionType() {
    return InteractionType.KICK;
  }

  @Override
  @Nonnull
  protected String getWhatResponse(@Nonnull final Player player) {
    return responseProvider.getKickWhat();
  }

  @Override
  @Nonnull
  protected String getCantInteract(@Nonnull final Player player, @Nonnull final String itemName) {
    return responseProvider.getCantKick(itemName);
  }

  @Override
  @Nonnull
  protected String getNotPresentResponse(@Nonnull final Player player) {
    return responseProvider.getKickNotPresent();
  }
}
package com.ifengine.command.handlers;

import com.ifengine.InteractionType;
import com.ifengine.game.AbstractInteractionHandler;
import com.ifengine.game.Player;
import com.ifengine.response.ResponseProvider;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Handles CLIMB interaction commands.
 * Follows the unified fallback pattern: real items → scenery objects → default response.
 *
 * This handler can be registered directly with CommandDispatcher (implements BaseCommandHandler
 * via AbstractInteractionHandler).
 */
public class ClimbHandler extends AbstractInteractionHandler {

  private final ResponseProvider responseProvider;

  public ClimbHandler(@Nonnull final ResponseProvider responseProvider) {
    this.responseProvider = responseProvider;
  }

  @Override
  @Nonnull
  public List<String> getSupportedVerbs() {
    return List.of("climb");
  }

  @Override
  @Nonnull
  protected InteractionType getInteractionType() {
    return InteractionType.CLIMB;
  }

  @Override
  @Nonnull
  protected String getWhatResponse(@Nonnull final Player player) {
    return responseProvider.getClimbWhat();
  }

  @Override
  @Nonnull
  protected String getCantInteract(@Nonnull final Player player, @Nonnull final String itemName) {
    return responseProvider.getCantClimb();
  }

  @Override
  @Nonnull
  protected String getNotPresentResponse(@Nonnull final Player player) {
    return responseProvider.getClimbNotPresent();
  }
}

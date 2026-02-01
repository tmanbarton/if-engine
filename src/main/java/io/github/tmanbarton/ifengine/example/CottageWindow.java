package io.github.tmanbarton.ifengine.example;

import io.github.tmanbarton.ifengine.InteractionType;
import io.github.tmanbarton.ifengine.OpenResult;
import io.github.tmanbarton.ifengine.OpenableSceneryObject;
import io.github.tmanbarton.ifengine.UnlockResult;
import io.github.tmanbarton.ifengine.game.GameMapInterface;
import io.github.tmanbarton.ifengine.game.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Example OpenableSceneryObject subclass demonstrating a no-lock scenery object.
 * <p>
 * The cottage window has no lock, so {@code open window} works directly without
 * needing to unlock first.
 *
 * <h2>Usage</h2>
 * <pre>
 * CottageWindow window = new CottageWindow();
 * location.addSceneryObject(window);
 * // Player can now type "open window" directly
 * </pre>
 */
public class CottageWindow extends OpenableSceneryObject {

  /**
   * Creates the cottage window scenery object.
   * No lock required — players can open it directly.
   */
  public CottageWindow() {
    super(
        "window",
        Set.of("dusty window"),
        Map.of(InteractionType.LOOK,
            "A dusty window with a view of the forest outside."),
        Map.of(),
        false,
        Set.of(),
        List.of(),
        false
    );
  }

  @Override
  @Nonnull
  public Set<String> getInferredTargetNames() {
    return Set.of("window");
  }

  @Override
  public boolean matchesUnlockTarget(@Nonnull final String name) {
    return matches(name);
  }

  @Override
  public boolean matchesOpenTarget(@Nonnull final String name) {
    return matches(name);
  }

  @Override
  @Nonnull
  public UnlockResult tryUnlock(@Nonnull final Player player,
                                @Nullable final String providedAnswer,
                                @Nonnull final GameMapInterface gameMap) {
    return new UnlockResult(false, "It doesn't have a lock.");
  }

  @Override
  @Nonnull
  public OpenResult tryOpen(@Nonnull final Player player,
                            @Nullable final String providedAnswer,
                            @Nonnull final GameMapInterface gameMap) {
    if (isOpen()) {
      return new OpenResult(false, "It's already open.");
    }

    setOpen(true);
    return new OpenResult(true,
        "You push open the dusty window. A cool breeze drifts in "
            + "carrying the scent of pine.");
  }
}

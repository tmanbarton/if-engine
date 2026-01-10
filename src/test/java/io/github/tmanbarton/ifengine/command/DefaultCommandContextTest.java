package io.github.tmanbarton.ifengine.command;

import io.github.tmanbarton.ifengine.Location;
import io.github.tmanbarton.ifengine.game.Player;
import io.github.tmanbarton.ifengine.parser.ObjectResolver;
import io.github.tmanbarton.ifengine.response.DefaultResponses;
import io.github.tmanbarton.ifengine.response.ResponseProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for DefaultCommandContext.
 */
@DisplayName("DefaultCommandContext")
class DefaultCommandContextTest {

  private Location location;
  private Player player;
  private ResponseProvider responseProvider;
  private ObjectResolver objectResolver;
  private DefaultCommandContext context;

  @BeforeEach
  void setUp() {
    location = new Location("room", "A test room.", "Test room.");
    player = new Player(location);
    responseProvider = new DefaultResponses();
    objectResolver = new ObjectResolver();
    context = new DefaultCommandContext(responseProvider, objectResolver, player);
  }
}
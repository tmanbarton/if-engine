package com.ifengine.command;

import com.ifengine.Location;
import com.ifengine.game.Player;
import com.ifengine.parser.ObjectResolver;
import com.ifengine.response.DefaultResponses;
import com.ifengine.response.ResponseProvider;

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
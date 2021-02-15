package io.github.mbarkley.rollens.eval;

import net.dv8tion.jda.api.entities.Message;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public interface Command {

  /**
   * @return the text for a message to send in response
   */
  CompletableFuture<String> execute(Random rand, Message message);
}

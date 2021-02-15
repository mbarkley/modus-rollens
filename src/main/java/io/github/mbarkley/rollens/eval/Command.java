package io.github.mbarkley.rollens.eval;

import lombok.Value;
import net.dv8tion.jda.api.entities.Message;
import org.jdbi.v3.core.Jdbi;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public interface Command {

  /**
   * @return the text for a message to send in response
   * @param context
   */
  CompletableFuture<String> execute(ExecutionContext context);

  @Value
  class ExecutionContext {
    Jdbi jdbi;
    Random rand;
    Message message;
  }
}

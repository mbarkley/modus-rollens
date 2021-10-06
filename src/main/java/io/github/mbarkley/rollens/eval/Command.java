package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.discord.CommandEvent;
import io.github.mbarkley.rollens.parse.Parser;
import org.jdbi.v3.core.Jdbi;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public interface Command<T extends Command.CommandOutput> {

  /**
   * @return the text for a message to send in response
   */
  CompletableFuture<T> execute(ExecutionContext context);

  record ExecutionContext(ExecutorService executorService,
                          Jdbi jdbi,
                          Parser parser,
                          Supplier<Random> rand,
                          CommandEvent commandEvent) {
    public Random getRand() {
      return rand.get();
    }
  }

  sealed interface CommandOutput permits StringOutput {}

  record StringOutput(String value) implements CommandOutput {}
}

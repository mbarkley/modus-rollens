package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.discord.CommandEvent;
import io.github.mbarkley.rollens.parse.Parser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public interface Command {

  /**
   * @return the text for a message to send in response
   */
  CompletableFuture<String> execute(ExecutionContext context);

  @RequiredArgsConstructor
  class ExecutionContext {
    @Getter
    private final ExecutorService executorService;
    @Getter
    private final Jdbi jdbi;
    @Getter
    private final Parser parser;
    private final Supplier<Random> rand;
    @Getter
    private final CommandEvent commandEvent;

    public Random getRand() {
      return rand.get();
    }
  }
}

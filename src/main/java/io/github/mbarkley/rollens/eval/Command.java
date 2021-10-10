package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.discord.CommandEvent;
import io.github.mbarkley.rollens.parse.TextParser;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public interface Command<T extends Command.CommandOutput> {

  /**
   * @return the text for a message to send in response
   */
  CompletableFuture<? extends T> execute(ExecutionContext context);

  record ExecutionContext(ExecutorService executorService,
                          Jdbi jdbi,
                          TextParser textParser,
                          Supplier<Random> rand,
                          CommandEvent commandEvent) {
    public Random getRand() {
      return rand.get();
    }
  }

  sealed interface CommandOutput permits ArgumentSelectOutput, CommandSelectOutput, StringOutput {
  }

  record StringOutput(String value) implements CommandOutput {}

  record CommandSelectOutput(String prompt, List<Option> options) implements CommandOutput {}

  record ArgumentSelectOutput(String prompt, String name, List<String> parameters,
                              String selectExpression) implements CommandOutput {}

  record Option(String label, String selectExpression) {}
}

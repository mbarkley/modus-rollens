package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.db.SavedRoll;
import io.github.mbarkley.rollens.db.SavedRollsDao;
import io.github.mbarkley.rollens.eval.Command.StringOutput;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.completedFuture;

@ToString
@EqualsAndHashCode
public class Invoke implements Command<StringOutput> {
  private final String identifier;
  private final int[] arguments;

  public Invoke(String identifier, int[] arguments) {
    this.identifier = identifier;
    this.arguments = arguments;
    if (arguments.length > Byte.MAX_VALUE) {
      throw new IllegalArgumentException("Can't have argument list longer than max byte value");
    }
  }

  @Override
  public CompletableFuture<StringOutput> execute(ExecutionContext context) {
    if (context.commandEvent().isFromGuild()) {
      return doInvoke(context);
    } else {
      return completedFuture(new StringOutput("Cannot invoke saved rolls from direct messages"));
    }
  }

  private CompletableFuture<StringOutput> doInvoke(ExecutionContext context) {
    return CompletableFuture.supplyAsync(() -> {
      long guildId = context.commandEvent().getGuild().getIdLong();
      return context.jdbi()
                    .withExtension(
                        SavedRollsDao.class,
                        dao -> dao.find(guildId, identifier, (byte) arguments.length)
                                  .orElseThrow(() -> new InvalidExpressionException(
                                      format("No saved roll found for `%s %d`", identifier, arguments.length))));

    }, context.executorService()).thenCompose(savedRoll -> {
      final String expressionWithArgs = getSubstitutedExpression(savedRoll);

      final Optional<RollCommand> parsed = context.parser().parseRoll(expressionWithArgs);
      RollCommand rollCommand = parsed
          .orElseThrow(() -> new InvalidExpressionException(
              format("Expression invalid after argument substitution: `%s`",
                     expressionWithArgs)));
      return rollCommand.execute(context)
                        .thenApply(result -> new StringOutput(
                            """
                                Evaluating: `%s`
                                %s""".formatted(expressionWithArgs, result.value())));
    });
  }

  private String getSubstitutedExpression(SavedRoll savedRoll) {
    final List<String> parameters = savedRoll.getDecodedParameters();
    String expressionWithArgs = savedRoll.getExpression();
    for (int i = 0; i < parameters.size(); i++) {
      var param = parameters.get(i);
      var arg = arguments[i];
      expressionWithArgs = expressionWithArgs.replace("{" + param + "}", String.valueOf(arg));
    }
    return expressionWithArgs;
  }

  public static class InvalidExpressionException extends RuntimeException {
    public InvalidExpressionException(String message) {
      super(message);
    }
  }
}

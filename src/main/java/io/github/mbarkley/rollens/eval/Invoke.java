package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.db.SavedRoll;
import io.github.mbarkley.rollens.db.SavedRollsDao;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jdbi.v3.core.Handle;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

@ToString
@EqualsAndHashCode
public class Invoke implements Command {
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
  public CompletableFuture<String> execute(ExecutionContext context) {
    return CompletableFuture.supplyAsync(() -> {
      try (Handle handle = context.getJdbi().open()) {
        long guildId = context.getMessage().getGuild().getIdLong();
        final SavedRoll savedRoll = handle.attach(SavedRollsDao.class)
                                          .find(guildId, identifier, (byte) arguments.length);

        final String expressionWithArgs = getSubstitutedExpression(savedRoll);

        final Optional<Roll> parsed = context.getParser().parseRoll(expressionWithArgs);
        return parsed
            .orElseThrow(() -> new InvalidExpressionException(
                format("Expression invalid after argument substitution: `%s`",
                       expressionWithArgs)));
      }
    }, context.getExecutorService()).thenCompose(command -> command.execute(context));
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

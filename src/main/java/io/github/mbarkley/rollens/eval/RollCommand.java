package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.dice.Output;
import io.github.mbarkley.rollens.dice.PoolResult;
import io.github.mbarkley.rollens.dice.RollExpression;
import io.github.mbarkley.rollens.discord.CommandEvent;
import io.github.mbarkley.rollens.util.MessageUtil;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.completedFuture;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class RollCommand implements Command {
  private final RollExpression rollExpression;

  @Override
  public CompletableFuture<String> execute(ExecutionContext context) {
    final Output output = rollExpression.apply(context.getRand());
    final String rollsDisplayString = buildRollsDisplayString(output);
    final CommandEvent commandEvent = context.getCommandEvent();
    final String username = MessageUtil.getAuthorDisplayName(commandEvent);

    return completedFuture(format("%s roll: `%s`\nResult: %d", username, rollsDisplayString, output.getValue()));
  }

  @NotNull
  private String buildRollsDisplayString(Output output) {
    final StringBuilder sb = new StringBuilder();
    List<List<PoolResult>> results = output.getResults();
    final String delimiter = ", ";
    for (var result : results) {
      for (var pr : result) {
        pr.writeString("`~~`", sb);
      }
      sb.append(delimiter);
    }
    if (!results.isEmpty()) {
      sb.delete(sb.length() - delimiter.length(), sb.length());
    }
    return sb.toString();
  }

}

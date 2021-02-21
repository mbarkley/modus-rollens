package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.dice.DicePool;
import io.github.mbarkley.rollens.dice.PoolResult;
import io.github.mbarkley.rollens.util.MessageUtil;
import lombok.*;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static java.lang.String.format;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class RollCommand implements Command {
  private final DicePool base;
  private final List<RollModifier> rollModifiers;
  @With
  @Getter
  private final ResultMapper resultMapper;

  @Override
  public CompletableFuture<String> execute(ExecutionContext context) {
    final Random rand = context.getRand();
    final PoolResult[] baseResults = base.execute(rand);
    final Message message = context.getMessage();
    final String username = MessageUtil.getAuthorDisplayName(message);

    final List<PoolResult[]> results = new ArrayList<>();
    results.add(baseResults);

    for (RollModifier rollModifier : rollModifiers) {
      rollModifier.modify(rand, results);
    }
    final IntStream allRollValues = results.stream()
                                           .flatMap(Arrays::stream)
                                           .flatMapToInt(pr -> Arrays.stream(pr.getValues()));
    final int value = resultMapper.mapResult(message, allRollValues);
    final String rollDisplay = displayRollsString(results);

    return CompletableFuture.completedFuture(format("%s roll: `%s`\nResult: %d", username, rollDisplay, value));
  }

  @NotNull
  private String displayRollsString(List<PoolResult[]> results) {
    final StringBuilder sb = new StringBuilder();
    final String delimiter = ", ";
    for (var result : results) {
      for (var pr : result) {
        pr.writeString(sb);
      }
      sb.append(delimiter);
    }
    if (!results.isEmpty()) {
      sb.delete(sb.length() - delimiter.length(), sb.length());
    }
    return sb.toString();
  }

}

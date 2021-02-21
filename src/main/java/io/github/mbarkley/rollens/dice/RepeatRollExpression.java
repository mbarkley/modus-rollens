package io.github.mbarkley.rollens.dice;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class RepeatRollExpression implements RollExpression {
  private final RollExpression rollExpression;
  private final int repeat;

  @Override
  @NotNull
  public Output apply(Random rand) {
    final List<Output> outputs = Stream.generate(() -> rollExpression.apply(rand))
                                       .limit(repeat)
                                       .collect(Collectors.toList());

    final List<List<PoolResult>> allResults = new ArrayList<>();
    for (int i = 0; ; i++) {
      final List<PoolResult> poolList = new ArrayList<>();
      allResults.add(poolList);
      for (var output : outputs) {
        final List<List<PoolResult>> results = output.getResults();
        if (i < results.size()) {
          poolList.addAll(results.get(i));
        }
      }
      if (poolList.isEmpty()) {
        allResults.remove(i);
        break;
      }
    }

    final int value = outputs.stream()
                             .mapToInt(Output::getValue)
                             .sum();

    return new Output(allResults, value);
  }
}

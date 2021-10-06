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

    if (outputs.isEmpty()) {
      return new Output(List.of(), 0);
    }

    final List<List<PoolResult>> allResults = new ArrayList<>();
    final int iLen = outputs.get(0).getResults().size();
    for (int i = 0; i < iLen; i++) {
      final List<PoolResult> cur = new ArrayList<>();
      allResults.add(cur);
      final int jLen = outputs.get(0).getResults().get(i).size();
      for (int j = 0; j < jLen; j++) {
        final int i1 = i, j1 = j;
        final PoolResult poolResult = outputs.stream()
                                             .map(Output::getResults)
                                             .map(l -> l.get(i1).get(j1))
                                             .reduce(PoolResult::combine)
                                             .orElseThrow(IllegalStateException::new);
        cur.add(poolResult);
      }
    }

    final int value = outputs.stream()
                             .mapToInt(Output::getValue)
                             .sum();

    return new Output(allResults, value);
  }
}

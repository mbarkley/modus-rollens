package io.github.mbarkley.rollens.dice;

import io.github.mbarkley.rollens.eval.ResultAggregator;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class SimpleRollExpression implements RollExpression {
  private final DicePool base;
  private final List<RollModifier> rollModifiers;
  private final ResultAggregator resultAggregator;

  @NotNull
  public Output apply(Random rand) {
    final PoolResult[] baseResults = base.execute(rand);

    final List<List<PoolResult>> results = new ArrayList<>();
    results.add(Arrays.asList(baseResults));

    for (RollModifier rollModifier : rollModifiers) {
      rollModifier.modify(rand, results);
    }

    final IntStream allRollValues = results.stream()
                                           .flatMap(List::stream)
                                           .flatMapToInt(pr -> Arrays.stream(pr.getValues()));
    final int value = resultAggregator.combineResult(allRollValues);

    return new Output(results, value);
  }

}

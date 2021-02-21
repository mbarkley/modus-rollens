package io.github.mbarkley.rollens.dice;

import io.github.mbarkley.rollens.math.Operator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.With;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@ToString
@EqualsAndHashCode
public class ComplexRollExpression implements RollExpression {
  @Getter @With
  private final Operator operator;
  private final RollExpression[] rollExpressions;

  public ComplexRollExpression(Operator operator, RollExpression... rollExpressions) {
    this.operator = operator;
    this.rollExpressions = rollExpressions;
  }

  @Override
  @NotNull
  public Output apply(Random rand) {
    final Output[] outputs = Arrays.stream(rollExpressions)
                                   .map(rollExpression -> rollExpression.apply(rand))
                                   .toArray(Output[]::new);

    final IntStream allRollValues = Arrays.stream(outputs)
                                          .mapToInt(Output::getValue);
    final int value = allRollValues.reduce(operator::apply).orElseThrow(IllegalArgumentException::new);
    final List<List<PoolResult>> allResults = mergeResults(outputs);

    return new Output(allResults, value);
  }

  @NotNull
  private List<List<PoolResult>> mergeResults(Output[] outputs) {
    final List<List<PoolResult>> allResults = new ArrayList<>();
    for (int i = 0; ; i++) {
      final List<PoolResult> poolList = new ArrayList<>();
      allResults.add(poolList);
      for (var output : Arrays.asList(outputs)) {
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
    return allResults;
  }

}

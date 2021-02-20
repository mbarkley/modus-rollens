package io.github.mbarkley.rollens.eval;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class ExplodingModifier implements RollModifier {
  public static final int MAX_ITERATION_CAP = 100;
  private final int explodingThreshold;
  private final int maxIterations;

  @Override
  public State modify(Random rand, DicePool dicePool, State state) {
    final Roll[] rolls = state.getRolls();
    List<Roll> allRolls = new ArrayList<>(Arrays.asList(rolls));

    int iterationCap = Math.min(maxIterations, MAX_ITERATION_CAP);
    for (var roll : rolls) {
      int iterations = 0;
      while (roll.getValue() >= explodingThreshold && iterations < iterationCap) {
        iterations++;
        roll = new Roll(roll.getNumDiceSides(), rand.nextInt(roll.getNumDiceSides()) + 1);
        allRolls.add(roll);
      }
    }

    final String rerollValues = allRolls.stream()
                                        .skip(rolls.length)
                                        .map(Roll::getValue)
                                        .map(Object::toString)
                                        .collect(Collectors.joining(", "));
    return new State(allRolls.toArray(new Roll[0]), state.getLog() + " `[" + rerollValues + "]`");
  }
}

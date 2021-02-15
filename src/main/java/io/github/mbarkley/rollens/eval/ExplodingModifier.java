package io.github.mbarkley.rollens.eval;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class ExplodingModifier implements RollModifier {
  public static final int MAX_ITERATION_CAP = 100;
  private final int explodingThreshold;
  private final int maxIterations;

  @Override
  public State modify(Random rand, BaseRoll baseRoll, State state) {
    final int[] rolls = state.getRolls();
    List<Integer> newRolls = new ArrayList<>();
    int iterationCap = Math.min(maxIterations, MAX_ITERATION_CAP);
    for (int roll : rolls) {
      int iterations = 0;
      while (roll >= explodingThreshold && iterations < iterationCap) {
        iterations++;
        roll = rand.nextInt(baseRoll.getNumberOfSides()) + 1;
        newRolls.add(roll);
      }
    }

    final int[] allRolls = Arrays.copyOf(rolls, rolls.length + newRolls.size());
    for (int i = 0; i < newRolls.size(); i++) {
      allRolls[rolls.length + i] = newRolls.get(i);
    }

    return new State(allRolls, state.getLog() + " `" + newRolls + "`");
  }
}

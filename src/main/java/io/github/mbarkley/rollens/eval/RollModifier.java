package io.github.mbarkley.rollens.eval;

import lombok.Value;

import java.util.Arrays;
import java.util.Random;

public interface RollModifier {
  @Value
  class State {
    Roll[] rolls;
    String log;

    public int[] getRollValues() {
      return Arrays.stream(rolls)
                   .mapToInt(Roll::getValue)
                   .toArray();
    }
  }

  State modify(Random rand, DicePool dicePool, State state);
}

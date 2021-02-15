package io.github.mbarkley.rollens.eval;

import lombok.Value;

import java.util.Random;

public interface RollModifier {
  @Value
  class State {
    int[] rolls;
    String log;
  }
  State modify(Random rand, BaseRoll baseRoll, State state);
}

package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.dice.PoolResult;

import java.util.List;
import java.util.Random;

public interface RollModifier {
  void modify(Random rand, List<PoolResult[]> results);
}

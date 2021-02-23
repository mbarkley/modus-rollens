package io.github.mbarkley.rollens.dice;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public interface RollModifier extends Comparable<RollModifier> {
  void modify(Random rand, List<List<PoolResult>> results);
  ModifierType type();

  @Override
  default int compareTo(@NotNull RollModifier o) {
    return Integer.signum(type().ordinal() - o.type().ordinal());
  }

  enum ModifierType {
    KEEP_HIGH,
    EXPLOSIVE
  }
}

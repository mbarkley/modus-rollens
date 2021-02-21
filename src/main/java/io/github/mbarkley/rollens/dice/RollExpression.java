package io.github.mbarkley.rollens.dice;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public interface RollExpression {
  @NotNull Output apply(Random rand);
}

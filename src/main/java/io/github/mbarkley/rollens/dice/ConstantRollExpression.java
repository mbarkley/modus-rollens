package io.github.mbarkley.rollens.dice;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

@Value
public class ConstantRollExpression implements RollExpression {
  int value;

  @NotNull
  public Output apply(Random rand) {
    return new Output(List.of(), value);
  }

}

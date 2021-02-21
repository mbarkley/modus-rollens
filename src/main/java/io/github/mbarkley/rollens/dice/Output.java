package io.github.mbarkley.rollens.dice;

import lombok.Value;

import java.util.List;

@Value
public class Output {
  List<List<PoolResult>> results;
  int value;
}

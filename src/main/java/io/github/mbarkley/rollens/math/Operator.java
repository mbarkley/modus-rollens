package io.github.mbarkley.rollens.math;

public enum Operator {
  PLUS, MINUS, MULTIPLY, DIVIDE;

  public int apply(int left, int right) {
    return switch (this) {
      case PLUS -> left + right;
      case MINUS -> left - right;
      case MULTIPLY -> left * right;
      case DIVIDE -> left / right;
    };
  }
}

package io.github.mbarkley.rollens.eval;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Message;

import java.util.stream.IntStream;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class OperationMapper implements ResultMapper {
  public enum Op {
    PLUS, MINUS, MULTIPLY, DIVIDE;

    public int apply(int left, int right) {
      return switch(this) {
        case PLUS -> left + right;
        case MINUS -> left - right;
        case MULTIPLY -> left * right;
        case DIVIDE -> left / right;
      };
    }
  }

  private final ResultMapper left;
  private final Op operator;
  private final int right;

  @Override
  public int mapResult(Message message, IntStream rawRolls) {
    final int left = this.left.mapResult(message, rawRolls);
    return operator.apply(left, right);
  }
}

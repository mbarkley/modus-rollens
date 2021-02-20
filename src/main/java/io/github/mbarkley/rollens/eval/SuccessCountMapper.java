package io.github.mbarkley.rollens.eval;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.With;
import net.dv8tion.jda.api.entities.Message;

import java.util.stream.IntStream;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@With
public class SuccessCountMapper implements ResultMapper {
  private final int successThreshold;
  private final int failureThreshold;

  @Override
  public int mapResult(Message message, IntStream rawRolls) {

    return rawRolls.map(value -> {
      if (value >= successThreshold) {
        return 1;
      } else if (value <= failureThreshold) {
        return -1;
      } else {
        return 0;
      }
    }).sum();
  }
}

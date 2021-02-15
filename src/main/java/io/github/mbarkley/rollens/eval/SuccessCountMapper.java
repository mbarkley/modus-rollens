package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.format.Formatter;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.With;
import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@With
public class SuccessCountMapper implements ResultMapper {
  private final int successThreshold;
  private final int failureThreshold;

  @Override
  public String mapResult(Message message, Formatter formatter, int[] rawRolls) {
    int sum = Arrays.stream(rawRolls)
                    .map(n -> {
                      if (n >= successThreshold) {
                        return 1;
                      } else if (n <= failureThreshold) {
                        return -1;
                      } else {
                        return 0;
                      }
                    })
                    .sum();

    return formatter.formatResponse(message, rawRolls, sum);
  }
}

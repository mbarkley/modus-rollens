package io.github.mbarkley.rollens.eval;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.With;
import net.dv8tion.jda.api.entities.Message;

import static java.lang.String.format;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@With
public class SuccessCountMapper implements ResultMapper {
  private final int successThreshold;
  private final int failureThreshold;

  @Override
  public String mapResult(Message message, int[] rawRolls) {
    int successes = 0;
    int failures = 0;
    for (int roll : rawRolls) {
      if (roll >= successThreshold) successes++;
      if (roll <= failureThreshold) failures++;
    }

    return format("%d=%d-%d", successes - failures, successes, failures);
  }
}

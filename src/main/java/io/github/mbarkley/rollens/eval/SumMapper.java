package io.github.mbarkley.rollens.eval;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class SumMapper implements ResultMapper {
  @Override
  public String mapResult(Message message, int[] rawRolls) {
    int sum = Arrays.stream(rawRolls).sum();
    return String.valueOf(sum);
  }
}

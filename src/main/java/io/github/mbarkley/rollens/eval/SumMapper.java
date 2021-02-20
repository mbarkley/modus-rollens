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
  public int mapResult(Message message, int[] rawRolls) {
    return Arrays.stream(rawRolls).sum();
  }
}

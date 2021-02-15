package io.github.mbarkley.rollens.eval;

import net.dv8tion.jda.api.entities.Message;

public interface ResultMapper {
  String mapResult(Message message, int[] rawRolls);
}

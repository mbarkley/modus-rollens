package io.github.mbarkley.rollens.eval;

import net.dv8tion.jda.api.entities.Message;

import java.util.stream.IntStream;

public interface ResultMapper {
  int mapResult(Message message, IntStream rawRolls);
}

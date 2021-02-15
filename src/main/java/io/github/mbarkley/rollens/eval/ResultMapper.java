package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.format.Formatter;
import net.dv8tion.jda.api.entities.Message;

public interface ResultMapper {
    String mapResult(Message message, Formatter formatter, int[] rawRolls);
}

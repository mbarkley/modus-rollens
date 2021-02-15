package io.github.mbarkley.rollens.format;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;
import java.util.Optional;

import static java.lang.String.format;

public class Formatter {
  public String formatResponse(Message message, int[] rawRolls, int result) {
    return format("%s roll: `%s`\nResult: %d",
                  Optional.ofNullable(message.getMember())
                          .map(Member::getNickname)
                          .orElseGet(() -> message.getAuthor().getName()),
                  Arrays.toString(rawRolls),
                  result);
  }
}

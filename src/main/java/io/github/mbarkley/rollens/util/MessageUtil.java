package io.github.mbarkley.rollens.util;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MessageUtil {
  private MessageUtil() {}

  @NotNull
  public static String getAuthorDisplayName(Message message) {
    return Optional.ofNullable(message.getMember())
                   .map(Member::getNickname)
                   .orElseGet(() -> message.getAuthor().getName());
  }
}

package io.github.mbarkley.rollens.util;

import io.github.mbarkley.rollens.discord.CommandEvent;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MessageUtil {
  private MessageUtil() {}

  @NotNull
  public static String getAuthorDisplayName(CommandEvent commandEvent) {
    return Optional.ofNullable(commandEvent.getMember())
                   .map(Member::getNickname)
                   .orElseGet(() -> commandEvent.getUser().getName());
  }
}

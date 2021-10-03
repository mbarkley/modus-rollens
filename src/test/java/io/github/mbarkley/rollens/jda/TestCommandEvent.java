package io.github.mbarkley.rollens.jda;

import io.github.mbarkley.rollens.discord.CommandEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class TestCommandEvent implements CommandEvent {
  @Setter
  @Getter
  private Member member;

  @Setter
  @Getter
  private Guild guild;

  private final String content;

  @Override
  public @NotNull String getId() {
    return "test123";
  }

  @Override
  public @NotNull MessageChannel getChannel() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NotNull User getUser() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NotNull String getCommand() {
    return content;
  }

  @Override
  public void reply(String response) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void markIgnored() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isFromGuild() {
    return guild != null;
  }
}

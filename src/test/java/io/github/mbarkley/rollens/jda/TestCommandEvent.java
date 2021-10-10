package io.github.mbarkley.rollens.jda;

import io.github.mbarkley.rollens.discord.CommandEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class TestCommandEvent implements CommandEvent {
  @Setter
  @Getter
  private Member member;

  @Setter
  @Getter
  private Guild guild;

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
  public void reply(@NotNull String response) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void reply(@NotNull Message message, boolean intermediate) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isFromGuild() {
    return guild != null;
  }
}

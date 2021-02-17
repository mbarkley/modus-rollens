package io.github.mbarkley.rollens.jda;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageActivity;
import net.dv8tion.jda.internal.entities.AbstractMessage;
import org.jetbrains.annotations.Nullable;

public class TestMessage extends AbstractMessage {
  @Setter
  @Getter
  private Member member;

  @Setter
  private Guild guild;

  public TestMessage(String content) {
    super(content, "", false);
  }

  @Override
  protected void unsupported() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public MessageActivity getActivity() {
    return null;
  }

  @Override
  public long getIdLong() {
    return 123L;
  }

  @Override
  public boolean isFromGuild() {
    return guild != null;
  }

  public Guild getGuild() {
    if (guild != null) {
      return guild;
    } else {
      throw new IllegalStateException("This message is not sent in a channel");
    }
  }
}

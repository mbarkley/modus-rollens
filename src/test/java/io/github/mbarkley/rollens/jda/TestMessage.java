package io.github.mbarkley.rollens.jda;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageActivity;
import net.dv8tion.jda.internal.entities.AbstractMessage;
import org.jetbrains.annotations.Nullable;

public class TestMessage extends AbstractMessage {
  @Getter
  @Setter
  private Member member;

  @Getter
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
}

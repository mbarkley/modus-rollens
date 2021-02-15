package io.github.mbarkley.rollens.parse;

import io.github.mbarkley.rollens.eval.*;
import net.dv8tion.jda.api.entities.MessageActivity;
import net.dv8tion.jda.internal.entities.AbstractMessage;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ParserTest {
  Parser parser = new Parser();

  @ParameterizedTest(name = "Should parse \"{0}\" as {1}")
  @MethodSource("rolls")
  public void shouldParseRoll(String input, Object result) {
    final TestMessage message = new TestMessage(input);
    final Optional<Command> parsed = parser.parse(message);
    Assertions.assertEquals(Optional.of(result), parsed);
  }

  @ParameterizedTest(name = "Should not parse \"{0}\"")
  @MethodSource("badExpressions")
  public void shouldNotParseBadExpressions(String input) {
    final TestMessage message = new TestMessage(input);
    final Optional<Command> parsed = parser.parse(message);
    Assertions.assertEquals(Optional.empty(), parsed);
  }

  private static Stream<Arguments> rolls() {
    return Stream.of(
        // sum rolls
        arguments("!mr 2d6", new Roll(new BaseRoll(2, 6), List.of(), new SumMapper())),
        arguments("!mr 1d10", new Roll(new BaseRoll(1, 10), List.of(), new SumMapper())),
        arguments("!mr 2D6", new Roll(new BaseRoll(2, 6), List.of(), new SumMapper())),
        arguments("!mr d6", new Roll(new BaseRoll(1, 6), List.of(), new SumMapper())),
        arguments("!mr D6", new Roll(new BaseRoll(1, 6), List.of(), new SumMapper())),
        // success counts
        arguments("!mr 2d6 t6", new Roll(new BaseRoll(2, 6), List.of(), new SuccessCountMapper(6, 0))),
        arguments("!mr 2d6 f1", new Roll(new BaseRoll(2, 6), List.of(), new SuccessCountMapper(Integer.MAX_VALUE, 1))),
        arguments("!mr 2d10 t7", new Roll(new BaseRoll(2, 10), List.of(), new SuccessCountMapper(7, 0))),
        arguments("!mr 2d10 t7 f2", new Roll(new BaseRoll(2, 10), List.of(), new SuccessCountMapper(7, 2))),
        arguments("!mr 2d10 f2 t7", new Roll(new BaseRoll(2, 10), List.of(), new SuccessCountMapper(7, 2))),
        arguments("!mr 2d10 t7 e10 f1", new Roll(new BaseRoll(2, 10), List
            .of(new ExplodingModifier(10, 1)), new SuccessCountMapper(7, 1))),
        // exploding sum
        arguments("!mr 2d6 e6", new Roll(new BaseRoll(2, 6), List.of(new ExplodingModifier(6, 1)), new SumMapper())),
        arguments("!mr 2d6 ie6", new Roll(new BaseRoll(2, 6), List
            .of(new ExplodingModifier(6, Integer.MAX_VALUE)), new SumMapper())),
        // exploding success count
        arguments("!mr 2d10 t7 e10", new Roll(new BaseRoll(2, 10), List
            .of(new ExplodingModifier(10, 1)), new SuccessCountMapper(7, 0))),
        arguments("!mr 2d10 t7 ie10", new Roll(new BaseRoll(2, 10), List
            .of(new ExplodingModifier(10, Integer.MAX_VALUE)), new SuccessCountMapper(7, 0))),
        arguments("!mr 2d10 t7 f2 e10", new Roll(new BaseRoll(2, 10), List
            .of(new ExplodingModifier(10, 1)), new SuccessCountMapper(7, 2))),
        arguments("!mr 2d10 t7 f2 ie10", new Roll(new BaseRoll(2, 10), List
            .of(new ExplodingModifier(10, Integer.MAX_VALUE)), new SuccessCountMapper(7, 2)))
    );
  }

  private static Stream<Arguments> badExpressions() {
    return Stream.of(
        arguments("!foo 2d6"),
        arguments("!mr 2 d6"),
        arguments("!mr 2d 6"),
        arguments("!mr -2d6"),
        arguments("!mr 2d-6"),
        arguments("! mr 2d6")
    );
  }

  static class TestMessage extends AbstractMessage {

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
}

package io.github.mbarkley.rollens.parse;

import io.github.mbarkley.rollens.command.Command;
import io.github.mbarkley.rollens.command.SimpleRoll;
import net.dv8tion.jda.api.entities.MessageActivity;
import net.dv8tion.jda.internal.entities.AbstractMessage;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ParserTest {
    Parser parser = new Parser();

    @ParameterizedTest(name = "Should parse \"{0}\" as {1}")
    @MethodSource("simpleRolls")
    public void shouldParseSimpleRoll(String input, SimpleRoll result) {
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

    public static Stream<Arguments> simpleRolls() {
        return Stream.of(
                arguments("!mr 2d6", new SimpleRoll(2, 6)),
                arguments("!mr 1d10", new SimpleRoll(1, 10)),
                arguments("!mr 2D6", new SimpleRoll(2, 6)),
                arguments("!mr d6", new SimpleRoll(1, 6)),
                arguments("!mr D6", new SimpleRoll(1, 6))
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

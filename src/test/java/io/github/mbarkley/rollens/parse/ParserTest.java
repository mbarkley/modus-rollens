package io.github.mbarkley.rollens.parse;

import io.github.mbarkley.rollens.eval.*;
import io.github.mbarkley.rollens.jda.TestCommandEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ParserTest {
  Parser parser = new Parser();

  @ParameterizedTest(name = "call \"{0}\" as {1}")
  @MethodSource("calls")
  public void should_saved_rolls(String input, Object result) {
    final TestCommandEvent message = new TestCommandEvent(input);
    final Optional<Command> parsed = parser.parse(message.getCommand());
    Assertions.assertEquals(Optional.of(result), parsed);
  }

  @Test
  public void should_parse_help() {
    final TestCommandEvent message = new TestCommandEvent("!mr help");
    final Optional<Command> parsed = parser.parse(message.getCommand());
    Assertions.assertEquals(Optional.of(Help.INSTANCE), parsed);
  }

  @Test
  public void should_parse_list() {
    final TestCommandEvent message = new TestCommandEvent("!mr list");
    final Optional<Command> parsed = parser.parse(message.getCommand());
    Assertions.assertEquals(Optional.of(ListSaved.INSTANCE), parsed);
  }

  @ParameterizedTest(name = "delete \"{0}\" as {1}")
  @MethodSource("deletes")
  public void should_parse_delete(String input, Object result) {
    final TestCommandEvent message = new TestCommandEvent(input);
    final Optional<Command> parsed = parser.parse(message.getCommand());
    Assertions.assertEquals(Optional.of(result), parsed);
  }

  @ParameterizedTest(name = "save \"{0}\" as {1}")
  @MethodSource("saves")
  public void should_parse_save(String input, Object result) {
    final TestCommandEvent message = new TestCommandEvent(input);
    final Optional<Command> parsed = parser.parse(message.getCommand());
    Assertions.assertEquals(Optional.of(result), parsed);
  }

  @ParameterizedTest(name = "bad expression \"{0}\"")
  @MethodSource("badExpressions")
  public void should_not_parse_bad_expressions(String input) {
    final TestCommandEvent message = new TestCommandEvent(input);
    final Optional<Command> parsed = parser.parse(message.getCommand());
    Assertions.assertEquals(Optional.empty(), parsed);
  }

  private static Stream<Arguments> calls() {
    return Stream.of(
        arguments("!mr foo", new Invoke("foo", new int[0])),
        arguments("!mr foo 1337 13", new Invoke("foo", new int[] {1337, 13}))
    );
  }

  private static Stream<Arguments> deletes() {
    return Stream.of(
        arguments("!mr delete foo 3", new Delete("foo", 3))
    );
  }

  private static Stream<Arguments> saves() {
    return Stream.of(
        arguments("!mr save (foo a b c) = 2d6", new Save("foo", List.of("a", "b", "c"), "2d6")),
        arguments("!mr save (foo a b c) = {a}d{b} t{c} f1", new Save("foo", List.of("a", "b", "c"), "{a}d{b} t{c} f1")),
        arguments("!mr save (foo a b c) = {a}d{b} + {c}", new Save("foo", List.of("a", "b", "c"), "{a}d{b} + {c}"))
    );
  }

  private static Stream<Arguments> badExpressions() {
    return Stream.of(
        arguments("!foo 2d6"),
        arguments("!mr 2d 6"),
        arguments("!mr -2d6"),
        arguments("!mr 2d-6"),
        arguments("! mr 2d6"),
        arguments("!mr notsave (foo a b c) = 2d6"),
        arguments("!mr {n}d6"),
        arguments("!mr 2d6 e4 e4"),
        arguments("!mr 2d6 t4 t4"),
        arguments("!mr 2d6 e4 ie4"),
        arguments("!mr 2d6 r4 ir4")
    );
  }

}

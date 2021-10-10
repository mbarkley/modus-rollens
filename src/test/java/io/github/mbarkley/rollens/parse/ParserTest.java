package io.github.mbarkley.rollens.parse;

import io.github.mbarkley.rollens.eval.*;
import io.github.mbarkley.rollens.eval.Command.StringOutput;
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
    final Optional<Command<?>> parsed = parser.parse(input);
    Assertions.assertEquals(Optional.of(result), parsed);
  }

  @Test
  public void should_parse_help() {
    final Optional<Command<?>> parsed = parser.parse("!mr help");
    Assertions.assertEquals(Optional.of(Help.INSTANCE), parsed);
  }

  @Test
  public void should_parse_list() {
    final Optional<Command<?>> parsed = parser.parse("!mr list");
    Assertions.assertEquals(Optional.of(ListSaved.INSTANCE), parsed);
  }

  @ParameterizedTest(name = "delete \"{0}\" as {1}")
  @MethodSource("deletes")
  public void should_parse_delete(String input, Object result) {
    final Optional<Command<?>> parsed = parser.parse(input);
    Assertions.assertEquals(Optional.of(result), parsed);
  }

  @ParameterizedTest(name = "save \"{0}\" as {1}")
  @MethodSource("saves")
  public void should_parse_save(String input, Object result) {
    final Optional<Command<?>> parsed = parser.parse(input);
    Assertions.assertEquals(Optional.of(result), parsed);
  }

  @ParameterizedTest(name = "select \"{0}\" as {1}")
  @MethodSource("selects")
  public void should_parse_select(String input, Object result) {
    final Optional<Command<?>> parsed = parser.parse(input);
    Assertions.assertEquals(Optional.of(result), parsed);
  }

  @ParameterizedTest(name = "annotate \"{0}\" as {1}")
  @MethodSource("annotates")
  public void should_parse_annotate(String input, Object result) {
    final Optional<Command<?>> parsed = parser.parse(input);
    Assertions.assertEquals(Optional.of(result), parsed);
  }

  @ParameterizedTest(name = "bad expression \"{0}\"")
  @MethodSource("badExpressions")
  public void should_not_parse_bad_expressions(String input) {
    final Optional<Command<?>> parsed = parser.parse(input);
    Assertions.assertEquals(Optional.empty(), parsed);
  }

  private record CommandTestCase(String expression, Command<?> command) {
    static CommandTestCase of(String expression, Command<?> command) {
      return new CommandTestCase(expression, command);
    }
  }

  private static Stream<Arguments> calls() {
    Command<StringOutput> command = new Invoke("foo", new int[0]);
    return Stream.of(
        CommandTestCase.of("foo", command),
        CommandTestCase.of("foo 1337 13", new Invoke("foo", new int[]{1337, 13}))
    ).flatMap(testCase -> Stream.of(
        arguments("!mr " + testCase.expression(), testCase.command()),
        arguments("!mr roll " + testCase.expression(), testCase.command()),
        arguments("/mr " + testCase.expression(), testCase.command()),
        arguments("/mr roll " + testCase.expression(), testCase.command())
    ));
  }

  private static Stream<Arguments> deletes() {
    return Stream.of(
        CommandTestCase.of("delete foo 3", new Delete("foo", 3))
    ).flatMap(testCase -> Stream.of(
        arguments("!mr " + testCase.expression(), testCase.command()),
        arguments("/mr " + testCase.expression(), testCase.command())
    ));
  }

  private static Stream<Arguments> saves() {
    return Stream.of(
        CommandTestCase.of("save (foo a b c) = 2d6", new Save("foo", List.of("a", "b", "c"), "2d6")),
        CommandTestCase.of("save foo a b c = 2d6", new Save("foo", List.of("a", "b", "c"), "2d6")),
        CommandTestCase
            .of("save (foo a b c) = {a}d{b} t{c} f1", new Save("foo", List.of("a", "b", "c"), "{a}d{b} t{c} f1")),
        CommandTestCase
            .of("save foo a b c = {a}d{b} t{c} f1", new Save("foo", List.of("a", "b", "c"), "{a}d{b} t{c} f1")),
        CommandTestCase
            .of("save (foo a b c) = {a}d{b} + {c}", new Save("foo", List.of("a", "b", "c"), "{a}d{b} + {c}")),
        CommandTestCase
            .of("save foo a b c = {a}d{b} + {c}", new Save("foo", List.of("a", "b", "c"), "{a}d{b} + {c}"))
    ).flatMap(testCase -> Stream.of(
        arguments("!mr " + testCase.expression(), testCase.command()),
        arguments("/mr " + testCase.expression(), testCase.command())
    ));
  }

  private static Stream<Arguments> annotates() {
    return Stream.of(
        CommandTestCase.of("annotate foo ! Some text", new Annotate("foo", null, null, "Some text")),
        CommandTestCase.of("annotate foo 1 ! Some text", new Annotate("foo", 1, null, "Some text")),
        CommandTestCase.of("annotate foo arg ! Some text", new Annotate("foo", null, "arg", "Some text")),
        CommandTestCase.of("annotate foo 2 arg ! Some text", new Annotate("foo", 2, "arg", "Some text"))
    ).flatMap(testCase -> Stream.of(
        arguments("!mr " + testCase.expression(), testCase.command()),
        arguments("/mr " + testCase.expression(), testCase.command())
    ));
  }

  private static Stream<Arguments> selects() {
    return Stream.of(
        CommandTestCase.of("select", new SelectSaved(null, null)),
        CommandTestCase.of("select foo", new SelectSaved(new DeclarationLHS("foo", List.of()), new int[0])),
        CommandTestCase.of("select foo a b", new SelectSaved(new DeclarationLHS("foo", List.of("a", "b")), new int[0])),
        CommandTestCase.of("select foo a b 1", new SelectSaved(new DeclarationLHS("foo", List.of("a", "b")), new int[] { 1 })),
        CommandTestCase.of("select foo a b 1 2", new SelectSaved(new DeclarationLHS("foo", List.of("a", "b")), new int[] { 1, 2}))
    ).flatMap(testCase -> Stream.of(
        arguments("!mr " + testCase.expression(), testCase.command()),
        arguments("/mr " + testCase.expression(), testCase.command())
    ));
  }

  private static Stream<Arguments> badExpressions() {
    return Stream.concat(
        Stream.of(
            CommandTestCase.of("2d 6", null),
            CommandTestCase.of("-2d6", null),
            CommandTestCase.of("2d-6", null),
            CommandTestCase.of("notsave (foo a b c) = 2d6", null),
            CommandTestCase.of("{n}d6", null),
            CommandTestCase.of("2d6 e4 e4", null),
            CommandTestCase.of("2d6 t4 t4", null),
            CommandTestCase.of("2d6 e4 ie4", null),
            CommandTestCase.of("2d6 r4 ir4", null)
        ).flatMap(testCase -> Stream.of(
            arguments("!mr " + testCase.expression(), testCase.command()),
            arguments("/mr " + testCase.expression(), testCase.command())
        )),
        Stream.of(
            arguments("! mr 2d6", null),
            arguments("/ mr 2d6", null),
            arguments("!foo 2d6", null)
        )
    );
  }

}

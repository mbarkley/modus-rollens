package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.db.DbUtil;
import io.github.mbarkley.rollens.eval.Command.StringOutput;
import io.github.mbarkley.rollens.jda.TestCommandEvent;
import io.github.mbarkley.rollens.jda.TestGuild;
import io.github.mbarkley.rollens.jda.TestMember;
import io.github.mbarkley.rollens.parse.Parser;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EvalTest {
  TestCommandEvent testCommandEvent;
  Jdbi jdbi;
  ExecutorService executorService;
  Parser parser;

  @BeforeEach
  public void setup() throws IOException {
    testCommandEvent = new TestCommandEvent("");
    final TestMember member = new TestMember();
    member.setNickname("Test User");
    testCommandEvent.setMember(member);
    testCommandEvent.setGuild(new TestGuild(123));

    final File dbFile = File.createTempFile("modus-rollens-", ".db");
    dbFile.deleteOnExit();
    jdbi = DbUtil.initDb(dbFile.getAbsolutePath());
    executorService = Executors.newFixedThreadPool(10);

    parser = new Parser();
  }

  @AfterEach
  public void tearDown() {
    executorService.shutdownNow();
  }

  @Test
  public void deleting_after_save_in_guild_removes_from_list() throws InterruptedException, ExecutionException, TimeoutException {
    final List<Save> saves = List.of(
        new Save("foo",
                 List.of(),
                 "2d6"),
        new Save("foo",
                 List.of("a"),
                 "{a}d6"),
        new Save("foo",
                 List.of("a", "b", "c"),
                 "{a}d{b} t{c}")
    );
    Command.ExecutionContext context = new Command.ExecutionContext(executorService, jdbi, parser, () -> new Random(1337), testCommandEvent);

    // do saves
    final CompletableFuture[] futures = saves.stream()
                                             .map(save -> save.execute(context))
                                             .toArray(CompletableFuture[]::new);
    CompletableFuture.allOf(futures).get(5, TimeUnit.SECONDS);

    // validate list
    final StringOutput loaded = new ListSaved().execute(context).get(1, TimeUnit.SECONDS);
    Assertions.assertEquals("""
                                __Saved Rolls__
                                `(foo) = 2d6`
                                `(foo a) = {a}d6`
                                `(foo a b c) = {a}d{b} t{c}`""",
                            loaded.value());

    // do delete
    final StringOutput deleted = new Delete("foo", 1).execute(context).get(1, TimeUnit.SECONDS);
    Assertions.assertEquals("Deleted `(foo a) = {a}d6`", deleted.value());

    // validate list
    final StringOutput newList = new ListSaved().execute(context).get(1, TimeUnit.SECONDS);
    Assertions.assertEquals("""
                                __Saved Rolls__
                                `(foo) = 2d6`
                                `(foo a b c) = {a}d{b} t{c}`""",
                            newList.value());
  }

  @Test
  public void save_and_list_in_guild_should_show_all_saved() throws InterruptedException, ExecutionException, TimeoutException {
    final List<Save> saves = List.of(
        new Save("foo0",
                 List.of(),
                 "2d6"),
        new Save("foo1",
                 List.of("a"),
                 "{a}d6"),
        new Save("foo3",
                 List.of("a", "b", "c"),
                 "{a}d{b} t{c}")
    );
    Command.ExecutionContext context = new Command.ExecutionContext(executorService, jdbi, parser, () -> new Random(1337), testCommandEvent);
    final CompletableFuture[] futures = saves.stream()
                                             .map(save -> save.execute(context))
                                             .toArray(CompletableFuture[]::new);
    CompletableFuture.allOf(futures).get(5, TimeUnit.SECONDS);

    final StringOutput loaded = new ListSaved().execute(context).get(1, TimeUnit.SECONDS);

    Assertions.assertEquals("""
                                __Saved Rolls__
                                `(foo0) = 2d6`
                                `(foo1 a) = {a}d6`
                                `(foo3 a b c) = {a}d{b} t{c}`""",
                            loaded.value());
  }

  @Test
  public void save_and_and_select_should_show_all_saved_in_guild() throws InterruptedException, ExecutionException, TimeoutException {
    final List<Save> saves = List.of(
        new Save("foo0",
                 List.of(),
                 "2d6"),
        new Save("foo1",
                 List.of("a"),
                 "{a}d6"),
        new Save("foo3",
                 List.of("a", "b", "c"),
                 "{a}d{b} t{c}")
    );
    Command.ExecutionContext context = new Command.ExecutionContext(executorService, jdbi, parser, () -> new Random(1337), testCommandEvent);
    final CompletableFuture[] futures = saves.stream()
                                             .map(save -> save.execute(context))
                                             .toArray(CompletableFuture[]::new);
    CompletableFuture.allOf(futures).get(5, TimeUnit.SECONDS);

    final Command.CommandOutput loaded = new SelectSaved(null, null).execute(context).get(1, TimeUnit.SECONDS);

    if (loaded instanceof Command.CommandSelectOutput selectOutput) {
      Assertions.assertEquals(
          List.of(
            new Command.Option("foo0", "!mr select foo0"),
            new Command.Option("foo1 a", "!mr select foo1 a"),
            new Command.Option("foo3 a b c", "!mr select foo3 a b c")
          ),
          selectOutput.options()
      );
    } else {
      Assertions.fail("Expected %s but got %s".formatted(Command.CommandSelectOutput.class.getSimpleName(), loaded));
    }
  }

  @Test
  public void save_and_select_should_return_prompt_for_no_arg_function() throws InterruptedException, ExecutionException, TimeoutException {
    final Save save = new Save("foo0",
                               List.of(),
                               "2d6");
    Command.ExecutionContext context = new Command.ExecutionContext(executorService, jdbi, parser, () -> new Random(1337), testCommandEvent);
    save.execute(context).get(5, TimeUnit.SECONDS);

    final Command.CommandOutput loaded = new SelectSaved(new DeclarationLHS("foo0", List.of()), new int[0])
        .execute(context).get(1, TimeUnit.SECONDS);

    if (loaded instanceof StringOutput stringOutput) {
      Assertions.assertEquals("""
                                  Evaluating: `2d6`
                                  Test User roll: `[2, 1]`
                                  Result: 3""",
                              stringOutput.value());
    } else {
      Assertions.fail("Expected %s but got %s".formatted(Command.StringOutput.class.getSimpleName(), loaded));
    }
  }

  @Test
  public void save_and_select_should_return_prompt_until_all_args_defined() throws InterruptedException, ExecutionException, TimeoutException {
    final Save save = new Save("foo2",
                               List.of("a", "b"),
                               "{a}d{b}");
    Command.ExecutionContext context = new Command.ExecutionContext(executorService, jdbi, parser, () -> new Random(1337), testCommandEvent);
    save.execute(context).get(5, TimeUnit.SECONDS);

    {
      final Command.CommandOutput loaded = new SelectSaved(new DeclarationLHS("foo2", List.of("a", "b")), new int[0])
          .execute(context).get(1, TimeUnit.SECONDS);

      if (loaded instanceof Command.ArgumentSelectOutput selectOutput) {
        Assertions.assertEquals("!mr select foo2 a b", selectOutput.selectExpression());
        Assertions.assertEquals("foo2", selectOutput.name());
        Assertions.assertEquals(List.of("a", "b"), selectOutput.parameters());
      } else {
        Assertions.fail("Expected %s but got %s".formatted(Command.ArgumentSelectOutput.class.getSimpleName(), loaded));
      }
    }

    {
      final Command.CommandOutput loaded = new SelectSaved(new DeclarationLHS("foo2", List.of("a", "b")), new int[]{2})
          .execute(context).get(1, TimeUnit.SECONDS);

      if (loaded instanceof Command.ArgumentSelectOutput selectOutput) {
        Assertions.assertEquals("!mr select foo2 a b 2", selectOutput.selectExpression());
        Assertions.assertEquals("foo2", selectOutput.name());
        Assertions.assertEquals(List.of("a", "b"), selectOutput.parameters());
      } else {
        Assertions.fail("Expected %s but got %s".formatted(Command.ArgumentSelectOutput.class.getSimpleName(), loaded));
      }
    }

    {
      final Command.CommandOutput loaded = new SelectSaved(new DeclarationLHS("foo2", List.of("a", "b")), new int[]{2, 6})
          .execute(context).get(1, TimeUnit.SECONDS);

      if (loaded instanceof StringOutput stringOutput) {
        Assertions.assertEquals("""
                                    Evaluating: `2d6`
                                    Test User roll: `[2, 1]`
                                    Result: 3""",
                                stringOutput.value());
      } else {
        Assertions.fail("Expected %s but got %s".formatted(Command.StringOutput.class.getSimpleName(), loaded));
      }
    }
  }

  @Test
  public void saving_in_guild_should_overwrite() throws InterruptedException, ExecutionException, TimeoutException {
    final Save firstSave = new Save("foo1",
                                    List.of("a"),
                                    "{a}d6");
    final Save secondSave = new Save("foo1",
                                     List.of("a"),
                                     "{a}d10");
    Command.ExecutionContext context = new Command.ExecutionContext(executorService, jdbi, parser, () -> new Random(1337), testCommandEvent);
    firstSave.execute(context)
             .thenCompose(output -> secondSave.execute(context))
             .get(5, TimeUnit.SECONDS);

    final StringOutput loaded = new ListSaved().execute(context).get(1, TimeUnit.SECONDS);

    Assertions.assertEquals("""
                                __Saved Rolls__
                                `(foo1 a) = {a}d10`""",
                            loaded.value());
  }

  @Test
  public void list_should_show_only_saved_from_relevant_guild() throws InterruptedException, ExecutionException, TimeoutException {
    // Setup
    save_and_list_in_guild_should_show_all_saved();
    testCommandEvent.setGuild(new TestGuild(321));
    Command.ExecutionContext context = new Command.ExecutionContext(executorService, jdbi, parser, () -> new Random(1337), testCommandEvent);
    final StringOutput loaded = new ListSaved().execute(context).get(1, TimeUnit.SECONDS);

    Assertions.assertEquals("""
                                __Saved Rolls__
                                No saved rolls""",
                            loaded.value());
  }

  @MethodSource("guildOnlyCommands")
  @ParameterizedTest(name = "invoking \"{0}\" should have failure message \"{1}\"")
  public void should_invoke_saved_roll_in_guild(Command<StringOutput> command, String result) throws InterruptedException, ExecutionException, TimeoutException {
    testCommandEvent.setGuild(null);
    final Command.ExecutionContext context = new Command.ExecutionContext(executorService, jdbi, parser, () -> new Random(1337), testCommandEvent);
    final StringOutput observed = command.execute(context).get(1, TimeUnit.SECONDS);
    Assertions.assertEquals(result, observed.value());
  }

  @MethodSource("invocations")
  @ParameterizedTest(name = "invoking \"{2}\" should have result \"{3}\"")
  public void should_invoke_saved_roll_in_guild(Random rand, Save save, Invoke invoke, String result) throws InterruptedException, ExecutionException, TimeoutException {
    final Command.ExecutionContext context = new Command.ExecutionContext(executorService, jdbi, parser, () -> rand, testCommandEvent);
    // setup
    save.execute(context).get(1, TimeUnit.SECONDS);
    // test
    final StringOutput invoked = invoke.execute(context).get(1, TimeUnit.SECONDS);
    Assertions.assertEquals(result, invoked.value());
  }

  private Stream<Arguments> invocations() {
    return Stream.of(
        arguments(
            new Random(1337),
            new Save("foo", List.of(), "2d6"),
            new Invoke("foo", new int[0]),
            """
                Evaluating: `2d6`
                Test User roll: `[2, 1]`
                Result: 3"""
        ),
        arguments(
            new Random(1337),
            new Save("foo", List.of("n", "m"), "{n}d{m}"),
            new Invoke("foo", new int[]{5, 10}),
            """
                Evaluating: `5d10`
                Test User roll: `[2, 5, 10, 3, 10]`
                Result: 30"""
        ),
        arguments(
            new Random(1337),
            new Save("foo", List.of("n"), "{n}d{n}"),
            new Invoke("foo", new int[]{10}),
            """
                Evaluating: `10d10`
                Test User roll: `[2, 5, 10, 3, 10, 9, 4, 5, 8, 8]`
                Result: 64"""
        ),
        arguments(
            new Random(1337),
            new Save("foo", List.of("n", "m", "t"), "{n}d{m} t{t} f1"),
            new Invoke("foo", new int[]{5, 10, 6}),
            """
                Evaluating: `5d10 t6 f1`
                Test User roll: `[2, 5, 10, 3, 10]`
                Result: 2"""
        ),
        arguments(
            new Random(1337),
            new Save("foo", List.of("n", "m", "a"), "{n}d{m} + {a}"),
            new Invoke("foo", new int[]{5, 10, 6}),
            """
                Evaluating: `5d10 + 6`
                Test User roll: `[2, 5, 10, 3, 10]`
                Result: 36"""
        )
    );
  }

  private static Stream<Arguments> guildOnlyCommands() {
    return Stream.of(
        arguments(new Save("foo", List.of(), "2d6"), "Cannot save rolls in direct messages"),
        arguments(new ListSaved(), "Cannot save and list rolls in direct messages"),
        arguments(new Delete("foo", 0), "Cannot delete rolls in direct messages"),
        arguments(new Invoke("foo", new int[0]), "Cannot invoke saved rolls from direct messages")
    );
  }
}

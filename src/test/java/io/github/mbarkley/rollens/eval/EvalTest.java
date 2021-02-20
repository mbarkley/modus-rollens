package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.db.DbUtil;
import io.github.mbarkley.rollens.jda.TestGuild;
import io.github.mbarkley.rollens.jda.TestMember;
import io.github.mbarkley.rollens.jda.TestMessage;
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
  TestMessage testMessage;
  Jdbi jdbi;
  ExecutorService executorService;
  Parser parser;

  @BeforeEach
  public void setup() throws IOException {
    testMessage = new TestMessage("");
    final TestMember member = new TestMember();
    member.setNickname("Test User");
    testMessage.setMember(member);
    testMessage.setGuild(new TestGuild(123));

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
    Command.ExecutionContext context = new Command.ExecutionContext(executorService, jdbi, parser, new Random(1337), testMessage);

    // do saves
    final CompletableFuture[] futures = saves.stream()
                                             .map(save -> save.execute(context))
                                             .toArray(CompletableFuture[]::new);
    CompletableFuture.allOf(futures).get(5, TimeUnit.SECONDS);

    // validate list
    final String loaded = new ListSaved().execute(context).get(1, TimeUnit.SECONDS);
    Assertions.assertEquals("""
                                __Saved Rolls__
                                `(foo) = 2d6`
                                `(foo a) = {a}d6`
                                `(foo a b c) = {a}d{b} t{c}`""",
                            loaded);

    // do delete
    final String deleted = new Delete("foo", 1).execute(context).get(1, TimeUnit.SECONDS);
    Assertions.assertEquals("Deleted `(foo a) = {a}d6`", deleted);

    // validate list
    final String newList = new ListSaved().execute(context).get(1, TimeUnit.SECONDS);
    Assertions.assertEquals("""
                                __Saved Rolls__
                                `(foo) = 2d6`
                                `(foo a b c) = {a}d{b} t{c}`""",
                            newList);
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
    Command.ExecutionContext context = new Command.ExecutionContext(executorService, jdbi, parser, new Random(1337), testMessage);
    final CompletableFuture[] futures = saves.stream()
                                             .map(save -> save.execute(context))
                                             .toArray(CompletableFuture[]::new);
    CompletableFuture.allOf(futures).get(5, TimeUnit.SECONDS);

    final String loaded = new ListSaved().execute(context).get(1, TimeUnit.SECONDS);

    Assertions.assertEquals("""
                                __Saved Rolls__
                                `(foo0) = 2d6`
                                `(foo1 a) = {a}d6`
                                `(foo3 a b c) = {a}d{b} t{c}`""",
                            loaded);
  }

  @Test
  public void saving_in_guild_should_overwrite() throws InterruptedException, ExecutionException, TimeoutException {
    final Save firstSave = new Save("foo1",
                             List.of("a"),
                             "{a}d6");
    final Save secondSave = new Save("foo1",
                             List.of("a"),
                             "{a}d10");
    Command.ExecutionContext context = new Command.ExecutionContext(executorService, jdbi, parser, new Random(1337), testMessage);
    firstSave.execute(context)
             .thenCompose(output -> secondSave.execute(context))
             .get(5, TimeUnit.SECONDS);

    final String loaded = new ListSaved().execute(context).get(1, TimeUnit.SECONDS);

    Assertions.assertEquals("""
                                __Saved Rolls__
                                `(foo1 a) = {a}d10`""",
                            loaded);
  }

  @Test
  public void list_should_show_only_saved_from_relevant_guild() throws InterruptedException, ExecutionException, TimeoutException {
    // Setup
    save_and_list_in_guild_should_show_all_saved();
    testMessage.setGuild(new TestGuild(321));
    Command.ExecutionContext context = new Command.ExecutionContext(executorService, jdbi, parser, new Random(1337), testMessage);
    final String loaded = new ListSaved().execute(context).get(1, TimeUnit.SECONDS);

    Assertions.assertEquals("""
                                __Saved Rolls__
                                No saved rolls""",
                            loaded);
  }

  @MethodSource("guildOnlyCommands")
  @ParameterizedTest(name = "invoking \"{0}\" should have failure message \"{1}\"")
  public void should_invoke_saved_roll_in_guild(Command command, String result) throws InterruptedException, ExecutionException, TimeoutException {
    testMessage.setGuild(null);
    final Command.ExecutionContext context = new Command.ExecutionContext(executorService, jdbi, parser, new Random(1337), testMessage);
    final String observed = command.execute(context).get(1, TimeUnit.SECONDS);
    Assertions.assertEquals(result, observed);
  }

  @MethodSource("invocations")
  @ParameterizedTest(name = "invoking \"{2}\" should have result \"{3}\"")
  public void should_invoke_saved_roll_in_guild(Random rand, Save save, Invoke invoke, String result) throws InterruptedException, ExecutionException, TimeoutException {
    final Command.ExecutionContext context = new Command.ExecutionContext(executorService, jdbi, parser, rand, testMessage);
    // setup
    save.execute(context).get(1, TimeUnit.SECONDS);
    // test
    final String invoked = invoke.execute(context).get(1, TimeUnit.SECONDS);
    Assertions.assertEquals(result, invoked);
  }

  @MethodSource("correctResults")
  @ParameterizedTest(name = "roll \"{1}\" should have result \"{2}\"")
  public void roll_in_direct_message_with_fixed_seed_should_give_correct_result(Random rand, RollCommand rollCommand, String result) throws ExecutionException, InterruptedException {
    testMessage.setGuild(null);
    final CompletableFuture<String> executed = rollCommand
        .execute(new Command.ExecutionContext(executorService, jdbi, parser, rand, testMessage));
    Assertions.assertTrue(executed.isDone(), "Returned future is not complete");
    final String observed = executed.get();
    Assertions.assertEquals(result, observed);
  }

  @MethodSource("correctResults")
  @ParameterizedTest(name = "roll \"{1}\" should have result \"{2}\"")
  public void roll_in_guild_with_fixed_seed_should_give_correct_result(Random rand, RollCommand rollCommand, String result) throws ExecutionException, InterruptedException {
    final CompletableFuture<String> executed = rollCommand
        .execute(new Command.ExecutionContext(executorService, jdbi, parser, rand, testMessage));
    Assertions.assertTrue(executed.isDone(), "Returned future is not complete");
    final String observed = executed.get();
    Assertions.assertEquals(result, observed);
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
            new Invoke("foo", new int[] {5, 10}),
            """
                Evaluating: `5d10`
                Test User roll: `[2, 5, 10, 3, 10]`
                Result: 30"""
        ),
        arguments(
            new Random(1337),
            new Save("foo", List.of("n"), "{n}d{n}"),
            new Invoke("foo", new int[] {10}),
            """
                Evaluating: `10d10`
                Test User roll: `[2, 5, 10, 3, 10, 9, 4, 5, 8, 8]`
                Result: 64"""
        ),
        arguments(
            new Random(1337),
            new Save("foo", List.of("n", "m", "t"), "{n}d{m} t{t} f1"),
            new Invoke("foo", new int[] {5, 10, 6}),
            """
                Evaluating: `5d10 t6 f1`
                Test User roll: `[2, 5, 10, 3, 10]`
                Result: 2"""
        ),
        arguments(
            new Random(1337),
            new Save("foo", List.of("n", "m", "a"), "{n}d{m} + {a}"),
            new Invoke("foo", new int[] {5, 10, 6}),
            """
                Evaluating: `5d10 + 6`
                Test User roll: `[2, 5, 10, 3, 10]`
                Result: 36"""
        )
    );
  }

  private static Stream<Arguments> correctResults() {
    return Stream.of(
        // basic roll
        arguments(new Random(1337),
                  new RollCommand(
                      new DicePool(new UniformDicePool(2, 6)),
                      List.of(),
                      new SumMapper()
                  ),
                  """
                      Test User roll: `[2, 1]`
                      Result: 3"""
        ),
        arguments(new Random(1337),
                  new RollCommand(
                      new DicePool(
                          new UniformDicePool(2, 6),
                          new UniformDicePool(3, 4)
                      ),
                      List.of(),
                      new SumMapper()
                  ),
                  """
                      Test User roll: `[2, 1, 3, 4, 4]`
                      Result: 14"""
        ),
        // Operators
        arguments(new Random(1337),
                  new RollCommand(
                      new DicePool(new UniformDicePool(2, 6)),
                      List.of(),
                      new OperationMapper(
                          new SumMapper(),
                          OperationMapper.Op.PLUS,
                          2
                      )
                  ),
                  """
                      Test User roll: `[2, 1]`
                      Result: 5"""
        ),
        arguments(new Random(1337),
                  new RollCommand(
                      new DicePool(new UniformDicePool(2, 6)),
                      List.of(),
                      new OperationMapper(
                          new SumMapper(),
                          OperationMapper.Op.MINUS,
                          2
                      )
                  ),
                  """
                      Test User roll: `[2, 1]`
                      Result: 1"""
        ),
        arguments(new Random(1337),
                  new RollCommand(
                      new DicePool(new UniformDicePool(2, 6)),
                      List.of(),
                      new OperationMapper(
                          new SumMapper(),
                          OperationMapper.Op.MULTIPLY,
                          2
                      )
                  ),
                  """
                      Test User roll: `[2, 1]`
                      Result: 6"""
        ),
        arguments(new Random(1337),
                  new RollCommand(
                      new DicePool(new UniformDicePool(2, 6)),
                      List.of(),
                      new OperationMapper(
                          new SumMapper(),
                          OperationMapper.Op.DIVIDE,
                          2
                      )
                  ),
                  """
                      Test User roll: `[2, 1]`
                      Result: 1"""
        ),
        arguments(new Random(1337),
                  new RollCommand(
                      new DicePool(new UniformDicePool(2, 6)),
                      List.of(),
                      new OperationMapper(
                          new OperationMapper(
                              new SumMapper(),
                              OperationMapper.Op.PLUS,
                              3
                          ),
                          OperationMapper.Op.DIVIDE,
                          2
                      )
                  ),
                  """
                      Test User roll: `[2, 1]`
                      Result: 3"""
        ),
        // success count
        arguments(new Random(1337),
                  new RollCommand(
                      new DicePool(new UniformDicePool(10, 10)),
                      List.of(),
                      new SuccessCountMapper(6, 2)
                  ),
                  """
                      Test User roll: `[2, 5, 10, 3, 10, 9, 4, 5, 8, 8]`
                      Result: 4"""
        ),
        // exploding sums
        arguments(new Random(1337),
                  new RollCommand(
                      new DicePool(new UniformDicePool(10, 10)),
                      List.of(
                          new ExplodingModifier(10, 1)
                      ),
                      new SumMapper()
                  ),
                  """
                      Test User roll: `[2, 5, 10, 3, 10, 9, 4, 5, 8, 8]` `[7, 4]`
                      Result: 75"""
        ),
        arguments(new Random(1337),
                  new RollCommand(
                      new DicePool(new UniformDicePool(10, 10)),
                      List.of(
                          new ExplodingModifier(7, Integer.MAX_VALUE)
                      ),
                      new SumMapper()
                  ),
                  """
                      Test User roll: `[2, 5, 10, 3, 10, 9, 4, 5, 8, 8]` `[7, 4, 9, 1, 4, 1, 1]`
                      Result: 91"""
        ),
        arguments(new Random(1337),
                  new RollCommand(
                      new DicePool(new UniformDicePool(10, 10)),
                      List.of(
                          new ExplodingModifier(7, Integer.MAX_VALUE)
                      ),
                      new SuccessCountMapper(7, 1)
                  ),
                  """
                      Test User roll: `[2, 5, 10, 3, 10, 9, 4, 5, 8, 8]` `[7, 4, 9, 1, 4, 1, 1]`
                      Result: 4"""
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

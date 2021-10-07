package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.db.SavedRoll;
import io.github.mbarkley.rollens.db.SavedRollsDao;
import io.github.mbarkley.rollens.eval.Command.CommandOutput;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jdbi.v3.core.Handle;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class SelectSaved implements Command<CommandOutput> {

  private final DeclarationLHS declarationLHS;
  private final int[] arguments;

  @Override
  public CompletableFuture<? extends CommandOutput> execute(ExecutionContext context) {
    if (context.commandEvent().isFromGuild()) {
      if (declarationLHS == null) {
        return doExpressionSelect(context);
      } else if (declarationLHS.parameters().size() > arguments.length) {
        return doArgSelect(declarationLHS, arguments);
      } else if (declarationLHS.parameters().size() == arguments.length) {
        return new Invoke(declarationLHS.name(), arguments).execute(context);
      } else {
        throw new IllegalStateException("Cannot do select with more arguments than parameters!");
      }
    } else {
      return completedFuture(new StringOutput("Cannot save and list rolls in direct messages"));
    }
  }

  @NotNull
  private static CompletableFuture<CommandOutput> doArgSelect(
      @NotNull DeclarationLHS declarationLHS,
      int[] arguments) {
    assert declarationLHS.parameters().size() > arguments.length;
    final String nextParam = declarationLHS.parameters().get(arguments.length);

    final String parameterList = String.join(" ", declarationLHS.parameters());
    final String argList = Arrays.stream(arguments)
                                 .mapToObj(String::valueOf)
                                 .collect(Collectors.joining(" "));
    return completedFuture(
        new ArgumentSelectOutput(
            "Select `%s` for `%s %s`".formatted(nextParam, declarationLHS.name(), parameterList),
            declarationLHS.name(),
            declarationLHS.parameters(),
            "!mr select %s %s %s".formatted(declarationLHS.name(), parameterList, argList)
        )
    );
  }

  @NotNull
  private CompletableFuture<CommandOutput> doExpressionSelect(ExecutionContext context) {
    return CompletableFuture.supplyAsync(() -> {
      try (Handle handle = context.jdbi().open()) {
        long guildId = context.commandEvent().getGuild().getIdLong();
        final List<SavedRoll> savedRolls = handle.attach(SavedRollsDao.class)
                                                 .findByGuild(guildId);
        savedRolls.sort(Comparator.comparing(SavedRoll::getRollName));
        final List<Option> options =
            savedRolls.stream()
                      .map(sr -> new Option(sr.toLHS(), "!mr select %s".formatted(sr.toLHS())))
                      .toList();

        return new CommandSelectOutput("Select a roll", options);
      }
    }, context.executorService());
  }
}

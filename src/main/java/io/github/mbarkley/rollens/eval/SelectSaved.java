package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.db.AnnotatedSavedRoll;
import io.github.mbarkley.rollens.db.SavedAnnotation;
import io.github.mbarkley.rollens.db.SavedRollsDao;
import io.github.mbarkley.rollens.eval.Command.CommandOutput;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return doArgSelect(context, declarationLHS, arguments);
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
  private static CompletableFuture<ArgumentSelectOutput> doArgSelect(
      ExecutionContext context,
      @NotNull DeclarationLHS declarationLHS,
      int[] arguments) {
    assert declarationLHS.parameters().size() > arguments.length;

    return CompletableFuture.supplyAsync(() -> {
      final String nextParam = declarationLHS.parameters().get(arguments.length);
      final long guildId = context.commandEvent().getGuild().getIdLong();
      final Optional<SavedAnnotation> foundAnnotation =
          context.jdbi()
                 .withExtension(SavedRollsDao.class, dao -> dao.findRollAnnotation(
                     guildId,
                     declarationLHS.name(),
                     (byte) declarationLHS.parameters().size(),
                     nextParam)
                 );
      final String paramText = foundAnnotation.map(text -> "`%s`\n%s".formatted(nextParam, text.getAnnotation()))
                                              .orElseGet(() -> "`%s`".formatted(nextParam));
      final String paramAndArgList = Stream.concat(
                                               declarationLHS.parameters().stream(),
                                               Arrays.stream(arguments).mapToObj(String::valueOf)
                                           ).map(" %s"::formatted)
                                           .collect(Collectors.joining());

      return new ArgumentSelectOutput(
          "Select %s".formatted(paramText),
          declarationLHS.name(),
          declarationLHS.parameters(),
          "!mr select %s%s".formatted(declarationLHS.name(), paramAndArgList)
      );
    });
  }

  @NotNull
  private CompletableFuture<CommandSelectOutput> doExpressionSelect(ExecutionContext context) {
    return CompletableFuture.supplyAsync(() -> {
        long guildId = context.commandEvent().getGuild().getIdLong();
      final List<AnnotatedSavedRoll> savedRolls = context.jdbi()
                                                         .withExtension(
                                                             SavedRollsDao.class,
                                                             dao -> dao.findAnnotatedByGuild(guildId));
        savedRolls.sort(Comparator.comparing(AnnotatedSavedRoll::getRollName));
        final List<Option> options =
            savedRolls.stream()
                      .map(sr -> new Option(expressionLabel(sr), "!mr select %s".formatted(sr.toLHS())))
                      .toList();

        return new CommandSelectOutput("Select a roll", options);
    }, context.executorService());
  }

  private String expressionLabel(AnnotatedSavedRoll sr) {
    if (sr.getAnnotation() != null) {
      return "%s ! %s".formatted(sr.toLHS(), sr.getAnnotation());
    } else {
      return sr.toLHS();
    }
  }
}

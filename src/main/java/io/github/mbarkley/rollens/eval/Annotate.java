package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.db.SavedAnnotation;
import io.github.mbarkley.rollens.db.SavedRollsDao;
import io.github.mbarkley.rollens.eval.Command.CommandOutput;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Annotate implements Command<CommandOutput> {

  private final String identifier;
  private final Integer arity;
  private final String parameter;
  private final String text;

  @Override
  public CompletableFuture<StringOutput> execute(ExecutionContext context) {
    if (context.commandEvent().isFromGuild()) {
      return CompletableFuture.supplyAsync(() -> {
        context.jdbi()
               .useExtension(
                   SavedRollsDao.class,
                   dao -> dao.insertOrReplace(new SavedAnnotation(
                       context.commandEvent().getGuild().getIdLong(),
                       identifier,
                       arity != null ? arity.byteValue() : null,
                       parameter,
                       text
                   )));

        return new StringOutput("Annotation saved for (%s%s%s)".formatted(identifier,
                                                                          arity != null ? " " + arity : "",
                                                                          parameter != null ? " " + parameter : ""));
      }, context.executorService());
    } else {
      return completedFuture(new StringOutput("Cannot annotate saved rolls in direct messages"));
    }
  }
}

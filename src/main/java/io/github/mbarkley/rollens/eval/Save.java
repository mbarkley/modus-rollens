package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.db.SavedRoll;
import io.github.mbarkley.rollens.db.SavedRollsDao;
import io.github.mbarkley.rollens.eval.Command.StringOutput;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class Save implements Command<StringOutput> {
  private final String identifier;
  private final List<String> parameters;
  private final String rhs;

  @Override
  public CompletableFuture<StringOutput> execute(ExecutionContext context) {
    if (context.commandEvent().isFromGuild()) {
      return doSave(context);
    } else {
      return completedFuture(new StringOutput("Cannot save rolls in direct messages"));
    }
  }

  @NotNull
  private CompletableFuture<StringOutput> doSave(ExecutionContext context) {
    return CompletableFuture.supplyAsync(() -> {
        long guildId = context.commandEvent().getGuild().getIdLong();
        SavedRoll savedRoll = new SavedRoll(guildId, identifier, parameters, rhs);
        context.jdbi().useExtension(SavedRollsDao.class, dao -> dao.insertOrReplace(savedRoll));

        return new StringOutput(
            "Saved (%s %s)".formatted(identifier, String.join(" ", parameters))
        );
    }, context.executorService());
  }
}

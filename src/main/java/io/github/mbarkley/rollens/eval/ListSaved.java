package io.github.mbarkley.rollens.eval;

import io.github.mbarkley.rollens.db.SavedRoll;
import io.github.mbarkley.rollens.db.SavedRollsDao;
import io.github.mbarkley.rollens.eval.Command.StringOutput;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jdbi.v3.core.Handle;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class ListSaved implements Command<StringOutput> {
  public static ListSaved INSTANCE = new ListSaved();
  @Override
  public CompletableFuture<StringOutput> execute(ExecutionContext context) {
    if (context.commandEvent().isFromGuild()) {
      return doList(context);
    } else {
      return completedFuture(new StringOutput("Cannot save and list rolls in direct messages"));
    }
  }

  @NotNull
  private CompletableFuture<StringOutput> doList(ExecutionContext context) {
    return CompletableFuture.supplyAsync(() -> {
      try (Handle handle = context.jdbi().open()) {
        long guildId = context.commandEvent().getGuild().getIdLong();
        final List<SavedRoll> savedRolls = handle.attach(SavedRollsDao.class)
                                                 .findByGuild(guildId);
        savedRolls.sort(Comparator.comparing(SavedRoll::getRollName));

        final StringBuilder sb = new StringBuilder();
        sb.append("__Saved Rolls__");
        if (savedRolls.isEmpty()) {
          sb.append("\nNo saved rolls");
        } else for (var savedRoll : savedRolls) {
          sb.append("\n`").append(savedRoll.toAssignmentString()).append('`');
        }

        return new StringOutput(sb.toString());
      }
    }, context.executorService());
  }
}

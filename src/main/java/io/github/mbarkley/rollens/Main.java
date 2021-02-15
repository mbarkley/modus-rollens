package io.github.mbarkley.rollens;

import io.github.mbarkley.rollens.parse.Parser;
import io.github.mbarkley.rollens.util.EnvUtil;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlite3.SQLitePlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import javax.security.auth.login.LoginException;
import java.util.concurrent.ForkJoinPool;

@Slf4j
public class Main {
  public static void main(String[] args) throws LoginException {
    final String token = EnvUtil.requireEnvString("DISCORD_TOKEN");
    final String dbFile = EnvUtil.optionalEnvString("DB_FILE", "app.db");
    log.info("Starting up...");
    log.info("Available processors: {}", Runtime.getRuntime().availableProcessors());
    log.info("Fork join common pool parallelism: {}", ForkJoinPool.getCommonPoolParallelism());
    log.info("Available JVM memory: {}MB", Runtime.getRuntime().maxMemory() / 1000000L);

    Jdbi jdbi = Jdbi.create("jdbc:sqlite:" + dbFile)
                    .installPlugin(new SqlObjectPlugin())
                    .installPlugin(new SQLitePlugin());
    try (Handle handle = jdbi.open()) {
      final Database db = DatabaseFactory.getInstance()
                                         .findCorrectDatabaseImplementation(new JdbcConnection(handle
                                                                                                   .getConnection()));
      final Liquibase liquibase = new Liquibase("/changelog.yaml", new ClassLoaderResourceAccessor(), db);
      liquibase.update(new Contexts());
      handle.commit();
    } catch (LiquibaseException e) {
      throw new IllegalStateException("Unable to initialize bot DB", e);
    }

    // We only need 2 intents in this bot. We only respond to messages in guilds and private channels.
    // All other events will be disabled.
    JDABuilder.create(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)
              .addEventListeners(new Bot(new Parser(), jdbi))
              .setAutoReconnect(true)
              .setActivity(Activity.listening("!mr"))
              .setCallbackPool(ForkJoinPool.commonPool(), true)
              .build();
  }
}

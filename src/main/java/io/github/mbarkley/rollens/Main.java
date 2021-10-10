package io.github.mbarkley.rollens;

import io.github.mbarkley.rollens.db.DbUtil;
import io.github.mbarkley.rollens.discord.Bot;
import io.github.mbarkley.rollens.parse.SlashCommandParser;
import io.github.mbarkley.rollens.parse.TextParser;
import io.github.mbarkley.rollens.util.EnvUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jdbi.v3.core.Jdbi;

import javax.security.auth.login.LoginException;
import java.util.concurrent.Executors;
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

    Jdbi jdbi = DbUtil.initDb(dbFile);

    // We only need 2 intents in this bot. We only respond to messages in guilds and private channels.
    // All other events will be disabled.
    final TextParser textParser = new TextParser();
    final SlashCommandParser slashCommandParser = new SlashCommandParser(textParser);
    final Bot bot = new Bot(textParser, slashCommandParser, jdbi, Executors.newCachedThreadPool());
    final JDA jda = JDABuilder.create(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)
                              .disableCache(CacheFlag.ACTIVITY,
                                            CacheFlag.VOICE_STATE,
                                            CacheFlag.EMOTE,
                                            CacheFlag.CLIENT_STATUS)
                              .addEventListeners(bot)
                              .setAutoReconnect(true)
                              .setActivity(Activity.listening("!mr"))
                              .setCallbackPool(ForkJoinPool.commonPool(), true)
                              .build();

    // This can take up to an hour to take affect
    jda.updateCommands()
       .addCommands(slashCommandParser.getSlashCommands())
       .queue();
  }

}

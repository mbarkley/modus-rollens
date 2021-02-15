package io.github.mbarkley.rollens.db;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlite3.SQLitePlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jetbrains.annotations.NotNull;

public class DbUtil {
  private DbUtil() {}

  @NotNull
  public static Jdbi initDb(String dbFile) {
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
    return jdbi;
  }
}

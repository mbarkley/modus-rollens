package io.github.mbarkley.rollens.db;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

@RegisterBeanMapper(SavedRoll.class)
public interface SavedRollsDao {
  @SqlUpdate("""
      INSERT OR REPLACE INTO saved_rolls (guild_id, roll_name, arity, parameters, expression)
                  values (:guildId, :rollName, :arity, :encodedParameters, :expression)
      """)
  void insertOrReplace(@BindBean SavedRoll saved);

  @SqlUpdate("""
      DELETE FROM saved_rolls
      WHERE saved_rolls.guild_id = :guildId AND saved_rolls.roll_name = :rollName AND saved_rolls.arity = :arity
      """)
  void delete(@Bind("guildId") long guildId, @Bind("rollName") String rollName, @Bind("arity") byte arity);

  @SqlQuery("""
      SELECT *
      FROM saved_rolls
      WHERE saved_rolls.guild_id = :id
      """)
  List<SavedRoll> findByGuild(@Bind("id") long guildId);

  @SqlQuery("""
      SELECT *
      FROM saved_rolls
      WHERE saved_rolls.guild_id = :guildId AND saved_rolls.roll_name = :rollName AND saved_rolls.arity = :arity
      """)
  Optional<SavedRoll> find(@Bind("guildId") long guildId, @Bind("rollName") String rollName, @Bind("arity") byte arity);
}

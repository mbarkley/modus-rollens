package io.github.mbarkley.rollens.db;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

@RegisterBeanMapper(SavedRoll.class)
public interface SavedRollsDao {
  @SqlUpdate("""
      INSERT INTO saved_rolls (guild_id, roll_name, arity, parameters, expression)
                  values (:guildId, :rollName, :arity, :encodedParameters, :expression)
      """)
  void insert(@BindBean SavedRoll saved);

  @SqlQuery("""
      SELECT *
      FROM saved_rolls
      WHERE saved_rolls.guild_id = :id
      """)
  List<SavedRoll> findByGuild(@Bind("id") long guildId);
}

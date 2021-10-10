package io.github.mbarkley.rollens.db;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterBeanMappers;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

@RegisterBeanMappers({
    @RegisterBeanMapper(SavedRoll.class),
    @RegisterBeanMapper(SavedAnnotation.class),
    @RegisterBeanMapper(AnnotatedSavedRoll.class)
})
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
      WITH
        ranked_matches AS (
          SELECT rolls.*, annos.annotation, row_number() OVER key_window AS row_number
          FROM saved_rolls AS rolls LEFT JOIN saved_roll_annotations AS annos
          ON rolls.guild_id = annos.guild_id
            AND rolls.roll_name = annos.roll_name
            AND annos.parameter IS NULL
            AND (annos.arity IS NULL OR rolls.arity = annos.arity)
          WHERE rolls.guild_id = :id
          WINDOW key_window AS (
            PARTITION BY rolls.guild_id, rolls.roll_name, rolls.arity
            -- nulls are less than non-nulls, so sort with no-nulls first in window
            ORDER BY annos.arity DESC
          )
        )
      SELECT * FROM ranked_matches
      WHERE row_number = 1
      """)
  List<AnnotatedSavedRoll> findAnnotatedByGuild(@Bind("id") long guildId);

  @SqlQuery("""
      SELECT *
      FROM saved_rolls
      WHERE saved_rolls.guild_id = :guildId AND saved_rolls.roll_name = :rollName AND saved_rolls.arity = :arity
      """)
  Optional<SavedRoll> find(@Bind("guildId") long guildId, @Bind("rollName") String rollName, @Bind("arity") byte arity);

  @SqlUpdate("""
      INSERT OR REPLACE INTO saved_roll_annotations (guild_id, roll_name, arity, parameter, annotation)
                  values (:guildId, :rollName, :arity, :parameter, :annotation)
      """)
  void insertOrReplace(@BindBean SavedAnnotation annotation);

  @SqlQuery("""
      SELECT *
      FROM saved_roll_annotations
      WHERE guild_id = :guildId
        AND roll_name = :rollName
        AND (:parameter IS NULL OR parameter = :parameter)
        AND (arity IS NULL OR arity = :arity)
      ORDER BY arity DESC -- In SQLite, null is less than non-null
      LIMIT 1
      """)
  Optional<SavedAnnotation> findRollAnnotation(
      @Bind("guildId") long guildId,
      @Bind("rollName") String rollName,
      @Bind("arity") byte arity,
      @Bind("parameter") String parameter
  );
}

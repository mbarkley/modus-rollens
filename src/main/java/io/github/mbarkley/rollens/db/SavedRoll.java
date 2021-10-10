package io.github.mbarkley.rollens.db;

import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class SavedRoll extends BaseSavedRoll<SavedRoll> {
  public SavedRoll(long guildId, String rollName, List<String> parameters, String expression) {
    super(guildId, rollName, parameters, expression);
  }
}

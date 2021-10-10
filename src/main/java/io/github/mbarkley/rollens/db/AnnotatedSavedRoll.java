package io.github.mbarkley.rollens.db;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AnnotatedSavedRoll extends BaseSavedRoll<AnnotatedSavedRoll> {
  private String annotation;
  public AnnotatedSavedRoll(long guildId, String rollName, List<String> parameters, String expression, String annotation) {
    super(guildId, rollName, parameters, expression);
    this.annotation = annotation;
  }

  @Override
  public String toAssignmentString() {
    return annotation != null ?
        super.toAssignmentString() + " ! " + annotation :
        super.toAssignmentString();
  }
}

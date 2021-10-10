package io.github.mbarkley.rollens.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SavedAnnotation {
  long guildId;
  String rollName;
  Byte arity;
  String parameter;
  String annotation;
}

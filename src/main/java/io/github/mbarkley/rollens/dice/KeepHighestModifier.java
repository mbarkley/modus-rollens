package io.github.mbarkley.rollens.dice;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class KeepHighestModifier extends KeepModifier {
  private final int keep;

  @Override
  protected void markKept(List<PoolTracker> poolTrackers) {
    poolTrackers.stream()
                .sorted(Comparator.comparing(PoolTracker::getValue).reversed())
                .limit(Math.max(keep, 0))
                .forEach(pt -> pt.kept = true);
  }

}

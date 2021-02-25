package io.github.mbarkley.rollens.dice;

import lombok.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class KeepHighestModifier implements RollModifier {
  private final int keep;

  @Override
  public ModifierType type() {
    return ModifierType.KEEP_HIGH;
  }

  @Override
  public void modify(Random rand, List<List<PoolResult>> allResults) {
    if (allResults.isEmpty()) {
      throw new IllegalArgumentException("Empty results");
    }

    List<PoolTracker> poolTrackers = new ArrayList<>();
    for (int k = 0; k < allResults.size(); k++) {
      var prs = allResults.get(k);
      for (int i = 0; i < prs.size(); i++) {
        for (int j = 0; j < prs.get(i).getValues().length; j++) {
          poolTrackers.add(new PoolTracker(new Location(k, i), prs.get(i).getValues()[j]));
        }
      }
    }
    poolTrackers.sort(Comparator.comparing(PoolTracker::getValue).reversed());

    poolTrackers.stream()
                .limit(Math.max(keep, 0))
                .forEach(pt -> pt.kept = true);

    final Map<Location, List<PoolTracker>> grouped = poolTrackers.stream()
                                                                 .collect(Collectors.groupingBy(pt -> pt.location));

    for (var entry : grouped.entrySet()) {
      final Location loc = entry.getKey();
      final List<PoolTracker> trackers = entry.getValue();
      final int[] values = trackers.stream()
                                   .filter(pt -> pt.kept)
                                   .mapToInt(pt -> pt.value)
                                   .toArray();
      final PoolResult poolResult = allResults.get(loc.topLevelIndex).get(loc.poolResultIndex);
      final int[] dropped = IntStream.concat(
          Arrays.stream(poolResult.getDropped()),
          trackers.stream()
                  .filter(pt -> !pt.kept)
                  .mapToInt(pt -> pt.value)
      ).toArray();

      allResults.get(loc.topLevelIndex).set(loc.poolResultIndex, new PoolResult(poolResult.getPool(), dropped, values));
    }
  }

  @Data
  @RequiredArgsConstructor
  private static class PoolTracker {
    final Location location;
    final int value;
    boolean kept = false;
  }

  @Value
  @EqualsAndHashCode
  private static class Location {
    int topLevelIndex;
    int poolResultIndex;
  }
}

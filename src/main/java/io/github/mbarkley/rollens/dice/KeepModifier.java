package io.github.mbarkley.rollens.dice;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class KeepModifier implements RollModifier {
  @Override
  public ModifierType type() {
    return ModifierType.KEEP;
  }

  @Override
  public void modify(Random rand, List<List<PoolResult>> allResults) {
    if (allResults.isEmpty()) {
      throw new IllegalArgumentException("Empty results");
    }

    List<PoolTracker> poolTrackers = makePoolTrackers(allResults);
    markKept(poolTrackers);
    final Map<Location, List<PoolTracker>> grouped = poolTrackers.stream()
                                                                 .collect(Collectors.groupingBy(pt -> pt.location));

    for (var entry : grouped.entrySet()) {
      final Location loc = entry.getKey();
      final List<PoolTracker> trackers = entry.getValue();
      final PoolResult newPoolResult = newPoolResultFromTracker(allResults.get(loc.topLevelIndex)
                                                                          .get(loc.poolResultIndex), trackers);

      allResults.get(loc.topLevelIndex).set(loc.poolResultIndex, newPoolResult);
    }
  }

  @NotNull
  private PoolResult newPoolResultFromTracker(PoolResult poolResult, List<PoolTracker> trackers) {
    final int[] values = trackers.stream()
                                 .filter(pt -> pt.kept)
                                 .mapToInt(pt -> pt.value)
                                 .toArray();
    final int[] dropped = IntStream.concat(
        Arrays.stream(poolResult.getDropped()),
        trackers.stream()
                .filter(pt -> !pt.kept)
                .mapToInt(pt -> pt.value)
    ).toArray();

    return new PoolResult(poolResult.getPool(), dropped, values);
  }

  /**
   * Implementations should set {@link PoolTracker#kept} as {@code true} for dice that should not be dropped.
   *
   * @param poolTrackers Tracker objects for all dice given in a call to {@link #modify(Random, List)}.
   *                    When called, all dice have {@link PoolTracker#kept} as {@code false}. Never null.
   */
  protected abstract void markKept(List<PoolTracker> poolTrackers);

  @NotNull
  private List<PoolTracker> makePoolTrackers(List<List<PoolResult>> allResults) {
    List<PoolTracker> poolTrackers = new ArrayList<>();
    for (int k = 0; k < allResults.size(); k++) {
      var prs = allResults.get(k);
      for (int i = 0; i < prs.size(); i++) {
        for (int j = 0; j < prs.get(i).getValues().length; j++) {
          poolTrackers.add(new PoolTracker(new Location(k, i), prs.get(i).getValues()[j]));
        }
      }
    }
    return poolTrackers;
  }

  @Data
  @RequiredArgsConstructor
  protected static class PoolTracker {
    final Location location;
    final int value;
    boolean kept = false;
  }

  @Value
  @EqualsAndHashCode
  protected static class Location {
    int topLevelIndex;
    int poolResultIndex;
  }
}

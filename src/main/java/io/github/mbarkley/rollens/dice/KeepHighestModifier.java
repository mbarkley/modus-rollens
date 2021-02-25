package io.github.mbarkley.rollens.dice;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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

    final List<PoolResult> curResults = allResults.get(allResults.size() - 1);
    curResults.forEach(pr -> Arrays.sort(pr.getValues()));
    final List<List<Integer>> newResults = new ArrayList<>(curResults.size());
    final List<int[]> dropped = new ArrayList<>(curResults.size());
    curResults.forEach(pr -> newResults.add(new ArrayList<>(pr.getValues().length)));

    int[] resultIndices = curResults.stream().mapToInt(pr -> pr.getValues().length - 1).toArray();
    int bound = Math.min(keep, curResults.stream().mapToInt(pr -> pr.getValues().length).sum());
    for (int kept = 0; kept < bound; kept++) {
      int maxVal = 0;
      int maxPoolIndex = -1;
      for (int candidatePoolIndex = 0; candidatePoolIndex < resultIndices.length; candidatePoolIndex++) {
        int candidateDieIndex = resultIndices[candidatePoolIndex];
        if (candidateDieIndex < curResults.get(candidatePoolIndex).getValues().length) {
          final int candidateValue = curResults.get(candidatePoolIndex).getValues()[candidateDieIndex];
          if (candidateValue > maxVal) {
            maxVal = candidateValue;
            maxPoolIndex = candidatePoolIndex;
            resultIndices[candidatePoolIndex] -= 1;
          }
        }
      }
      newResults.get(maxPoolIndex).add(maxVal);
    }
    for (int i = 0; i < resultIndices.length; i++) {
      dropped.add(new int[resultIndices[i] + 1]);
      System.arraycopy(curResults.get(i).getValues(),
                       0,
                       dropped.get(i),
                       0,
                       resultIndices[i] + 1);
    }

    final List<PoolResult> updatedDicePools = new ArrayList<>(newResults.size());
    for (int i = 0; i < curResults.size(); i++) {
      updatedDicePools.add(new PoolResult(curResults.get(i).getPool(),
                                          dropped.get(i),
                                          newResults.get(i)
                                                    .stream()
                                                    .mapToInt(Integer::intValue)
                                                    .toArray()));
    }

    allResults.set(allResults.size() - 1, updatedDicePools);
  }
}

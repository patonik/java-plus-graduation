package ru.practicum.aggregator.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SimilarityStat {
    private double similarity;
    private double eventMinSum;

    public void updateSimilarity(double eventWeightSumRootA, double eventWeightSumRootB) {
        this.similarity = eventMinSum / (eventWeightSumRootA * eventWeightSumRootB);
    }
}

package ru.practicum.aggregator.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventStat {
    private double eventWeightSum;
    private double eventWeightSumRoot;

    public void updateEventWeightSumRoot() {
        this.eventWeightSumRoot = Math.sqrt(this.eventWeightSum);
    }
}

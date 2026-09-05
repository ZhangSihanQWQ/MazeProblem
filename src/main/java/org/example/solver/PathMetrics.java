package org.example.solver;

public record PathMetrics(
        double distance,
        double turnPenalty,
        double travelTime
) {
}

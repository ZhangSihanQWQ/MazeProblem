package org.example.solver;

import org.example.geometry.GeometryNode;

import java.util.List;

public record GeometricPathResult(
        List<GeometryNode> path,
        double distance,
        int visitedStates
) {
    public boolean found() {
        return !path.isEmpty();
    }
}

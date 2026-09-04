package org.example.solver;

public enum SearchAlgorithm {
    BFS("BFS"),
    ASTAR("A*");

    private final String displayName;

    SearchAlgorithm(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

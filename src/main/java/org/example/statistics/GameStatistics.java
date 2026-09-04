package org.example.statistics;

import org.example.model.Cell;
import org.example.model.Maze;

import java.util.List;

public class GameStatistics {
    private final Maze maze;
    private final int visitedCells;
    private final long generationTimeNanos;
    private final long searchTimeNanos;
    private final int pathLength;
    private final int dfsVisitedCells;
    private final long dfsSearchTimeNanos;
    private final int dfsPathLength;

    public GameStatistics(
            Maze maze,
            int visitedCells,
            long generationTimeNanos,
            long searchTimeNanos,
            List<Cell> path,
            int dfsVisitedCells,
            long dfsSearchTimeNanos,
            List<Cell> dfsPath
    ) {
        this.maze = maze;
        this.visitedCells = visitedCells;
        this.generationTimeNanos = generationTimeNanos;
        this.searchTimeNanos = searchTimeNanos;
        this.pathLength = path.size();
        this.dfsVisitedCells = dfsVisitedCells;
        this.dfsSearchTimeNanos = dfsSearchTimeNanos;
        this.dfsPathLength = dfsPath.size();
    }

    @Override
    public String toString() {
        return """
                 --- 迷宫统计 ---
                尺寸: %d x %d
                总格子数: %d
                入口: %s
                出口: %s

                --- BFS / DFS 算法对比 ---
                %s
                %s
                %s
                %s
                %s

                迷宫生成耗时: %.3f ms
                """.formatted(
                maze.getRows(),
                maze.getCols(),
                maze.getRows() * maze.getCols(),
                maze.getEntrance(),
                maze.getExit(),
                comparisonRow("指标", "BFS", "DFS"),
                comparisonSeparator(),
                comparisonRow("路径长度", String.valueOf(pathLength), String.valueOf(dfsPathLength)),
                comparisonRow("访问格子数", String.valueOf(visitedCells), String.valueOf(dfsVisitedCells)),
                comparisonRow(
                        "搜索耗时",
                        "%.3f ms".formatted(searchTimeNanos / 1_000_000.0),
                        "%.3f ms".formatted(dfsSearchTimeNanos / 1_000_000.0)
                ),
                generationTimeNanos / 1_000_000.0
        );
    }

    private String comparisonRow(String metric, String bfsValue, String dfsValue) {
        return "| " + padRight(metric, 12)
                + " | " + padRight(bfsValue, 12)
                + " | " + padRight(dfsValue, 12) + " |";
    }

    private String comparisonSeparator() {
        return "|--------------|--------------|--------------|";
    }

    private String padRight(String value, int displayWidth) {
        StringBuilder result = new StringBuilder(value);
        int currentWidth = displayWidth(value);
        while (currentWidth < displayWidth) {
            result.append(' ');
            currentWidth++;
        }
        return result.toString();
    }

    private int displayWidth(String value) {
        int width = 0;
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            width += isWideCharacter(character) ? 2 : 1;
        }
        return width;
    }

    private boolean isWideCharacter(char character) {
        return (character >= '\u2E80' && character <= '\uA4CF')
                || (character >= '\uAC00' && character <= '\uD7A3')
                || (character >= '\uF900' && character <= '\uFAFF')
                || (character >= '\uFF01' && character <= '\uFF60');
    }
}

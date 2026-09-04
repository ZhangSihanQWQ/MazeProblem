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

    public GameStatistics(
            Maze maze,
            int visitedCells,
            long generationTimeNanos,
            long searchTimeNanos,
            List<Cell> path
    ) {
        this.maze = maze;
        this.visitedCells = visitedCells;
        this.generationTimeNanos = generationTimeNanos;
        this.searchTimeNanos = searchTimeNanos;
        this.pathLength = path.size();
    }

    @Override
    public String toString() {
        return """
                --- 迷宫统计 ---
                尺寸: %d x %d
                总格子数: %d
                入口: %s
                出口: %s
                路径长度: %d
                搜索访问格子数: %d
                生成耗时: %.3f ms
                搜索耗时: %.3f ms
                """.formatted(
                maze.getRows(),
                maze.getCols(),
                maze.getRows() * maze.getCols(),
                maze.getEntrance(),
                maze.getExit(),
                pathLength,
                visitedCells,
                generationTimeNanos / 1_000_000.0,
                searchTimeNanos / 1_000_000.0
        );
    }
}

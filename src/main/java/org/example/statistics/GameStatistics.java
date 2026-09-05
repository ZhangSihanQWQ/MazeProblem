package org.example.statistics;

import org.example.model.Cell;
import org.example.model.Maze;
import org.example.solver.SearchAlgorithm;

import java.util.List;

public class GameStatistics {
    private final Maze maze;
    private final SearchAlgorithm algorithm;
    private final int visitedStates;
    private final long generationTimeNanos;
    private final long searchTimeNanos;
    private final int cellPathLength;
    private final double geometricDistance;
    private final double turnPenalty;
    private final double travelTime;

    public GameStatistics(
            Maze maze,
            SearchAlgorithm algorithm,
            int visitedStates,
            long generationTimeNanos,
            long searchTimeNanos,
            List<Cell> cellPath,
            double geometricDistance,
            double turnPenalty,
            double travelTime
    ) {
        this.maze = maze;
        this.algorithm = algorithm;
        this.visitedStates = visitedStates;
        this.generationTimeNanos = generationTimeNanos;
        this.searchTimeNanos = searchTimeNanos;
        this.cellPathLength = cellPath.size();
        this.geometricDistance = geometricDistance;
        this.turnPenalty = turnPenalty;
        this.travelTime = travelTime;
    }

    @Override
    public String toString() {
        String pathDescription = algorithm == SearchAlgorithm.BFS
                ? "路径长度: %d 个格子".formatted(cellPathLength)
                : """
                几何路径长度: %.3f units
                转弯时间损耗: %.3f s
                预计移动时间: %.3f s
                """.formatted(geometricDistance, turnPenalty, travelTime);
        return """
                 --- 迷宫统计 ---
                尺寸: %d x %d
                总格子数: %d
                入口: %s
                出口: %s

                --- %s 最短路径 ---
                %s
                访问状态数: %d
                搜索耗时: %.3f ms
                迷宫生成耗时: %.3f ms
                """.formatted(
                maze.getRows(),
                maze.getCols(),
                maze.getRows() * maze.getCols(),
                maze.getEntrance(),
                maze.getExit(),
                algorithm.getDisplayName(),
                pathDescription,
                visitedStates,
                searchTimeNanos / 1_000_000.0,
                generationTimeNanos / 1_000_000.0
        );
    }
}

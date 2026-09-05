package org.example.generator;

import org.example.model.Cell;
import org.example.model.Direction;
import org.example.model.Maze;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class MazeGenerator {
    private final Random random;

    public MazeGenerator() {
        this(new Random());
    }

    public MazeGenerator(Random random) {
        this.random = random;
    }

    public void generate(Maze maze) {
        generate(maze, true);
    }

    public void generate(Maze maze, boolean perfect) {
        maze.resetVisited();
        Deque<Cell> stack = new LinkedList<>();
        int totalCells = maze.getRows() * maze.getCols();
        // 用访问计数代替反复扫描整个迷宫，生成过程保持接近 O(V)。
        int visitedCount = 1;
        Cell current = maze.getCell(random.nextInt(maze.getRows()), random.nextInt(maze.getCols()));
        current.setVisited(true);

        while (visitedCount < totalCells) {
            List<Cell> unvisitedNeighbors = getUnvisitedNeighbors(maze, current);
            if (!unvisitedNeighbors.isEmpty()) {
                // 随机选择一个未访问邻居并拆墙，等价于小鼠向该方向“咬开”墙前进。
                Cell next = unvisitedNeighbors.get(random.nextInt(unvisitedNeighbors.size()));
                stack.push(current);
                maze.removeWall(current, next);
                current = next;
                current.setVisited(true);
                visitedCount++;
            } else {
                // 当前格子走不通时回溯到上一个分叉点。
                current = stack.pop();
            }
        }

        if (!perfect) {
            addRandomPassages(maze);
        }
        maze.resetVisited();
    }

    private List<Cell> getUnvisitedNeighbors(Maze maze, Cell cell) {
        List<Cell> result = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            Cell neighbor = maze.getNeighbor(cell, direction);
            if (neighbor != null && !neighbor.isVisited()) {
                result.add(neighbor);
            }
        }
        return result;
    }

    private void addRandomPassages(Maze maze) {
        // 普通连通迷宫先保证连通，再额外拆墙形成环和多条可选路径。
        List<WallCandidate> candidates = new ArrayList<>();
        for (int row = 0; row < maze.getRows(); row++) {
            for (int col = 0; col < maze.getCols(); col++) {
                Cell cell = maze.getCell(row, col);
                if (col + 1 < maze.getCols() && cell.hasWall(Direction.RIGHT)) {
                    candidates.add(new WallCandidate(cell, Direction.RIGHT));
                }
                if (row + 1 < maze.getRows() && cell.hasWall(Direction.DOWN)) {
                    candidates.add(new WallCandidate(cell, Direction.DOWN));
                }
            }
        }

        if (candidates.isEmpty()) {
            return;
        }

        int passagesToAdd = 1 + random.nextInt(candidates.size());
        for (int index = 0; index < passagesToAdd; index++) {
            WallCandidate candidate = candidates.get(index);
            Cell neighbor = maze.getNeighbor(candidate.cell(), candidate.direction());
            maze.removeWall(candidate.cell(), neighbor);
        }
    }

    private record WallCandidate(Cell cell, Direction direction) {
    }
}

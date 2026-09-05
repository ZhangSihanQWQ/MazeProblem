package org.example.validator;

import org.example.model.Cell;
import org.example.model.Direction;
import org.example.model.Maze;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class MazeValidator {
    public boolean isPerfectMaze(Maze maze) {
        // 完美迷宫在图论上是一棵树：连通，并且边数等于顶点数减一。
        return isConnected(maze) && countOpenConnections(maze) == totalCells(maze) - 1;
    }

    public boolean isConnected(Maze maze) {
        Set<Cell> visited = new HashSet<>();
        Queue<Cell> queue = new ArrayDeque<>();
        Cell start = maze.getCell(0, 0);
        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            for (Direction direction : Direction.values()) {
                Cell neighbor = maze.getNeighbor(current, direction);
                if (neighbor != null
                        && !current.hasWall(direction)
                        && visited.add(neighbor)) {
                    queue.offer(neighbor);
                }
            }
        }
        return visited.size() == totalCells(maze);
    }

    public int countOpenConnections(Maze maze) {
        int connections = 0;
        for (int row = 0; row < maze.getRows(); row++) {
            for (int col = 0; col < maze.getCols(); col++) {
                Cell cell = maze.getCell(row, col);
                if (col + 1 < maze.getCols() && !cell.hasWall(Direction.RIGHT)) {
                    connections++;
                }
                if (row + 1 < maze.getRows() && !cell.hasWall(Direction.DOWN)) {
                    connections++;
                }
            }
        }
        return connections;
    }

    private int totalCells(Maze maze) {
        return maze.getRows() * maze.getCols();
    }
}

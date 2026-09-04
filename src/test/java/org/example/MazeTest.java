package org.example;

import org.example.generator.MazeGenerator;
import org.example.model.Cell;
import org.example.model.Direction;
import org.example.model.Maze;
import org.example.solver.MazeSolver;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MazeTest {
    @Test
    void generatedMazeShouldBeFullyConnected() {
        Maze maze = createGeneratedMaze(5, 5);
        Set<Cell> visited = new HashSet<>();
        Queue<Cell> queue = new ArrayDeque<>();
        queue.offer(maze.getCell(0, 0));
        visited.add(maze.getCell(0, 0));

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            for (Direction direction : Direction.values()) {
                Cell neighbor = maze.getNeighbor(current, direction);
                if (neighbor != null && !current.hasWall(direction) && visited.add(neighbor)) {
                    queue.offer(neighbor);
                }
            }
        }

        assertEquals(25, visited.size());
    }

    @Test
    void bfsShouldFindAValidPath() {
        Maze maze = createGeneratedMaze(10, 20);
        maze.setEntrance(0, 0);
        maze.setExit(9, 19);

        List<Cell> path = new MazeSolver().findPath(maze);

        assertFalse(path.isEmpty());
        assertEquals(maze.getEntrance(), path.get(0));
        assertEquals(maze.getExit(), path.get(path.size() - 1));
        for (int index = 1; index < path.size(); index++) {
            Cell previous = path.get(index - 1);
            Cell current = path.get(index);
            boolean adjacentWithoutWall = false;
            for (Direction direction : Direction.values()) {
                if (maze.getNeighbor(previous, direction) == current
                        && !previous.hasWall(direction)) {
                    adjacentWithoutWall = true;
                }
            }
            assertTrue(adjacentWithoutWall);
        }
    }

    @Test
    void removeWallShouldUpdateBothCells() {
        Maze maze = new Maze(1, 2);
        Cell left = maze.getCell(0, 0);
        Cell right = maze.getCell(0, 1);

        maze.removeWall(left, right);

        assertFalse(left.hasWall(Direction.RIGHT));
        assertFalse(right.hasWall(Direction.LEFT));
    }

    private Maze createGeneratedMaze(int rows, int cols) {
        Maze maze = new Maze(rows, cols);
        new MazeGenerator().generate(maze);
        return maze;
    }
}

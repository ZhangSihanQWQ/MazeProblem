package org.example;

import org.example.generator.MazeGenerator;
import org.example.model.Cell;
import org.example.model.Direction;
import org.example.model.Maze;
import org.example.solver.MazeSolver;
import org.example.validator.MazeValidator;
import org.example.util.SeedUtil;
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
    void perfectGenerationShouldProduceAPerfectMaze() {
        Maze maze = new Maze(5, 5);
        new MazeGenerator().generate(maze, true);
        MazeValidator validator = new MazeValidator();

        assertTrue(validator.isPerfectMaze(maze));
        assertEquals(24, validator.countOpenConnections(maze));
    }

    @Test
    void nonPerfectGenerationShouldRemainConnectedButContainAnExtraPassage() {
        Maze maze = new Maze(5, 5);
        new MazeGenerator().generate(maze, false);
        MazeValidator validator = new MazeValidator();

        assertTrue(validator.isConnected(maze));
        assertFalse(validator.isPerfectMaze(maze));
        assertTrue(validator.countOpenConnections(maze) > 24);
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
    void bfsShouldSupportAnExitInTheMiddle() {
        Maze maze = createGeneratedMaze(10, 20);
        maze.setEntrance(0, 0);
        maze.setExit(5, 7);

        List<Cell> path = new MazeSolver().findPath(maze);

        assertFalse(path.isEmpty());
        assertEquals(maze.getEntrance(), path.get(0));
        assertEquals(maze.getExit(), path.get(path.size() - 1));
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

    @Test
    void sameSeedShouldGenerateTheSameMaze() {
        Maze first = createSeededMaze("class-demo");
        Maze second = createSeededMaze("class-demo");

        assertEquals(mazeSignature(first), mazeSignature(second));
    }

    @Test
    void dfsShouldFindAValidPath() {
        Maze maze = createGeneratedMaze(10, 20);
        maze.setEntrance(0, 0);
        maze.setExit(5, 7);

        MazeSolver dfsSolver = new MazeSolver();
        List<Cell> path = dfsSolver.findPathByDfs(maze);

        assertFalse(path.isEmpty());
        assertEquals(maze.getEntrance(), path.get(0));
        assertEquals(maze.getExit(), path.get(path.size() - 1));
    }

    @Test
    void bfsPathShouldNotBeLongerThanDfsPath() {
        Maze maze = createGeneratedMaze(10, 20);
        maze.setEntrance(0, 0);
        maze.setExit(5, 7);

        List<Cell> bfsPath = new MazeSolver().findPath(maze);
        List<Cell> dfsPath = new MazeSolver().findPathByDfs(maze);

        assertFalse(bfsPath.isEmpty());
        assertFalse(dfsPath.isEmpty());
        assertTrue(bfsPath.size() <= dfsPath.size());
    }

    private Maze createGeneratedMaze(int rows, int cols) {
        Maze maze = new Maze(rows, cols);
        new MazeGenerator().generate(maze);
        return maze;
    }

    private Maze createSeededMaze(String seedText) {
        Maze maze = new Maze(6, 8);
        new MazeGenerator(new java.util.Random(SeedUtil.toLong(seedText))).generate(maze, true);
        return maze;
    }

    private String mazeSignature(Maze maze) {
        StringBuilder signature = new StringBuilder();
        for (int row = 0; row < maze.getRows(); row++) {
            for (int col = 0; col < maze.getCols(); col++) {
                Cell cell = maze.getCell(row, col);
                signature.append(cell.getRow()).append(',').append(cell.getCol());
                for (Direction direction : Direction.values()) {
                    signature.append(cell.hasWall(direction) ? '1' : '0');
                }
            }
        }
        return signature.toString();
    }
}

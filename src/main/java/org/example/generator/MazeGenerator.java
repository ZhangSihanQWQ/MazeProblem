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
        int visitedCount = 1;
        Cell current = maze.getCell(random.nextInt(maze.getRows()), random.nextInt(maze.getCols()));
        current.setVisited(true);

        while (visitedCount < totalCells) {
            List<Cell> unvisitedNeighbors = getUnvisitedNeighbors(maze, current);
            if (!unvisitedNeighbors.isEmpty()) {
                Cell next = unvisitedNeighbors.get(random.nextInt(unvisitedNeighbors.size()));
                stack.push(current);
                maze.removeWall(current, next);
                current = next;
                current.setVisited(true);
                visitedCount++;
            } else {
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

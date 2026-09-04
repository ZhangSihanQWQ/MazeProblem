package org.example.generator;

import org.example.model.Cell;
import org.example.model.Direction;
import org.example.model.Maze;

import java.util.ArrayList;
import java.util.Collections;
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
        maze.resetVisited();
        Deque<Cell> stack = new LinkedList<>();
        Cell current = maze.getCell(random.nextInt(maze.getRows()), random.nextInt(maze.getCols()));
        current.setVisited(true);

        while (!stack.isEmpty() || hasUnvisitedCell(maze)) {
            List<Cell> unvisitedNeighbors = getUnvisitedNeighbors(maze, current);
            if (!unvisitedNeighbors.isEmpty()) {
                Cell next = unvisitedNeighbors.get(random.nextInt(unvisitedNeighbors.size()));
                stack.push(current);
                maze.removeWall(current, next);
                current = next;
                current.setVisited(true);
            } else if (!stack.isEmpty()) {
                current = stack.pop();
            } else {
                current = findUnvisitedCell(maze);
                current.setVisited(true);
            }
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
        Collections.shuffle(result, random);
        return result;
    }

    private boolean hasUnvisitedCell(Maze maze) {
        return findUnvisitedCell(maze) != null;
    }

    private Cell findUnvisitedCell(Maze maze) {
        for (int row = 0; row < maze.getRows(); row++) {
            for (int col = 0; col < maze.getCols(); col++) {
                Cell cell = maze.getCell(row, col);
                if (!cell.isVisited()) {
                    return cell;
                }
            }
        }
        return null;
    }
}

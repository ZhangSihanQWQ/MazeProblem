package org.example.solver;

import org.example.model.Cell;
import org.example.model.Direction;
import org.example.model.Maze;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class MazeSolver {
    private int visitedCount;

    public List<Cell> findPath(Maze maze) {
        if (maze.getEntrance() == null || maze.getExit() == null) {
            throw new IllegalStateException("请先设置迷宫入口和出口");
        }

        visitedCount = 0;
        Queue<Cell> queue = new ArrayDeque<>();
        Set<Cell> visited = new HashSet<>();
        Map<Cell, Cell> parent = new HashMap<>();
        Cell entrance = maze.getEntrance();
        Cell exit = maze.getExit();
        queue.offer(entrance);
        visited.add(entrance);

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            visitedCount++;
            if (current.equals(exit)) {
                return buildPath(parent, entrance, exit);
            }

            for (Direction direction : Direction.values()) {
                Cell neighbor = maze.getNeighbor(current, direction);
                if (neighbor != null
                        && !current.hasWall(direction)
                        && visited.add(neighbor)) {
                    parent.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }
        return List.of();
    }

    public int getVisitedCount() {
        return visitedCount;
    }

    private List<Cell> buildPath(Map<Cell, Cell> parent, Cell entrance, Cell exit) {
        List<Cell> path = new ArrayList<>();
        for (Cell current = exit; current != null; current = parent.get(current)) {
            path.add(current);
            if (current.equals(entrance)) {
                break;
            }
        }
        Collections.reverse(path);
        return path;
    }
}

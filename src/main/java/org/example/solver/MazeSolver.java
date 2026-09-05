package org.example.solver;

import org.example.model.Cell;
import org.example.model.Direction;
import org.example.model.Maze;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
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
            // BFS 按层扩展；边权相同，因此第一次到达出口就是最少格子数路径。
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
                    // parent 记录从哪个格子来到当前邻居，用于最后反向还原路径。
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

    public List<Cell> findPathByDfs(Maze maze) {
        if (maze.getEntrance() == null || maze.getExit() == null) {
            throw new IllegalStateException("请先设置迷宫入口和出口");
        }

        visitedCount = 0;
        Deque<Cell> stack = new ArrayDeque<>();
        Set<Cell> visited = new HashSet<>();
        Map<Cell, Cell> parent = new HashMap<>();
        Cell entrance = maze.getEntrance();
        Cell exit = maze.getExit();
        stack.push(entrance);
        visited.add(entrance);

        while (!stack.isEmpty()) {
            // DFS 用栈向深处搜索，主要用于和 BFS 的访问量、路径结果做对比。
            Cell current = stack.pop();
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
                    stack.push(neighbor);
                }
            }
        }
        return List.of();
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

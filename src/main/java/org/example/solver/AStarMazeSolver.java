package org.example.solver;

import org.example.geometry.GeometryEdge;
import org.example.geometry.GeometryNode;
import org.example.geometry.MazeGeometryGraph;
import org.example.model.Maze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class AStarMazeSolver {
    private static final int NO_HEADING = -1;

    public GeometricPathResult findShortestPath(Maze maze) {
        if (maze.getEntrance() == null || maze.getExit() == null) {
            throw new IllegalStateException("请先设置迷宫入口和出口");
        }

        MazeGeometryGraph graph = MazeGeometryGraph.from(maze);
        GeometryNode start = graph.getEntranceNode(maze);
        GeometryNode goal = graph.getExitNode(maze);
        // 状态包含“几何位置 + 进入方向”，这样同一点不同方向到达会有不同转弯代价。
        SearchState startState = new SearchState(start, NO_HEADING);
        Map<SearchState, Double> distances = new HashMap<>();
        Map<SearchState, SearchState> parents = new HashMap<>();
        PriorityQueue<QueueEntry> queue = new PriorityQueue<>();
        distances.put(startState, 0.0);
        queue.offer(new QueueEntry(startState, heuristic(start, goal), 0.0));
        int visitedStates = 0;

        while (!queue.isEmpty()) {
            QueueEntry entry = queue.poll();
            SearchState current = entry.state();
            double currentDistance = distances.getOrDefault(current, Double.POSITIVE_INFINITY);
            if (entry.distance() > currentDistance) {
                continue;
            }
            visitedStates++;
            if (current.node().equals(goal)) {
                return buildResult(parents, current, currentDistance, visitedStates);
            }

            for (GeometryEdge edge : graph.getEdges(current.node())) {
                // heading 每 45 度为一档；超过 90 度的急转弯不作为可行动作。
                int turnSteps = current.heading() == NO_HEADING
                        ? 0
                        : TurnPenaltyModel.turnDistance(current.heading(), edge.heading());
                if (turnSteps > 2) {
                    continue;
                }
                SearchState next = new SearchState(edge.target(), edge.heading());
                double turnPenalty = TurnPenaltyModel.calculateTurnPenalty(turnSteps);
                // A* 的 g(n)：已用时间 + 本段路程时间 + 本次转弯损耗。
                double nextTime = currentDistance
                        + edge.distance() / TurnPenaltyModel.SPEED_UNITS_PER_SECOND
                        + turnPenalty;
                if (nextTime < distances.getOrDefault(next, Double.POSITIVE_INFINITY)) {
                    distances.put(next, nextTime);
                    parents.put(next, current);
                    queue.offer(new QueueEntry(
                            next,
                            // h(n) 使用到终点的欧氏距离作为乐观估计，保证搜索有方向性。
                            nextTime + heuristic(edge.target(), goal)
                                    / TurnPenaltyModel.SPEED_UNITS_PER_SECOND,
                            nextTime
                    ));
                }
            }
        }
        return new GeometricPathResult(
                List.of(),
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                visitedStates
        );
    }

    private GeometricPathResult buildResult(
            Map<SearchState, SearchState> parents,
            SearchState goal,
            double travelTime,
            int visitedStates
    ) {
        List<GeometryNode> path = new ArrayList<>();
        for (SearchState current = goal; current != null; current = parents.get(current)) {
            path.add(current.node());
        }
        Collections.reverse(path);
        // 搜索时使用总时间作为代价；这里重新拆分出路程和转弯损耗，便于展示统计。
        double geometricDistance = 0.0;
        double turnPenalty = 0.0;
        for (int index = 1; index < path.size(); index++) {
            GeometryNode previous = path.get(index - 1);
            GeometryNode current = path.get(index);
            geometricDistance += Math.hypot(
                    current.x() - previous.x(),
                    current.y() - previous.y()
            );
            if (index >= 2) {
                int previousHeading = calculateHeading(path.get(index - 2), previous);
                int currentHeading = calculateHeading(previous, current);
                turnPenalty += TurnPenaltyModel.calculateTurnPenalty(
                        TurnPenaltyModel.turnDistance(previousHeading, currentHeading)
                );
            }
        }
        return new GeometricPathResult(
                path,
                geometricDistance,
                turnPenalty,
                travelTime,
                visitedStates
        );
    }

    private double heuristic(GeometryNode current, GeometryNode goal) {
        return Math.hypot(goal.x() - current.x(), goal.y() - current.y());
    }

    private int calculateHeading(GeometryNode from, GeometryNode to) {
        double angle = Math.atan2(to.y() - from.y(), to.x() - from.x());
        return ((int) Math.round(angle / (Math.PI / 4)) + 8) % 8;
    }

    private record SearchState(GeometryNode node, int heading) {
    }

    private record QueueEntry(SearchState state, double priority, double distance)
            implements Comparable<QueueEntry> {
        @Override
        public int compareTo(QueueEntry other) {
            return Double.compare(priority, other.priority);
        }
    }
}

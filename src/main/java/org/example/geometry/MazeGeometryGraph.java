package org.example.geometry;

import org.example.model.Cell;
import org.example.model.Direction;
import org.example.model.Maze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MazeGeometryGraph {
    private final List<GeometryNode> nodes = new ArrayList<>();
    private final Map<GeometryNode, List<GeometryEdge>> edges = new HashMap<>();
    private final Map<Cell, GeometryNode> centers = new HashMap<>();
    private final Map<PassageKey, GeometryNode> passages = new HashMap<>();

    private MazeGeometryGraph() {
    }

    public static MazeGeometryGraph from(Maze maze) {
        MazeGeometryGraph graph = new MazeGeometryGraph();
        graph.createCenterNodes(maze);
        graph.createPassageNodes(maze);
        graph.connectCenterAndPassageNodes(maze);
        graph.connectPassagesInsideCells(maze);
        return graph;
    }

    public GeometryNode getEntranceNode(Maze maze) {
        return centers.get(maze.getEntrance());
    }

    public GeometryNode getExitNode(Maze maze) {
        return centers.get(maze.getExit());
    }

    public List<GeometryEdge> getEdges(GeometryNode node) {
        return edges.getOrDefault(node, List.of());
    }

    private void createCenterNodes(Maze maze) {
        for (int row = 0; row < maze.getRows(); row++) {
            for (int col = 0; col < maze.getCols(); col++) {
                GeometryNode node = addNode(new GeometryNode(nodes.size(), col + 0.5, row + 0.5));
                centers.put(maze.getCell(row, col), node);
            }
        }
    }

    private void createPassageNodes(Maze maze) {
        for (int row = 0; row < maze.getRows(); row++) {
            for (int col = 0; col < maze.getCols(); col++) {
                Cell cell = maze.getCell(row, col);
                if (col + 1 < maze.getCols() && !cell.hasWall(Direction.RIGHT)) {
                    passages.put(
                            new PassageKey(cell, Direction.RIGHT),
                            addNode(new GeometryNode(nodes.size(), col + 1.0, row + 0.5))
                    );
                }
                if (row + 1 < maze.getRows() && !cell.hasWall(Direction.DOWN)) {
                    passages.put(
                            new PassageKey(cell, Direction.DOWN),
                            addNode(new GeometryNode(nodes.size(), col + 0.5, row + 1.0))
                    );
                }
            }
        }
    }

    private void connectCenterAndPassageNodes(Maze maze) {
        for (Map.Entry<PassageKey, GeometryNode> entry : passages.entrySet()) {
            PassageKey passage = entry.getKey();
            GeometryNode passageNode = entry.getValue();
            Cell first = passage.cell();
            Cell second = maze.getNeighbor(first, passage.direction());
            addUndirectedEdge(centers.get(first), passageNode);
            addUndirectedEdge(centers.get(second), passageNode);
        }
    }

    private void connectPassagesInsideCells(Maze maze) {
        for (int row = 0; row < maze.getRows(); row++) {
            for (int col = 0; col < maze.getCols(); col++) {
                Cell cell = maze.getCell(row, col);
                List<GeometryNode> cellPassages = new ArrayList<>();
                for (Direction direction : Direction.values()) {
                    GeometryNode passage = findPassage(cell, direction);
                    if (passage != null) {
                        cellPassages.add(passage);
                    }
                }
                for (int first = 0; first < cellPassages.size(); first++) {
                    for (int second = first + 1; second < cellPassages.size(); second++) {
                        addUndirectedEdge(cellPassages.get(first), cellPassages.get(second));
                    }
                }
            }
        }
    }

    private GeometryNode findPassage(Cell cell, Direction direction) {
        GeometryNode passage = passages.get(new PassageKey(cell, direction));
        if (passage != null) {
            return passage;
        }
        if (direction == Direction.LEFT) {
            return passages.get(new PassageKey(
                    new Cell(cell.getRow(), cell.getCol() - 1), Direction.RIGHT
            ));
        }
        if (direction == Direction.UP) {
            return passages.get(new PassageKey(
                    new Cell(cell.getRow() - 1, cell.getCol()), Direction.DOWN
            ));
        }
        return null;
    }

    private GeometryNode addNode(GeometryNode node) {
        nodes.add(node);
        edges.put(node, new ArrayList<>());
        return node;
    }

    private void addUndirectedEdge(GeometryNode first, GeometryNode second) {
        double distance = Math.hypot(second.x() - first.x(), second.y() - first.y());
        edges.get(first).add(new GeometryEdge(second, distance, calculateHeading(first, second)));
        edges.get(second).add(new GeometryEdge(first, distance, calculateHeading(second, first)));
    }

    private int calculateHeading(GeometryNode from, GeometryNode to) {
        double angle = Math.atan2(to.y() - from.y(), to.x() - from.x());
        return ((int) Math.round(angle / (Math.PI / 4)) + 8) % 8;
    }

    private record PassageKey(Cell cell, Direction direction) {
    }
}

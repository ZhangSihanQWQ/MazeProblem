package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class Maze {
    private final int rows;
    private final int cols;
    private final Cell[][] cells;
    private Cell entrance;
    private Cell exit;

    public Maze(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("迷宫行数和列数必须大于 0");
        }
        this.rows = rows;
        this.cols = cols;
        this.cells = new Cell[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                cells[row][col] = new Cell(row, col);
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Cell getCell(int row, int col) {
        if (!isValidPosition(row, col)) {
            return null;
        }
        return cells[row][col];
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public Cell getNeighbor(Cell cell, Direction direction) {
        return getCell(
                cell.getRow() + direction.getDeltaRow(),
                cell.getCol() + direction.getDeltaCol()
        );
    }

    public List<Cell> getNeighbors(Cell cell) {
        List<Cell> neighbors = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            Cell neighbor = getNeighbor(cell, direction);
            if (neighbor != null) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    public void removeWall(Cell first, Cell second) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("参与拆墙的格子不能为 null");
        }
        for (Direction direction : Direction.values()) {
            if (getNeighbor(first, direction) == second) {
                first.removeWall(direction);
                second.removeWall(direction.opposite());
                return;
            }
        }
        throw new IllegalArgumentException("两个格子不是相邻格子");
    }

    public Cell getEntrance() {
        return entrance;
    }

    public void setEntrance(int row, int col) {
        entrance = requireCell(row, col);
    }

    public Cell getExit() {
        return exit;
    }

    public void setExit(int row, int col) {
        exit = requireCell(row, col);
    }

    public void resetVisited() {
        for (Cell[] row : cells) {
            for (Cell cell : row) {
                cell.setVisited(false);
            }
        }
    }

    private Cell requireCell(int row, int col) {
        Cell cell = getCell(row, col);
        if (cell == null) {
            throw new IllegalArgumentException("坐标超出迷宫范围");
        }
        return cell;
    }
}

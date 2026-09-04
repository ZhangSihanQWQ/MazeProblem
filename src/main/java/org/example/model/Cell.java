package org.example.model;

import java.util.Objects;

public class Cell {
    private final int row;
    private final int col;
    private boolean visited;
    private boolean topWall = true;
    private boolean rightWall = true;
    private boolean bottomWall = true;
    private boolean leftWall = true;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public boolean hasWall(Direction direction) {
        return switch (direction) {
            case UP -> topWall;
            case RIGHT -> rightWall;
            case DOWN -> bottomWall;
            case LEFT -> leftWall;
        };
    }

    public void removeWall(Direction direction) {
        switch (direction) {
            case UP -> topWall = false;
            case RIGHT -> rightWall = false;
            case DOWN -> bottomWall = false;
            case LEFT -> leftWall = false;
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Cell other)) {
            return false;
        }
        return row == other.row && col == other.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}

package org.example.renderer;

import org.example.model.Cell;
import org.example.model.Direction;
import org.example.model.Maze;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MazeRenderer {
    public void render(Maze maze, List<Cell> path) {
        Set<Cell> pathCells = new HashSet<>(path);
        for (int col = 0; col < maze.getCols(); col++) {
            System.out.print("+---");
        }
        System.out.println("+");

        for (int row = 0; row < maze.getRows(); row++) {
            StringBuilder content = new StringBuilder("|");
            StringBuilder bottom = new StringBuilder("+");
            for (int col = 0; col < maze.getCols(); col++) {
                Cell cell = maze.getCell(row, col);
                char marker = cell.equals(maze.getEntrance()) ? 'S'
                        : cell.equals(maze.getExit()) ? 'E'
                        : pathCells.contains(cell) ? '*' : ' ';
                content.append(' ').append(marker).append(' ')
                        .append(cell.hasWall(Direction.RIGHT) ? '|' : ' ');
                bottom.append(cell.hasWall(Direction.DOWN) ? "---" : "   ").append("+");
            }
            System.out.println(content);
            System.out.println(bottom);
        }
    }
}

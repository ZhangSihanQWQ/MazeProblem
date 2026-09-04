package org.example;

import org.example.generator.MazeGenerator;
import org.example.model.Cell;
import org.example.model.Maze;
import org.example.renderer.MazeRenderer;
import org.example.solver.MazeSolver;
import org.example.statistics.GameStatistics;
import org.example.validator.MazeValidator;

import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        int rows = args.length > 0 ? parsePositiveInt(args[0], "行数") : 5;
        int cols = args.length > 1 ? parsePositiveInt(args[1], "列数") : 5;
        boolean perfect = args.length > 2 && parsePerfectFlag(args[2]);

        Maze maze = new Maze(rows, cols);
        MazeGenerator generator = new MazeGenerator();
        long generationStart = System.nanoTime();
        generator.generate(maze, perfect);
        long generationTime = System.nanoTime() - generationStart;

        maze.setEntrance(0, 0);
        setRandomExit(maze, new Random());

        MazeSolver solver = new MazeSolver();
        long searchStart = System.nanoTime();
        List<Cell> path = solver.findPath(maze);
        long searchTime = System.nanoTime() - searchStart;

        new MazeRenderer().render(maze, path);
        MazeValidator validator = new MazeValidator();
        System.out.println("生成模式: " + (perfect ? "完美迷宫" : "普通连通迷宫"));
        System.out.println("完美迷宫验证: " + (validator.isPerfectMaze(maze) ? "通过" : "未通过"));
        System.out.println(new GameStatistics(
                maze, solver.getVisitedCount(), generationTime, searchTime, path
        ));
    }

    private static int parsePositiveInt(String value, String name) {
        try {
            int result = Integer.parseInt(value);
            if (result <= 0) {
                throw new NumberFormatException();
            }
            return result;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(name + "必须是正整数: " + value);
        }
    }

    private static boolean parsePerfectFlag(String value) {
        if ("--perfect".equalsIgnoreCase(value)) {
            return true;
        }
        throw new IllegalArgumentException("第三个参数只能是 --perfect: " + value);
    }

    private static void setRandomExit(Maze maze, Random random) {
        if (maze.getRows() * maze.getCols() == 1) {
            maze.setExit(0, 0);
            return;
        }

        int exitRow;
        int exitCol;
        do {
            exitRow = random.nextInt(maze.getRows());
            exitCol = random.nextInt(maze.getCols());
        } while (exitRow == maze.getEntrance().getRow()
                && exitCol == maze.getEntrance().getCol());
        maze.setExit(exitRow, exitCol);
    }
}

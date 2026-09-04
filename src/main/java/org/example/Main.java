package org.example;

import org.example.generator.MazeGenerator;
import org.example.model.Cell;
import org.example.model.Maze;
import org.example.renderer.MazeRenderer;
import org.example.solver.MazeSolver;
import org.example.statistics.GameStatistics;
import org.example.util.SeedUtil;
import org.example.validator.MazeValidator;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        int rows = args.length > 0 ? parsePositiveInt(args[0], "行数") : 5;
        int cols = args.length > 1 ? parsePositiveInt(args[1], "列数") : 5;
        RunOptions options = parseOptions(args);
        String seedText = options.seedText() == null
                ? UUID.randomUUID().toString()
                : options.seedText();
        Random random = new Random(SeedUtil.toLong(seedText));

        Maze maze = new Maze(rows, cols);
        MazeGenerator generator = new MazeGenerator(random);
        long generationStart = System.nanoTime();
        generator.generate(maze, options.perfect());
        long generationTime = System.nanoTime() - generationStart;

        maze.setEntrance(0, 0);
        setRandomExit(maze, random);

        MazeSolver bfsSolver = new MazeSolver();
        long bfsStart = System.nanoTime();
        List<Cell> bfsPath = bfsSolver.findPath(maze);
        long bfsTime = System.nanoTime() - bfsStart;

        MazeSolver dfsSolver = new MazeSolver();
        long dfsStart = System.nanoTime();
        List<Cell> dfsPath = dfsSolver.findPathByDfs(maze);
        long dfsTime = System.nanoTime() - dfsStart;

        new MazeRenderer().render(maze, bfsPath);
        MazeValidator validator = new MazeValidator();
        System.out.println("生成模式: " + (options.perfect() ? "完美迷宫" : "普通连通迷宫"));
        System.out.println("种子: seed-" + seedText);
        System.out.println("完美迷宫验证: " + (validator.isPerfectMaze(maze) ? "通过" : "未通过"));
        System.out.println(new GameStatistics(
                maze,
                bfsSolver.getVisitedCount(),
                generationTime,
                bfsTime,
                bfsPath,
                dfsSolver.getVisitedCount(),
                dfsTime,
                dfsPath
        ));
    }

    private static RunOptions parseOptions(String[] args) {
        boolean perfect = false;
        String seedText = null;
        for (int index = 2; index < args.length; index++) {
            String argument = args[index];
            if ("--perfect".equalsIgnoreCase(argument)) {
                perfect = true;
            } else if (argument.regionMatches(true, 0, "seed-", 0, 5)
                    && argument.length() > 5) {
                seedText = argument.substring(5);
            } else {
                throw new IllegalArgumentException(
                        "可选参数只能是 --perfect 或 seed-<种子>: " + argument
                );
            }
        }
        return new RunOptions(perfect, seedText);
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

    private record RunOptions(boolean perfect, String seedText) {
    }
}

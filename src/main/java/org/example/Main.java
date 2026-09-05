package org.example;

import org.example.generator.MazeGenerator;
import org.example.model.Cell;
import org.example.model.Maze;
import org.example.renderer.MazeRenderer;
import org.example.solver.AStarMazeSolver;
import org.example.solver.GeometricPathResult;
import org.example.solver.MazeSolver;
import org.example.solver.PathMetrics;
import org.example.solver.SearchAlgorithm;
import org.example.solver.TurnPenaltyModel;
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
        // 没有显式传入 seed 时生成一个 UUID，并打印出来，方便下次复现同一个迷宫。
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
        // 出口保持随机，但不能和入口重合；1x1 迷宫在方法内部单独处理。
        setRandomExit(maze, random);

        List<Cell> displayPath;
        int visitedStates;
        long searchTime;
        List<Cell> cellPath;
        double totalDistance = Double.NaN;
        double turnPenalty = Double.NaN;
        double travelTime = Double.NaN;
        if (options.searchAlgorithm() == SearchAlgorithm.BFS) {
            // BFS 先找经过格子数最少的路径，再单独计算这条路径的路程和转弯损耗。
            MazeSolver solver = new MazeSolver();
            long searchStart = System.nanoTime();
            cellPath = solver.findPath(maze);
            searchTime = System.nanoTime() - searchStart;
            visitedStates = solver.getVisitedCount();
            displayPath = cellPath;
            PathMetrics metrics = TurnPenaltyModel.calculateCellPathMetrics(cellPath);
            totalDistance = metrics.distance();
            turnPenalty = metrics.turnPenalty();
            travelTime = metrics.travelTime();
        } else {
            // A* 在搜索过程中直接把路程时间和转弯损耗放进代价函数。
            AStarMazeSolver solver = new AStarMazeSolver();
            long searchStart = System.nanoTime();
            GeometricPathResult result = solver.findShortestPath(maze);
            searchTime = System.nanoTime() - searchStart;
            visitedStates = result.visitedStates();
            totalDistance = result.distance();
            turnPenalty = result.turnPenalty();
            travelTime = result.travelTime();
            cellPath = toCellPath(maze, result);
            displayPath = cellPath;
        }
        new MazeRenderer().render(maze, displayPath);
        MazeValidator validator = new MazeValidator();
        System.out.println("生成模式: " + (options.perfect() ? "完美迷宫" : "普通连通迷宫"));
        System.out.println("种子: seed-" + seedText);
        System.out.println("最短路径算法: " + options.searchAlgorithm().getDisplayName());
        System.out.println("完美迷宫验证: " + (validator.isPerfectMaze(maze) ? "通过" : "未通过"));
        System.out.println(new GameStatistics(
                maze,
                options.searchAlgorithm(),
                visitedStates,
                generationTime,
                searchTime,
                cellPath,
                totalDistance,
                turnPenalty,
                travelTime
        ));
    }

    private static RunOptions parseOptions(String[] args) {
        boolean perfect = false;
        String seedText = null;
        SearchAlgorithm searchAlgorithm = null;
        for (int index = 2; index < args.length; index++) {
            String argument = args[index];
            if ("-perfect".equalsIgnoreCase(argument)) {
                perfect = true;
            } else if (argument.regionMatches(true, 0, "seed-", 0, 5)
                    && argument.length() > 5) {
                seedText = argument.substring(5);
            } else if ("-bfs".equalsIgnoreCase(argument)) {
                if (searchAlgorithm != null && searchAlgorithm != SearchAlgorithm.BFS) {
                    throw new IllegalArgumentException("-bfs 和 -astar 只能选择一个");
                }
                searchAlgorithm = SearchAlgorithm.BFS;
            } else if ("-astar".equalsIgnoreCase(argument)) {
                if (searchAlgorithm != null && searchAlgorithm != SearchAlgorithm.ASTAR) {
                    throw new IllegalArgumentException("-bfs 和 -astar 只能选择一个");
                }
                searchAlgorithm = SearchAlgorithm.ASTAR;
            } else {
                throw new IllegalArgumentException(
                        "可选参数只能是 -perfect、seed-<种子>、-bfs 或 -astar: " + argument
                );
            }
        }
        return new RunOptions(
                perfect,
                seedText,
                searchAlgorithm == null ? SearchAlgorithm.BFS : searchAlgorithm
        );
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

    private static List<Cell> toCellPath(Maze maze, GeometricPathResult result) {
        // A* 返回的是几何节点路径；渲染时需要转换成经过的格子，用 * 标记。
        java.util.LinkedHashSet<Cell> cells = new java.util.LinkedHashSet<>();
        for (var node : result.path()) {
            int column = (int) Math.floor(node.x());
            int row = (int) Math.floor(node.y());
            boolean verticalBoundary = Math.abs(node.x() - Math.rint(node.x())) < 0.000001;
            boolean horizontalBoundary = Math.abs(node.y() - Math.rint(node.y())) < 0.000001;
            if (!verticalBoundary && !horizontalBoundary) {
                addCell(cells, maze.getCell((int) Math.floor(node.y()), (int) Math.floor(node.x())));
            } else if (verticalBoundary && !horizontalBoundary) {
                addCell(cells, maze.getCell(row, column - 1));
                addCell(cells, maze.getCell(row, column));
            } else if (!verticalBoundary) {
                addCell(cells, maze.getCell(row - 1, column));
                addCell(cells, maze.getCell(row, column));
            }
        }
        return List.copyOf(cells);
    }

    private static void addCell(java.util.Set<Cell> cells, Cell cell) {
        if (cell != null) {
            cells.add(cell);
        }
    }

    private record RunOptions(
            boolean perfect,
            String seedText,
            SearchAlgorithm searchAlgorithm
    ) {
    }
}

package org.example.solver;

import org.example.model.Cell;
import org.example.model.Direction;

import java.util.List;

public final class TurnPenaltyModel {
    public static final double SPEED_UNITS_PER_SECOND = 1.0;
    public static final double TURN_LOSS_COEFFICIENT_SECONDS = 0.25;

    private TurnPenaltyModel() {
    }

    public static double calculateTurnPenalty(int headingSteps) {
        // 速度向量夹角为 theta 时，|Δv| / v = 2sin(theta/2)，据此估计时间损耗。
        double angle = headingSteps * Math.PI / 4.0;
        double velocityChangeRatio = 2.0 * Math.sin(angle / 2.0);
        return TURN_LOSS_COEFFICIENT_SECONDS * velocityChangeRatio;
    }

    public static PathMetrics calculateCellPathMetrics(List<Cell> path) {
        if (path.isEmpty()) {
            return new PathMetrics(
                    Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY
            );
        }

        double distance = Math.max(0, path.size() - 1);
        double turnPenalty = 0.0;
        // BFS 的路径按格子中心一步步走，所以每两个连续方向形成一次转弯。
        for (int index = 2; index < path.size(); index++) {
            int previousHeading = headingBetween(path.get(index - 2), path.get(index - 1));
            int currentHeading = headingBetween(path.get(index - 1), path.get(index));
            turnPenalty += calculateTurnPenalty(
                    turnDistance(previousHeading, currentHeading)
            );
        }
        return new PathMetrics(
                distance,
                turnPenalty,
                distance / SPEED_UNITS_PER_SECOND + turnPenalty
        );
    }

    public static int headingBetween(Cell from, Cell to) {
        int deltaRow = to.getRow() - from.getRow();
        int deltaCol = to.getCol() - from.getCol();
        for (Direction direction : Direction.values()) {
            if (direction.getDeltaRow() == deltaRow
                    && direction.getDeltaCol() == deltaCol) {
                return switch (direction) {
                    case RIGHT -> 0;
                    case DOWN -> 2;
                    case LEFT -> 4;
                    case UP -> 6;
                };
            }
        }
        throw new IllegalArgumentException("路径中的两个格子必须相邻");
    }

    public static int turnDistance(int firstHeading, int secondHeading) {
        int difference = Math.abs(firstHeading - secondHeading);
        return Math.min(difference, 8 - difference);
    }
}

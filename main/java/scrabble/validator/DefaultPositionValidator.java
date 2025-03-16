package scrabble.validator;

import scrabble.Board.Board;
import scrabble.placement.BoardOperator;
import scrabble.placement.PlaceTile.TilePlacement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * DefaultPositionValidator - 实现位置验证接口，确保字母牌放置符合Scrabble规则
 */
public class DefaultPositionValidator implements PositionValidator {
    private final BoardOperator boardOperator;

    /**
     * 创建一个新的位置验证器
     * @param boardOperator 棋盘操作器
     */
    public DefaultPositionValidator(BoardOperator boardOperator) {
        this.boardOperator = boardOperator;
    }

    /**
     * 验证字母牌位置是否有效
     * @param placements 当前回合的字母牌放置列表
     * @return 如果放置位置有效则返回true，否则返回false
     */
    @Override
    public boolean validatePositions(List<TilePlacement> placements) {
        if (placements == null || placements.isEmpty()) {
            return false;
        }

        // 检查所有放置是否在一条直线上（水平或垂直）
        boolean isHorizontal = isHorizontalPlacement(placements);
        boolean isVertical = isVerticalPlacement(placements);

        if (!isHorizontal && !isVertical) {
            return false; // 字母牌必须在一条直线上
        }

        // 确保放置的字母牌之间没有间隙
        if (!areConsecutive(placements, isHorizontal)) {
            return false;
        }

        // 检查是否是第一步（棋盘为空）
        boolean isBoardEmpty = isBoardEmpty();

        // 如果是第一步，检查是否有字母放在中心格
        if (isBoardEmpty) {
            boolean centerCellCovered = false;
            for (TilePlacement placement : placements) {
                if (placement.getRow() == 7 && placement.getCol() == 7) {
                    centerCellCovered = true;
                    break;
                }
            }
            return centerCellCovered;
        }

        // 如果不是第一步，检查是否与现有的字母牌连接
        return connectsWithExistingTiles(placements);
    }

    /**
     * 检查所有放置是否水平（同一行）
     * @param placements 放置列表
     * @return 如果所有放置都在同一行返回true
     */
    private boolean isHorizontalPlacement(List<TilePlacement> placements) {
        if (placements.size() == 1) {
            return true; // 单个放置可以被视为水平
        }

        int row = placements.get(0).getRow();
        for (int i = 1; i < placements.size(); i++) {
            if (placements.get(i).getRow() != row) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查所有放置是否垂直（同一列）
     * @param placements 放置列表
     * @return 如果所有放置都在同一列返回true
     */
    private boolean isVerticalPlacement(List<TilePlacement> placements) {
        if (placements.size() == 1) {
            return true; // 单个放置可以被视为垂直
        }

        int col = placements.get(0).getCol();
        for (int i = 1; i < placements.size(); i++) {
            if (placements.get(i).getCol() != col) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查放置的字母牌是否连续无间隙
     * @param placements 放置列表
     * @param isHorizontal 是否水平放置
     * @return 如果放置是连续的返回true
     */
    private boolean areConsecutive(List<TilePlacement> placements, boolean isHorizontal) {
        if (placements.size() == 1) {
            return true; // 单个放置总是连续的
        }

        // 根据方向对放置进行排序
        List<TilePlacement> sortedPlacements = new ArrayList<>(placements);
        if (isHorizontal) {
            Collections.sort(sortedPlacements, Comparator.comparingInt(TilePlacement::getCol));

            int row = sortedPlacements.get(0).getRow();
            int startCol = sortedPlacements.get(0).getCol();

            for (int i = 1; i < sortedPlacements.size(); i++) {
                int expectedCol = startCol + i;
                int actualCol = sortedPlacements.get(i).getCol();

                // 检查间隙
                if (actualCol > expectedCol) {
                    // 检查间隙是否被现有字母牌填充
                    for (int col = expectedCol; col < actualCol; col++) {
                        if (!boardOperator.isCellOccupied(row, col)) {
                            return false; // 找到间隙
                        }
                    }
                }
            }
        } else {
            Collections.sort(sortedPlacements, Comparator.comparingInt(TilePlacement::getRow));

            int col = sortedPlacements.get(0).getCol();
            int startRow = sortedPlacements.get(0).getRow();

            for (int i = 1; i < sortedPlacements.size(); i++) {
                int expectedRow = startRow + i;
                int actualRow = sortedPlacements.get(i).getRow();

                // 检查间隙
                if (actualRow > expectedRow) {
                    // 检查间隙是否被现有字母牌填充
                    for (int row = expectedRow; row < actualRow; row++) {
                        if (!boardOperator.isCellOccupied(row, col)) {
                            return false; // 找到间隙
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * 检查棋盘是否为空（无占用格子）
     * @return 如果棋盘为空返回true
     */
    private boolean isBoardEmpty() {
        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int col = 0; col < Board.BOARD_SIZE; col++) {
                if (boardOperator.isCellOccupied(row, col)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 检查放置是否与棋盘上现有的字母牌连接
     * @param placements 放置列表
     * @return 如果至少有一个放置与现有字母牌连接返回true
     */
    private boolean connectsWithExistingTiles(List<TilePlacement> placements) {
        for (TilePlacement placement : placements) {
            int row = placement.getRow();
            int col = placement.getCol();

            // 检查相邻格子（上、下、左、右）
            if ((row > 0 && boardOperator.isCellOccupied(row - 1, col)) ||
                    (row < Board.BOARD_SIZE - 1 && boardOperator.isCellOccupied(row + 1, col)) ||
                    (col > 0 && boardOperator.isCellOccupied(row, col - 1)) ||
                    (col < Board.BOARD_SIZE - 1 && boardOperator.isCellOccupied(row, col + 1))) {
                return true;
            }
        }
        return false;
    }
}
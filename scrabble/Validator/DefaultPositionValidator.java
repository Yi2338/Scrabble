package scrabble.Validator;

import scrabble.Board.Board;
import scrabble.Placement.BoardOperator;
import scrabble.Placement.PlaceTile.TilePlacement;

import java.util.List;

/**
 * 位置验证器，负责验证字母牌放置是否符合Scrabble规则
 * 主要验证：
 * 1. 所有字母必须在一条直线上（水平或垂直）
 * 2. 第一次放置必须覆盖中心格
 * 3. 后续放置必须与已有字母相连
 */
public class DefaultPositionValidator implements PositionValidator {
    private final BoardOperator boardOperator;

    public DefaultPositionValidator(BoardOperator boardOperator) {
        this.boardOperator = boardOperator;
    }

    /**
     * 验证字母牌放置是否符合规则
     *
     * @param placements 当前回合的字母牌放置列表
     * @return 如果放置位置有效则返回true，否则返回false
     */
    @Override
    public boolean validatePositions(List<TilePlacement> placements) {
        if (placements == null || placements.isEmpty()) {
            return false;
        }

        // 检查所有放置是否在一条直线上（水平或垂直）
        // 根据Scrabble规则，所有新放置的字母必须在同一行或同一列
        boolean isHorizontal = isHorizontalPlacement(placements);
        boolean isVertical = isVerticalPlacement(placements);

        if (!isHorizontal && !isVertical) {
            return false; // 字母牌必须在一条直线上
        }

        // 检查是否是游戏首次放置（棋盘为空）
        boolean isBoardEmpty = isBoardEmpty();

        // 根据Scrabble规则，首次放置必须覆盖中心格
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

        // 根据Scrabble规则，后续放置必须与棋盘上已有的字母相连
        return connectsWithExistingTiles(placements);
    }

    /**
     * 检查所有放置是否水平（同一行）
     * 单个字母放置时总是返回true，因为单字母可以作为水平或垂直放置的一部分
     *
     * @param placements 放置列表
     * @return 如果所有放置都在同一行或只有一个放置返回true
     */
    private boolean isHorizontalPlacement(List<TilePlacement> placements) {
        if (placements.size() == 1) {
            // 单个放置时，方向不影响位置验证结果
            // 方向将在WordFormer中根据相邻字母确定
            return true;
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
     * 单个字母放置时总是返回true，原因同isHorizontalPlacement
     *
     * @param placements 放置列表
     * @return 如果所有放置都在同一列或只有一个放置返回true
     */
    private boolean isVerticalPlacement(List<TilePlacement> placements) {
        if (placements.size() == 1) {
            // 单个放置可被视为垂直方向，实际方向由相邻字母决定
            return true;
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
     * 检查棋盘是否为空（无占用格子）
     * 用于确定是否为游戏首次放置
     *
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
     * 检查放置是否与棋盘上现有的字母牌相连
     * 根据Scrabble规则，新放置的字母必须至少与一个已有字母相邻
     *
     * @param placements 放置列表
     * @return 如果至少有一个放置与现有字母牌相连返回true
     */
    private boolean connectsWithExistingTiles(List<TilePlacement> placements) {
        // 检查是否至少有一个新放置的字母与现有字母相邻（上下左右）
        for (TilePlacement placement : placements) {
            int row = placement.getRow();
            int col = placement.getCol();

            // 检查四个相邻位置是否有已存在的字母
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
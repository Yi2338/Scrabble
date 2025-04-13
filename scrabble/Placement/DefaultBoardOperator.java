package scrabble.Placement;


import scrabble.Board.Board;
import scrabble.Board.Cell;
import scrabble.Tile.Tile;

/**
 * DefaultBoardOperator
 */
public class DefaultBoardOperator implements BoardOperator {
    private final Board board;

    public DefaultBoardOperator(Board board) {
        this.board = board;
    }

    /**
     * 放置指定字母牌到指定位置
     * @param tile 字母牌
     * @param row 行
     * @param col 列
     * @return 成功放置则返回true，失败则返回false(超出棋盘直接返回false)
     */
    @Override
    public boolean placeTileOnBoard(Tile tile, int row, int col) {
        if (isOutOfBounds(row, col)) {
            return false;
        }

        // 直接访问并修改原始棋盘
        return board.getGrid()[row][col].placeTile(tile);
    }

    /**
     *移除指定格子处的字母牌
     * @param row 行
     * @param col 列
     * @return 返回被移除的字母牌(超出棋盘直接返回false)
     */
    @Override
    public Tile removeTileFromBoard(int row, int col) {
        if (isOutOfBounds(row, col)) {
            return null;
        }

        // 直接访问并修改原始棋盘
        return board.getGrid()[row][col].removeTile();
    }

    /**
     * 检查当前格子是否有字母牌
     * @param row
     * @param col
     * @return 有就返回ture，反之返回false(超出棋盘直接返回false)
     */
    @Override
    public boolean isCellOccupied(int row, int col) {
        if (isOutOfBounds(row, col)) {
            return false;
        }

        // 直接检查原始棋盘
        return board.getGrid()[row][col].hasTile();
    }

    /**
     * 获取当前格子的直接引用
     * @param row
     * @param col
     * @return 返回当前位置格子的直接引用
     */
    @Override
    public Cell getCell(int row, int col) {
        if (isOutOfBounds(row, col)) {
            return null;
        }

        // 直接返回原始棋盘单元格
        return board.getGrid()[row][col];
    }

    private boolean isOutOfBounds(int row, int col) {
        return row < 0 || row >= Board.BOARD_SIZE || col < 0 || col >= Board.BOARD_SIZE;
    }
}
package scrabble.Placement;

import scrabble.Board.Cell;
import scrabble.Tile.Tile;


/**
 * BoardOperator接口
 */
public interface BoardOperator {
    /**
     * 在指定位置放置字母牌到棋盘上
     */
    boolean placeTileOnBoard(Tile tile, int row, int col);

    /**
     * 从棋盘指定位置移除字母牌
     */
    Tile removeTileFromBoard(int row, int col);

    /**
     * 检查单元格是否被字母牌占用
     */
    boolean isCellOccupied(int row, int col);

    /**
     * 获取指定位置的单元格
     */
    Cell getCell(int row, int col);
}

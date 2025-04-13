package scrabble.Board;

import scrabble.Tile.Tile;
import java.io.Serializable;

/**
 * Cell 类表示 Scrabble 游戏棋盘上的一个格子。
 * 每个格子都有特定的位置（行和列）、类型（普通、加倍等）以及可能放置的字母牌。
 */
public class Cell implements Serializable {
    /** 序列化ID */
    private static final long serialVersionUID = 1L;
    
    /** 格子所在的行号 */
    private final int row;
    /** 格子所在的列号 */
    private final int col;
    /** 格子的类型（普通、双倍字母等） */
    private CellType cellType;
    /** 格子上放置的字母牌 */
    private Tile tile;

    /**
     * 创建一个指定位置和类型的格子
     * @param row 行号
     * @param col 列号
     * @param cellType 格子类型
     */
    public Cell(int row, int col, CellType cellType) {
        this.row = row;
        this.col = col;
        this.cellType = cellType;
        this.tile = null;
    }

    /**
     * 创建一个指定位置的普通格子
     * @param row 行号
     * @param col 列号
     */
    public Cell(int row, int col) {
        this(row, col, CellType.NONE);
    }

    /**
     * 设置格子的类型
     * @param cellType 新的格子类型
     */
    public void setCellType(CellType cellType) {
        this.cellType = cellType;
    }

    /**
     * 获取格子的类型
     * @return 格子的类型
     */
    public CellType getCellType() {
        return cellType;
    }

    /**
     * 获取格子的位置坐标
     * @return 包含行号和列号的整数数组
     */
    public int[] getPosition() {
        return new int[]{row, col};
    }

    /**
     * 获取格子的行号
     * @return 行号
     */
    public int getRow() {
        return row;
    }

    /**
     * 获取格子的列号
     * @return 列号
     */
    public int getCol() {
        return col;
    }

    /**
     * 创建当前格子的一个副本
     * @return 一个新的具有相同属性的Cell对象
     */
    public Cell copy() {
        Cell copy = new Cell(this.row, this.col, this.cellType);
        if (this.tile != null) {
            copy.tile = this.tile.copy();
        }
        return copy;
    }

    /**
     * 在格子上放置字母牌
     * @param tile 要放置的字母牌
     * @return 如果放置成功返回true，如果格子已有字母牌则返回false
     */
    public boolean placeTile(Tile tile) {
        if (this.tile != null) {
            return false;
        }
        this.tile = tile;
        return true;
    }

    /**
     * 移除格子上的字母牌
     * @return 被移除的字母牌，如果格子上没有字母牌则返回null
     */
    public Tile removeTile() {
        Tile removed = this.tile;
        this.tile = null;
        return removed;
    }

    /**
     * 检查格子上是否有字母牌
     * @return 如果有字母牌返回true，否则返回false
     */
    public boolean hasTile() {
        return tile != null;
    }

    /**
     * 获取格子上的字母牌
     * @return 格子上的字母牌，如果没有则返回null
     */
    public Tile getTile() {
        return tile;
    }

    /**
     * 获取字母分值的倍数
     * @return 字母分值的倍数（1、2或3）
     */
    public int getLetterMultiplier() {
        if (cellType == CellType.DOUBLE_LETTER) return 2;
        if (cellType == CellType.TRIPLE_LETTER) return 3;
        return 1;
    }

    /**
     * 获取单词分值的倍数
     * @return 单词分值的倍数（1、2或3）
     */
    public int getWordMultiplier() {
        if (cellType == CellType.DOUBLE_WORD) return 2;
        if (cellType == CellType.TRIPLE_WORD) return 3;
        return 1;
    }

    /**
     * 返回格子的字符串表示
     * @return 包含格子位置和类型信息的字符串
     */
    @Override
    public String toString() {
        return "position:(" + row + "," + col + ")\n"
                + "celltype:" + cellType;
    }
}


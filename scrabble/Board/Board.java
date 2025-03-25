package scrabble.Board;

/**
 * Board 类表示 Scrabble 游戏的棋盘。
 * 棋盘由 15x15 的格子组成，包括普通格子和特殊加分格子（如双倍字母、三倍单词等）。
 */
public class Board {
    /** 棋盘的大小（15x15） */
    public static final int BOARD_SIZE = 15;
    
    /** 棋盘的二维网格，每个元素都是一个Cell对象 */
    private final Cell[][] grid;

    /**
     * 创建一个新的 Scrabble 棋盘
     * 初始化所有格子并设置特殊格子的类型
     */
    public Board() {
        grid = new Cell[BOARD_SIZE][BOARD_SIZE];
        initBoard();
    }

    /**
     * 初始化棋盘
     * 创建所有格子并设置它们的初始类型为NONE
     */
    public void initBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                grid[row][col] = new Cell(row, col, CellType.NONE);
            }
        }
        setupSpecialCells();
    }

    /**
     * 设置棋盘上的特殊格子
     * 包括三倍单词分值、双倍单词分值、三倍字母分值、双倍字母分值和中心格子
     */
    private void setupSpecialCells() {
        // 三倍词值格子 (TW)
        int[][] twCells = {
                {0, 0}, {0, 7}, {0, 14},
                {7, 0}, {7, 14},
                {14, 0}, {14, 7}, {14, 14}
        };

        // 二倍词值格子 (DW)
        int[][] dwCells = {
                {1, 1}, {1, 13}, {2, 2}, {2, 12}, {3, 3}, {3, 11}, {4, 4}, {4, 10},
                {7, 7},
                {10, 4}, {10, 10}, {11, 3}, {11, 11}, {12, 2}, {12, 12}, {13, 1}, {13, 13}
        };

        // 三倍字母值格子 (TL)
        int[][] tlCells = {
                {1, 5}, {1, 9}, {5, 1}, {5, 5}, {5, 9}, {5, 13},
                {9, 1}, {9, 5}, {9, 9}, {9, 13}, {13, 5}, {13, 9}
        };

        // 二倍字母值格子 (DL)
        int[][] dlCells = {
                {0, 3}, {0, 11}, {2, 6}, {2, 8}, {3, 0}, {3, 7}, {3, 14},
                {6, 2}, {6, 6}, {6, 8}, {6, 12}, {7, 3}, {7, 11},
                {8, 2}, {8, 6}, {8, 8}, {8, 12}, {11, 0}, {11, 7}, {11, 14},
                {12, 6}, {12, 8}, {14, 3}, {14, 11}
        };

        // 设置三倍词值格子
        for (int[] pos : twCells) {
            grid[pos[0]][pos[1]].setCellType(CellType.TRIPLE_WORD);
        }

        // 设置二倍词值格子
        for (int[] pos : dwCells) {
            grid[pos[0]][pos[1]].setCellType(CellType.DOUBLE_WORD);
        }

        // 设置三倍字母值格子
        for (int[] pos : tlCells) {
            grid[pos[0]][pos[1]].setCellType(CellType.TRIPLE_LETTER);
        }

        // 设置二倍字母值格子
        for (int[] pos : dlCells) {
            grid[pos[0]][pos[1]].setCellType(CellType.DOUBLE_LETTER);
        }

        // 设置中心格子
        grid[7][7].setCellType(CellType.CENTER);
    }

    /**
     * 获取棋盘原始二维数组（非副本）
     * @return 棋盘的二维单元格数组
     */
    public Cell[][] getGrid() {
        return grid;
    }

    /**
     * 获取棋盘的深拷贝
     * @return 一个新的二维Cell数组，包含所有格子的副本
     */
    public Cell[][] getGridCopy() {
        Cell[][] copy = new Cell[BOARD_SIZE][BOARD_SIZE];
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                copy[row][col] = grid[row][col].copy();
            }
        }
        return copy;
    }
}

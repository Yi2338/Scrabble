package scrabble.Board;

/**
 * CellType 枚举定义了 Scrabble 游戏棋盘上所有可能的格子类型。
 * 包括普通格子、双倍字母分、三倍字母分、双倍单词分、三倍单词分和中心格。
 */
public enum CellType {
    /** 普通格子，无特殊加分 */
    NONE,
    /** 双倍字母分格子 */
    DOUBLE_LETTER,
    /** 三倍字母分格子 */
    TRIPLE_LETTER,
    /** 双倍单词分格子 */
    DOUBLE_WORD,
    /** 三倍单词分格子 */
    TRIPLE_WORD,
    /** 棋盘中心格子 */
    CENTER
}

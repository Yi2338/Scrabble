package scrabble.logging;

/**
 * Scrabble游戏中的事件类型枚举
 * 定义了游戏过程中可能发生的所有事件类型
 */
public enum GameEventType {
    /** 游戏开始事件 */
    GAME_START,
    /** 游戏结束事件 */
    GAME_END,
    /** 玩家回合开始事件 */
    TURN_START,
    /** 玩家回合结束事件 */
    TURN_END,
    /** 字母牌放置事件 */
    TILE_PLACEMENT,
    /** 字母牌移动事件 */
    TILE_MOVEMENT,
    /** 字母牌返回事件 */
    TILE_RETURN,
    /** 确认字母牌放置事件 */
    PLACEMENT_CONFIRM,
    /** 取消字母牌放置事件 */
    PLACEMENT_CANCEL,
    /** 单词验证事件 */
    WORD_VALIDATED,
    /** 分数计算事件 */
    SCORE_CALCULATED
} 
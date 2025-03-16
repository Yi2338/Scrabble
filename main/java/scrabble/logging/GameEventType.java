package scrabble.logging;

/**
 * 游戏事件类型枚举
 * 定义了所有可能的游戏事件类型
 */
public enum GameEventType {
    TILE_PLACEMENT,    // 放置字母牌
    TILE_MOVEMENT,     // 移动字母牌
    TILE_RETURN,       // 返回字母牌到架子
    PLACEMENT_CONFIRM, // 确认放置
    PLACEMENT_CANCEL,  // 取消放置
    GAME_START,        // 游戏开始
    GAME_END,         // 游戏结束
    TURN_START,       // 回合开始
    TURN_END,         // 回合结束
    DEBUG,            // 调试信息
    INFO,             // 普通信息
    WARN,             // 警告信息
    ERROR            // 错误信息
} 
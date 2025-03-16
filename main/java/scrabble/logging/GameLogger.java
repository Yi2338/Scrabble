package scrabble.logging;

import scrabble.Tile.Tile;
import java.util.List;

/**
 * Scrabble游戏日志记录接口
 * 提供了一组方法用于记录游戏中的各种事件，包括：
 * - 字母牌的放置、移动和返回
 * - 回合的确认和取消
 * - 游戏的开始和结束
 * - 玩家回合的开始和结束
 * - 通用日志记录功能
 */
public interface GameLogger {
    /**
     * 记录字母牌放置事件
     * @param player 执行放置的玩家
     * @param source 字母牌的来源（如："RACK"或"BOARD"）
     * @param row 目标行坐标
     * @param col 目标列坐标
     * @param tile 被放置的字母牌
     */
    void logTilePlacement(Object player, String source, int row, int col, Tile tile);

    /**
     * 记录字母牌移动事件
     * @param player 执行移动的玩家
     * @param fromRow 起始行坐标
     * @param fromCol 起始列坐标
     * @param toRow 目标行坐标
     * @param toCol 目标列坐标
     * @param tile 被移动的字母牌
     */
    void logTileMovement(Object player, int fromRow, int fromCol, int toRow, int toCol, Tile tile);

    /**
     * 记录字母牌返回事件（通常是返回到玩家的牌架）
     * @param player 执行返回的玩家
     * @param row 字母牌原始行坐标
     * @param col 字母牌原始列坐标
     * @param rackIndex 牌架上的目标位置
     * @param tile 被返回的字母牌
     */
    void logTileReturn(Object player, int row, int col, int rackIndex, Tile tile);
    
    /**
     * 记录玩家确认放置字母牌事件
     * @param player 确认放置的玩家
     * @param words 形成的单词列表
     * @param score 本次放置获得的分数
     */
    void logPlacementConfirmation(Object player, List<String> words, int score);

    /**
     * 记录玩家取消放置字母牌事件
     * @param player 取消放置的玩家
     */
    void logPlacementCancellation(Object player);
    
    /**
     * 记录游戏开始事件
     * @param players 参与游戏的玩家列表
     */
    void logGameStart(List<Object> players);

    /**
     * 记录游戏结束事件
     * @param winner 获胜的玩家
     * @param score 获胜玩家的最终分数
     */
    void logGameEnd(Object winner, int score);

    /**
     * 记录玩家回合开始事件
     * @param player 当前回合的玩家
     */
    void logTurnStart(Object player);

    /**
     * 记录玩家回合结束事件
     * @param player 结束回合的玩家
     */
    void logTurnEnd(Object player);
    
    /**
     * 记录调试级别的日志信息
     * @param message 日志消息
     * @param params 可变参数列表，用于格式化消息
     */
    void debug(String message, Object... params);

    /**
     * 记录信息级别的日志信息
     * @param message 日志消息
     * @param params 可变参数列表，用于格式化消息
     */
    void info(String message, Object... params);

    /**
     * 记录警告级别的日志信息
     * @param message 日志消息
     * @param params 可变参数列表，用于格式化消息
     */
    void warn(String message, Object... params);

    /**
     * 记录错误级别的日志信息
     * @param message 日志消息
     * @param throwable 异常对象
     * @param params 可变参数列表，用于格式化消息
     */
    void error(String message, Throwable throwable, Object... params);
} 
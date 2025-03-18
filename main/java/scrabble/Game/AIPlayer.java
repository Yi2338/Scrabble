package scrabble.Game;

import java.util.List;

/**
 * AI玩家接口，定义AI在Scrabble游戏中的行为
 */
public interface AIPlayer {
    /**
     * 计算并返回AI的最佳移动
     * @param board 当前游戏棋盘
     * @return 最佳移动的描述
     */
    String findBestMove(Object board);

    /**
     * 评估当前棋盘状态的分数
     * @param board 当前游戏棋盘
     * @return 评估分数，越高表示对AI越有利
     */
    int evaluatePosition(Object board);

    /**
     * 设置AI的难度级别
     * @param level 难度级别（如1=简单，2=中等，3=困难）
     */
    void setDifficulty(int level);

    /**
     * 根据当前字母架和棋盘状态生成单词
     * @param tiles 当前可用的字母牌
     * @param board 当前游戏棋盘
     * @return 生成的单词和放置位置
     */
    String generateWord(List<Object> tiles, Object board);
}
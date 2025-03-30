package scrabble.Score;

import scrabble.Placement.PlaceTile.TilePlacement;
import java.util.List;

/**
 * ScoreCalculator 接口 - 定义计算 Scrabble 游戏分数的方法
 */
public interface ScoreCalculator {
    /**
     * 计算给定单词列表的总分
     * @param words 单词列表
     * @param placements 字母牌放置列表
     * @return 总分
     */
    int calculateScore(List<String> words, List<TilePlacement> placements);

    /**
     * 检查当前回合是否获得七字母奖励
     * @param placements 当前回合的放置列表
     * @return 是否获得奖励
     */
    boolean isBingo(List<TilePlacement> placements);

    /**
     * 设置七字母奖励的分值
     * @param bonus 奖励分值
     */
    void setBingoBonusValue(int bonus);

    /**
     * 获取当前设置的七字母奖励分值
     * @return 奖励分值
     */
    int getBingoBonusValue();

    /**
     * 设置是否启用七字母奖励
     * @param enabled 是否启用
     */
    void setBingoEnabled(boolean enabled);

    /**
     * 检查七字母奖励是否启用
     * @return 是否启用
     */
    boolean isBingoEnabled();
}
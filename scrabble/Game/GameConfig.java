package scrabble.Game;

import java.io.Serializable;

/**
 * 游戏配置类，用于存储和管理Scrabble游戏的各种设置参数
 */
public class GameConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 达到此分数游戏结束 */
    private int targetScore;
    /** 回合时间限制（秒） */
    private int timeLimit;
    /** 当前使用的词典语言 */
    private String dictionaryLanguage;

    /**
     * 创建具有默认值的游戏配置
     */
    public GameConfig(){
        this.targetScore=200;
        this.timeLimit=30;//分钟
        this.dictionaryLanguage="english";
    }

    /**
     * 自定义版（英语）
     * @param targetScore
     * @param timeLimit
     */
    public GameConfig(int targetScore,int timeLimit){
        this.targetScore=targetScore;
        this.timeLimit=timeLimit;
    }

    /**Getter和Setter方法*/
    public void setTargetScore(int targetScore) {
        if (targetScore > 0) {
            this.targetScore = targetScore;
        }
    }

    public int getTargetScore() {
        return targetScore;
    }

    public void setTimeLimit(int timeLimit) {
        if (timeLimit >= 0) {
            this.timeLimit = timeLimit;
        }
    }

    public int getTimeLimit() {
        return timeLimit;
    }
}
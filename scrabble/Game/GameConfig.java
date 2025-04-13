package scrabble.Game;

import java.io.Serializable;
import scrabble.AIPlayer.AIPlayer;

/**
 * 游戏配置类，用于存储和管理Scrabble游戏的各种设置参数
 */
public class GameConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 达到此分数游戏结束 */
    private int targetScore;
    /** 游戏总时间限制（分钟） */
    private int timeLimit;
    /** 回合时间限制（秒） */
    private int turnTimeLimit;
    /** 当前使用的词典语言 */
    private String dictionaryLanguage;
    /** AI玩家难度 */
    private AIPlayer.Difficulty aiDifficulty;

    /**
     * 创建具有默认值的游戏配置
     */
    public GameConfig(){
        this.targetScore =100;
        this.timeLimit = 30; // 游戏总时间限制（分钟）
        this.turnTimeLimit = 90; // 默认1.5分钟（90秒）
        this.dictionaryLanguage = "english";
        this.aiDifficulty = AIPlayer.Difficulty.NORMAL; // 默认普通难度
    }

    /**
     * 简易自定义版（英语）
     * @param targetScore 目标分数
     * @param timeLimit 游戏总时间限制（分钟）
     */
    public GameConfig(int targetScore, int timeLimit){
        this.targetScore = targetScore;
        this.timeLimit = timeLimit;
        this.turnTimeLimit = 90; // 默认1.5分钟（90秒）
        this.dictionaryLanguage = "english";
        this.aiDifficulty = AIPlayer.Difficulty.NORMAL; // 默认普通难度
    }

    /**
     * 半自定义版
     * @param targetScore 目标分数
     * @param timeLimit 游戏总时间限制（分钟）
     * @param turnTimeLimit 回合时间限制（秒）
     * @param dictionaryLanguage 词典语言
     */
    public GameConfig(int targetScore, int timeLimit, int turnTimeLimit, String dictionaryLanguage){
        this.targetScore = targetScore;
        this.timeLimit = timeLimit;
        this.turnTimeLimit = turnTimeLimit;
        this.dictionaryLanguage = dictionaryLanguage;
        this.aiDifficulty = AIPlayer.Difficulty.NORMAL; // 默认普通难度
    }
    
    /**
     * 完整自定义版（含AI难度）
     * @param targetScore 目标分数
     * @param timeLimit 游戏总时间限制（分钟）
     * @param turnTimeLimit 回合时间限制（秒）
     * @param dictionaryLanguage 词典语言
     * @param aiDifficulty AI难度级别
     */
    public GameConfig(int targetScore, int timeLimit, int turnTimeLimit, String dictionaryLanguage, AIPlayer.Difficulty aiDifficulty){
        this.targetScore = targetScore;
        this.timeLimit = timeLimit;
        this.turnTimeLimit = turnTimeLimit;
        this.dictionaryLanguage = dictionaryLanguage;
        this.aiDifficulty = aiDifficulty;
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

    public int getTurnTimeLimit() {
        return turnTimeLimit;
    }

    public void setTurnTimeLimit(int turnTimeLimit) {
        if (turnTimeLimit >= 0) {
            this.turnTimeLimit = turnTimeLimit;
        }
    }

    public String getDictionaryLanguage() {
        return dictionaryLanguage;
    }

    public void setDictionaryLanguage(String dictionaryLanguage) {
        if (dictionaryLanguage != null && !dictionaryLanguage.isEmpty()) {
            this.dictionaryLanguage = dictionaryLanguage;
        }
    }
    

    public AIPlayer.Difficulty getAIDifficulty() {
        return aiDifficulty;
    }
    
    public void setAIDifficulty(AIPlayer.Difficulty aiDifficulty) {
        this.aiDifficulty = aiDifficulty;
    }
}
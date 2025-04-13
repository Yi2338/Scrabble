package scrabble.Game;

import java.io.Serializable;

/**
 * 玩家类
 */
public class Player implements Serializable {
    /** 序列化ID */
    private static final long serialVersionUID = 1L;
    
    /** 玩家序号*/
    private final int playerIndex;
    /** 玩家当前累计分数*/
    private int score;
    /** 玩家是否为真人*/
    private final boolean isHuman;

    /**
     * 创建一个玩家实例
     * @param playerIndex 玩家编号
     * @param isHuman 玩家是否为人类
     */
    public Player(int playerIndex, boolean isHuman) {
        this.playerIndex = playerIndex;
        this.isHuman = isHuman;
    }

    /**
     * 获取玩家序号
     * @return playerIndex 玩家的序号
     */
    public int getPlayerIndex(){return playerIndex;}

    /**
     * 获取玩家是否为人类
     * @return 玩家是人类则返回ture，反之返回false
     */
    public boolean isHuman() {return isHuman;}

    /**
     * 获取玩家当前分数
     * @return 当前分数
     */
    public int getScore() {return score;}

    /**
     * 添加分数到玩家的当前分数
     * @param points 要添加的分数点数
     */
    public void addScore(int points) {
        if (points > 0) {
            this.score += points;
        }
    }

    /**
     * 返回玩家的字符串表示
     * @return 包含玩家名称和分数的字符串
     */
    @Override
    public String toString() {
        return "player"+playerIndex + " (分数: " + score + ")";
    }

}






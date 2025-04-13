package scrabble.Game;

import scrabble.Placement.PlaceTile;
import scrabble.Tile.Tile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 表示scrabble游戏中的一个回合
 */
public class Turn implements Serializable {
    private static final long serialVersionUID = 1L;

    /**执行此回合的玩家*/
    private final Player player;
    /**此回合放置的字母牌列表*/
    private final List<PlaceTile.TilePlacement> playedTiles;
    /**此回合形成的单词列表*/
    private List<String> formedWords;
    /**次回合得分*/
    private int scoreGained;
    /**是否跳过次回合*/
    private boolean isPass;
    /**回合是否确认*/
    private boolean isConfirmed;

    /**
     * 创建一个新回合对象
     * @param player 执行此回合的玩家
     */
    public Turn(Player player){
        this.player=player;
        this.playedTiles=new ArrayList<>();
        this.formedWords=new ArrayList<>();
        this.scoreGained=0;
        this.isPass=false;
        this.isConfirmed=false;
    }

    /**
     * 记录放置字母牌的动作
     * @param placement 字母牌放置信息
     */
    public void recordPlacement(PlaceTile.TilePlacement placement){
        if (!isConfirmed){
            playedTiles.add(placement);
        }
    }

    /**
     * 记录形成的单词
     * @param words 形成的单词列表
     */
    public void recordFormedWords(List<String> words){
        if (!isConfirmed && words !=null){
            this.formedWords=new ArrayList<>(words);
        }
    }

    /**
     * 设置回合获得的分数
     * @param score 获得的分数
     */
    public void setScore(int score){
        if (!isConfirmed && score>=0){
            this.scoreGained=score;
        }
    }

    /**
     * 将回合标记为跳过
     */
    public void markAsPass() {
        if (!isConfirmed) {
            this.isPass = true;
        }
    }

    /**
     * 确认回合，分数将被应用到玩家
     */
    public void confirm(){
        if (!isConfirmed){
            if (scoreGained>0){
                player.addScore(scoreGained);
            }
            isConfirmed=true;
        }
    }

    /**
     * 获取回合的玩家
     * @return 回合的玩家
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * 获取回合放置的字母牌列表
     * @return 放置的字母牌列表（只读）
     */
    public List<PlaceTile.TilePlacement> getPlayedTiles() {
        return new ArrayList<>(playedTiles);
    }

    /**
     * 获取回合形成的单词列表
     * @return 形成的单词列表
     */
    public List<String> getFormedWords() {
        return new ArrayList<>(formedWords);
    }

    /**
     * 获取回合获得的分数
     * @return 获得的分数
     */
    public int getScoreGained() {
        return scoreGained;
    }

    /**
     * 检查回合是否为跳过
     * @return 如果跳过则为true
     */
    public boolean isPass() {
        return isPass;
    }

    /**
     * 检查回合是否已确认
     * @return 如果已确认则为true
     */
    public boolean isConfirmed() {
        return isConfirmed;
    }

    /**
     * 返回回合的字符串表示
     * @return 包含回合信息的字符串
     */
    @Override
    public String toString() {
        if (isPass) {
            return player.getPlayerIndex() + " 跳过了回合";
        }
        StringBuilder sb = new StringBuilder(player.getPlayerIndex());
        sb.append(" 放置了 ").append(playedTiles.size()).append(" 个字母牌");
        if (!formedWords.isEmpty()) {
            sb.append("，形成了单词: ").append(String.join(", ", formedWords));
        }
        sb.append("，获得 ").append(scoreGained).append(" 分");
        return sb.toString();
    }
}
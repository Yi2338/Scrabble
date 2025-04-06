package scrabble.AIPlayer;

import scrabble.Game.Game;
import scrabble.Game.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * AI玩家工厂类，负责管理游戏中的AI玩家实例
 * 提供玩家与AI托管之间的切换功能
 */
public class AIPlayerFactory {
    /** 存储AI玩家实例的映射表 */
    private final Map<Player, AIPlayer> aiPlayers;
    /** 游戏实例 */
    private final Game game;
    
    /**
     * 创建一个AI玩家工厂
     * 
     * @param game 关联的游戏实例
     */
    public AIPlayerFactory(Game game) {
        this.game = game;
        this.aiPlayers = new HashMap<>();
    }
    
    /**
     * 创建并获取玩家对应的AI实例
     * 
     * @param player 要创建AI的玩家
     * @return 创建的AI玩家实例
     */
    public AIPlayer createAIPlayer(Player player) {
        AIPlayer aiPlayer = new AIPlayer(player, game);
        aiPlayers.put(player, aiPlayer);
        return aiPlayer;
    }
    
    /**
     * 获取玩家对应的AI实例，如果不存在则创建
     * 
     * @param player 玩家对象
     * @return 对应的AI玩家实例
     */
    public AIPlayer getAIPlayer(Player player) {
        if (!aiPlayers.containsKey(player)) {
            return createAIPlayer(player);
        }
        return aiPlayers.get(player);
    }
    
    /**
     * 将玩家切换为AI托管
     * 
     * @param player 要托管的玩家
     * @return 是否成功切换
     */
    public boolean enableAIControl(Player player) {
        if (player == null) {
            return false;
        }
        
        // 确保AI玩家实例存在
        getAIPlayer(player);
        return true;
    }
    
    /**
     * 取消玩家的AI托管
     * 
     * @param player 要取消托管的玩家
     * @return 是否成功取消
     */
    public boolean disableAIControl(Player player) {
        return aiPlayers.remove(player) != null;
    }
    
    /**
     * 检查玩家是否处于AI托管状态
     * 
     * @param player 要检查的玩家
     * @return 是否为AI托管
     */
    public boolean isAIControlled(Player player) {
        return aiPlayers.containsKey(player);
    }
    
    /**
     * 如果当前玩家由AI托管，则执行AI回合
     * 
     * @param currentPlayer 当前回合的玩家
     * @return 是否由AI执行了回合
     */
    public boolean playAITurnIfControlled(Player currentPlayer) {
        if (isAIControlled(currentPlayer)) {
            AIPlayer aiPlayer = getAIPlayer(currentPlayer);
            aiPlayer.playTurn();
            return true;
        }
        return false;
    }
    
    /**
     * 清除所有AI托管
     */
    public void clearAllAIControl() {
        aiPlayers.clear();
    }
} 
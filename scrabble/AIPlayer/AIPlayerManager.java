package scrabble.AIPlayer;

import scrabble.Game.Game;
import scrabble.Game.Player;
import scrabble.Game.Turn;
import scrabble.Logging.GameLogger;
import scrabble.Logging.GameLoggerFactory;
import scrabble.Placement.PlaceTile;
import scrabble.Validator.WordValidator;
import scrabble.Validator.PositionValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AIPlayerManager - 管理游戏中的AI玩家，处理AI的回合行动
 */
public class AIPlayerManager {
    /** 游戏日志记录器 */
    private final GameLogger logger = GameLoggerFactory.getLogger();

    /** 游戏实例 */
    private final Game game;

    /** 单词验证器 */
    private final WordValidator wordValidator;

    /** 位置验证器 */
    private final PositionValidator positionValidator;

    /** 字母放置工具 */
    private final PlaceTile placeTile;

    /** 关联的AI玩家映射表（玩家 -> AI玩家实例） */
    private final Map<Player, AIPlayer> aiPlayers;

    /**
     * 创建一个AI玩家管理器
     * @param game 游戏实例
     * @param wordValidator 单词验证器
     * @param positionValidator 位置验证器
     * @param placeTile 字母放置工具
     */
    public AIPlayerManager(Game game, WordValidator wordValidator,
                           PositionValidator positionValidator, PlaceTile placeTile) {
        this.game = game;
        this.wordValidator = wordValidator;
        this.positionValidator = positionValidator;
        this.placeTile = placeTile;
        this.aiPlayers = new HashMap<>();
    }

    /**
     * 注册一个AI玩家
     * @param player 玩家对象
     * @param aiPlayer AI玩家实例
     */
    public void registerAIPlayer(Player player, AIPlayer aiPlayer) {
        aiPlayers.put(player, aiPlayer);
        logger.info("注册AI玩家: " + player.getPlayerIndex());
    }

    /**
     * 移除一个AI玩家
     * @param player 玩家对象
     * @return 是否成功移除
     */
    public boolean removeAIPlayer(Player player) {
        if (aiPlayers.containsKey(player)) {
            aiPlayers.remove(player);
            logger.info("移除AI玩家: " + player.getPlayerIndex());
            return true;
        }
        return false;
    }

    /**
     * 检查指定玩家是否为AI玩家
     * @param player 要检查的玩家
     * @return 如果是AI玩家则返回true，否则返回false
     */
    public boolean isAIPlayer(Player player) {
        return aiPlayers.containsKey(player);
    }

    /**
     * 获取指定玩家的AI实例
     * @param player 玩家对象
     * @return AI玩家实例，如果不存在则返回null
     */
    public AIPlayer getAIPlayer(Player player) {
        return aiPlayers.get(player);
    }

    /**
     * 处理AI玩家的回合
     * 此方法应由游戏逻辑在轮到AI玩家时调用
     * @param player 当前回合的AI玩家
     * @return 操作结果描述
     */
    public String processAITurn(Player player) {
        // 检查是否为AI玩家
        if (!isAIPlayer(player)) {
            logger.warn("尝试处理非AI玩家的回合: " + player.getPlayerIndex());
            return "NOT_AI";
        }

        // 记录AI回合开始
        logger.logTurnStart(player);

        // 获取AI实例
        AIPlayer aiPlayer = getAIPlayer(player);

        // 获取游戏棋盘
        Object board = game.getBoard();

        // 让AI计算最佳移动
        String moveResult = aiPlayer.findBestMove(board);
        logger.info("AI玩家 " + player.getPlayerIndex() + " 的移动: " + moveResult);

        // 如果AI决定跳过回合
        if ("PASS".equals(moveResult)) {
            // 记录AI回合结束
            logger.logTurnEnd(player);

            game.passTurn();
            return "AI玩家 " + player.getPlayerIndex() + " 跳过回合";
        }

        // 获取当前放置列表（用于日志记录）
        List<PlaceTile.TilePlacement> placements = game.getPlaceTile().getCurrentPlacements(player);

        // 确认AI的放置
        int score = game.confirmPlacement();

        // 根据结果记录不同类型的日志
        if (score > 0) {
            // 放置成功，获取形成的单词（可能需要从游戏的当前回合中获取）
            Turn currentTurn = game.getCurrentTurn();
            List<String> formedWords = currentTurn != null ? currentTurn.getFormedWords() : new ArrayList<>();

            // 记录放置确认
            logger.logPlacementConfirmation(player, formedWords, score);

            // 记录回合结束
            logger.logTurnEnd(player);

            return "AI玩家 " + player.getPlayerIndex() + " 放置成功，得分: " + score;
        } else {
            // 放置失败，记录取消
            logger.logPlacementCancellation(player);

            // 取消放置并跳过回合
            game.cancelPlacement();

            // 记录回合结束
            logger.logTurnEnd(player);

            game.passTurn();
            return "AI玩家 " + player.getPlayerIndex() + " 放置失败，跳过回合";
        }
    }

    /**
     * 处理当前回合的AI玩家（如果当前回合的玩家是AI）
     * @return 如果当前玩家是AI并已处理则返回true，否则返回false
     */
    public boolean processCurrentAITurnIfNeeded() {
        Player currentPlayer = game.getCurrentPlayer();

        if (isAIPlayer(currentPlayer)) {
            logger.info("检测到当前回合玩家 " + currentPlayer.getPlayerIndex() + " 是AI玩家，自动处理其回合");

            // 记录自动AI回合的开始
            String result = processAITurn(currentPlayer);

            logger.info("AI回合处理结果: " + result);
            return true;
        }

        logger.debug("当前回合玩家 " + currentPlayer.getPlayerIndex() + " 不是AI玩家，无需自动处理");
        return false;
    }
}
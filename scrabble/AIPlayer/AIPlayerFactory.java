package scrabble.AIPlayer;

import scrabble.Game.Player;
import scrabble.Placement.BoardOperator;
import scrabble.Placement.PlaceTile;
import scrabble.Score.ScoreCalculator;
import scrabble.Tile.TileManager;
import scrabble.Validator.Dictionary;

/**
 * AIPlayerFactory - 工厂类，用于创建AI玩家实例
 */
public class AIPlayerFactory {
    /**
     * 创建AI玩家
     * @param player 关联的Player对象
     * @param dictionary 词典
     * @param boardOperator 棋盘操作器
     * @param tileManager 字母管理器
     * @param placeTile 字母放置工具
     * @param scoreCalculator 分数计算器
     * @return 创建的AI玩家实例
     */
    public static AIPlayer createAIPlayer(Player player, Dictionary dictionary,
                                          BoardOperator boardOperator, TileManager tileManager,
                                          PlaceTile placeTile, ScoreCalculator scoreCalculator) {
        return new DefaultAIPlayer(player, dictionary, boardOperator, tileManager,
                placeTile, scoreCalculator);
    }
}
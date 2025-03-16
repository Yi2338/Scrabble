package scrabble.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import scrabble.Tile.Tile;

import java.util.List;
import java.util.UUID;

/**
 * 基于Log4j2实现的Scrabble游戏日志记录器
 * 使用不同的Logger实例分别记录游戏、棋盘和回合相关的日志
 * 通过ThreadContext维护游戏ID和玩家信息的上下文
 */
public class Log4j2GameLogger implements GameLogger {
    /** 游戏整体事件的日志记录器 */
    private static final Logger GAME_LOGGER = LogManager.getLogger("scrabble.game");
    /** 字母牌操作相关的日志记录器 */
    private static final Logger TILE_LOGGER = LogManager.getLogger("scrabble.tile");
    /** 回合相关事件的日志记录器 */
    private static final Logger TURN_LOGGER = LogManager.getLogger("scrabble.turn");
    
    /** 当前游戏的唯一标识符 */
    private final String gameId;
    
    /**
     * 创建新的日志记录器实例
     * 生成唯一的游戏ID并设置到ThreadContext中
     */
    public Log4j2GameLogger() {
        this.gameId = UUID.randomUUID().toString();
        ThreadContext.put("gameId", gameId);
    }
    
    /**
     * 设置当前线程的玩家上下文
     * @param player 当前操作的玩家
     */
    private void setupContext(Object player) {
        ThreadContext.put("player", player.toString());
    }
    
    /**
     * 清除当前线程的玩家上下文
     */
    private void clearContext() {
        ThreadContext.remove("player");
    }
    
    @Override
    public void logTilePlacement(Object player, String source, int row, int col, Tile tile) {
        try {
            setupContext(player);
            ThreadContext.put("action", "PLACE");
            ThreadContext.put("tileChar", String.valueOf(tile.getLetter()));
            ThreadContext.put("position", row + "," + col);
            
            TILE_LOGGER.info("Tile {} placed from {} to position ({},{}) by {}",
                    tile.getLetter(), source, row, col, player);
        } finally {
            ThreadContext.remove("action");
            ThreadContext.remove("tileChar");
            ThreadContext.remove("position");
            clearContext();
        }
    }

    @Override
    public void logTileMovement(Object player, int fromRow, int fromCol, int toRow, int toCol, Tile tile) {
        try {
            setupContext(player);
            ThreadContext.put("action", "MOVE");
            ThreadContext.put("tileChar", String.valueOf(tile.getLetter()));
            ThreadContext.put("from", fromRow + "," + fromCol);
            ThreadContext.put("to", toRow + "," + toCol);
            
            TILE_LOGGER.info("Tile {} moved from ({},{}) to ({},{}) by {}",
                    tile.getLetter(), fromRow, fromCol, toRow, toCol, player);
        } finally {
            ThreadContext.remove("action");
            ThreadContext.remove("tileChar");
            ThreadContext.remove("from");
            ThreadContext.remove("to");
            clearContext();
        }
    }

    @Override
    public void logTileReturn(Object player, int row, int col, int rackIndex, Tile tile) {
        try {
            setupContext(player);
            ThreadContext.put("action", "RETURN");
            ThreadContext.put("tileChar", String.valueOf(tile.getLetter()));
            ThreadContext.put("position", row + "," + col);
            ThreadContext.put("rackIndex", String.valueOf(rackIndex));
            
            TILE_LOGGER.info("Tile {} returned from position ({},{}) to rack index {} by {}",
                    tile.getLetter(), row, col, rackIndex, player);
        } finally {
            ThreadContext.remove("action");
            ThreadContext.remove("tileChar");
            ThreadContext.remove("position");
            ThreadContext.remove("rackIndex");
            clearContext();
        }
    }

    @Override
    public void logPlacementConfirmation(Object player, List<String> words, int score) {
        try {
            setupContext(player);
            ThreadContext.put("action", "CONFIRM");
            ThreadContext.put("words", String.join(",", words));
            ThreadContext.put("score", String.valueOf(score));
            
            TURN_LOGGER.info("Player {} confirmed placement, forming words: {} (score: {})",
                    player, String.join(", ", words), score);
        } finally {
            ThreadContext.remove("action");
            ThreadContext.remove("words");
            ThreadContext.remove("score");
            clearContext();
        }
    }

    @Override
    public void logPlacementCancellation(Object player) {
        try {
            setupContext(player);
            ThreadContext.put("action", "CANCEL");
            
            TURN_LOGGER.info("Player {} cancelled placement", player);
        } finally {
            ThreadContext.remove("action");
            clearContext();
        }
    }

    @Override
    public void logGameStart(List<Object> players) {
        GAME_LOGGER.info("Game started with players: {}", players);
    }

    @Override
    public void logGameEnd(Object winner, int score) {
        GAME_LOGGER.info("Game ended. Winner: {} with score: {}", winner, score);
    }

    @Override
    public void logTurnStart(Object player) {
        try {
            setupContext(player);
            TURN_LOGGER.info("Turn started for player {}", player);
        } finally {
            clearContext();
        }
    }

    @Override
    public void logTurnEnd(Object player) {
        try {
            setupContext(player);
            TURN_LOGGER.info("Turn ended for player {}", player);
        } finally {
            clearContext();
        }
    }

    @Override
    public void debug(String message, Object... params) {
        GAME_LOGGER.debug(message, params);
    }

    @Override
    public void info(String message, Object... params) {
        GAME_LOGGER.info(message, params);
    }

    @Override
    public void warn(String message, Object... params) {
        GAME_LOGGER.warn(message, params);
    }

    @Override
    public void error(String message, Throwable throwable, Object... params) {
        GAME_LOGGER.error(message, params, throwable);
    }
}
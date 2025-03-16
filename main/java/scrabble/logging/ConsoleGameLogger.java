package scrabble.logging;

import scrabble.Tile.Tile;
import java.time.Instant;
import java.util.List;

/**
 * 简单的控制台日志记录器
 * 将所有日志信息直接输出到控制台
 */
public class ConsoleGameLogger implements GameLogger {
    
    @Override
    public void logTilePlacement(Object player, String source, int row, int col, Tile tile) {
        System.out.printf("[%s] TILE_PLACEMENT - Player: %s, Source: %s, Position: (%d,%d), Tile: %s%n",
                Instant.now(), player, source, row, col, tile);
    }

    @Override
    public void logTileMovement(Object player, int fromRow, int fromCol, int toRow, int toCol, Tile tile) {
        System.out.printf("[%s] TILE_MOVEMENT - Player: %s, From: (%d,%d), To: (%d,%d), Tile: %s%n",
                Instant.now(), player, fromRow, fromCol, toRow, toCol, tile);
    }

    @Override
    public void logTileReturn(Object player, int row, int col, int rackIndex, Tile tile) {
        System.out.printf("[%s] TILE_RETURN - Player: %s, From: (%d,%d), To Rack: %d, Tile: %s%n",
                Instant.now(), player, row, col, rackIndex, tile);
    }

    @Override
    public void logPlacementConfirmation(Object player, List<String> words, int score) {
        System.out.printf("[%s] PLACEMENT_CONFIRM - Player: %s, Words: %s, Score: %d%n",
                Instant.now(), player, String.join(", ", words), score);
    }

    @Override
    public void logPlacementCancellation(Object player) {
        System.out.printf("[%s] PLACEMENT_CANCEL - Player: %s%n",
                Instant.now(), player);
    }

    @Override
    public void logGameStart(List<Object> players) {
        System.out.printf("[%s] GAME_START - Players: %s%n",
                Instant.now(), players);
    }

    @Override
    public void logGameEnd(Object winner, int score) {
        System.out.printf("[%s] GAME_END - Winner: %s, Score: %d%n",
                Instant.now(), winner, score);
    }

    @Override
    public void logTurnStart(Object player) {
        System.out.printf("[%s] TURN_START - Player: %s%n",
                Instant.now(), player);
    }

    @Override
    public void logTurnEnd(Object player) {
        System.out.printf("[%s] TURN_END - Player: %s%n",
                Instant.now(), player);
    }

    @Override
    public void debug(String message, Object... params) {
        System.out.printf("[%s] DEBUG - %s%n",
                Instant.now(), formatMessage(message, params));
    }

    @Override
    public void info(String message, Object... params) {
        System.out.printf("[%s] INFO - %s%n",
                Instant.now(), formatMessage(message, params));
    }

    @Override
    public void warn(String message, Object... params) {
        System.out.printf("[%s] WARN - %s%n",
                Instant.now(), formatMessage(message, params));
    }

    @Override
    public void error(String message, Throwable throwable, Object... params) {
        System.out.printf("[%s] ERROR - %s%n",
                Instant.now(), formatMessage(message, params));
        if (throwable != null) {
            throwable.printStackTrace(System.err);
        }
    }
    
    /**
     * 格式化消息字符串，替换占位符
     * @param message 消息模板
     * @param params 参数列表
     * @return 格式化后的消息
     */
    private String formatMessage(String message, Object... params) {
        if (params == null || params.length == 0) {
            return message;
        }
        
        String result = message;
        for (Object param : params) {
            result = result.replaceFirst("\\{\\}", param != null ? param.toString() : "null");
        }
        return result;
    }
} 
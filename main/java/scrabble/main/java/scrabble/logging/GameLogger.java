package scrabble.logging;

import scrabble.Tile.Tile;
import java.util.List;

public interface GameLogger {
    // 字母牌操作日志方法
    void logTilePlacement(Object player, String source, int row, int col, Tile tile);
    void logTileMovement(Object player, int fromRow, int fromCol, int toRow, int toCol, Tile tile);
    void logTileReturn(Object player, int row, int col, int rackIndex, Tile tile);
    
    // 回合操作日志方法
    void logPlacementConfirmation(Object player, List<String> words, int score);
    void logPlacementCancellation(Object player);
    
    // 游戏状态日志方法
    void logGameStart(List<Object> players);
    void logGameEnd(Object winner, int score);
    void logTurnStart(Object player);
    void logTurnEnd(Object player);
    
    // 通用日志方法
    void debug(String message, Object... params);
    void info(String message, Object... params);
    void warn(String message, Object... params);
    void error(String message, Throwable throwable, Object... params);
} 
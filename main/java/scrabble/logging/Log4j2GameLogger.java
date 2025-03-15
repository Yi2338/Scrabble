package scrabble.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import scrabble.Tile.Tile;

import java.util.List;
import java.util.UUID;

public class Log4j2GameLogger implements GameLogger {
    private static final Logger GAME_LOGGER = LogManager.getLogger("scrabble.game");
    private static final Logger TILE_LOGGER = LogManager.getLogger("scrabble.tile");
    private static final Logger TURN_LOGGER = LogManager.getLogger("scrabble.turn");
    
    private final String gameId;
    
    public Log4j2GameLogger() {
        this.gameId = UUID.randomUUID().toString();
        ThreadContext.put("gameId", gameId);
    }
    
    private void setupContext(Object player) {
        ThreadContext.put("player", player.toString());
    }
    
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
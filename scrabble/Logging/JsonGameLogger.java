package scrabble.Logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import scrabble.Tile.Tile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

/**
 * 基于JSON格式的Scrabble游戏日志记录器
 * 每局游戏创建一个独立的JSON文件，支持后续追加输出
 */
public class JsonGameLogger implements GameLogger {
    /** Jackson对象映射器 */
    private final ObjectMapper objectMapper;

    /** 回合计数器 */
    private int turnCounter = 0;

    /** 当前游戏的唯一标识符 */
    private final String gameId;
    /** 日志文件目录 */
    private final String logDirectory;
    /** 当前游戏的日志文件路径 */
    private final String logFilePath;
    /** 内存中的事件列表 */
    private final List<Map<String, Object>> events;
    
    /**
     * 创建新的JSON日志记录器实例
     */
    public JsonGameLogger() {
        this("logs/json");
    }
    
    /**
     * 创建指定目录的JSON日志记录器实例
     * @param logDirectory 日志文件存储目录
     */
    public JsonGameLogger(String logDirectory) {
        this.gameId = UUID.randomUUID().toString();
        this.logDirectory = logDirectory;
        this.logFilePath = logDirectory + "/scrabble-game-" + gameId + ".json";
        this.events = new ArrayList<>();
        
        // 配置Jackson对象映射器
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        // 创建日志目录
        try {
            Files.createDirectories(Paths.get(logDirectory));
        } catch (IOException e) {
            System.err.println("Failed to create log directory: " + e.getMessage());
        }
        
        // 初始化日志文件
        initLogFile();
    }
    
    /**
     * 初始化日志文件，创建包含游戏基本信息的JSON结构
     */
    private void initLogFile() {
        Map<String, Object> gameInfo = new HashMap<>();
        gameInfo.put("gameId", gameId);
        gameInfo.put("startTime", Instant.now().toString());
        gameInfo.put("events", events);
        
        try {
            objectMapper.writeValue(new File(logFilePath), gameInfo);
        } catch (IOException e) {
            System.err.println("Failed to initialize log file: " + e.getMessage());
        }
    }
    
    /**
     * 将事件添加到内存并写入JSON文件
     * @param eventType 事件类型
     * @param player 玩家对象
     * @param eventData 事件数据
     */
    private void logEvent(GameEventType eventType, Object player, Map<String, Object> eventData) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType != null ? eventType.name() : "LOG");
        event.put("timestamp", Instant.now().toString());
        event.put("player", player != null ? player.toString() : "SYSTEM");
        event.put("data", eventData);
        
        // 添加到内存中的事件列表
        events.add(event);
        
        // 追加到文件
        appendEventToFile(event);
    }
    
    /**
     * 将事件追加到JSON文件
     * @param event 事件数据
     */
    private synchronized void appendEventToFile(Map<String, Object> event) {
        try {
            // 读取当前文件内容
            File file = new File(logFilePath);
            Map<String, Object> gameInfo = objectMapper.readValue(file, Map.class);
            List<Map<String, Object>> fileEvents = (List<Map<String, Object>>) gameInfo.get("events");
            
            // 添加新事件
            fileEvents.add(event);
            
            // 更新文件
            ObjectWriter writer = objectMapper.writer().withDefaultPrettyPrinter();
            writer.writeValue(file, gameInfo);
        } catch (IOException e) {
            System.err.println("Failed to append event to log file: " + e.getMessage());
        }
    }
    
    @Override
    public void logTilePlacement(Object player, String source, int row, int col, Tile tile) {
        Map<String, Object> data = new HashMap<>();
        data.put("source", source);
        data.put("row", row);
        data.put("col", col);
        data.put("letter", String.valueOf(tile.getLetter()));
        data.put("value", tile.getValue());
        data.put("isBlank", tile.isBlank());
        
        logEvent(GameEventType.TILE_PLACEMENT, player, data);
    }

    @Override
    public void logTileMovement(Object player, int fromRow, int fromCol, int toRow, int toCol, Tile tile) {
        Map<String, Object> data = new HashMap<>();
        data.put("fromRow", fromRow);
        data.put("fromCol", fromCol);
        data.put("toRow", toRow);
        data.put("toCol", toCol);
        data.put("letter", String.valueOf(tile.getLetter()));
        data.put("value", tile.getValue());
        data.put("isBlank", tile.isBlank());
        
        logEvent(GameEventType.TILE_MOVEMENT, player, data);
    }

    @Override
    public void logTileExchange(Object player, List<Tile> exchangedTiles) {
        Map<String, Object> data = new HashMap<>();
        data.put("tileCount", exchangedTiles.size());

        // 添加详细的字母牌信息
        List<Map<String, Object>> tilesInfo = new ArrayList<>();
        for (Tile tile : exchangedTiles) {
            Map<String, Object> tileInfo = new HashMap<>();
            tileInfo.put("letter", String.valueOf(tile.getLetter()));
            tileInfo.put("value", tile.getValue());
            tileInfo.put("isBlank", tile.isBlank());
            tilesInfo.add(tileInfo);
        }
        data.put("tiles", tilesInfo);

        logEvent(GameEventType.TILE_EXCHANGE, player, data);
    }

    @Override
    public void logBlankTileAssign(Object player, Tile tile, char letter) {
        Map<String, Object> data = new HashMap<>();
        data.put("letter", String.valueOf(letter));
        data.put("oldValue", 0); // 空白牌初始分值为0
        data.put("newValue", tile.getValue()); // 更新后的分值
        data.put("isBlank", tile.isBlank());

        logEvent(GameEventType.BLANK_TILE_ASSIGN, player, data);
    }

    @Override
    public void logTileReturn(Object player, int row, int col, int rackIndex, Tile tile) {
        Map<String, Object> data = new HashMap<>();
        data.put("row", row);
        data.put("col", col);
        data.put("rackIndex", rackIndex);
        data.put("letter", String.valueOf(tile.getLetter()));
        data.put("value", tile.getValue());
        data.put("isBlank", tile.isBlank());
        
        logEvent(GameEventType.TILE_RETURN, player, data);
    }

    @Override
    public void logPlacementConfirmation(Object player, List<String> words, int score) {
        Map<String, Object> data = new HashMap<>();
        data.put("words", words);
        data.put("score", score);
        
        logEvent(GameEventType.PLACEMENT_CONFIRM, player, data);
    }

    @Override
    public void logPlacementCancellation(Object player) {
        logEvent(GameEventType.PLACEMENT_CANCEL, player, new HashMap<>());
    }

    @Override
    public void logGameStart(List<Object> players) {
        Map<String, Object> data = new HashMap<>();
        data.put("players", players);
        data.put("numPlayers", players.size());

        logEvent(GameEventType.GAME_START, null , data);

    }

    @Override
    public void logGameEnd(Object winner, int score) {
        Map<String, Object> data = new HashMap<>();
        data.put("winner", winner);
        data.put("finalScore", score);
        data.put("endTime", Instant.now().toString());
        
        logEvent(GameEventType.GAME_END, winner, data);
        
        // 游戏结束时，添加结束时间到游戏信息
        try {
            File file = new File(logFilePath);
            Map<String, Object> gameInfo = objectMapper.readValue(file, Map.class);
            gameInfo.put("endTime", Instant.now().toString());
            gameInfo.put("winner", winner.toString());
            gameInfo.put("finalScore", score);
            
            objectMapper.writeValue(file, gameInfo);
        } catch (IOException e) {
            System.err.println("Failed to update game end information: " + e.getMessage());
        }
    }

    @Override
    public void logTurnStart(Object player) {
        turnCounter++;
        Map<String, Object> data = new HashMap<>();
        data.put("turnNumber", turnCounter);

        logEvent(GameEventType.TURN_START, player, data);
    }

    @Override
    public void logTurnEnd(Object player) {
        Map<String, Object> data = new HashMap<>();
        data.put("turnNumber", turnCounter);

        logEvent(GameEventType.TURN_END, player, data);
    }

    @Override
    public void debug(String message, Object... params) {
        Map<String, Object> data = new HashMap<>();
        data.put("level", "DEBUG");
        data.put("message", formatMessage(message, params));
        
        logEvent(GameEventType.DEBUG, null, data);
    }

    @Override
    public void info(String message, Object... params) {
        Map<String, Object> data = new HashMap<>();
        data.put("level", "INFO");
        data.put("message", formatMessage(message, params));
        
        logEvent(GameEventType.INFO, null, data);
    }

    @Override
    public void warn(String message, Object... params) {
        Map<String, Object> data = new HashMap<>();
        data.put("level", "WARN");
        data.put("message", formatMessage(message, params));
        
        logEvent(GameEventType.WARN, null, data);
    }

    @Override
    public void error(String message, Throwable throwable, Object... params) {
        Map<String, Object> data = new HashMap<>();
        data.put("level", "ERROR");
        data.put("message", formatMessage(message, params));
        data.put("exception", throwable != null ? throwable.getMessage() : null);
        data.put("stackTrace", throwable != null ? Arrays.toString(throwable.getStackTrace()) : null);
        
        logEvent(GameEventType.ERROR, null, data);
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
    
    /**
     * 获取当前游戏ID
     * @return 游戏ID
     */
    public String getGameId() {
        return gameId;
    }
    
    /**
     * 获取日志文件路径
     * @return 日志文件路径
     */
    public String getLogFilePath() {
        return logFilePath;
    }
} 
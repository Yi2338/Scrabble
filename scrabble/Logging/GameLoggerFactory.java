package scrabble.Logging;

import java.util.HashMap;
import java.util.Map;

/**
 * 游戏日志记录器工厂类
 * 负责创建和管理游戏日志记录器实例
 */
public class GameLoggerFactory {
    /** 当前活动的日志记录器实例 */
    private static GameLogger instance;
    
    /** 游戏ID与对应日志记录器的映射 */
    private static final Map<String, GameLogger> gameLoggers = new HashMap<>();
    
    /** 默认日志目录路径 */
    private static final String DEFAULT_LOG_DIRECTORY = "src/main/resources/logs";

    static {
        // 初始化JsonGameLogger
        initJsonLogger();
    }

    /**
     * 初始化JSON日志记录器
     */
    private static synchronized void initJsonLogger() {
        if (instance == null) {
            instance = new JsonGameLogger(DEFAULT_LOG_DIRECTORY);
        }
    }

    /**
     * 获取当前活动的日志记录器实例
     * @return 日志记录器实例
     */
    public static synchronized GameLogger getLogger() {
        if (instance == null) {
            initJsonLogger();
        }
        return instance;
    }
    
    /**
     * 获取特定游戏的日志记录器实例
     * 如果该游戏ID的日志记录器不存在，将创建一个新的
     * @param gameId 游戏唯一标识符
     * @return 对应该游戏的日志记录器实例
     */
    public static synchronized GameLogger getLoggerForGame(String gameId) {
        if (gameId == null || gameId.isEmpty()) {
            return getLogger(); // 如果没有提供游戏ID，返回默认记录器
        }
        
        if (!gameLoggers.containsKey(gameId)) {
            GameLogger gameLogger = new JsonGameLogger(gameId, DEFAULT_LOG_DIRECTORY);
            gameLoggers.put(gameId, gameLogger);
        }
        
        return gameLoggers.get(gameId);
    }

    /**
     * 设置活动的日志记录器实例
     * 主要用于测试或特殊情况
     * @param logger 要设置的日志记录器实例
     */
    public static void setLogger(GameLogger logger) {
        instance = logger;
    }
    
    /**
     * 清理特定游戏的日志记录器
     * 当游戏结束时可以调用此方法释放资源
     * @param gameId 游戏唯一标识符
     */
    public static synchronized void cleanupGameLogger(String gameId) {
        if (gameId != null && !gameId.isEmpty()) {
            gameLoggers.remove(gameId);
        }
    }

    /**
     * 获取JSON日志文件路径
     * @param gameId 游戏唯一标识符
     * @return JSON日志文件路径，如果实例不是JsonGameLogger则返回null
     */
    public static String getJsonLogFilePath(String gameId) {
        GameLogger logger = gameId != null ? gameLoggers.get(gameId) : instance;
        
        if (logger instanceof JsonGameLogger) {
            return ((JsonGameLogger) logger).getLogFilePath();
        }
        return null;
    }
    
    /**
     * 获取默认JSON日志文件路径
     * @return JSON日志文件路径，如果实例不是JsonGameLogger则返回null
     */
    public static String getJsonLogFilePath() {
        if (instance instanceof JsonGameLogger) {
            return ((JsonGameLogger) instance).getLogFilePath();
        }
        return null;
    }
}
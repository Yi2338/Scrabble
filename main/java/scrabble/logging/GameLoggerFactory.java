package scrabble.logging;

/**
 * 游戏日志记录器工厂类
 * 负责创建和管理游戏日志记录器实例
 */
public class GameLoggerFactory {
    /** 当前活动的日志记录器实例 */
    private static GameLogger instance;
    
    static {
        // 初始化日志记录器，使用配置中指定的类型
        GameLoggerConfig.initLogger();
    }
    
    /**
     * 获取当前活动的日志记录器实例
     * @return 日志记录器实例
     */
    public static synchronized GameLogger getLogger() {
        if (instance == null) {
            // 调用配置初始化
            GameLoggerConfig.initLogger();
        }
        return instance;
    }
    
    /**
     * 设置活动的日志记录器实例
     * @param logger 要设置的日志记录器实例
     */
    public static void setLogger(GameLogger logger) {
        instance = logger;
    }
    
    /**
     * 判断当前日志记录器是否为JSON格式
     * @return 如果是JSON格式日志记录器返回true，否则返回false
     */
    public static boolean isJsonLogger() {
        return instance instanceof JsonGameLogger;
    }
    
    /**
     * 获取JSON日志文件路径
     * 仅当当前日志记录器为JsonGameLogger时有效
     * @return JSON日志文件路径，如果不是JSON日志记录器则返回null
     */
    public static String getJsonLogFilePath() {
        if (instance instanceof JsonGameLogger) {
            return ((JsonGameLogger) instance).getLogFilePath();
        }
        return null;
    }
} 
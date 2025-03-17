package scrabble.logging;

/**
 * 游戏日志记录器工厂类
 * 负责创建和管理游戏日志记录器实例
 */
public class GameLoggerFactory {
    /** 当前活动的日志记录器实例 */
    private static GameLogger instance;

    static {
        // 初始化JsonGameLogger
        initJsonLogger();
    }

    /**
     * 初始化JSON日志记录器
     */
    private static synchronized void initJsonLogger() {
        if (instance == null) {
            instance = new JsonGameLogger();
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
     * 设置活动的日志记录器实例
     * 主要用于测试或特殊情况
     * @param logger 要设置的日志记录器实例
     */
    public static void setLogger(GameLogger logger) {
        instance = logger;
    }

    /**
     * 获取JSON日志文件路径
     * @return JSON日志文件路径，如果实例不是JsonGameLogger则返回null
     */
    public static String getJsonLogFilePath() {
        if (instance instanceof JsonGameLogger) {
            return ((JsonGameLogger) instance).getLogFilePath();
        }
        return null;
    }
}
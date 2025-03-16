package scrabble.logging;

/**
 * 游戏日志记录器配置类
 * 用于配置和初始化游戏使用的日志记录器
 */
public class GameLoggerConfig {
    /** 日志类型枚举 */
    public enum LoggerType {
        JSON,     // 使用JSON格式记录器
        CONSOLE   // 使用控制台记录器
    }
    
    private static LoggerType activeLoggerType = LoggerType.JSON; // 默认使用JSON
    
    /**
     * 设置活动的日志记录器类型
     * @param type 日志记录器类型
     */
    public static void setActiveLoggerType(LoggerType type) {
        activeLoggerType = type;
        // 初始化对应的日志记录器
        initLogger();
    }
    
    /**
     * 创建并初始化对应类型的日志记录器
     */
    public static void initLogger() {
        GameLogger logger;
        
        switch (activeLoggerType) {
            case CONSOLE:
                // 创建一个简单的控制台日志记录器
                logger = new ConsoleGameLogger();
                break;
            case JSON:
            default:
                // 默认使用JSON日志记录器
                logger = new JsonGameLogger();
                break;
        }
        
        // 设置为活动日志记录器
        GameLoggerFactory.setLogger(logger);
    }
    
    /**
     * 获取当前活动的日志记录器类型
     * @return 当前活动的日志记录器类型
     */
    public static LoggerType getActiveLoggerType() {
        return activeLoggerType;
    }
} 
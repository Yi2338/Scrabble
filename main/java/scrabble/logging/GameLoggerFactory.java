package scrabble.logging;

/**
 * Scrabble游戏日志记录器工厂类
 * 使用单例模式管理游戏日志记录器的实例
 */
public class GameLoggerFactory {
    /** 日志记录器单例实例 */
    private static GameLogger instance;
    
    /**
     * 获取游戏日志记录器实例
     * 如果实例不存在，则创建一个新的Log4j2GameLogger实例
     * @return GameLogger实例
     */
    public static synchronized GameLogger getLogger() {
        if (instance == null) {
            instance = new Log4j2GameLogger();
        }
        return instance;
    }
    
    /**
     * 设置自定义的日志记录器实例
     * 主要用于测试时替换默认实现
     * @param logger 要使用的自定义日志记录器
     */
    public static void setLogger(GameLogger logger) {
        instance = logger;
    }
} 
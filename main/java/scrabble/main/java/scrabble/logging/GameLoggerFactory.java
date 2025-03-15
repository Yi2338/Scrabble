package scrabble.logging;

public class GameLoggerFactory {
    private static GameLogger instance;
    
    public static synchronized GameLogger getLogger() {
        if (instance == null) {
            instance = new Log4j2GameLogger();
        }
        return instance;
    }
    
    // 允许在测试时替换实现
    public static void setLogger(GameLogger logger) {
        instance = logger;
    }
} 
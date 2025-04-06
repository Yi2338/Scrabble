package scrabble.Game;

import scrabble.Logging.GameLoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GameFactory类负责创建、保存、加载和管理Scrabble游戏实例
 * 确保游戏存档和日志正确对应
 */
public class GameFactory {
    // 游戏存档目录
    private static final String GAMES_DIRECTORY = "src/main/resources/games";
    // 日志目录
    private static final String LOGS_DIRECTORY = "src/main/resources/logs";
    // 默认词典路径
    private static final String DEFAULT_DICTIONARY_PATH = "src/main/resources/Dictionary/words.txt";

    private static GameFactory instance;

    // 缓存当前活动的游戏实例
    private final Map<String, Game> activeGames = new HashMap<>();

    /**
     * 获取GameFactory单例实例
     */
    public static synchronized GameFactory getInstance() {
        if (instance == null) {
            instance = new GameFactory();
        }
        return instance;
    }

    /**
     * 私有构造函数，确保单例模式
     */
    private GameFactory() {
        initDirectories();
    }

    /**
     * 初始化游戏存档和日志目录
     */
    private void initDirectories() {
        try {
            Files.createDirectories(Paths.get(GAMES_DIRECTORY));
            Files.createDirectories(Paths.get(LOGS_DIRECTORY));
        } catch (IOException e) {
            System.err.println("无法创建游戏存档或日志目录: " + e.getMessage());
        }
    }

    /**
     * 创建新的Scrabble游戏实例
     */
    public Game createGame(List<Player> players, GameConfig config) throws IOException {
        return createGame(players, config, DEFAULT_DICTIONARY_PATH);
    }

    /**
     * 创建新的Scrabble游戏实例，使用指定的词典
     */
    public Game createGame(List<Player> players, GameConfig config, String dictionaryPath) throws IOException {
        Game game = new Game(players, config, dictionaryPath);
        activeGames.put(game.getGameId(), game);
        return game;
    }

    /**
     * 保存游戏到游戏目录
     */
    public String saveGame(Game game) throws IOException {
        String fileName = generateGameFileName(game);
        String filePath = GAMES_DIRECTORY + "/" + fileName;
        game.saveGame(filePath);
        return filePath;
    }

    /**
     * 生成游戏文件名
     */
    private String generateGameFileName(Game game) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return "scrabble_" + formatter.format(now) + "_" + game.getGameId() + ".sav";
    }

    /**
     * 加载保存的游戏
     */
    public Game loadGame(String filePath) throws IOException, ClassNotFoundException {
        Game game = Game.loadGame(filePath, DEFAULT_DICTIONARY_PATH);
        activeGames.put(game.getGameId(), game);
        return game;
    }

    /**
     * 获取当前活动的游戏实例
     */
    public Game getActiveGame(String gameId) {
        return activeGames.get(gameId);
    }

    /**
     * 关闭游戏实例
     */
    public void closeGame(String gameId) {
        Game game = activeGames.remove(gameId);
        if (game != null) {
            cleanupGameResources(game);
        }
    }

    /**
     * 获取已保存游戏文件信息列表
     */
    public List<SavedGameInfo> getSavedGamesInfo() {
        List<SavedGameInfo> gameInfos = new ArrayList<>();
        File gamesDir = new File(GAMES_DIRECTORY);

        if (gamesDir.exists() && gamesDir.isDirectory()) {
            File[] files = gamesDir.listFiles((dir, name) -> name.endsWith(".sav"));
            if (files != null) {
                for (File file : files) {
                    SavedGameInfo info = extractGameInfo(file);
                    if (info != null) {
                        gameInfos.add(info);
                    }
                }
            }
        }

        return gameInfos;
    }

    /**
     * 从文件名提取游戏信息
     */
    private SavedGameInfo extractGameInfo(File file) {
        try {
            String fileName = file.getName();
            // 解析文件名 scrabble_yyyyMMdd_HHmmss_gameId.sav
            String[] parts = fileName.split("_");
            if (parts.length >= 4) {
                String dateTimeStr = parts[1] + "_" + parts[2];
                String gameId = parts[3].replace(".sav", "");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
                LocalDateTime saveTime = LocalDateTime.parse(dateTimeStr, formatter);

                return new SavedGameInfo(
                        gameId,
                        file.getAbsolutePath(),
                        saveTime,
                        file.length()
                );
            }
        } catch (Exception e) {
            // 解析失败，返回基本信息
            return new SavedGameInfo(
                    "未知",
                    file.getAbsolutePath(),
                    LocalDateTime.now(),
                    file.length()
            );
        }
        return null;
    }

    /**
     * 删除已保存的游戏文件
     */
    public boolean deleteSavedGame(String filePath) {
        File gameFile = new File(filePath);
        return gameFile.exists() && gameFile.delete();
    }

    /**
     * 获取游戏对应的日志文件路径
     */
    public String getGameLogFilePath(Game game) {
        return GameLoggerFactory.getJsonLogFilePath(game.getGameId());
    }

    /**
     * 清理游戏相关资源
     */
    public void cleanupGameResources(Game game) {
        GameLoggerFactory.cleanupGameLogger(game.getGameId());
    }

    /**
     * 内部类：保存的游戏文件信息
     */
    public static class SavedGameInfo {
        private final String gameId;
        private final String filePath;
        private final LocalDateTime saveTime;
        private final long fileSize;

        public SavedGameInfo(String gameId, String filePath, LocalDateTime saveTime, long fileSize) {
            this.gameId = gameId;
            this.filePath = filePath;
            this.saveTime = saveTime;
            this.fileSize = fileSize;
        }

        public String getGameId() {
            return gameId;
        }

        public String getFilePath() {
            return filePath;
        }

        public LocalDateTime getSaveTime() {
            return saveTime;
        }

        public long getFileSize() {
            return fileSize;
        }

        @Override
        public String toString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return "保存时间: " + saveTime.format(formatter) +
                    ", 文件大小: " + (fileSize / 1024) + " KB";
        }
    }
}
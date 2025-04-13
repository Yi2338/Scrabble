package scrabble.UI;

import javafx.stage.Stage;
import scrabble.Game.Game;
import scrabble.Game.GameConfig;
import scrabble.Game.GameFactory;
import scrabble.Game.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 游戏管理器类 - 负责管理游戏实例和界面导航
 * 作为UI和后端逻辑的连接桥梁
 */
public class GameManager {
    private static GameManager instance;
    private Stage primaryStage;
    private Game currentGame;

    // 私有构造函数，确保单例模式
    private GameManager() {
    }

    /**
     * 获取GameManager单例实例
     */
    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    /**
     * 设置主舞台
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * 获取主舞台
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * 创建新游戏
     * @param playerCount 玩家数量(2-4)
     * @return 创建的游戏实例
     */
    public Game createNewGame(int playerCount) throws IOException {
        // 创建玩家列表
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= playerCount; i++) {
            players.add(new Player(i, true)); // 创建人类玩家
        }

        // 使用默认配置创建游戏
        GameConfig config = new GameConfig();
        currentGame = GameFactory.getInstance().createGame(players, config);

        return currentGame;
    }

    /**
     * 获取当前游戏实例
     */
    public Game getCurrentGame() {
        return currentGame;
    }

    /**
     * 设置当前游戏
     */
    public void setCurrentGame(Game game) {
        this.currentGame = game;
    }

    /**
     * 保存当前游戏
     * @return 保存的文件路径
     */
    public String saveCurrentGame() throws IOException {
        if (currentGame != null) {
            // 验证保存目录是否存在
            java.io.File gamesDir = new java.io.File("src/main/resources/games");
            if (!gamesDir.exists()) {
                System.out.println("创建保存目录: " + gamesDir.getAbsolutePath());
                gamesDir.mkdirs();
            } else {
                System.out.println("保存目录已存在: " + gamesDir.getAbsolutePath());
            }
            
            // 保存游戏并获取文件路径
            String filePath = GameFactory.getInstance().saveGame(currentGame);
            System.out.println("游戏已保存到: " + filePath);
            
            // 打印已保存的游戏列表
            java.util.List<GameFactory.SavedGameInfo> savedGames = GameFactory.getInstance().getSavedGamesInfo();
            System.out.println("当前已保存游戏数量: " + savedGames.size());
            for (GameFactory.SavedGameInfo info : savedGames) {
                System.out.println("- " + info.getFilePath() + ", " + info.toString());
            }
            
            return filePath;
        }
        return null;
    }

    /**
     * 加载游戏
     * @param filePath 游戏存档文件路径
     * @return 加载的游戏实例
     */
    public Game loadGame(String filePath) throws IOException, ClassNotFoundException {
        currentGame = GameFactory.getInstance().loadGame(filePath);
        return currentGame;
    }
}
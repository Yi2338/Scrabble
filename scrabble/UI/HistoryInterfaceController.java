package scrabble.UI;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import scrabble.Game.GameFactory;
import scrabble.Game.Game;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import javafx.application.Platform;

/**
 * 历史记录界面控制器 - 负责显示和加载已保存的游戏
 */
public class HistoryInterfaceController {

    @FXML
    private Button save1;

    @FXML
    private Button save2;

    @FXML
    private Button save3;

    @FXML
    private Button save4;

    @FXML
    private Button returnToMainInterface;

    // 存储游戏存档的文件路径
    private List<GameFactory.SavedGameInfo> savedGames;
    private final Button[] saveButtons;
    private AudioManager audioManager;

    /**
     * 构造函数，加载已保存的游戏列表
     */
    public HistoryInterfaceController() {
        // 获取已保存的游戏列表
        savedGames = GameFactory.getInstance().getSavedGamesInfo();
        saveButtons = new Button[4]; // 假设UI中有4个保存按钮
    }

    /**
     * 初始化方法
     */
    @FXML
    private void initialize() {
        System.out.println("历史记录界面已初始化");
        
        // 初始化音频管理器
        audioManager = AudioManager.getInstance();

        // 初始化按钮数组
        saveButtons[0] = save1;
        saveButtons[1] = save2;
        saveButtons[2] = save3;
        saveButtons[3] = save4;

        // 设置返回按钮事件
        returnToMainInterface.setOnAction(event -> {
            audioManager.playClickSound();
            handleReturnButtonAction();
        });

        // 显示已保存的游戏
        displaySavedGames();
    }

    /**
     * 显示已保存的游戏列表
     */
    private void displaySavedGames() {
        System.out.println("开始加载历史游戏列表...");
        
        // 检查保存目录是否存在
        java.io.File gamesDir = new java.io.File("src/main/resources/games");
        if (!gamesDir.exists()) {
            System.out.println("保存目录不存在: " + gamesDir.getAbsolutePath());
        } else {
            System.out.println("保存目录存在: " + gamesDir.getAbsolutePath());
            // 列出目录中的文件
            java.io.File[] files = gamesDir.listFiles((dir, name) -> name.endsWith(".sav"));
            if (files != null) {
                System.out.println("目录中找到" + files.length + "个保存文件：");
                for (java.io.File file : files) {
                    System.out.println("- " + file.getName() + " (" + file.length() + " bytes)");
                }
            } else {
                System.out.println("无法列出保存目录中的文件");
            }
        }
        
        // 先清空所有按钮
        for (Button button : saveButtons) {
            button.setText("空存档槽");
            button.setOnAction(null);
            button.setDisable(true);
        }

        // 按保存时间对保存游戏进行排序（从最新到最旧）
        savedGames.sort(Comparator.comparing(GameFactory.SavedGameInfo::getSaveTime).reversed());
        
        System.out.println("从GameFactory获取到的历史游戏数量: " + savedGames.size());
        
        // 只取最近的4个存档
        List<GameFactory.SavedGameInfo> recentGames = new ArrayList<>();
        for (int i = 0; i < Math.min(savedGames.size(), 4); i++) {
            recentGames.add(savedGames.get(i));
        }
        
        // 填充已保存的游戏（save1显示第四近的，save4显示最近的）
        for (int i = 0; i < recentGames.size(); i++) {
            // 计算反向索引：0->3, 1->2, 2->1, 3->0
            int reverseIndex = recentGames.size() - 1 - i;
            GameFactory.SavedGameInfo gameInfo = recentGames.get(i);
            Button button = saveButtons[reverseIndex];

            // 设置按钮文本显示保存时间和游戏ID
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String saveDateText = gameInfo.getSaveTime().format(formatter);
            button.setText("存档 " + (reverseIndex + 1) + " - " + saveDateText);
            button.setDisable(false);
            System.out.println("设置按钮 " + (reverseIndex + 1) + " 为: " + gameInfo.toString());

            // 设置点击事件，加载对应的游戏，并添加音频
            final int gameIndex = i;
            button.setOnAction(event -> {
                audioManager.playClickSound();
                loadSavedGame(gameIndex);
            });
        }
    }

    /**
     * 加载已保存的游戏
     */
    private void loadSavedGame(int index) {
        if (index >= 0 && index < savedGames.size()) {
            // 播放点击音效
            audioManager.playClickSound();
            
            GameFactory.SavedGameInfo gameInfo = savedGames.get(index);
            String filePath = gameInfo.getFilePath();

            try {
                // 加载游戏
                Game game = GameManager.getInstance().loadGame(filePath);
                
                // 确保游戏是运行状态（而不是暂停状态）
                if (game.getGameState() == Game.GameState.PAUSED) {
                    System.out.println("游戏处于暂停状态，恢复游戏...");
                    game.resumeGame();
                }
                
                // 切换到游戏界面 - 这会注册GameInterfaceController作为监听器
                GameInterfaceController controller = SceneManager.getInstance().switchToGameInterface(game);
                
                // 在UI线程中安排更新时间显示
                Platform.runLater(() -> {
                    // 确保控制器已接收到初始时间值
                    controller.onGameTimeUpdated(game.getRemainingGameTime());
                    controller.onTurnTimeUpdated(game.getRemainingTurnTime());
                });
                
                System.out.println("已加载游戏: " + filePath);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("加载游戏失败: " + e.getMessage());
                e.printStackTrace();

                // 显示错误提示
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("加载错误");
                alert.setHeaderText("无法加载游戏");
                alert.setContentText("加载游戏失败: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    /**
     * 处理返回按钮事件
     */
    private void handleReturnButtonAction() {
        try {
            SceneManager.getInstance().switchToMainInterface();
        } catch (IOException e) {
            System.err.println("无法返回主界面: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
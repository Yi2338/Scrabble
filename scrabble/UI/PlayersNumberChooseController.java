package scrabble.UI;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import scrabble.Game.Game;

import java.io.IOException;

/**
 * 玩家数量选择界面控制器
 */
public class PlayersNumberChooseController {

    @FXML
    private Button twoPlayersButton;

    @FXML
    private Button threePlayersButton;

    @FXML
    private Button fourPlayersButton;

    @FXML
    private Button backbutton;
    
    private AudioManager audioManager;

    /**
     * 初始化方法
     */
    @FXML
    private void initialize() {
        System.out.println("玩家数量选择界面已初始化");
        
        // 初始化音频管理器
        audioManager = AudioManager.getInstance();

        // 设置按钮事件
        twoPlayersButton.setOnAction(event -> {
            audioManager.playClickSound();
            startGameWithPlayers(2);
        });
        
        threePlayersButton.setOnAction(event -> {
            audioManager.playClickSound();
            startGameWithPlayers(3);
        });
        
        fourPlayersButton.setOnAction(event -> {
            audioManager.playClickSound();
            startGameWithPlayers(4);
        });
        
        backbutton.setOnAction(event -> {
            audioManager.playClickSound();
            handleBackButtonAction(event);
        });
    }

    /**
     * 返回按钮事件
     */
    private void handleBackButtonAction(ActionEvent event) {
        try {
            SceneManager.getInstance().switchToMainInterface();
        } catch (IOException e) {
            System.err.println("无法返回主界面: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建指定玩家数量的游戏并启动
     */
    private void startGameWithPlayers(int playerCount) {
        try {
            // 创建新游戏
            Game game = GameManager.getInstance().createNewGame(playerCount);
            if (game == null) {
                System.err.println("错误：无法创建游戏实例");
                return;
            }

            // 切换到游戏界面
            GameInterfaceController controller = SceneManager.getInstance().switchToGameInterface(game);

            // 延迟启动游戏，确保界面已完全加载
            javafx.application.Platform.runLater(() -> {
                try {
                    // 启动游戏
                    game.startGame();
                } catch (Exception e) {
                    System.err.println("启动游戏失败: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("创建游戏失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
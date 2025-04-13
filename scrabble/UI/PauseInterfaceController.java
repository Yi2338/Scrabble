package scrabble.UI;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import scrabble.Game.Game;

import java.io.IOException;

/**
 * 保存界面控制器 - 负责处理游戏保存和退出
 */
public class PauseInterfaceController {

    @FXML
    private Button saveButton;

    @FXML
    private Button exitButton;

    @FXML
    private Button continueButton;

    // 当前游戏实例
    private Game game;
    
    // 音频管理器
    private AudioManager audioManager;

    /**
     * 初始化方法
     */
    @FXML
    private void initialize() {
        System.out.println("保存界面已初始化");
        
        // 初始化音频管理器
        audioManager = AudioManager.getInstance();

        // 设置按钮事件
        saveButton.setOnAction(event -> {
            audioManager.playClickSound();
            handleSaveGame();
        });
        
        exitButton.setOnAction(event -> {
            audioManager.playClickSound();
            handleExitGame();
        });
        
        continueButton.setOnAction(event -> {
            audioManager.playClickSound();
            handleContinueGame();
        });
    }

    /**
     * 设置游戏实例
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * 处理保存游戏按钮
     */
    private void handleSaveGame() {
        if (game != null) {
            try {
                // 保存游戏
                String filePath = GameManager.getInstance().saveCurrentGame();

                // 显示保存成功提示
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("保存成功");
                alert.setHeaderText("游戏已保存");
                alert.setContentText("游戏已成功保存到: " + filePath);
                alert.showAndWait();

                // 返回主界面
                returnToMainMenu();

            } catch (IOException e) {
                System.err.println("保存游戏失败: " + e.getMessage());
                e.printStackTrace();

                // 显示错误提示
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("保存错误");
                alert.setHeaderText("无法保存游戏");
                alert.setContentText("保存游戏失败: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    /**
     * 处理退出游戏按钮
     */
    private void handleExitGame() {
        // 直接返回主界面，不保存游戏
        returnToMainMenu();
    }

    /**
     * 处理继续游戏按钮
     */
    private void handleContinueGame() {
        try {
            // 先恢复游戏
            game.resumeGame();
            // 再切换场景
            SceneManager.getInstance().switchToGameInterface(game);
        } catch (IOException e) {
            System.err.println("无法继续游戏: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 返回主菜单
     */
    private void returnToMainMenu() {
        try {
            SceneManager.getInstance().switchToMainInterface();
        } catch (IOException e) {
            System.err.println("无法返回主界面: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
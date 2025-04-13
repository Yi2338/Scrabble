package scrabble.UI;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

import java.io.IOException;

/**
 * 主界面控制器，处理用户交互事件
 */
public class MainInterfaceController {

    @FXML
    private Button startButton;

    @FXML
    private Button historyButton;

    @FXML
    private Button helpButton;

    @FXML
    private Button settingButton;

    /**
     * 初始化方法，在FXML加载完成后由FXMLLoader调用
     */
    @FXML
    private void initialize() {
        System.out.println("主界面控制器已初始化");
    }

    /**
     * 开始游戏按钮事件处理方法
     */
    @FXML
    private void handleStartButtonAction(ActionEvent event) {
        System.out.println("点击了开始游戏按钮");

        try {
            SceneManager.getInstance().switchToPlayersNumberChoose();
        } catch (IOException e) {
            System.err.println("无法加载玩家数量选择界面: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 历史记录按钮事件处理方法
     */
    @FXML
    private void handleHistoryButtonAction(ActionEvent event) {
        System.out.println("点击了历史记录按钮");

        try {
            SceneManager.getInstance().switchToHistoryInterface();
        } catch (IOException e) {
            System.err.println("无法加载历史记录界面: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 帮助按钮事件处理方法
     */
    @FXML
    private void handleHelpButtonAction(ActionEvent event) {
        System.out.println("点击了帮助按钮");
        
        try {
            SceneManager.getInstance().switchToHelpInterface();
        } catch (IOException e) {
            System.err.println("无法加载帮助界面: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 设置按钮事件处理方法
     */
    @FXML
    private void handleSettingButtonAction(ActionEvent event) {
        System.out.println("点击了设置按钮");
        // 当前不实现设置功能
    }
}
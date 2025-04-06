package scrabble.UI;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

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
        // 这里添加开始游戏的逻辑
    }
    
    /**
     * 历史记录按钮事件处理方法
     */
    @FXML
    private void handleHistoryButtonAction(ActionEvent event) {
        System.out.println("点击了历史记录按钮");
        // 这里添加查看历史记录的逻辑
    }
    
    /**
     * 帮助按钮事件处理方法
     */
    @FXML
    private void handleHelpButtonAction(ActionEvent event) {
        System.out.println("点击了帮助按钮");
        // 这里添加显示帮助信息的逻辑
    }
    
    /**
     * 设置按钮事件处理方法
     */
    @FXML
    private void handleSettingButtonAction(ActionEvent event) {
        System.out.println("点击了设置按钮");
        // 这里添加打开设置界面的逻辑
    }
}
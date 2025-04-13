package scrabble.UI;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.util.StringConverter;
import java.io.IOException;
import scrabble.Game.GameConfig;
import scrabble.AIPlayer.AIPlayer;

/**
 * 设置界面控制器，处理设置界面的交互
 */
public class SettingInterfaceController {

    @FXML private Button returnButton;
    @FXML private Button saveButton;
    
    // 目标分数相关控件
    @FXML private TextField targetScoreField;
    @FXML private Slider targetScoreSlider;
    @FXML private Label targetScoreValueLabel;
    
    // 游戏总时长相关控件
    @FXML private TextField timeLimitField;
    @FXML private Slider timeLimitSlider;
    @FXML private Label timeLimitValueLabel;
    
    // 回合时长相关控件
    @FXML private TextField turnTimeLimitField;
    @FXML private Slider turnTimeLimitSlider;
    @FXML private Label turnTimeLimitValueLabel;
    
    // AI难度选择控件
    @FXML private ComboBox<AIPlayer.Difficulty> aiDifficultyComboBox;
    
    // 游戏配置对象
    private GameConfig gameConfig;
    
    // 添加音频管理器
    private AudioManager audioManager;

    /**
     * 初始化方法，在FXML加载完成后由FXMLLoader调用
     */
    @FXML
    private void initialize() {
        System.out.println("设置界面控制器已初始化");
        
        // 初始化音频管理器
        audioManager = AudioManager.getInstance();
        
        // 初始化游戏配置
        gameConfig = GameManager.getInstance().getGameConfig();
        if (gameConfig == null) {
            gameConfig = new GameConfig(); // 如果不存在则创建默认配置
        }
        
        // 设置AI难度下拉框
        aiDifficultyComboBox.getItems().addAll(AIPlayer.Difficulty.values());
        aiDifficultyComboBox.setValue(gameConfig.getAIDifficulty());
        
        // 设置难度显示文本
        aiDifficultyComboBox.setConverter(new StringConverter<AIPlayer.Difficulty>() {
            @Override
            public String toString(AIPlayer.Difficulty difficulty) {
                if (difficulty == null) return "";
                switch (difficulty) {
                    case NOVICE: return "简单";
                    case NORMAL: return "普通";
                    case MASTER: return "困难";
                    default: return "";
                }
            }
            
            @Override
            public AIPlayer.Difficulty fromString(String string) {
                return null; // 不需要从字符串转换
            }
        });
        
        // 初始化目标分数设置
        targetScoreField.setText(String.valueOf(gameConfig.getTargetScore()));
        targetScoreSlider.setValue(gameConfig.getTargetScore());
        targetScoreValueLabel.setText(String.valueOf(gameConfig.getTargetScore()));
        
        // 添加目标分数滑块监听器
        targetScoreSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int value = newValue.intValue();
            targetScoreField.setText(String.valueOf(value));
            targetScoreValueLabel.setText(String.valueOf(value));
        });
        
        // 添加目标分数文本框监听器
        targetScoreField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int value = Integer.parseInt(newValue);
                if (value >= targetScoreSlider.getMin() && value <= targetScoreSlider.getMax()) {
                    targetScoreSlider.setValue(value);
                    targetScoreValueLabel.setText(String.valueOf(value));
                }
            } catch (NumberFormatException e) {
                // 忽略非数字输入
            }
        });
        
        // 初始化游戏总时长设置
        timeLimitField.setText(String.valueOf(gameConfig.getTimeLimit()));
        timeLimitSlider.setValue(gameConfig.getTimeLimit());
        timeLimitValueLabel.setText(String.valueOf(gameConfig.getTimeLimit()));
        
        // 添加游戏总时长滑块监听器
        timeLimitSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int value = newValue.intValue();
            timeLimitField.setText(String.valueOf(value));
            timeLimitValueLabel.setText(String.valueOf(value));
        });
        
        // 添加游戏总时长文本框监听器
        timeLimitField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int value = Integer.parseInt(newValue);
                if (value >= timeLimitSlider.getMin() && value <= timeLimitSlider.getMax()) {
                    timeLimitSlider.setValue(value);
                    timeLimitValueLabel.setText(String.valueOf(value));
                }
            } catch (NumberFormatException e) {
                // 忽略非数字输入
            }
        });
        
        // 初始化回合时长设置
        turnTimeLimitField.setText(String.valueOf(gameConfig.getTurnTimeLimit()));
        turnTimeLimitSlider.setValue(gameConfig.getTurnTimeLimit());
        turnTimeLimitValueLabel.setText(String.valueOf(gameConfig.getTurnTimeLimit()));
        
        // 添加回合时长滑块监听器
        turnTimeLimitSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int value = newValue.intValue();
            turnTimeLimitField.setText(String.valueOf(value));
            turnTimeLimitValueLabel.setText(String.valueOf(value));
        });
        
        // 添加回合时长文本框监听器
        turnTimeLimitField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int value = Integer.parseInt(newValue);
                if (value >= turnTimeLimitSlider.getMin() && value <= turnTimeLimitSlider.getMax()) {
                    turnTimeLimitSlider.setValue(value);
                    turnTimeLimitValueLabel.setText(String.valueOf(value));
                }
            } catch (NumberFormatException e) {
                // 忽略非数字输入
            }
        });
    }
    
    /**
     * 保存按钮点击事件处理
     */
    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
        System.out.println("点击了保存按钮");
        // 播放点击音效
        audioManager.playClickSound();
        
        // 获取设置的值
        int targetScore = (int) targetScoreSlider.getValue();
        int timeLimit = (int) timeLimitSlider.getValue();
        int turnTimeLimit = (int) turnTimeLimitSlider.getValue();
        AIPlayer.Difficulty aiDifficulty = aiDifficultyComboBox.getValue();
        
        // 更新游戏配置
        gameConfig.setTargetScore(targetScore);
        gameConfig.setTimeLimit(timeLimit);
        gameConfig.setTurnTimeLimit(turnTimeLimit);
        gameConfig.setAIDifficulty(aiDifficulty);
        
        // 保存配置到GameManager
        GameManager.getInstance().setGameConfig(gameConfig);
        
        // 提示保存成功
        System.out.println("设置已保存：目标分数=" + targetScore + 
                ", 游戏总时长=" + timeLimit + 
                ", 回合时长=" + turnTimeLimit + 
                ", AI难度=" + aiDifficulty);
        
        try {
            SceneManager.getInstance().switchToMainInterface();
        } catch (IOException e) {
            System.err.println("无法切换到主界面: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 返回按钮点击事件处理
     */
    @FXML
    private void handleReturnButtonAction(ActionEvent event) {
        System.out.println("点击了返回按钮");
        // 播放点击音效
        audioManager.playClickSound();
        
        try {
            SceneManager.getInstance().switchToMainInterface();
        } catch (IOException e) {
            System.err.println("无法切换到主界面: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 
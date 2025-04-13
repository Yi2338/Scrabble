package scrabble.UI;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.event.ActionEvent;
import java.io.IOException;

/**
 * 帮助界面控制器，处理帮助界面的交互
 */
public class HelpInterfaceController {

    @FXML
    private Button returnButton;

    @FXML
    private ScrollPane helpContent;
    
    // 添加音频管理器
    private AudioManager audioManager;

    /**
     * 初始化方法，在FXML加载完成后由FXMLLoader调用
     */
    @FXML
    private void initialize() {
        System.out.println("帮助界面控制器已初始化");
        
        // 初始化音频管理器
        audioManager = AudioManager.getInstance();
        
        // 添加帮助文本内容到ScrollPane
        populateHelpContent();
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
    
    /**
     * 填充帮助内容
     */
    private void populateHelpContent() {
        TextFlow textFlow = new TextFlow();
        textFlow.setPrefWidth(900); // 设置文本流的宽度
        textFlow.getStyleClass().add("font-size-18"); // 应用游戏字体样式
        textFlow.setStyle("-fx-font-family: '游戏字体', 'Arial', sans-serif;"); // 直接设置游戏字体
        
        // 添加标题 - Introduction to Scrabble
        Text titleText1 = new Text("Introduction to Scrabble\n\n");
        titleText1.getStyleClass().add("font-size-22");
        titleText1.setStyle("-fx-font-family: '游戏字体', 'Arial', sans-serif; -fx-font-size: 22px;"); // 直接设置游戏字体
        
        // 添加第一部分内容
        Text contentText1 = new Text(
            "Scrabble is a strategic word - building game played on a 15x15 square grid. It supports 2 - 4 players. " +
            "When there aren't enough human players, players can opt to have robots join the game, and the difficulty level " +
            "of these robots can be adjusted. At the start of the game, each player is dealt 7 tiles, each marked with a " +
            "letter and a point value.\n\n"
        );
        contentText1.getStyleClass().add("font-size-18");
        contentText1.setStyle("-fx-font-family: '游戏字体', 'Arial', sans-serif; -fx-font-size: 18px;"); // 直接设置游戏字体
        
        // 添加标题 - Game Objectives
        Text titleText2 = new Text("Game Objectives\n\n");
        titleText2.getStyleClass().add("font-size-22");
        titleText2.setStyle("-fx-font-family: '游戏字体', 'Arial', sans-serif; -fx-font-size: 22px;"); // 直接设置游戏字体
        
        // 添加第二部分内容
        Text contentText2 = new Text(
            "The goal is for players to place letters horizontally or vertically on the board to form words and accumulate " +
            "points. Players earn basic points equivalent to the number of letters in the word they create. Moreover, " +
            "squares of different colors on the board have special scoring rules, such as doubling the letter score or " +
            "tripling the word score.\n\n"
        );
        contentText2.getStyleClass().add("font-size-18");
        contentText2.setStyle("-fx-font-family: '游戏字体', 'Arial', sans-serif; -fx-font-size: 18px;"); // 直接设置游戏字体
        
        // 添加标题 - Game Operations
        Text titleText3 = new Text("Game Operations\n\n");
        titleText3.getStyleClass().add("font-size-22");
        titleText3.setStyle("-fx-font-family: '游戏字体', 'Arial', sans-serif; -fx-font-size: 22px;"); // 直接设置游戏字体
        
        // 添加第三部分内容
        Text contentText3 = new Text(
            "During the game, instead of using the keyboard to confirm word placement to end a turn, players use the mouse " +
            "to drag letters from the letter bar onto the board. Players can not only place words but also exchange tiles " +
            "when necessary. Once a player finishes placing a word, the action cannot be changed, and then it's the next " +
            "player's turn.\n\n"
        );
        contentText3.getStyleClass().add("font-size-18");
        contentText3.setStyle("-fx-font-family: '游戏字体', 'Arial', sans-serif; -fx-font-size: 18px;"); // 直接设置游戏字体
        
        // 添加标题 - Explanation of Special Squares
        Text titleText4 = new Text("Explanation of Special Squares\n\n");
        titleText4.getStyleClass().add("font-size-22");
        titleText4.setStyle("-fx-font-family: '游戏字体', 'Arial', sans-serif; -fx-font-size: 22px;"); // 直接设置游戏字体
        
        // 添加第四部分内容
        Text contentText4 = new Text(
            "Squares marked with \"L\" on the board mean that the letter placed on that square will have its score doubled. " +
            "Squares marked with \"W\" indicate that the entire word covering that square will have its score doubled."
        );
        contentText4.getStyleClass().add("font-size-18");
        contentText4.setStyle("-fx-font-family: '游戏字体', 'Arial', sans-serif; -fx-font-size: 18px;"); // 直接设置游戏字体
        
        // 将所有文本添加到TextFlow
        textFlow.getChildren().addAll(
            titleText1, contentText1, 
            titleText2, contentText2, 
            titleText3, contentText3, 
            titleText4, contentText4
        );
        
        // 创建一个AnchorPane作为ScrollPane的内容容器
        AnchorPane contentPane = new AnchorPane();
        contentPane.getChildren().add(textFlow);
        contentPane.setPrefWidth(900);
        contentPane.setStyle("-fx-background-color: transparent;"); // 设置透明背景
        
        // 根据文本内容动态调整内容高度
        textFlow.setLayoutX(20);
        textFlow.setLayoutY(20);
        contentPane.setPrefHeight(Math.max(textFlow.prefHeight(-1) + 40, 900));
        
        helpContent.setContent(contentPane);
    }
} 
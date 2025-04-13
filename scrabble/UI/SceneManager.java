package scrabble.UI;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import scrabble.Game.Game;

import java.io.IOException;

/**
 * 场景管理器 - 负责统一管理所有界面的切换
 */
public class SceneManager {
    private static SceneManager instance;
    private static final String FXML_PATH = "/scrabble/UI/";
    
    // 定义所有FXML文件路径
    public static final String MAIN_INTERFACE = FXML_PATH + "Main_Interface.fxml";
    public static final String GAME_INTERFACE = FXML_PATH + "Main_Game_Interface.fxml";
    public static final String PAUSE_INTERFACE = FXML_PATH + "Pause_Interface.fxml";
    public static final String HISTORY_INTERFACE = FXML_PATH + "History_Interface.fxml";
    public static final String PLAYERS_NUMBER_CHOOSE = FXML_PATH + "Players_Number_Choose.fxml";
    public static final String HELP_INTERFACE = FXML_PATH + "Help_Interface.fxml";
    public static final String SETTING_INTERFACE = FXML_PATH + "Setting_Interface.fxml";

    private SceneManager() {}

    public static synchronized SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    /**
     * 通用的场景切换方法
     */
    public <T> T switchScene(String fxmlPath, Class<T> controllerClass) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (controllerClass != null) {
                loader.setControllerFactory(param -> {
                    try {
                        return controllerClass.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = GameManager.getInstance().getPrimaryStage();
            stage.setScene(scene);
            stage.show();
            
            return loader.getController();
        } catch (IOException e) {
            System.err.println("无法切换到场景 " + fxmlPath + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * 切换到主界面
     */
    public void switchToMainInterface() throws IOException {
        switchScene(MAIN_INTERFACE, MainInterfaceController.class);
    }

    /**
     * 切换到游戏界面
     */
    public GameInterfaceController switchToGameInterface(Game game) throws IOException {
        GameInterfaceController controller = switchScene(GAME_INTERFACE, GameInterfaceController.class);
        controller.setGame(game);
        return controller;
    }

    /**
     * 切换到暂停界面
     */
    public PauseInterfaceController switchToPauseInterface(Game game) throws IOException {
        PauseInterfaceController controller = switchScene(PAUSE_INTERFACE, PauseInterfaceController.class);
        controller.setGame(game);
        return controller;
    }

    /**
     * 切换到历史记录界面
     */
    public void switchToHistoryInterface() throws IOException {
        switchScene(HISTORY_INTERFACE, HistoryInterfaceController.class);
    }

    /**
     * 切换到玩家数量选择界面
     */
    public void switchToPlayersNumberChoose() throws IOException {
        switchScene(PLAYERS_NUMBER_CHOOSE, PlayersNumberChooseController.class);
    }
    
    /**
     * 切换到帮助界面
     */
    public void switchToHelpInterface() throws IOException {
        switchScene(HELP_INTERFACE, HelpInterfaceController.class);
    }
    
    /**
     * 切换到设置界面
     */
    public void switchToSettingInterface() throws IOException {
        switchScene(SETTING_INTERFACE, SettingInterfaceController.class);
    }
} 
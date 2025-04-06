package scrabble.UI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

/**
 * 启动Main_Interface界面的类
 */
public class UILauncher extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 使用类加载器获取资源，这样可以确保在打包后也能找到资源
            URL fxmlUrl = getClass().getResource("/scrabble/UI/Main_Interface.fxml");
            
            if (fxmlUrl == null) {
                // 如果找不到，尝试另一种路径格式
                fxmlUrl = getClass().getResource("/Main_Interface.fxml");
            }
            
            if (fxmlUrl == null) {
                throw new IOException("无法找到FXML文件，请检查资源路径");
            }
            
            System.out.println("加载FXML文件: " + fxmlUrl);
            
            // 加载FXML文件
            Parent root = FXMLLoader.load(fxmlUrl);
            
            // 创建场景
            Scene scene = new Scene(root);
            
            // 设置窗口标题
            primaryStage.setTitle("Scrabble游戏");
            
            // 设置窗口内容
            primaryStage.setScene(scene);
            
            // 显示窗口
            primaryStage.show();
            
            System.out.println("主界面已成功打开！");
            
        } catch (IOException e) {
            System.err.println("加载FXML文件失败: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("UI加载过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 主方法，启动JavaFX应用
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * 提供一个静态方法来从其他类中启动UI
     */
    public static void launchUI() {
        main(new String[]{});
    }
} 
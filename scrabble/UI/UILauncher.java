package scrabble.UI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Scrabble游戏启动器
 * 负责初始化JavaFX应用程序和加载主界面
 */
public class UILauncher extends Application {

    // 作为资源打包的字体路径

    private static final String BUNDLED_FONT_PATH = "/fonts/WinkySans-Light.ttf";
    
    // 字体名称 - 与CSS中使用的字体名称保持一致
    private static final String CUSTOM_FONT_NAME = "游戏字体";

    @Override
    public void start(Stage primaryStage) {
        try {
            // 加载打包的字体
            loadBundledFont();
            
            // 初始化GameManager并设置主舞台
            GameManager.getInstance().setPrimaryStage(primaryStage);

            // 使用类加载器获取资源，这样可以确保在打包后也能找到资源
            URL fxmlUrl = getClass().getResource("/scrabble/UI/Main_Interface.fxml");

            if (fxmlUrl == null) {
                // 如果找不到，尝试另一种路径格式
                fxmlUrl = getClass().getResource("/Main_Interface.fxml");
            }

            if (fxmlUrl == null) {
                // 如果仍然找不到，尝试相对路径
                fxmlUrl = getClass().getClassLoader().getResource("scrabble/UI/Main_Interface.fxml");
            }

            if (fxmlUrl == null) {
                throw new IOException("无法找到FXML文件，请检查资源路径");
            }

            System.out.println("加载FXML文件: " + fxmlUrl);

            // 加载FXML文件
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // 创建场景
            Scene scene = new Scene(root, 1000, 800);
            
            // 添加样式表
            scene.getStylesheets().add("/css/style.css");

            // 设置窗口标题
            primaryStage.setTitle("Scrabble游戏");

            // 设置窗口大小
            primaryStage.setWidth(1016);  // 1000 + 边框
            primaryStage.setHeight(838);  // 800 + 标题栏 + 边框
            primaryStage.setResizable(false);

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
     * 加载打包的字体文件
     */
    private void loadBundledFont() {
        try {
            System.out.println("尝试加载打包的字体: " + BUNDLED_FONT_PATH);
            
            // 从应用资源加载字体
            InputStream fontStream = getClass().getResourceAsStream(BUNDLED_FONT_PATH);
            if (fontStream != null) {
                Font customFont = Font.loadFont(fontStream, 12);
                if (customFont != null) {
                    System.out.println("成功加载打包的自定义字体: " + CUSTOM_FONT_NAME);
                } else {
                    System.out.println("警告: 字体加载返回null，将使用系统字体");
                }
                fontStream.close();
            } else {
                System.out.println("警告: 找不到打包的字体文件: " + BUNDLED_FONT_PATH);
                System.out.println("请确保将字体文件放在 src/main/resources/fonts/ 目录中");
            }
        } catch (Exception e) {
            System.out.println("警告: 加载打包字体时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        System.out.println("应用程序正在关闭...");
        // 执行清理操作，如保存配置等
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
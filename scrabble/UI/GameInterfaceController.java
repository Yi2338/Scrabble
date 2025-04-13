package scrabble.UI;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.effect.DropShadow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import scrabble.Board.Board;
import scrabble.Board.Cell;
import scrabble.Board.CellType;
import scrabble.Game.Game;
import scrabble.Game.Player;
import scrabble.Placement.PlaceTile;
import scrabble.Tile.Tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * 游戏主界面控制器 - 处理棋盘绘制、字母牌交互和游戏流程
 */
public class GameInterfaceController implements Game.GameStateListener {

    @FXML
    private AnchorPane saveGame; // 根容器

    @FXML
    private GridPane boardGrid; // 棋盘网格

    @FXML
    private Label currentplayer; // 当前玩家标签

    @FXML
    private StackPane Tile1, Tile2, Tile3, Tile4, Tile5, Tile6, Tile7; // 字母架上的字母牌

    @FXML
    private Button confirmPlacement; // 确认放置按钮

    @FXML
    private Button cancelPlacement; // 取消放置按钮

    @FXML
    private Button swap; // 交换字母牌按钮

    @FXML
    private Button passTurn; // 跳过回合按钮

    @FXML
    private Button returnToMainInterface; // 返回主界面按钮

    @FXML
    private Button pauseGame; // 暂停游戏按钮

    @FXML
    private Button saveButton; // 保存游戏按钮

    @FXML
    private Label scoreOfPlayer1, scoreOfPlayer2, scoreOfPlayer3, scoreOfPlayer4; // 玩家分数标签

    @FXML
    private Label TurnRemainingTime, GameRemainingTime;//时间标签

    @FXML
    private ToggleButton isAIControl1, isAIControl2, isAIControl3, isAIControl4; // AI控制切换按钮

    @FXML
    private ImageView isAIControl1Image, isAIControl2Image, isAIControl3Image, isAIControl4Image; // AI控制按钮图片

    // 游戏实例
    private Game game;

    // 存储棋盘格子的UI元素
    private StackPane[][] boardCells = new StackPane[Board.BOARD_SIZE][Board.BOARD_SIZE];

    // 存储字母架的UI元素
    private StackPane[] rackTiles;


    private DragDropManager dragDropManager;

    // AI超时相关变量
    private final int AI_TIMEOUT_SECONDS = 10; // AI动作超时时间（秒）
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<Integer, ScheduledFuture<?>> aiTimeoutTasks = new HashMap<>(); // 存储每个AI玩家的计时器任务

    /**
     * 初始化方法
     */
    @FXML
    private void initialize() {
        System.out.println("游戏主界面已初始化");

        // 初始化字母架引用数组
        rackTiles = new StackPane[]{Tile1, Tile2, Tile3, Tile4, Tile5, Tile6, Tile7};

        // 初始化按钮事件
        confirmPlacement.setOnAction(event -> handleConfirmPlacement());
        cancelPlacement.setOnAction(event -> handleCancelPlacement());
        swap.setOnAction(event -> handleSwapTiles());
        passTurn.setOnAction(event -> handlePassTurn());
        pauseGame.setOnAction(event -> handlePauseGame());
        returnToMainInterface.setOnAction(event -> handleReturnToMainInterface());
        saveButton.setOnAction(event -> handleSaveGame());

        // 初始化AI控制按钮
        isAIControl1.setOnAction(event -> toggleAIControl(0));
        isAIControl2.setOnAction(event -> toggleAIControl(1));
        isAIControl3.setOnAction(event -> toggleAIControl(2));
        isAIControl4.setOnAction(event -> toggleAIControl(3));
        
        // 设置AI按钮图片
        setupAIButtonImages();


        // 立即设置棋盘尺寸
        if (boardGrid != null) {
            boardGrid.setPrefSize(600, 600);
            boardGrid.setMaxSize(600, 600);
            boardGrid.setMinSize(600, 600);
            
            // 确保列约束设置正确
            for (int i = 0; i < boardGrid.getColumnConstraints().size(); i++) {
                boardGrid.getColumnConstraints().get(i).setPrefWidth(40);
                boardGrid.getColumnConstraints().get(i).setMinWidth(40);
                boardGrid.getColumnConstraints().get(i).setMaxWidth(40);
            }
            
            // 确保行约束设置正确
            for (int i = 0; i < boardGrid.getRowConstraints().size(); i++) {
                boardGrid.getRowConstraints().get(i).setPrefHeight(40);
                boardGrid.getRowConstraints().get(i).setMinHeight(40);
                boardGrid.getRowConstraints().get(i).setMaxHeight(40);
            }
            
            // 监听场景加载完成
            boardGrid.sceneProperty().addListener((observable, oldScene, newScene) -> {
                if (newScene != null) {
                    // 确保样式表被应用
                    if (!newScene.getStylesheets().contains("/css/style.css")) {
                        newScene.getStylesheets().add("/css/style.css");
                    } else {
                        // 重新加载样式表，确保最新修改生效
                        newScene.getStylesheets().clear();
                        newScene.getStylesheets().add("/css/style.css");
                    }
                    
                    // 在JavaFX应用线程上执行布局更新
                    Platform.runLater(() -> {
                        forceLayoutUpdate();
                    });
                }
            });
        }
    }



    /**
     * 增强强制更新布局方法
     */
    private void forceLayoutUpdate() {
        if (boardGrid != null) {
            // 设置棋盘尺寸
            boardGrid.setPrefSize(600, 600);
            boardGrid.setMaxSize(600, 600);
            boardGrid.setMinSize(600, 600);
            
            // 确保列约束设置正确
            for (int i = 0; i < boardGrid.getColumnConstraints().size(); i++) {
                boardGrid.getColumnConstraints().get(i).setPrefWidth(40);
                boardGrid.getColumnConstraints().get(i).setMinWidth(40);
                boardGrid.getColumnConstraints().get(i).setMaxWidth(40);
            }
            
            // 确保行约束设置正确
            for (int i = 0; i < boardGrid.getRowConstraints().size(); i++) {
                boardGrid.getRowConstraints().get(i).setPrefHeight(40);
                boardGrid.getRowConstraints().get(i).setMinHeight(40);
                boardGrid.getRowConstraints().get(i).setMaxHeight(40);
            }
            
            // 确保字母架正确显示
            if (rackTiles != null) {
                for (StackPane tilePane : rackTiles) {
                    if (tilePane != null) {
                        tilePane.setPrefSize(55, 55);
                        tilePane.setMinSize(55, 55);
                        tilePane.setMaxSize(55, 55);
                    }
                }
            }
            
            // 应用CSS并立即重新布局
            boardGrid.applyCss();
            boardGrid.layout();
            
            // 请求父容器重新布局
            if (boardGrid.getParent() != null) {
                boardGrid.getParent().requestLayout();
                boardGrid.getParent().layout();
            }
            
            // 确保整个场景重新布局
            if (boardGrid.getScene() != null) {
                boardGrid.getScene().getRoot().layout();
            }
            
            // 确保AnchorPane正确显示
            if (saveGame != null) {
                saveGame.requestLayout();
                saveGame.layout();
            }
        }
    }

    /**
     * 设置游戏实例
     */
    public void setGame(Game game) {
        this.game = game;

        // 检查game是否为null
        if (game == null) {
            System.err.println("错误：setGame方法接收到null游戏实例");
            return;
        }
        
        // 初始化拖拽管理器
        dragDropManager = new DragDropManager(game);
        // 设置UI更新回调
        dragDropManager.setUIUpdaters(
            // 棋盘格子更新
            (row, col) -> updateBoardCell(row, col),
            // 字母架更新
            () -> updateRack()
        );
        
        // 在JavaFX应用线程中执行，确保UI操作的线程安全
        Platform.runLater(() -> {
            // 强制更新布局
            forceLayoutUpdate();
            
            try {
                // 初始化界面
                initializeBoard();
                updatePlayerInfo();
                updateRack();
                
                // 再次强制更新布局，确保棋盘大小正确
                forceLayoutUpdate();
            } catch (Exception e) {
                System.err.println("游戏界面初始化异常：" + e.getMessage());
                e.printStackTrace();
            }
        });
        
        // 注册为游戏状态监听器
        game.addGameStateListener(this);
    }

    /**
     * 初始化棋盘
     */
    private void initializeBoard() {
        // 检查是否已找到棋盘网格
        if (boardGrid == null) {
            // 尝试查找GridPane
            boardGrid = (GridPane) saveGame.lookup("GridPane");
            if (boardGrid == null) {
                System.err.println("无法找到棋盘网格，使用默认GridPane");
                boardGrid = new GridPane();
                saveGame.getChildren().add(boardGrid);
            }
        }
        
        // 先确保棋盘大小正确
        boardGrid.setPrefSize(600, 600);
        boardGrid.setMaxSize(600, 600);
        boardGrid.setMinSize(600, 600);
        
        // 确保列约束设置正确
        for (int i = 0; i < boardGrid.getColumnConstraints().size(); i++) {
            boardGrid.getColumnConstraints().get(i).setPrefWidth(40);
            boardGrid.getColumnConstraints().get(i).setMinWidth(40);
            boardGrid.getColumnConstraints().get(i).setMaxWidth(40);
        }
        
        // 确保行约束设置正确
        for (int i = 0; i < boardGrid.getRowConstraints().size(); i++) {
            boardGrid.getRowConstraints().get(i).setPrefHeight(40);
            boardGrid.getRowConstraints().get(i).setMinHeight(40);
            boardGrid.getRowConstraints().get(i).setMaxHeight(40);
        }

        // 清空棋盘
        boardGrid.getChildren().clear();

        // 获取游戏棋盘
        Board board = game.getBoard();
        Cell[][] grid = board.getGrid();

        // 创建棋盘格子
        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int col = 0; col < Board.BOARD_SIZE; col++) {
                Cell cell = grid[row][col];
                StackPane cellPane = createBoardCell(cell, row, col);

                // 添加到GridPane
                boardGrid.add(cellPane, col, row);

                // 保存引用
                boardCells[row][col] = cellPane;
            }
        }
        
        // 确保界面会立即渲染
        Platform.runLater(() -> {
            // 设置棋盘和单元格尺寸
            boardGrid.setPrefSize(600, 600);
            boardGrid.setMaxSize(600, 600);
            boardGrid.setMinSize(600, 600);
            
            for (int row = 0; row < Board.BOARD_SIZE; row++) {
                for (int col = 0; col < Board.BOARD_SIZE; col++) {
                    // 确保每个单元格尺寸一致
                    if (boardCells[row][col] != null) {
                        boardCells[row][col].setPrefSize(40, 40);
                        boardCells[row][col].setMaxSize(40, 40);
                        boardCells[row][col].setMinSize(40, 40);
                    }
                }
            }
            
            // 强制布局更新
            forceLayoutUpdate();
        });
    }

    /**
     * 创建单个棋盘格子
     */
    private StackPane createBoardCell(Cell cell, int row, int col) {
        StackPane cellPane = new StackPane();
        cellPane.setPrefSize(40, 40); // 设置大小
        cellPane.setAlignment(Pos.CENTER);

        // 创建背景矩形
        Rectangle background = new Rectangle(38, 38);
        // 添加圆角效果
        background.setArcWidth(10);
        background.setArcHeight(10);

        // 保存原始颜色，以便在更新时恢复
        Color cellColor = Color.TRANSPARENT;
        
        // 根据格子类型设置颜色
        switch (cell.getCellType()) {
            case DOUBLE_LETTER:
                cellColor = Color.rgb(255,200,151);
                break;
            case TRIPLE_LETTER:
                cellColor = Color.rgb(212,239,93);
                break;
            case DOUBLE_WORD:
                cellColor = Color.rgb(214,202,255);
                break;
            case TRIPLE_WORD:
                cellColor = Color.rgb(37,236,255);
                break;
            case CENTER:
                cellColor = Color.rgb(255,215,0);
                break;
            default:
                cellColor = Color.rgb(255, 247, 229);
                break;
        }

        // 设置背景颜色
        background.setFill(cellColor);
        background.setStroke(Color.rgb(178,230,255));
        
        // 给背景添加ID，方便后续查找
        background.setId("cell-background");
        cellPane.getChildren().add(background);

        // 添加格子类型标签
        if (cell.getCellType() != CellType.NONE) {
            Text typeText = new Text(getCellTypeText(cell.getCellType()));
            // 使用CSS样式设置字体
            typeText.getStyleClass().add("cell-type-label");
            typeText.setId("cell-type-label");
            cellPane.getChildren().add(typeText);
        }

        // 如果格子已有字母牌，显示字母
        if (cell.hasTile()) {
            addTileToCell(cellPane, cell.getTile());
        }

        // 设置拖放目标
        setupCellAsDropTarget(cellPane, row, col);

        // 保存单元格原始颜色到用户数据
        cellPane.setUserData(cellColor);

        return cellPane;
    }

    /**
     * 获取格子类型的显示文本
     */
    private String getCellTypeText(CellType type) {
        switch (type) {
            case DOUBLE_LETTER: return "2L";
            case TRIPLE_LETTER: return "3L";
            case DOUBLE_WORD: return "2W";
            case TRIPLE_WORD: return "3W";
            case CENTER: return "★";
            default: return "";
        }
    }

    /**
     * 为字母牌添加拖拽功能
     */
    private void setupTileForDrag(StackPane tilePane, Tile tile, int rackIndex) {
        // 使用DragDropManager处理拖拽
        dragDropManager.setupSourceFromRack(tilePane, tile, rackIndex);
    }

    /**
     * 更新字母牌的显示
     */
    private void updateTileDisplay(StackPane tilePane, Tile tile) {
        tilePane.getChildren().clear();
        
        // 使用与字母架一致的大小参数
        StackPane newTile = createTileComponent(tile.getLetter(), tile.getValue(), 40, 24, 12, false);
        tilePane.getChildren().addAll(newTile.getChildren());
        
        // 找到该字母牌在字母架中的索引
        List<Tile> rack = game.getTileManager().getPlayerRackList(game.getCurrentPlayer());
        int rackIndex = -1;
        for (int i = 0; i < rack.size(); i++) {
            if (rack.get(i) == tile) {
                rackIndex = i;
                break;
            }
        }
        
        // 重新设置拖拽和点击事件，使用正确的索引
        setupTileForDrag(tilePane, tile, rackIndex);
    }

    /**
     * 为棋盘格子设置拖放目标
     */
    private void setupCellAsDropTarget(StackPane cellPane, int row, int col) {
        // 使用DragDropManager处理拖放
        dragDropManager.setupCellAsDropTarget(cellPane, row, col);
    }

    /**
     * 创建通用字母牌UI组件
     * @param letter 字母
     * @param value 分值
     * @param size 字母牌尺寸（宽高相同）
     * @param fontSize 字母字体大小
     * @param valueOffset 分值偏移量
     * @param addIds 是否添加ID标识
     * @return 字母牌UI组件
     */
    private StackPane createTileComponent(char letter, int value, double size, 
                                         double fontSize, double valueOffset, 
                                         boolean addIds) {
        StackPane tilePane = new StackPane();
        tilePane.setAlignment(Pos.CENTER);
        
        // 创建字母牌背景
        Rectangle tileRect = new Rectangle(size, size);
        // 添加圆角效果
        tileRect.setArcWidth(10);
        tileRect.setArcHeight(10);
        tileRect.setFill(Color.rgb(255,247,229));
        
        // 为空白牌添加特殊的淡蓝色发光边框
        if (value == 0) {
            tileRect.setStroke(Color.rgb(80, 180, 255));
            tileRect.setStrokeWidth(2);
            tileRect.setEffect(new DropShadow(10, Color.rgb(80, 180, 255, 0.7)));
        } else {
            tileRect.setStroke(Color.BLACK);
        }
        
        if (addIds) {
            tileRect.setId("tile-background");
        }
        
        // 创建字母显示
        Text letterText = new Text(String.valueOf(letter));
        // 使用CSS样式设置字体
        letterText.getStyleClass().add("tile-letter-text");
        if (addIds) {
            letterText.setId("tile-letter");
        }
        
        // 创建数值显示
        Text valueText = new Text(String.valueOf(value));
        // 使用CSS设置字体
        valueText.getStyleClass().add("tile-value-text");
        valueText.setTranslateX(valueOffset);
        valueText.setTranslateY(valueOffset);
        if (addIds) {
            valueText.setId("tile-value");
        }
        
        tilePane.getChildren().addAll(tileRect, letterText, valueText);
        return tilePane;
    }

    /**
     * 向格子添加字母牌显示
     */
    private void addTileToCell(StackPane cellPane, Tile tile) {
        // 首先移除所有现有的字母牌组件
        cellPane.getChildren().removeIf(node -> 
            !(node.getId() != null && 
              (node.getId().equals("cell-background") || 
               node.getId().equals("cell-type-label"))));
        
        // 确保背景矩形保持原始颜色
        Rectangle background = null;
        for (javafx.scene.Node node : cellPane.getChildren()) {
            if (node.getId() != null && node.getId().equals("cell-background") && node instanceof Rectangle) {
                background = (Rectangle) node;
                
                // 确保背景使用原始颜色
                if (cellPane.getUserData() instanceof Color) {
                    background.setFill((Color) cellPane.getUserData());
                }
                
                break;
            }
        }

        // 创建字母牌并添加到单元格
        StackPane tilePaneComponent = createTileComponent(tile.getLetter(), tile.getValue(), 30, 18, 8, true);
        
        // 从原始组件中提取子节点并添加到单元格
        cellPane.getChildren().addAll(tilePaneComponent.getChildren());
    }

    /**
     * 更新字母架显示
     */
    private void updateRack() {
        // 检查game是否为null
        if (game == null || game.getTileManager() == null || game.getCurrentPlayer() == null) {
            System.err.println("更新字母架失败：游戏、TileManager或当前玩家为null");
            return;
        }
        
        // 获取当前玩家的字母架
        List<Tile> rack = game.getTileManager().getPlayerRackList(game.getCurrentPlayer());
        
        // 检查rack是否为null
        if (rack == null) {
            System.err.println("更新字母架失败：玩家字母架为null");
            return;
        }

        // 清空字母架UI
        for (StackPane tilePane : rackTiles) {
            if (tilePane != null) {
                tilePane.getChildren().clear();
                // 清除所有事件处理器
                tilePane.setOnMouseClicked(null);
                tilePane.setOnDragDetected(null);
                tilePane.setOnDragOver(null);
                tilePane.setOnDragEntered(null);
                tilePane.setOnDragExited(null);
                tilePane.setOnDragDropped(null);
            }
        }

        // 显示字母牌
        for (int i = 0; i < rack.size() && i < rackTiles.length; i++) {
            Tile tile = rack.get(i);
            StackPane tilePane = rackTiles[i];
            
            // 检查组件是否为null
            if (tilePane == null) {
                continue;
            }

            // 创建字母牌并添加到字母架
            StackPane tilePaneComponent = createTileComponent(tile.getLetter(), tile.getValue(), 40, 24, 12, false);
            tilePane.getChildren().addAll(tilePaneComponent.getChildren());

            // 设置拖拽和点击功能
            setupTileForDrag(tilePane, tile, i);
            
            // 设置字母架格子为拖拽目标
            dragDropManager.setupRackAsDropTarget(tilePane, i);
        }
        
        // 设置空格子也可以作为拖拽目标
        for (int i = rack.size(); i < rackTiles.length; i++) {
            if (rackTiles[i] != null) {
                dragDropManager.setupRackAsDropTarget(rackTiles[i], i);
            }
        }
    }

    /**
     * 设置AI按钮的图片
     */
    private void setupAIButtonImages() {
        try {
            // 获取图片资源
            Image aiImage = new Image(getClass().getResourceAsStream("/photos/AI.PNG"));
            Image humanImage = new Image(getClass().getResourceAsStream("/photos/human.PNG"));
            
            // 设置初始图片为人类图标
            isAIControl1Image.setImage(humanImage);
            isAIControl2Image.setImage(humanImage);
            isAIControl3Image.setImage(humanImage);
            isAIControl4Image.setImage(humanImage);
            
            // 移除按钮的背景和边框，使图片完全显示
            String buttonStyle = "-fx-background-color: transparent; -fx-background-radius: 0; -fx-border-color: transparent;";
            isAIControl1.setStyle(buttonStyle);
            isAIControl2.setStyle(buttonStyle);
            isAIControl3.setStyle(buttonStyle);
            isAIControl4.setStyle(buttonStyle);
            
        } catch (Exception e) {
            System.err.println("加载AI/人类图片失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 更新玩家信息显示
     */
    private void updatePlayerInfo() {
        List<Player> players = game.getPlayers();

        // 更新当前玩家标签
        Player currentPlayer = game.getCurrentPlayer();
        currentplayer.setText("Player " + currentPlayer.getPlayerIndex());

        // 更新分数显示
        Label[] scoreLabels = {scoreOfPlayer1, scoreOfPlayer2, scoreOfPlayer3, scoreOfPlayer4};
        ToggleButton[] aiButtons = {isAIControl1, isAIControl2, isAIControl3, isAIControl4};
        ImageView[] aiImages = {isAIControl1Image, isAIControl2Image, isAIControl3Image, isAIControl4Image};

        try {
            // 获取AI和人类图片，注意大小写
            Image aiImage = new Image(getClass().getResourceAsStream("/photos/AI.PNG"));
            Image humanImage = new Image(getClass().getResourceAsStream("/photos/human.PNG"));

            for (int i = 0; i < players.size(); i++) {
                if (i < scoreLabels.length) {
                    Player player = players.get(i);
                    Label scoreLabel = scoreLabels[i];
                    ToggleButton aiButton = aiButtons[i];
                    ImageView aiImageView = aiImages[i];

                    // 设置分数显示，使用"score"作为得分的英文表示
                    scoreLabel.setText("Player " + player.getPlayerIndex() + ": " + player.getScore() + " score");
                    scoreLabel.setVisible(true);

                    // 高亮显示当前玩家，但保持字体样式
                    if (player == currentPlayer) {
                        // 仅设置背景色，不修改字体样式
                        scoreLabel.setStyle("-fx-background-color: rgba(255,255,0,0.3);");
                    } else {
                        // 移除背景色，保持默认字体样式
                        scoreLabel.setStyle("");
                    }

                    // 显示AI控制状态
                    aiButton.setVisible(true);
                    boolean isAI = game.isAIControlled(player);
                    aiButton.setSelected(isAI);
                    
                    // 更新AI按钮图片
                    if (isAI) {
                        aiImageView.setImage(aiImage);
                        aiButton.setStyle("-fx-background-color: transparent; -fx-background-radius: 0; -fx-border-color: transparent;");
                    } else {
                        aiImageView.setImage(humanImage);
                        aiButton.setStyle("-fx-background-color: transparent; -fx-background-radius: 0; -fx-border-color: transparent;");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("更新AI按钮图片失败: " + e.getMessage());
            e.printStackTrace();
        }

        // 隐藏未使用的玩家信息
        for (int i = players.size(); i < scoreLabels.length; i++) {
            scoreLabels[i].setVisible(false);
            aiButtons[i].setVisible(false);
        }
    }

    /**
     * 处理确认放置按钮
     */
    private void handleConfirmPlacement() {
        int score = game.confirmPlacement();

        if (score > 0) {
            System.out.println("放置确认成功，得分: " + score);
            // 获取当前回合信息，包括形成的单词
            List<String> formedWords = game.getCurrentTurn().getFormedWords();
            if (formedWords != null && !formedWords.isEmpty()) {
                System.out.println("UI层 - 玩家 " + game.getCurrentPlayer().toString() + " 拼出了以下单词:");
                for (String word : formedWords) {
                    System.out.println("  - " + word);
                }
            }
        } else {
            System.out.println("放置确认失败");
   
            // 更新棋盘和字母架显示，确保字母牌返回字母架
            updateBoard();
            updateRack();
        }
    }

    /**
     * 处理取消放置按钮
     */
    private void handleCancelPlacement() {
        boolean canceled = game.cancelPlacement();

        if (canceled) {
            System.out.println("成功取消放置");
            // 更新棋盘和字母架显示
            updateBoard();
            updateRack();
        } else {
            System.out.println("取消放置失败");
        }
    }

    /**
     * 处理交换字母牌按钮
     */
    private void handleSwapTiles() {
        // 先取消所有已放置的字母牌
        game.cancelPlacement();
        
        updateBoard();
        updateRack();

        // 获取玩家字母架
        List<Tile> rack = game.getTileManager().getPlayerRackList(game.getCurrentPlayer());
        
        // 检查是否有选中的字母牌
        List<Tile> selectedTiles = game.getTileManager().getSelectedTile(game.getCurrentPlayer());
        List<Integer> selectedIndices = new ArrayList<>();
        
        if (selectedTiles.isEmpty()) {
            // 如果没有选中任何字母牌，则选中所有字母牌
            for (int i = 0; i < rack.size(); i++) {
                selectedIndices.add(i);
                game.getTileManager().markTileAsSelected(game.getCurrentPlayer(), rack.get(i));
            }
        } else {
            // 找出选中字母在字母架中的索引
            for (Tile tile : selectedTiles) {
                for (int i = 0; i < rack.size(); i++) {
                    if (rack.get(i) == tile) {
                        selectedIndices.add(i);
                        break;
                    }
                }
            }
        }
        
        // 先执行交换字母，但不立即更新UI
        boolean swapped = game.exchangeTiles();
        
        if (swapped) {
            System.out.println("成功交换字母牌");
            
            // 获取交换后的字母架
            List<Tile> newRack = game.getTileManager().getPlayerRackList(game.getCurrentPlayer());
            
            // 创建一个并行动画，同时执行所有选中字母的老虎机效果
            ParallelTransition parallelTransition = new ParallelTransition();
            
            // 为每个选中的位置创建字母翻滚动画
            for (Integer index : selectedIndices) {
                if (index < 0 || index >= rackTiles.length || index >= newRack.size()) continue;
                
                StackPane tilePane = rackTiles[index];
                if (tilePane == null) continue;
                
                // 创建老虎机动画效果
                ParallelTransition slotMachineEffect = createSlotMachineEffect(tilePane, newRack.get(index).getLetter());
                parallelTransition.getChildren().add(slotMachineEffect);
            }
            
            // 所有动画完成后的操作
            parallelTransition.setOnFinished(event -> {
                // 确保选中状态被清除（模型层面）
                game.getTileManager().clearSelectedTiles(game.getCurrentPlayer());
                
                // 更新字母架显示
                updateRack();
                
                // 清除所有选中状态的样式（UI层面）
                for (StackPane tilePane : rackTiles) {
                    if (tilePane != null) {
                        tilePane.setStyle("");
                    }
                }
                
                // 延迟0.5秒后切换到下一个玩家
                PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
                delay.setOnFinished(e -> {
                    // 手动结束当前玩家的回合
                    game.endPlayerTurn();
                });
                delay.play();
            });
            
            // 开始播放动画
            parallelTransition.play();
        } else {
            System.out.println("交换字母牌失败");
        }
    }

    /**
     * 创建老虎机滚动效果
     * @param tilePane 字母牌容器
     * @param finalLetter 最终显示的字母
     * @return 动画效果
     */
    private ParallelTransition createSlotMachineEffect(StackPane tilePane, char finalLetter) {
        // 清空现有内容
        tilePane.getChildren().clear();
        
        // 创建老虎机效果的容器
        VBox slotContainer = new VBox();
        slotContainer.setAlignment(Pos.CENTER);
        tilePane.getChildren().add(slotContainer);
        
        // 创建随机字母序列
        Random random = new Random();
        char[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        
        // 创建初始字母牌
        StackPane letterTile = createTileComponent(
            letters[random.nextInt(letters.length)], 
            random.nextInt(10) + 1,
            40, 24, 12, false
        );
        slotContainer.getChildren().add(letterTile);
        
        // 创建字母变换动画
        Timeline letterChangeTimeline = new Timeline();
        for (int i = 0; i < 15; i++) {
            final int iteration = i;
            KeyFrame keyFrame = new KeyFrame(Duration.millis(100 * i), e -> {
                char randomLetter = (iteration == 14) ? finalLetter : letters[random.nextInt(letters.length)];
                int randomValue = (iteration == 14) ? getTileValue(finalLetter) : random.nextInt(10) + 1;
                
                // 更新显示的字母牌
                slotContainer.getChildren().clear();
                slotContainer.getChildren().add(createTileComponent(
                    randomLetter, randomValue, 40, 24, 12, false
                ));
            });
            letterChangeTimeline.getKeyFrames().add(keyFrame);
        }
        
        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().add(letterChangeTimeline);
        return parallelTransition;
    }

    /**
     * 获取字母的分值
     */
    private int getTileValue(char letter) {
        // 这里应该使用游戏中的实际字母分值，这只是示例
        switch (Character.toUpperCase(letter)) {
            case 'A': case 'E': case 'I': case 'O': case 'U': case 'L': case 'N': case 'S': case 'T': case 'R':
                return 1;
            case 'D': case 'G':
                return 2;
            case 'B': case 'C': case 'M': case 'P':
                return 3;
            case 'F': case 'H': case 'V': case 'W': case 'Y':
                return 4;
            case 'K':
                return 5;
            case 'J': case 'X':
                return 8;
            case 'Q': case 'Z':
                return 10;
            default:
                return 0;
        }
    }

    /**
     * 处理跳过回合按钮
     */
    private void handlePassTurn() {
        game.passTurn();
        System.out.println("跳过当前回合");
    }

    /**
     * 处理暂停游戏按钮
     */
    private void handlePauseGame() {
        if (game.getGameState() == Game.GameState.RUNNING) {
            game.pauseGame();
            pauseGame.setText("Continue");
            
            try {
                SceneManager.getInstance().switchToPauseInterface(game);
            } catch (IOException e) {
                System.err.println("无法加载暂停界面: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (game.getGameState() == Game.GameState.PAUSED) {
            game.resumeGame();
            pauseGame.setText("Pause");
        }
    }

    /**
     * 处理返回主界面按钮
     */
    private void handleReturnToMainInterface() {
        try {
            // 暂停游戏，防止AI在后台继续运行
            if (game.getGameState() == Game.GameState.RUNNING) {
                game.pauseGame();
            }
            
            // 移除游戏状态监听器，避免继续触发UI更新
            game.removeGameStateListener(this);
            
            // 关闭所有AI超时计时器
            for (Player player : game.getPlayers()) {
                cancelAITimeoutTimer(player);
            }
            
            // 关闭调度器
            shutdownScheduler();
            
            SceneManager.getInstance().switchToMainInterface();
        } catch (IOException e) {
            System.err.println("无法返回主界面: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 切换AI控制
     */
    private void toggleAIControl(int playerIndex) {
        game.cancelPlacement();
        List<Player> players = game.getPlayers();
        if (playerIndex < players.size()) {
            Player player = players.get(playerIndex);
            boolean isAIControlled = game.isAIControlled(player);

            if (isAIControlled) {
                game.disableAIControl(player);
                System.out.println("已关闭玩家 " + player.getPlayerIndex() + " 的AI控制");
                // 取消AI超时计时器
                cancelAITimeoutTimer(player);
            } else {
                game.enableAIControl(player);
                System.out.println("已开启玩家 " + player.getPlayerIndex() + " 的AI控制");
                
                // 如果是当前玩家被设置为AI托管，立即执行AI回合并启动超时计时器
                if (player == game.getCurrentPlayer()) {
                    // 启动AI超时计时器
                    startAITimeoutTimer(player);
                    
                    new Thread(() -> {
                        try {
                            // 为AI回合添加一点延迟，使游戏更自然
                            Thread.sleep(500);
                            game.getAIPlayerFactory().playAITurnIfControlled(player);
                        } catch (InterruptedException e) {
                            System.err.println("AI回合执行被中断: " + e.getMessage());
                        }
                    }).start();
                }
            }
            
            // 更新UI显示
            updatePlayerInfo();
        }
    }

    /**
     * 更新整个棋盘
     */
    private void updateBoard() {
        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int col = 0; col < Board.BOARD_SIZE; col++) {
                updateBoardCell(row, col);
            }
        }
    }

    /**
     * 更新单个棋盘格子
     */
    private void updateBoardCell(int row, int col) {
        StackPane cellPane = boardCells[row][col];
        Cell cell = game.getBoardOperator().getCell(row, col);

        // 检查是否包含背景矩形
        boolean hasBackground = false;
        for (javafx.scene.Node node : cellPane.getChildren()) {
            if (node.getId() != null && node.getId().equals("cell-background")) {
                hasBackground = true;
                
                // 确保背景使用原始颜色
                if (node instanceof Rectangle && cellPane.getUserData() instanceof Color) {
                    ((Rectangle) node).setFill((Color) cellPane.getUserData());
                }
                
                break;
            }
        }

        // 如果没有背景，创建一个
        if (!hasBackground && cellPane.getUserData() instanceof Color) {
            Rectangle background = new Rectangle(38, 38);
            background.setFill((Color) cellPane.getUserData());
            background.setStroke(Color.BLACK);
            background.setId("cell-background");
            
            // 确保背景是第一个子元素
            if (!cellPane.getChildren().isEmpty()) {
                cellPane.getChildren().add(0, background);
            } else {
                cellPane.getChildren().add(background);
            }
        }

        // 移除所有现有的字母牌组件
        cellPane.getChildren().removeIf(node -> 
            !(node.getId() != null && 
              (node.getId().equals("cell-background") || 
               node.getId().equals("cell-type-label"))));

        // 如果格子有字母牌，显示字母
        if (cell.hasTile()) {
            addTileToCell(cellPane, cell.getTile());
        }
    }

    // GameStateListener接口实现

    @Override
    public void onGameStateChanged(Game.GameState newState) {
        Platform.runLater(() -> {
            System.out.println("游戏状态变为: " + newState);

            switch (newState) {
                case RUNNING:
                    pauseGame.setText("Pause");
                    break;
                case PAUSED:
                    pauseGame.setText("Continue");
                    break;
                case FINISHED:
                    // 游戏结束，显示结果
                    showGameResult();
                    break;
            }
        });
    }

    @Override
    public void onTurnChanged(Player player, int remainingTurnTime) {
        Platform.runLater(() -> {
            System.out.println("轮到玩家 " + player.getPlayerIndex() + " 的回合，剩余时间: " + remainingTurnTime + "秒");

            // 清除当前玩家的所有选中状态
            game.getTileManager().clearSelectedTiles(player);
            
            // 清除UI上的选中样式
            for (StackPane tilePane : rackTiles) {
                if (tilePane != null) {
                    tilePane.setStyle("");
                }
            }

            // 更新玩家信息
            updatePlayerInfo();
            // 更新棋盘，确保AI玩家的移动显示在棋盘上
            updateBoard();
            // 更新字母架
            updateRack();
            // 更新回合时间
            TurnRemainingTime.setText("Turn Time: " + formatTime(remainingTurnTime));
            
            // 如果当前玩家是AI控制的，启动超时计时器
            if (game.isAIControlled(player)) {
                startAITimeoutTimer(player);
            } else {
                // 不是AI控制的玩家，取消所有计时器
                for (Player p : game.getPlayers()) {
                    cancelAITimeoutTimer(p);
                }
            }
        });
    }

    @Override
    public void onTurnTimeUpdated(int remainingTurnTime) {
        Platform.runLater(() -> {
            TurnRemainingTime.setText("Turn Time: " + formatTime(remainingTurnTime));
        });
    }

    @Override
    public void onGameTimeUpdated(int remainingGameTimeSeconds) {
        Platform.runLater(() -> {
            GameRemainingTime.setText("Game Time: " + formatTime(remainingGameTimeSeconds));
        });
    }

    @Override
    public void onGameOver(Player winner) {
        Platform.runLater(() -> {
            // 关闭所有AI超时计时器
            for (Player player : game.getPlayers()) {
                cancelAITimeoutTimer(player);
            }
            
            // 关闭调度器
            shutdownScheduler();
            
            showGameResult();
        });
    }

    /**
     * 格式化时间显示
     */
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    /**
     * 显示游戏结果
     */
    private void showGameResult() {
        // 找出获胜者
        Player winner = null;
        int highestScore = -1;

        for (Player player : game.getPlayers()) {
            if (player.getScore() > highestScore) {
                highestScore = player.getScore();
                winner = player;
            }
        }

        // 显示游戏结果对话框
        if (winner != null) {
            // 这里可以实现一个游戏结果对话框，但为简单起见，我们只在控制台输出
            System.out.println("游戏结束！玩家 " + winner.getPlayerIndex() + " 获胜，得分: " + winner.getScore());

            // 也可以在UI上显示
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText("Game Finished");
            alert.setContentText("Player " + winner.getPlayerIndex() + " wins, final score: " + winner.getScore());
            alert.showAndWait();
        }
    }

    /**
     * 处理保存游戏按钮
     */
    private void handleSaveGame() {
        try {
            // 调用GameManager的saveCurrentGame方法保存游戏
            String filePath = GameManager.getInstance().saveCurrentGame();
            
            if (filePath != null) {
                // 显示保存成功提示
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Save Successful");
                alert.setHeaderText("Game Saved");
                alert.setContentText("Game saved to: " + filePath);
                alert.showAndWait();
            } else {
                System.err.println("保存游戏失败: 返回的文件路径为null");
                // 显示保存失败提示
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Save Error");
                alert.setHeaderText("Game Save Failed");
                alert.setContentText("Save game failed: invalid file path");
                alert.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("保存游戏失败: " + e.getMessage());
            e.printStackTrace();
            
            // 显示保存失败提示
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Save Error");
            alert.setHeaderText("Game Save Failed");
            alert.setContentText("Save game failed: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * 启动AI超时计时器，如果AI超过指定时间没有行动，则刷新托管
     * @param player 被监控的AI玩家
     */
    private void startAITimeoutTimer(Player player) {
        // 取消之前的计时器（如果存在）
        cancelAITimeoutTimer(player);
        
        // 创建新的计时器任务 - 每AI_TIMEOUT_SECONDS秒执行一次
        ScheduledFuture<?> timeoutTask = scheduler.scheduleAtFixedRate(() -> {
            // 在JavaFX应用线程上执行UI操作
            Platform.runLater(() -> {
                // 只有当游戏仍在运行且玩家仍然是当前玩家时才刷新托管
                if (game.getGameState() == Game.GameState.RUNNING && 
                    game.getCurrentPlayer() == player && 
                    game.isAIControlled(player)) {
                    
                    System.out.println("AI玩家 " + player.getPlayerIndex() + " 超时未行动，重新启动托管");
                    
                    // 先禁用AI控制，然后重新启用
                    game.disableAIControl(player);
                    game.enableAIControl(player);
                    
                    // 重新执行AI回合
                    new Thread(() -> {
                        try {
                            Thread.sleep(500); // 增加短暂延迟
                            game.getAIPlayerFactory().playAITurnIfControlled(player);
                        } catch (InterruptedException e) {
                            System.err.println("AI回合执行被中断: " + e.getMessage());
                        }
                    }).start();
                }
            });
        }, AI_TIMEOUT_SECONDS, AI_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        // 保存计时器任务
        aiTimeoutTasks.put(player.getPlayerIndex(), timeoutTask);
    }
    
    /**
     * 取消AI超时计时器
     * @param player 玩家
     */
    private void cancelAITimeoutTimer(Player player) {
        ScheduledFuture<?> task = aiTimeoutTasks.get(player.getPlayerIndex());
        if (task != null && !task.isDone()) {
            task.cancel(true);
        }
    }
    
    /**
     * 关闭调度器
     */
    private void shutdownScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    /**
     * 创建单个字母牌UI组件
     */
    private StackPane createLetterTile(char letter, int value) {
        return createTileComponent(letter, value, 40, 24, 12, false);
    }
}
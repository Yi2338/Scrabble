package scrabble.UI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import scrabble.Board.Cell;
import scrabble.Game.Game;
import scrabble.Game.Player;
import scrabble.Placement.PlaceTile;
import scrabble.Tile.Tile;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 拖放管理器 - 处理游戏中字母牌的拖拽操作
 */
public class DragDropManager {

    private final Game game;

    // 拖拽源信息
    public static class DragSource {
        public int row = -1; // 棋盘行
        public int col = -1; // 棋盘列
        public int rackIndex = -1; // 字母架索引
        public Tile tile; // 字母牌

        public void reset() {
            row = -1;
            col = -1;
            rackIndex = -1;
            tile = null;
        }

        public boolean isFromRack() {
            return rackIndex >= 0;
        }

        public boolean isFromBoard() {
            return row >= 0 && col >= 0;
        }
    }

    private final DragSource dragSource = new DragSource();
    
    // 用于UI更新的回调
    private BiConsumer<Integer, Integer> boardCellUpdater;
    private Runnable rackUpdater;

    /**
     * 构造函数
     */
    public DragDropManager(Game game) {
        this.game = game;
    }

    /**
     * 设置UI更新回调
     * @param boardCellUpdater 棋盘格子更新回调，参数为行和列
     * @param rackUpdater 字母架更新回调
     */
    public void setUIUpdaters(BiConsumer<Integer, Integer> boardCellUpdater, Runnable rackUpdater) {
        this.boardCellUpdater = boardCellUpdater;
        this.rackUpdater = rackUpdater;
    }

    /**
     * 获取拖拽源信息
     */
    public DragSource getDragSource() {
        return dragSource;
    }

    /**
     * 设置节点为拖拽源（从字母架）
     */
    public void setupSourceFromRack(Node node, Tile tile, int rackIndex) {
        node.setOnDragDetected(event -> {
            // 重置拖拽源信息
            dragSource.reset();

            // 记录当前拖拽源
            dragSource.rackIndex = rackIndex;
            dragSource.tile = tile;

            // 选中字母牌
            game.getTileManager().clearSelectedTiles(game.getCurrentPlayer());
            game.getTileManager().markTileAsSelected(game.getCurrentPlayer(), tile);

            // 创建拖拽内容
            Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(tile.getLetter()));
            db.setContent(content);

            // 使用节点快照作为拖拽图标
            db.setDragView(node.snapshot(null, null));

            event.consume();
        });
        
        // 为节点添加点击事件处理
        node.setOnMouseClicked(event -> {
            if (tile.isBlank()) {
                System.out.println("点击了空白牌");
                handleBlankTileClick(tile, (StackPane) node);
                event.consume(); 
            } else {
                // 选中或取消选中字母牌
                List<Tile> rack = game.getTileManager().getPlayerRackList(game.getCurrentPlayer());
                if (rackIndex >= 0 && rackIndex < rack.size()) {
                    Tile clickedTile = rack.get(rackIndex);
                    if (game.getTileManager().getSelectedTile(game.getCurrentPlayer()).contains(clickedTile)) {
                        game.getTileManager().unmarkTileAsSelected(game.getCurrentPlayer(), clickedTile);
                        // 移除高亮
                        node.setStyle("");
                    } else {
                        game.getTileManager().markTileAsSelected(game.getCurrentPlayer(), clickedTile);
                        // 添加高亮
                        node.setStyle("-fx-border-color: blue; -fx-border-width: 2;");
                    }
                }
            }
        });
    }

    /**
     * 处理空白字母牌的点击事件
     */
    private void handleBlankTileClick(Tile tile, StackPane tilePane) {
        // 创建字母选择对话框
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("选择字母");

        // 创建字母选择界面
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        // 创建字母按钮网格
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setAlignment(Pos.CENTER);

        // 添加A-Z的按钮
        int col = 0;
        int row = 0;
        for (char c = 'A'; c <= 'Z'; c++) {
            final char selectedLetter = c; // 创建final变量
            Button letterButton = new Button(String.valueOf(c));
            letterButton.setPrefSize(40, 40);
            letterButton.setOnAction(e -> {
                // 使用TileManager的方法设置空白牌的字母
                if (game.getTileManager().assignLetterToBlankTile(game.getCurrentPlayer(), tile, selectedLetter)) {
                    // 通知更新字母架显示
                    if (rackUpdater != null) {
                        rackUpdater.run();
                    }
                    dialog.close();
                }
            });
            grid.add(letterButton, col, row);
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }

        vbox.getChildren().add(grid);
        Scene scene = new Scene(vbox);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * 设置节点为拖拽源（从棋盘）
     */
    public void setupSourceFromBoard(Node node, int row, int col, Tile tile) {
        node.setOnDragDetected(event -> {
            // 重置拖拽源信息
            dragSource.reset();

            // 记录当前拖拽源
            dragSource.row = row;
            dragSource.col = col;
            dragSource.tile = tile;

            // 创建拖拽内容
            Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(tile.getLetter()));
            db.setContent(content);

            // 使用节点快照作为拖拽图标
            db.setDragView(node.snapshot(null, null));

            event.consume();
        });
    }

    /**
     * 设置棋盘格子作为拖拽源，只允许当前回合放置的字母牌被拖拽
     */
    public void setupBoardCellAsSource(StackPane cellPane, int row, int col) {
        cellPane.setOnDragDetected(event -> {
            // 只有当格子中有字母牌才允许拖拽
            Cell cell = game.getBoardOperator().getCell(row, col);
            if (cell != null && cell.hasTile()) {
                Tile tile = cell.getTile();
                
                // 检查是否是当前回合放置的字母牌
                boolean isCurrentTurnTile = false;
                List<PlaceTile.TilePlacement> placements = game.getPlaceTile().getCurrentPlacements(game.getCurrentPlayer());
                if (placements != null) {
                    for (PlaceTile.TilePlacement placement : placements) {
                        if (placement.getRow() == row && placement.getCol() == col) {
                            isCurrentTurnTile = true;
                            break;
                        }
                    }
                }

                // 只有当前回合放置的字母牌才能移动
                if (isCurrentTurnTile) {
                    // 记录拖拽源信息
                    dragSource.reset();
                    dragSource.row = row;
                    dragSource.col = col;
                    dragSource.tile = tile;

                    // 创建拖拽内容
                    Dragboard db = cellPane.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(String.valueOf(tile.getLetter()));
                    db.setContent(content);

                    // 使用当前节点的快照作为拖拽图标
                    db.setDragView(cellPane.snapshot(null, null));

                    event.consume();
                }
            }
        });
    }

    /**
     * 设置面板为拖放目标
     */
    public void setupTarget(Pane targetPane, int row, int col, Runnable onSuccessDrop) {
        // 接受拖拽进入
        targetPane.setOnDragOver(event -> {
            if (event.getGestureSource() != targetPane && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        // 拖拽进入时高亮显示
        targetPane.setOnDragEntered(event -> {
            if (event.getGestureSource() != targetPane && event.getDragboard().hasString()) {
                // 只设置边框样式，不改变背景颜色或填充
                targetPane.setStyle("-fx-border-color: green; -fx-border-width: 2;");
            }
            event.consume();
        });

        // 拖拽离开时恢复显示
        targetPane.setOnDragExited(event -> {
            // 完全清除样式设置
            targetPane.setStyle("");
            event.consume();
        });

        // 处理拖放
        targetPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString() && dragSource.tile != null) {
                // 处理拖放
                if (dragSource.isFromRack()) {
                    // 从字母架拖到棋盘
                    success = placeTileFromRackToBoard(dragSource.tile, row, col);
                } else if (dragSource.isFromBoard()) {
                    // 在棋盘上移动字母牌
                    success = moveTileOnBoard(dragSource.row, dragSource.col, row, col);
                }

                // 如果成功，清空拖拽源信息
                if (success) {
                    dragSource.reset();

                    // 执行成功回调
                    if (onSuccessDrop != null) {
                        onSuccessDrop.run();
                    }
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }
    
    /**
     * 从字母架放置字母牌到棋盘
     */
    private boolean placeTileFromRackToBoard(Tile tile, int row, int col) {
        // 检查格子是否为空
        if (game.getBoardOperator().isCellOccupied(row, col)) {
            System.out.println("目标格子 (" + row + "," + col + ") 已被占用");
            return false;
        }

        // 放置字母牌
        boolean success = game.getPlaceTile().placeSelectedTileOnBoard(game.getCurrentPlayer(), row, col);

        if (success) {
            System.out.println("成功放置字母牌到 (" + row + "," + col + ")");
            // 更新UI
            if (boardCellUpdater != null) {
                boardCellUpdater.accept(row, col);
            }
            if (rackUpdater != null) {
                rackUpdater.run();
            }
        } else {
            System.out.println("放置字母牌失败");
        }

        return success;
    }

    /**
     * 在棋盘上移动字母牌
     */
    private boolean moveTileOnBoard(int fromRow, int fromCol, int toRow, int toCol) {
        // 移动字母牌
        boolean success = game.getPlaceTile().movePlacedTile(game.getCurrentPlayer(), fromRow, fromCol, toRow, toCol);

        if (success) {
            System.out.println("成功移动字母牌从 (" + fromRow + "," + fromCol + ") 到 (" + toRow + "," + toCol + ")");
            // 更新UI
            if (boardCellUpdater != null) {
                boardCellUpdater.accept(fromRow, fromCol);
                boardCellUpdater.accept(toRow, toCol);
            }
        } else {
            System.out.println("移动字母牌失败");
        }

        return success;
    }
    
    /**
     * 设置棋盘格子为拖拽目标
     */
    public void setupCellAsDropTarget(StackPane cellPane, int row, int col) {
        // 设置为拖放目标
        setupTarget(cellPane, row, col, null);
        
        // 设置为拖拽源
        setupBoardCellAsSource(cellPane, row, col);
    }

    /**
     * 设置字母架组件为拖拽目标
     * @param rackTile 字母架组件
     * @param rackIndex 字母架索引
     */
    public void setupRackAsDropTarget(StackPane rackTile, int rackIndex) {
        // 接受拖拽进入
        rackTile.setOnDragOver(event -> {
            // 只有从棋盘拖拽的字母牌才能返回字母架
            if (event.getGestureSource() != rackTile && event.getDragboard().hasString() && dragSource.isFromBoard()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        // 拖拽进入时高亮显示
        rackTile.setOnDragEntered(event -> {
            if (event.getGestureSource() != rackTile && event.getDragboard().hasString() && dragSource.isFromBoard()) {
                rackTile.setStyle("-fx-border-color: blue; -fx-border-width: 2;");
            }
            event.consume();
        });

        // 拖拽离开时恢复显示
        rackTile.setOnDragExited(event -> {
            rackTile.setStyle("");
            event.consume();
        });

        // 处理拖放
        rackTile.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            // 只处理从棋盘拖动到字母架的情况
            if (db.hasString() && dragSource.isFromBoard()) {
                // 使用PlaceTile类的returnPlacedTileToRack方法
                success = returnTileFromBoardToRack(dragSource.row, dragSource.col, rackIndex);

                // 如果成功，清空拖拽源信息
                if (success) {
                    dragSource.reset();
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    /**
     * 从棋盘返回字母牌到字母架
     */
    private boolean returnTileFromBoardToRack(int row, int col, int rackIndex) {
        // 使用PlaceTile的returnPlacedTileToRack方法
        boolean success = game.getPlaceTile().returnPlacedTileToRack(game.getCurrentPlayer(), row, col, rackIndex);

        if (success) {
            System.out.println("成功从棋盘 (" + row + "," + col + ") 返回字母牌到字母架位置 " + rackIndex);
            // 更新UI
            if (boardCellUpdater != null) {
                boardCellUpdater.accept(row, col);
            }
            if (rackUpdater != null) {
                rackUpdater.run();
            }
        } else {
            System.out.println("从棋盘返回字母牌到字母架失败");
        }

        return success;
    }
}
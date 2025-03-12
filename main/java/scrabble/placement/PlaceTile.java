package scrabble.placement;

import scrabble.Tile.Tile;
import scrabble.validator.WordValidator;
import scrabble.validator.PositionValidator;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;


/**
 * PlaceTile类处理与棋盘上字母牌放置相关的所有操作。
 * 它管理玩家回合期间字母牌的放置、移动和返回。
 */
public class PlaceTile {
    private static final Logger LOGGER = Logger.getLogger(PlaceTile.class.getName());

    private final BoardOperator boardOperator;
    private final TileRackOperator tileRackOperator;
    private final Map<Object, List<TilePlacement>> currentTurnPlacements;

    /**
     * 内部类，用于跟踪回合中的字母牌放置情况。
     */
    public static class TilePlacement {
        private final Tile tile;
        private final int row;
        private final int col;
        private final int rackIndex;

        public TilePlacement(Tile tile, int row, int col, int rackIndex) {
            this.tile = tile;
            this.row = row;
            this.col = col;
            this.rackIndex = rackIndex;
        }

        public Tile getTile() {
            return tile;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public int getRackIndex() {
            return rackIndex;
        }

        @Override
        public String toString() {
            return String.format("Tile: %c, Position: (%d,%d)", tile.getLetter(), row, col);
        }
    }

    /**
     * 构造函数
     *
     * @param boardOperator    棋盘操作器（默认输入DefaultBoardOperator）
     * @param tileRackOperator 字母架操作器（默认输入DefaultTileRackOperator）
     */
    public PlaceTile(BoardOperator boardOperator, TileRackOperator tileRackOperator) {
        this.boardOperator = boardOperator;
        this.tileRackOperator = tileRackOperator;
        this.currentTurnPlacements = new HashMap<>();

        //初始化验证器（待补充）
    }
    /**
     * 方法1: 将玩家字母架上选中的字母牌放置到棋盘上。
     * @param player 玩家
     * @param row 行
     * @param col 列
     * @return 成功放置则返回ture ，失败则返回false
     */
    public boolean placeSelectedTileOnBoard(Object player, int row, int col) {
        List<Tile> selectedTiles = tileRackOperator.getSelectedTiles(player);
        if (selectedTiles.isEmpty()) {
            LOGGER.log(Level.INFO, "No tiles selected by player: {0}", player);
            return false;
        }

        // 使用第一个选中的字母牌
        Tile tile = selectedTiles.get(0);

        // 检查目标单元格是否为空
        if (boardOperator.isCellOccupied(row, col)) {
            LOGGER.log(Level.INFO, "Target cell ({0},{1}) is already occupied", new Object[]{row, col});
            return false;
        }

        // 找到字母牌在字母架中的索引，以备后续可能的返回
        int rackIndex = tileRackOperator.findTileIndex(player, tile);

        // 从字母架中移除字母牌并放置到棋盘上
        Tile removedTile = tileRackOperator.removeTileFromRack(player, tile);
        if (removedTile != null && boardOperator.placeTileOnBoard(removedTile, row, col)) {
            // 跟踪当前回合的这次放置
            if (!currentTurnPlacements.containsKey(player)) {
                currentTurnPlacements.put(player, new ArrayList<>());
            }
            currentTurnPlacements.get(player).add(new TilePlacement(removedTile, row, col, rackIndex));

            // 记录移动信息
            logMoveInfo(player, "rack", row, col, removedTile);

            // 清除选择
            tileRackOperator.clearSelectedTiles(player);

            return true;
        }

        LOGGER.log(Level.WARNING, "Failed to place tile {0} on board at ({1},{2})",
                new Object[]{tile.getLetter(), row, col});
        return false;
    }

    /**
     * 方法2: 将当前回合已经放置的字母牌移动到新位置。
     * @param player 玩家
     * @param fromRow 当前行
     * @param fromCol 当前列
     * @param toRow 目的行
     * @param toCol 目的列
     * @return 返回是否移动成功
     */
    public boolean movePlacedTile(Object player, int fromRow, int fromCol, int toRow, int toCol) {
        // 验证这个字母牌是当前回合由这个玩家放置的
        if (!currentTurnPlacements.containsKey(player)) {
            return false;
        }

        List<TilePlacement> placements = currentTurnPlacements.get(player);
        TilePlacement targetPlacement = null;
        int placementIndex = -1;

        // 查找放置记录
        for (int i = 0; i < placements.size(); i++) {
            TilePlacement placement = placements.get(i);
            if (placement.getRow() == fromRow && placement.getCol() == fromCol) {
                targetPlacement = placement;
                placementIndex = i;
                break;
            }
        }

        if (targetPlacement == null) {
            LOGGER.log(Level.INFO, "No tile found at ({0},{1}) for the current turn",
                    new Object[]{fromRow, fromCol});
            return false;
        }

        // 检查目标单元格是否为空
        if (boardOperator.isCellOccupied(toRow, toCol)) {
            LOGGER.log(Level.INFO, "Target cell ({0},{1}) is already occupied",
                    new Object[]{toRow, toCol});
            return false;
        }

        // 在棋盘上移动字母牌
        Tile tile = boardOperator.removeTileFromBoard(fromRow, fromCol);
        if (tile == null || !boardOperator.placeTileOnBoard(tile, toRow, toCol)) {
            // 如果移动失败则恢复
            if (tile != null) {
                boardOperator.placeTileOnBoard(tile, fromRow, fromCol);
            }
            LOGGER.log(Level.WARNING, "Failed to move tile from ({0},{1}) to ({2},{3})",
                    new Object[]{fromRow, fromCol, toRow, toCol});
            return false;
        }

        // 更新放置记录
        placements.set(placementIndex, new TilePlacement(
                tile, toRow, toCol, targetPlacement.getRackIndex()));

        // 记录移动信息
        logMoveInfo(player, fromRow, fromCol, toRow, toCol, tile);

        return true;
    }

    /**
     * 方法3: 将已放置的字母牌返回到玩家的字母架。
     */
    public boolean returnPlacedTileToRack(Object player, int row, int col, int rackIndex) {
        // 验证这个字母牌是当前回合由这个玩家放置的
        if (!currentTurnPlacements.containsKey(player)) {
            return false;
        }

        List<TilePlacement> placements = currentTurnPlacements.get(player);
        TilePlacement targetPlacement = null;
        int placementIndex = -1;

        // 查找放置记录
        for (int i = 0; i < placements.size(); i++) {
            TilePlacement placement = placements.get(i);
            if (placement.getRow() == row && placement.getCol() == col) {
                targetPlacement = placement;
                placementIndex = i;
                break;
            }
        }

        if (targetPlacement == null) {
            LOGGER.log(Level.INFO, "No tile found at ({0},{1}) for the current turn",
                    new Object[]{row, col});
            return false;
        }

        // 从棋盘上移除字母牌
        Tile tile = boardOperator.removeTileFromBoard(row, col);
        if (tile == null) {
            LOGGER.log(Level.WARNING, "Failed to remove tile from board at ({0},{1})",
                    new Object[]{row, col});
            return false;
        }

        // 如果没有提供索引，则使用原始的字母架索引
        if (rackIndex < 0 && targetPlacement.getRackIndex() >= 0) {
            rackIndex = targetPlacement.getRackIndex();
        }

        // 将字母牌添加回字母架
        if (tileRackOperator.addTileToRack(player, tile, rackIndex)) {
            // 从放置记录中移除
            placements.remove(placementIndex);

            // 记录移动信息
            logMoveInfo(player, "board to rack", row, col, rackIndex, tile);

            return true;
        } else {
            // 如果返回失败则恢复
            boardOperator.placeTileOnBoard(tile, row, col);
            LOGGER.log(Level.WARNING, "Failed to return tile to rack, placing back on board at ({0},{1})",
                    new Object[]{row, col});
            return false;
        }
    }


    /**
     * 方法4: 记录移动信息的方法
     */
    private void logMoveInfo(Object player, String source, int row, int col, Tile tile) {
        String message = String.format("Player %s moved %c from %s to board position (%d,%d)",
                player, tile.getLetter(), source, row, col);
        System.out.println(message);
        LOGGER.info(message);
    }

    private void logMoveInfo(Object player, int fromRow, int fromCol, int toRow, int toCol, Tile tile) {
        String message = String.format("Player %s moved %c from board position (%d,%d) to (%d,%d)",
                player, tile.getLetter(), fromRow, fromCol, toRow, toCol);
        System.out.println(message);
        LOGGER.info(message);
    }

    private void logMoveInfo(Object player, String source, int row, int col, int rackIndex, Tile tile) {
        String message = String.format("Player %s returned %c from board position (%d,%d) to rack index %d",
                player, tile.getLetter(), row, col, rackIndex);
        System.out.println(message);
        LOGGER.info(message);
    }

    private void logConfirmation(Object player, List<String> words) {
        String message = String.format("Player %s confirmed placement, forming words: %s",
                player, String.join(", ", words));
        System.out.println(message);
        LOGGER.info(message);
    }

    private void logCancellation(Object player) {
        String message = String.format("Player %s canceled all placements for this turn", player);
        System.out.println(message);
        LOGGER.info(message);
    }

    /**
     * 方法5: 确认当前回合的所有放置。
     * 这与单词验证器和位置验证器集成
     */
    public boolean confirmPlacements(Object player, WordValidator wordValidator, PositionValidator positionValidator) {
        if (!currentTurnPlacements.containsKey(player) || currentTurnPlacements.get(player).isEmpty()) {
            LOGGER.log(Level.INFO, "No placements to confirm for player: {0}", player);
            return false;
        }

        List<TilePlacement> placements = currentTurnPlacements.get(player);

        // 1. 验证位置（相连、成一行等）
        if (!positionValidator.validatePositions(placements)) {
            LOGGER.log(Level.INFO, "Invalid tile positions for player: {0}", player);
            return false;
        }

        // 2. 从放置中形成单词并检查字典
        List<String> formedWords = formWords(placements);
        for (String word : formedWords) {
            if (!wordValidator.isValidWord(word)) {
                LOGGER.log(Level.INFO, "Invalid word formed: {0}", word);
                return false;
            }
        }

        // 所有验证通过，提交更改
        currentTurnPlacements.remove(player);

        // 记录确认信息
        logConfirmation(player, formedWords);

        return true;
    }

    /**
     * 取消当前回合的所有放置。
     */
    public boolean cancelPlacements(Object player) {
        if (!currentTurnPlacements.containsKey(player) || currentTurnPlacements.get(player).isEmpty()) {
            return false;
        }

        // 复制列表以避免并发修改问题
        List<TilePlacement> placements = new ArrayList<>(currentTurnPlacements.get(player));
        boolean allReturned = true;

        // 将所有放置的字母牌返回到字母架
        for (TilePlacement placement : placements) {
            boolean returned = returnPlacedTileToRack(player,
                    placement.getRow(),
                    placement.getCol(),
                    placement.getRackIndex());
            if (!returned) {
                allReturned = false;
                LOGGER.log(Level.WARNING, "Failed to return tile at ({0},{1}) to rack",
                        new Object[]{placement.getRow(), placement.getCol()});
            }
        }

        // 不管返回状态如何都清除放置记录
        currentTurnPlacements.remove(player);

        // 记录取消信息
        logCancellation(player);

        return allReturned;
    }

    /**
     * 获取玩家当前的放置记录。
     */
    public List<TilePlacement> getCurrentPlacements(Object player) {
        if (!currentTurnPlacements.containsKey(player)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(currentTurnPlacements.get(player));
    }

    /**
     * 辅助方法，用于识别由放置形成的单词（占位符）。
     * 在实际实现中，这将分析棋盘以找到由放置形成的所有单词。
     */
    private List<String> formWords(List<TilePlacement> placements) {
        // 这是单词形成逻辑的简化占位符
        // 在实际实现中，这将：
        // 1. 检查所有放置是否在一行或一列中
        // 2. 识别形成的主要单词
        // 3. 查找与放置相连的所有垂直单词

        LOGGER.log(Level.INFO, "Word formation logic would analyze {0} placements", placements.size());
        return new ArrayList<>();
    }
}



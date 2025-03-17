package scrabble.placement;

import scrabble.Board.Board;
import scrabble.Tile.Tile;
import scrabble.validator.WordValidator;
import scrabble.validator.PositionValidator;
import scrabble.validator.WordFormer;
import scrabble.score.DefaultScoreCalculator;
import scrabble.logging.GameLogger;
import scrabble.logging.GameLoggerFactory;

import java.util.*;

/**
 * PlaceTile类处理与棋盘上字母牌放置相关的所有操作。
 * 它管理玩家回合期间字母牌的放置、移动和返回。
 */
public class PlaceTile {
    private final GameLogger logger = GameLoggerFactory.getLogger();

    private final BoardOperator boardOperator;
    private final TileRackOperator tileRackOperator;
    private final WordFormer wordFormer;
    private final DefaultScoreCalculator scoreCalculator;
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
     * @param boardOperator    棋盘操作器
     * @param tileRackOperator 字母架操作器
     */
    public PlaceTile(BoardOperator boardOperator, TileRackOperator tileRackOperator) {
        this.boardOperator = boardOperator;
        this.tileRackOperator = tileRackOperator;
        this.currentTurnPlacements = new HashMap<>();

        // 初始化单词形成器和分数计算器
        this.wordFormer = new WordFormer(boardOperator);
        this.scoreCalculator = new DefaultScoreCalculator(boardOperator);
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
            logger.info("No tiles selected by player: {}", player);
            return false;
        }

        // 使用第一个选中的字母牌
        Tile tile = selectedTiles.get(0);

        // 检查目标单元格是否为空
        if (boardOperator.isCellOccupied(row, col)) {
            logger.info("Target cell ({},{}) is already occupied", row, col);
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
            logger.logTilePlacement(player, "rack", row, col, removedTile);

            // 清除选择
            tileRackOperator.clearSelectedTiles(player);

            return true;
        }

        logger.warn("Failed to place tile {} on board at ({},{})", tile.getLetter(), row, col);
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
        TilePlacement targetPlacement = findPlacement(player, fromRow, fromCol);
        if (targetPlacement == null) {
            logger.info("No tile found at ({},{}) for the current turn", fromRow, fromCol);
            return false;
        }

        // 检查目标单元格是否为空
        if (boardOperator.isCellOccupied(toRow, toCol)) {
            logger.info("Target cell ({},{}) is already occupied", toRow, toCol);
            return false;
        }

        // 在棋盘上移动字母牌
        Tile tile = boardOperator.removeTileFromBoard(fromRow, fromCol);
        if (tile == null || !boardOperator.placeTileOnBoard(tile, toRow, toCol)) {
            // 如果移动失败则恢复
            if (tile != null) {
                boardOperator.placeTileOnBoard(tile, fromRow, fromCol);
            }
            logger.warn("Failed to move tile from ({},{}) to ({},{})", fromRow, fromCol, toRow, toCol);
            return false;
        }

        // 查找索引并更新放置记录
        List<TilePlacement> placements = currentTurnPlacements.get(player);
        int placementIndex = -1;
        for (int i = 0; i < placements.size(); i++) {
            if (placements.get(i).getRow() == fromRow && placements.get(i).getCol() == fromCol) {
                placementIndex = i;
                break;
            }
        }

        // 更新放置记录
        placements.set(placementIndex, new TilePlacement(
                tile, toRow, toCol, targetPlacement.getRackIndex()));

        // 记录移动信息
        logger.logTileMovement(player, fromRow, fromCol, toRow, toCol, tile);

        return true;
    }

    /**
     * 方法3: 将已放置的字母牌返回到玩家的字母架。
     * @param player 玩家
     * @param row 字母牌所在的行坐标
     * @param col 字母牌所在的列坐标
     * @param rackIndex 返回到字母架的目标位置索引，如果为负数则使用原始位置
     * @return 操作是否成功
     */
    public boolean returnPlacedTileToRack(Object player, int row, int col, int rackIndex) {
        // 使用辅助方法查找字母牌放置记录
        TilePlacement targetPlacement = findPlacement(player, row, col);
        if (targetPlacement == null) {
            logger.info("No tile found at ({},{}) for the current turn", row, col);
            return false;
        }

        // 从棋盘上移除字母牌
        Tile tile = boardOperator.removeTileFromBoard(row, col);
        if (tile == null) {
            logger.warn("Failed to remove tile from board at ({},{})", row, col);
            return false;
        }

        // 如果没有提供有效索引，则使用原始的字母架索引
        if (rackIndex < 0 && targetPlacement.getRackIndex() >= 0) {
            rackIndex = targetPlacement.getRackIndex();
        }

        // 将字母牌添加回字母架
        if (tileRackOperator.addTileToRack(player, tile, rackIndex)) {
            // 从当前放置记录中移除该条记录
            List<TilePlacement> placements = currentTurnPlacements.get(player);

            // 遍历查找并移除放置记录
            for (int i = 0; i < placements.size(); i++) {
                TilePlacement placement = placements.get(i);
                if (placement.getRow() == row && placement.getCol() == col) {
                    placements.remove(i);
                    break;
                }
            }

            // 如果移除后列表为空，可以考虑移除该玩家的记录
            if (placements.isEmpty()) {
                currentTurnPlacements.remove(player);
            }

            // 记录移动信息
            logger.logTileReturn(player, row, col, rackIndex, tile);

            return true;
        } else {
            // 如果返回失败则恢复棋盘状态
            boardOperator.placeTileOnBoard(tile, row, col);
            logger.warn("Failed to return tile to rack, placing back on board at ({},{})", row, col);
            return false;
        }
    }

    /**
     * 方法4: 确认当前回合的所有放置。
     * 验证玩家当前回合的所有字母牌放置是否符合Scrabble规则，并计算得分。
     * 如果任何验证步骤失败，将取消所有放置并返回0分。
     *
     * @param player 执行放置的玩家
     * @param wordValidator 单词验证器，用于检查单词是否在词典中
     * @param positionValidator 位置验证器，用于检查放置位置是否符合规则
     * @return 成功时返回计算的分数，验证失败时返回0
     */
    public int confirmPlacements(Object player, WordValidator wordValidator, PositionValidator positionValidator) {
        if (!currentTurnPlacements.containsKey(player) || currentTurnPlacements.get(player).isEmpty()) {
            logger.info("No placements to confirm for player: {}", player);
            return 0; // 没有放置时返回0分
        }

        List<TilePlacement> placements = currentTurnPlacements.get(player);

        // 1. 验证位置（放置在一条线上且与现有字母连接）
        // 位置验证器会检查：
        // - 首次放置是否覆盖中心格
        // - 非首次放置是否与现有字母相连
        if (!positionValidator.validatePositions(placements)) {
            logger.info("Invalid tile positions for player: {}", player);
            cancelPlacements(player);
            return 0;
        }

        // 2. 形成单词并检查是否有有效单词
        // WordFormer会检查：
        // - 是否形成连续完整的单词（无间隙）
        // - 单词是否包含至少一个新放置的字母
        // - 非首次放置时，单词是否与现有字母相连
        // - 识别所有主单词和交叉单词
        List<String> formedWords = wordFormer.formWords(placements);
        if (formedWords.isEmpty()) {
            logger.info("No valid words formed by the placement");
            cancelPlacements(player);
            return 0;
        }

        // 3. 验证所有形成的单词是否在字典中存在
        // 包括主单词和所有因放置而形成的交叉单词
        for (String word : formedWords) {
            if (!wordValidator.isValidWord(word)) {
                logger.info("Invalid word formed: {} - not in dictionary", word);
                cancelPlacements(player);
                return 0;
            }
        }

        // 4. 计算分数（主单词和所有交叉单词）
        // 分数计算考虑：
        // - 字母本身的分值
        // - 特殊格子的加成（双倍分数字母、三倍单词等）
        // - 使用全部7个字母的额外奖励（如果启用）
        int score = scoreCalculator.calculateScore(formedWords, placements);

        currentTurnPlacements.remove(player);

        logger.logPlacementConfirmation(player, formedWords, score);

        return score;
    }

    /**
     * 方法5.取消当前回合的所有放置。
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
                logger.warn("Failed to return tile at ({},{}) to rack",
                        placement.getRow(), placement.getCol());
            }
        }

        // 不管返回状态如何都清除放置记录
        currentTurnPlacements.remove(player);

        // 记录取消信息
        logger.logPlacementCancellation(player);

        return allReturned;
    }

    /**
     * 方法6：获取玩家当前的放置记录。
     */
    public List<TilePlacement> getCurrentPlacements(Object player) {
        if (!currentTurnPlacements.containsKey(player)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(currentTurnPlacements.get(player));
    }


    /**
     * 辅助方法：查找玩家当前回合在指定位置的字母牌放置记录
     * @param player 玩家
     * @param row 行坐标
     * @param col 列坐标
     * @return 找到的字母牌放置记录，如果不存在则返回null
     */
    private TilePlacement findPlacement(Object player, int row, int col) {
        if (!currentTurnPlacements.containsKey(player)) {
            return null;
        }

        List<TilePlacement> placements = currentTurnPlacements.get(player);
        for (TilePlacement placement : placements) {
            if (placement.getRow() == row && placement.getCol() == col) {
                return placement;
            }
        }
        return null;
    }
}
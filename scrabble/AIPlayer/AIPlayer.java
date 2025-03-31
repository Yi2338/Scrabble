package scrabble.AIPlayer;

import scrabble.Board.Board;
import scrabble.Board.Cell;
import scrabble.Game.Player;
import scrabble.Game.Game;
import scrabble.Placement.PlaceTile;
import scrabble.Placement.BoardOperator;
import scrabble.Placement.TileRackOperator;
import scrabble.Tile.Tile;
import scrabble.Tile.TileManager;
import scrabble.Validator.Dictionary;
import scrabble.Validator.PositionValidator;
import scrabble.Validator.WordValidator;
import scrabble.Validator.WordFormer;
import scrabble.Score.ScoreCalculator;
import scrabble.Logging.GameLogger;

import java.util.*;

/**
 * AI玩家类，用于模拟人类玩家进行游戏
 * 能够自动执行拼单词、换牌、设置空白牌和跳过回合等操作
 */
public class AIPlayer {
    private final Player player;
    private final Game game;
    private final Random random;
    private static final double EXCHANGE_PROBABILITY = 0.8; // 80%概率交换手牌
    private final GameLogger logger;

    /**
     * 表示一个可能的单词放置方案
     */
    private static class WordPlacement {
        private final String word;
        private final int row;
        private final int col;
        private final boolean isHorizontal;
        private final int score;
        private final List<PlaceTile.TilePlacement> placements;

        public WordPlacement(String word, int row, int col, boolean isHorizontal, int score, List<PlaceTile.TilePlacement> placements) {
            this.word = word;
            this.row = row;
            this.col = col;
            this.isHorizontal = isHorizontal;
            this.score = score;
            this.placements = placements;
        }

        public String getWord() {
            return word;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public boolean isHorizontal() {
            return isHorizontal;
        }

        public int getScore() {
            return score;
        }

        public List<PlaceTile.TilePlacement> getPlacements() {
            return placements;
        }

        @Override
        public String toString() {
            return String.format("单词：'%s'，位置：(%d,%d)，方向：%s，分数：%d",
                    word, row, col, isHorizontal ? "水平" : "垂直", score);
        }
    }

    /**
     * 创建一个AI玩家实例
     *
     * @param player 关联的玩家对象
     * @param game 游戏实例
     */
    public AIPlayer(Player player, Game game) {
        this.player = player;
        this.game = game;
        this.random = new Random();
        this.logger = game.getLogger();
    }

    /**
     * 执行AI回合
     * 策略：智能分析棋盘和词典，找出最优单词放置。如果不能拼单词，则考虑交换或跳过
     */
    public void playTurn() {
        logger.info("AI玩家 {} 开始执行回合", player);

        // 处理空白牌
        TileManager tileManager = game.getTileManager();
        List<Tile> playerRack = tileManager.getPlayerRackList(player);

        // 尝试智能拼单词并放置
        if (tryPlaceOptimalWord()) {
            logger.info("AI成功放置单词");
            return;
        }

        logger.info("AI无法找到有效单词放置");

        // 如果不能拼单词，根据概率决定交换手牌或跳过回合
        double rand = random.nextDouble();
        if (rand < EXCHANGE_PROBABILITY) {
            logger.info("AI决定交换所有手牌（概率：{}%）", EXCHANGE_PROBABILITY * 100);
            exchangeAllTiles();
        } else {
            logger.info("AI决定跳过回合（概率：{}%）", (1 - EXCHANGE_PROBABILITY) * 100);
            game.passTurn();
        }
    }

    /**
     * 尝试放置最优单词
     *
     * @return 是否成功放置了单词
     */
    private boolean tryPlaceOptimalWord() {
        // 获取游戏所需组件
        BoardOperator boardOperator = game.getBoardOperator();
        Board board = game.getBoard();
        WordValidator wordValidator = game.getWordValidator();
        TileManager tileManager = game.getTileManager();
        PlaceTile placeTile = game.getPlaceTile();
        ScoreCalculator scoreCalculator = null;  // 将通过Game中的confirmPlacement间接使用

        // 获取当前玩家的字母牌架
        List<Tile> playerRack = tileManager.getPlayerRackList(player);
        if (playerRack == null || playerRack.isEmpty()) {
            logger.warn("AI玩家手牌为空");
            return false;
        }

        logger.info("AI玩家手牌: {}", playerRack);

        // 判断是否是首次放置（棋盘为空）
        boolean isFirstPlacement = isBoardEmpty(board);

        // 查找所有可能的单词放置方案
        List<WordPlacement> possiblePlacements = findAllPossiblePlacements(playerRack, board, boardOperator, wordValidator);

        // 如果没有可能的放置方案，返回失败
        if (possiblePlacements.isEmpty()) {
            logger.info("未找到任何可能的单词放置方案");
            return false;
        }

        // 按分数排序，选择最高分的放置方案
        possiblePlacements.sort(Comparator.comparingInt(WordPlacement::getScore).reversed());
        WordPlacement bestPlacement = possiblePlacements.get(0);
        logger.info("找到了 {} 种可能的放置方案，最佳方案是：{}",
                possiblePlacements.size(), bestPlacement);

        // 执行放置
        boolean success = placeTilesForWord(bestPlacement, placeTile, tileManager);
        if (success) {
            logger.info("成功放置单词：{}", bestPlacement);
            return true;
        } else {
            logger.warn("放置单词失败：{}", bestPlacement);
            return false;
        }
    }

    /**
     * 判断棋盘是否为空
     *
     * @param board 游戏棋盘
     * @return 如果棋盘为空返回true
     */
    private boolean isBoardEmpty(Board board) {
        Cell[][] grid = board.getGrid();
        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int col = 0; col < Board.BOARD_SIZE; col++) {
                if (grid[row][col].hasTile()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 查找棋盘上所有可能的锚点位置
     * 锚点是新单词可以连接到现有单词的位置
     *
     * @param board 游戏棋盘
     * @param boardOperator 棋盘操作器
     * @return 锚点位置列表
     */
    private List<int[]> findAnchorPoints(Board board, BoardOperator boardOperator) {
        List<int[]> anchorPoints = new ArrayList<>();
        Cell[][] grid = board.getGrid();

        // 判断是否是首次放置
        boolean isEmpty = isBoardEmpty(board);

        // 如果是首次放置，中心点是唯一的锚点
        if (isEmpty) {
            anchorPoints.add(new int[]{7, 7});
            return anchorPoints;
        }

        // 否则，查找所有字母牌周围的空格子作为锚点
        boolean[][] visited = new boolean[Board.BOARD_SIZE][Board.BOARD_SIZE];

        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int col = 0; col < Board.BOARD_SIZE; col++) {
                if (grid[row][col].hasTile()) {
                    // 检查四个方向的相邻空格子
                    checkAndAddAnchor(row-1, col, boardOperator, visited, anchorPoints);
                    checkAndAddAnchor(row+1, col, boardOperator, visited, anchorPoints);
                    checkAndAddAnchor(row, col-1, boardOperator, visited, anchorPoints);
                    checkAndAddAnchor(row, col+1, boardOperator, visited, anchorPoints);
                }
            }
        }

        return anchorPoints;
    }

    /**
     * 检查并添加可能的锚点
     */
    private void checkAndAddAnchor(int row, int col, BoardOperator boardOperator, boolean[][] visited, List<int[]> anchorPoints) {
        if (row >= 0 && row < Board.BOARD_SIZE && col >= 0 && col < Board.BOARD_SIZE
                && !boardOperator.isCellOccupied(row, col) && !visited[row][col]) {
            anchorPoints.add(new int[]{row, col});
            visited[row][col] = true;
        }
    }

    /**
     * 查找所有可能的单词放置方案
     *
     * @param playerRack 玩家的字母牌架
     * @param board 游戏棋盘
     * @param boardOperator 棋盘操作器
     * @param wordValidator 单词验证器
     * @return 所有可能的单词放置方案列表
     */
    private List<WordPlacement> findAllPossiblePlacements(List<Tile> playerRack, Board board,
                                                          BoardOperator boardOperator, WordValidator wordValidator) {
        List<WordPlacement> possiblePlacements = new ArrayList<>();

        // 查找所有可能的锚点位置
        List<int[]> anchorPoints = findAnchorPoints(board, boardOperator);

        // 将玩家手牌转换为字符映射，便于后续处理
        Map<Character, Integer> availableLetters = new HashMap<>();
        for (Tile tile : playerRack) {
            char letter = Character.toUpperCase(tile.getLetter());
            if (tile.isBlank()) {
                // 处理空白牌
                availableLetters.put('*', availableLetters.getOrDefault('*', 0) + 1);
            } else {
                availableLetters.put(letter, availableLetters.getOrDefault(letter, 0) + 1);
            }
        }

        // 获取字典中的所有单词
        Dictionary dictionary = null;
        try {
            // 尝试获取字典的实例
            if (wordValidator instanceof scrabble.Validator.DefaultWordValidator) {
                java.lang.reflect.Field dictionaryField = scrabble.Validator.DefaultWordValidator.class.getDeclaredField("dictionary");
                dictionaryField.setAccessible(true);
                dictionary = (Dictionary) dictionaryField.get(wordValidator);
            }
        } catch (Exception e) {
            logger.error("获取字典实例失败: {}", e, e.getMessage());
            return possiblePlacements;
        }

        if (dictionary == null) {
            logger.error("无法获取字典实例", null);
            return possiblePlacements;
        }

        Set<String> allDictionaryWords = dictionary.getWords();
        if (allDictionaryWords == null || allDictionaryWords.isEmpty()) {
            logger.error("字典中没有单词", null);
            return possiblePlacements;
        }

        // 对每个锚点，尝试水平和垂直方向的单词放置
        for (int[] anchorPoint : anchorPoints) {
            int anchorRow = anchorPoint[0];
            int anchorCol = anchorPoint[1];

            // 尝试水平方向
            tryDirectionalPlacements(anchorRow, anchorCol, true, board, boardOperator,
                    playerRack, availableLetters, allDictionaryWords,
                    wordValidator, possiblePlacements);

            // 尝试垂直方向
            tryDirectionalPlacements(anchorRow, anchorCol, false, board, boardOperator,
                    playerRack, availableLetters, allDictionaryWords,
                    wordValidator, possiblePlacements);
        }

        return possiblePlacements;
    }

    /**
     * 尝试在指定方向上的单词放置
     */
    private void tryDirectionalPlacements(int anchorRow, int anchorCol, boolean isHorizontal,
                                          Board board, BoardOperator boardOperator,
                                          List<Tile> playerRack, Map<Character, Integer> availableLetters,
                                          Set<String> dictionary, WordValidator wordValidator,
                                          List<WordPlacement> possiblePlacements) {
        // 获取锚点前的已有字母序列（如果有）
        StringBuilder prefix = new StringBuilder();
        int startRow = anchorRow;
        int startCol = anchorCol;

        if (isHorizontal) {
            // 向左查找已有字母
            int col = anchorCol - 1;
            while (col >= 0 && boardOperator.isCellOccupied(anchorRow, col)) {
                prefix.insert(0, boardOperator.getCell(anchorRow, col).getTile().getLetter());
                startCol = col;
                col--;
            }
        } else {
            // 向上查找已有字母
            int row = anchorRow - 1;
            while (row >= 0 && boardOperator.isCellOccupied(row, anchorCol)) {
                prefix.insert(0, boardOperator.getCell(row, anchorCol).getTile().getLetter());
                startRow = row;
                row--;
            }
        }

        // 获取锚点后的已有字母序列（如果有）
        StringBuilder suffix = new StringBuilder();

        if (isHorizontal) {
            // 向右查找已有字母
            int col = anchorCol + 1;
            while (col < Board.BOARD_SIZE && boardOperator.isCellOccupied(anchorRow, col)) {
                suffix.append(boardOperator.getCell(anchorRow, col).getTile().getLetter());
                col++;
            }
        } else {
            // 向下查找已有字母
            int row = anchorRow + 1;
            while (row < Board.BOARD_SIZE && boardOperator.isCellOccupied(row, anchorCol)) {
                suffix.append(boardOperator.getCell(row, anchorCol).getTile().getLetter());
                row++;
            }
        }

        // 如果锚点周围已经有字母，我们需要考虑这些字母形成的单词
        if (prefix.length() > 0 || suffix.length() > 0) {
            // 尝试使用玩家手牌填充锚点
            for (Tile tile : playerRack) {
                char letter = tile.getLetter();
                String potentialWord = prefix.toString() + letter + suffix.toString();

                // 验证单词是否有效
                if (potentialWord.length() >= 2 && wordValidator.isValidWord(potentialWord)) {
                    // 创建放置列表
                    List<PlaceTile.TilePlacement> placements = new ArrayList<>();
                    placements.add(new PlaceTile.TilePlacement(tile, anchorRow, anchorCol, 0));

                    // 验证并评估放置
                    evaluatePlacement(potentialWord, startRow, startCol, isHorizontal,
                            placements, board, boardOperator, wordValidator, possiblePlacements);
                }
            }
        }

        // 尝试使用玩家手牌形成新单词
        for (String word : dictionary) {
            // 跳过太短的单词
            if (word.length() < 2) continue;

            // 检查单词是否可以使用当前手牌和棋盘上的字母形成
            if (canFormWord(word, playerRack, availableLetters, board, boardOperator, anchorRow, anchorCol, isHorizontal)) {
                // 尝试单词的不同放置位置
                for (int i = 0; i < word.length(); i++) {
                    // 计算放置的起始位置
                    int startR = isHorizontal ? anchorRow : anchorRow - i;
                    int startC = isHorizontal ? anchorCol - i : anchorCol;

                    // 检查起始位置是否有效且不会超出棋盘
                    if (startR >= 0 && startR + (isHorizontal ? 0 : word.length() - 1) < Board.BOARD_SIZE &&
                            startC >= 0 && startC + (isHorizontal ? word.length() - 1 : 0) < Board.BOARD_SIZE) {

                        // 创建放置列表
                        List<PlaceTile.TilePlacement> placements = createPlacements(word, startR, startC, isHorizontal,
                                playerRack, boardOperator);

                        // 如果可以创建有效的放置列表
                        if (!placements.isEmpty()) {
                            // 验证并评估放置
                            evaluatePlacement(word, startR, startC, isHorizontal,
                                    placements, board, boardOperator, wordValidator, possiblePlacements);
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查是否可以使用玩家手牌和棋盘上的字母形成指定单词
     */
    private boolean canFormWord(String word, List<Tile> playerRack, Map<Character, Integer> availableLetters,
                                Board board, BoardOperator boardOperator, int anchorRow, int anchorCol, boolean isHorizontal) {
        // 创建可用字母的副本，避免修改原始映射
        Map<Character, Integer> lettersCopy = new HashMap<>(availableLetters);

        // 检查锚点是否会被使用
        boolean usesAnchor = false;

        // 遍历单词的每个字母
        for (int i = 0; i < word.length(); i++) {
            int row = isHorizontal ? anchorRow : anchorRow + i;
            int col = isHorizontal ? anchorCol + i : anchorCol;

            // 检查位置是否在棋盘范围内
            if (row < 0 || row >= Board.BOARD_SIZE || col < 0 || col >= Board.BOARD_SIZE) {
                return false;
            }

            char needed = Character.toUpperCase(word.charAt(i));

            if (boardOperator.isCellOccupied(row, col)) {
                // 如果格子已被占用，检查字母是否匹配
                char existing = Character.toUpperCase(boardOperator.getCell(row, col).getTile().getLetter());
                if (existing != needed) {
                    return false;
                }

                // 标记使用了锚点或其他已有字母
                if (row == anchorRow && col == anchorCol) {
                    usesAnchor = true;
                }
            } else {
                // 如果格子为空，检查玩家是否有所需字母牌
                if (lettersCopy.containsKey(needed) && lettersCopy.get(needed) > 0) {
                    lettersCopy.put(needed, lettersCopy.get(needed) - 1);

                    // 标记使用了锚点
                    if (row == anchorRow && col == anchorCol) {
                        usesAnchor = true;
                    }
                } else if (lettersCopy.containsKey('*') && lettersCopy.get('*') > 0) {
                    // 使用空白牌替代
                    lettersCopy.put('*', lettersCopy.get('*') - 1);

                    // 标记使用了锚点
                    if (row == anchorRow && col == anchorCol) {
                        usesAnchor = true;
                    }
                } else {
                    return false;
                }
            }
        }

        // 如果是首次放置，必须使用中心格
        if (isBoardEmpty(board)) {
            int centerRow = Board.BOARD_SIZE / 2;
            int centerCol = Board.BOARD_SIZE / 2;

            boolean usesCenterCell = false;
            for (int i = 0; i < word.length(); i++) {
                int row = isHorizontal ? anchorRow : anchorRow + i;
                int col = isHorizontal ? anchorCol + i : anchorCol;

                if (row == centerRow && col == centerCol) {
                    usesCenterCell = true;
                    break;
                }
            }

            return usesCenterCell;
        }

        // 非首次放置，必须使用锚点或与现有字母相邻
        return usesAnchor;
    }

    /**
     * 创建单词放置的字母牌放置列表
     */
    private List<PlaceTile.TilePlacement> createPlacements(String word, int startRow, int startCol, boolean isHorizontal,
                                                           List<Tile> playerRack, BoardOperator boardOperator) {
        List<PlaceTile.TilePlacement> placements = new ArrayList<>();
        Map<Character, List<Tile>> tilesByLetter = groupTilesByLetter(playerRack);

        // 遍历单词的每个字母
        for (int i = 0; i < word.length(); i++) {
            int row = isHorizontal ? startRow : startRow + i;
            int col = isHorizontal ? startCol + i : startCol;

            // 如果位置超出棋盘范围，返回空列表
            if (row < 0 || row >= Board.BOARD_SIZE || col < 0 || col >= Board.BOARD_SIZE) {
                return Collections.emptyList();
            }

            // 如果格子已被占用，跳过
            if (boardOperator.isCellOccupied(row, col)) {
                continue;
            }

            // 获取所需字母
            char needed = Character.toUpperCase(word.charAt(i));
            Tile tile = findTileForLetter(needed, tilesByLetter);

            if (tile == null) {
                // 没有找到匹配的字母牌，尝试使用空白牌
                tile = findTileForLetter('*', tilesByLetter);

                if (tile == null) {
                    // 如果没有空白牌，无法完成单词
                    return Collections.emptyList();
                }
            }

            // 添加到放置列表
            placements.add(new PlaceTile.TilePlacement(tile, row, col, 0));
        }

        return placements;
    }

    /**
     * 按字母对字母牌进行分组
     */
    private Map<Character, List<Tile>> groupTilesByLetter(List<Tile> tiles) {
        Map<Character, List<Tile>> tilesByLetter = new HashMap<>();

        for (Tile tile : tiles) {
            char letter = tile.isBlank() ? '*' : Character.toUpperCase(tile.getLetter());
            if (!tilesByLetter.containsKey(letter)) {
                tilesByLetter.put(letter, new ArrayList<>());
            }
            tilesByLetter.get(letter).add(tile);
        }

        return tilesByLetter;
    }

    /**
     * 查找表示特定字母的字母牌
     */
    private Tile findTileForLetter(char letter, Map<Character, List<Tile>> tilesByLetter) {
        if (tilesByLetter.containsKey(letter) && !tilesByLetter.get(letter).isEmpty()) {
            Tile tile = tilesByLetter.get(letter).remove(0);
            return tile;
        }
        return null;
    }

    /**
     * 验证并评估单词放置
     */
    private void evaluatePlacement(String word, int startRow, int startCol, boolean isHorizontal,
                                   List<PlaceTile.TilePlacement> placements, Board board,
                                   BoardOperator boardOperator, WordValidator wordValidator,
                                   List<WordPlacement> possiblePlacements) {
        if (placements.isEmpty()) {
            return;
        }

        // 使用WordFormer验证是否能形成有效单词
        WordFormer wordFormer = new WordFormer(boardOperator);

        // 临时放置字母牌到棋盘上
        Map<Integer, Map<Integer, Tile>> originalTiles = new HashMap<>();
        boolean placementSuccessful = true;

        // 记录原始状态，以便后续恢复
        for (PlaceTile.TilePlacement placement : placements) {
            int row = placement.getRow();
            int col = placement.getCol();

            // 如果位置已有字母牌，跳过
            if (boardOperator.isCellOccupied(row, col)) {
                continue;
            }

            // 记录当前位置的状态
            if (!originalTiles.containsKey(row)) {
                originalTiles.put(row, new HashMap<>());
            }
            originalTiles.get(row).put(col, null);

            // 临时放置字母牌
            if (!boardOperator.placeTileOnBoard(placement.getTile(), row, col)) {
                placementSuccessful = false;
                break;
            }
        }

        if (placementSuccessful) {
            // 使用WordFormer验证单词
            List<String> formedWords = wordFormer.formWords(placements);

            // 验证所有形成的单词是否有效
            boolean allWordsValid = true;
            for (String formedWord : formedWords) {
                if (!wordValidator.isValidWord(formedWord)) {
                    allWordsValid = false;
                    break;
                }
            }

            // 如果所有单词都有效，计算分数并添加到可能的放置列表
            if (allWordsValid && !formedWords.isEmpty()) {
                // 这里不直接计算分数，而是将放置信息记录下来
                // 在实际执行放置时，使用Game.confirmPlacement()计算分数
                // 设置一个估计分数来排序放置方案
                int estimatedScore = estimateScore(word, placements);
                possiblePlacements.add(new WordPlacement(word, startRow, startCol, isHorizontal, estimatedScore, placements));
            }
        }

        // 恢复棋盘状态
        for (Map.Entry<Integer, Map<Integer, Tile>> rowEntry : originalTiles.entrySet()) {
            int row = rowEntry.getKey();
            for (Map.Entry<Integer, Tile> colEntry : rowEntry.getValue().entrySet()) {
                int col = colEntry.getKey();
                // 移除临时放置的字母牌
                boardOperator.removeTileFromBoard(row, col);
            }
        }
    }

    /**
     * 估计单词的分数
     * 简单实现，用于排序可能的单词放置
     */
    private int estimateScore(String word, List<PlaceTile.TilePlacement> placements) {
        int score = 0;

        // 简单地累加每个字母的分值
        for (PlaceTile.TilePlacement placement : placements) {
            score += placement.getTile().getValue();
        }

        // 对于长单词给予额外奖励
        if (word.length() >= 7) {
            score += 50;
        } else if (word.length() >= 5) {
            score += 20;
        }

        return score;
    }

    /**
     * 在棋盘上放置单词
     */
    private boolean placeTilesForWord(WordPlacement placement, PlaceTile placeTile, TileManager tileManager) {
        // 清除当前选中状态
        tileManager.clearSelectedTiles(player);

        // 遍历单词的每个字母
        for (PlaceTile.TilePlacement tilePlacement : placement.getPlacements()) {
            Tile tile = tilePlacement.getTile();
            int row = tilePlacement.getRow();
            int col = tilePlacement.getCol();

            // 如果是空白牌且没有设置字母，设置字母
            if (tile.isBlank()) {
                // 根据单词和位置确定字母
                char letter = getLetter(placement.getWord(), placement.getRow(), placement.getCol(),
                        row, col, placement.isHorizontal());
                if (letter != '\0') {
                    game.assignLetterToBlankTile(tile, letter);
                }
            }

            // 标记字母牌为选中状态
            tileManager.markTileAsSelected(player, tile);

            // 放置字母牌
            if (!placeTile.placeSelectedTileOnBoard(player, row, col)) {
                // 放置失败，取消所有放置
                placeTile.cancelPlacements(player);
                return false;
            }

            // 清除选中状态，为下一个字母牌准备
            tileManager.clearSelectedTiles(player);
        }

        // 确认放置并计算分数
        int score = game.confirmPlacement();
        return score > 0;
    }

    /**
     * 根据单词和位置获取特定位置的字母
     */
    private char getLetter(String word, int startRow, int startCol, int row, int col, boolean isHorizontal) {
        if (isHorizontal) {
            int index = col - startCol;
            if (index >= 0 && index < word.length()) {
                return word.charAt(index);
            }
        } else {
            int index = row - startRow;
            if (index >= 0 && index < word.length()) {
                return word.charAt(index);
            }
        }
        return '\0';
    }

    /**
     * 交换所有字母牌
     */
    private void exchangeAllTiles() {
        TileManager tileManager = game.getTileManager();
        List<Tile> playerRack = tileManager.getPlayerRackList(player);

        // 标记所有字母牌为已选中
        tileManager.clearSelectedTiles(player);
        for (Tile tile : playerRack) {
            tileManager.markTileAsSelected(player, tile);
        }

        // 执行交换
        boolean success = game.exchangeTiles();
        if (success) {
            logger.info("成功交换了 {} 个字母牌", playerRack.size());
        } else {
            logger.warn("字母牌交换失败");
        }
    }

    /**
     * 获取关联的玩家对象
     *
     * @return 玩家对象
     */
    public Player getPlayer() {
        return player;
    }
}
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
import scrabble.Score.DefaultScoreCalculator;
import scrabble.Logging.GameLogger;

import java.util.*;
import java.io.IOException;

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
        private final List<Tile> tilesUsed;
        
        public WordPlacement(String word, int row, int col, boolean isHorizontal, int score, List<Tile> tilesUsed) {
            this.word = word;
            this.row = row;
            this.col = col;
            this.isHorizontal = isHorizontal;
            this.score = score;
            this.tilesUsed = tilesUsed;
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
        
        public List<Tile> getTilesUsed() {
            return tilesUsed;
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
        processBlankTiles(playerRack);
        

        
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
        TileManager tileManager = game.getTileManager();
        BoardOperator boardOperator = game.getBoardOperator();
        Board board = game.getBoard();
        WordValidator wordValidator = game.getWordValidator();
        PlaceTile placeTile = game.getPlaceTile();
        
        // 获取当前玩家的字母牌架
        List<Tile> playerRack = tileManager.getPlayerRackList(player);
        if (playerRack == null || playerRack.isEmpty()) {
            logger.warn("AI玩家手牌为空");
            return false;
        }
        
        // 如果是首次放置，尝试在中心位置放置最优单词
        if (isFirstPlacement(board)) {
            return placeFirstWord(playerRack, wordValidator);
        }
        
        // 寻找所有可能的单词放置方案

        List<WordPlacement> possiblePlacements = findAllPossiblePlacements(playerRack, board, wordValidator);
        
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
        boolean success = placeTilesForWord(bestPlacement);
        if (success) {
            logger.info("成功放置单词：{}", bestPlacement);
        } else {
            logger.warn("放置单词失败：{}", bestPlacement);
        }
        return success;
    }
    
    /**
     * 找出所有可能的单词放置方案
     * 
     * @param playerRack 玩家的字母牌架
     * @param board 游戏棋盘
     * @param wordValidator 单词验证器
     * @return 所有可能的单词放置方案列表
     */
    private List<WordPlacement> findAllPossiblePlacements(List<Tile> playerRack, Board board, WordValidator wordValidator) {
        List<WordPlacement> possiblePlacements = new ArrayList<>();
        Cell[][] grid = board.getGrid();
        
        // 查找棋盘上所有已有字母的位置
        List<int[]> anchorPoints = findAnchorPoints(grid);
        
        // 对每个锚点，尝试水平和垂直方向的单词放置
        for (int[] point : anchorPoints) {
            int row = point[0];
            int col = point[1];
            
            // 尝试水平方向
            List<WordPlacement> horizontal = findPossibleWordsFromAnchor(playerRack, grid, row, col, true, wordValidator);
            if (!horizontal.isEmpty()) {
                possiblePlacements.addAll(horizontal);
            }
            
            // 尝试垂直方向
            List<WordPlacement> vertical = findPossibleWordsFromAnchor(playerRack, grid, row, col, false, wordValidator);
            if (!vertical.isEmpty()) {
                possiblePlacements.addAll(vertical);
            }
        }
        
        return possiblePlacements;
    }
    
    /**
     * 在棋盘上找出所有可能作为锚点的位置（已有字母周围的空位）
     * 
     * @param grid 棋盘格子二维数组
     * @return 锚点位置列表
     */
    private List<int[]> findAnchorPoints(Cell[][] grid) {
        List<int[]> anchorPoints = new ArrayList<>();
        boolean[][] visited = new boolean[Board.BOARD_SIZE][Board.BOARD_SIZE];
        
        // 查找所有已放置字母的格子
        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int col = 0; col < Board.BOARD_SIZE; col++) {
                if (grid[row][col].hasTile()) {
                    // 检查四周的空格子
                    checkAndAddAnchor(grid, visited, anchorPoints, row - 1, col); // 上
                    checkAndAddAnchor(grid, visited, anchorPoints, row + 1, col); // 下
                    checkAndAddAnchor(grid, visited, anchorPoints, row, col - 1); // 左
                    checkAndAddAnchor(grid, visited, anchorPoints, row, col + 1); // 右
                }
            }
        }
        
        return anchorPoints;
    }
    
    /**
     * 检查并添加可能的锚点
     */
    private void checkAndAddAnchor(Cell[][] grid, boolean[][] visited, List<int[]> anchorPoints, int row, int col) {
        if (row >= 0 && row < Board.BOARD_SIZE && col >= 0 && col < Board.BOARD_SIZE 
                && !grid[row][col].hasTile() && !visited[row][col]) {
            anchorPoints.add(new int[]{row, col});
            visited[row][col] = true;
        }
    }
    
    /**
     * 从锚点位置开始寻找可能的单词
     * 
     * @param playerRack 玩家的字母牌架
     * @param grid 棋盘格子二维数组
     * @param row 锚点行
     * @param col 锚点列
     * @param isHorizontal 是否水平方向
     * @param wordValidator 单词验证器
     * @return 可能的单词放置方案列表
     */
    private List<WordPlacement> findPossibleWordsFromAnchor(List<Tile> playerRack, Cell[][] grid, 
                                                          int row, int col, boolean isHorizontal, WordValidator wordValidator) {
        List<WordPlacement> placements = new ArrayList<>();
        
        // 获取可使用的字母集合（包括玩家手牌和已放置的字母）
        Map<Character, Integer> availableLetters = new HashMap<>();
        for (Tile tile : playerRack) {
            char letter = Character.toUpperCase(tile.getLetter());
            availableLetters.put(letter, availableLetters.getOrDefault(letter, 0) + 1);
        }
        
        // 从锚点出发，尝试所有可能的单词
        Set<String> possibleWords = generatePossibleWords(availableLetters);
        
        // 过滤出词典中的有效单词
        List<String> validWords = new ArrayList<>();
        for (String word : possibleWords) {
            if (word.length() >= 2 && wordValidator.isValidWord(word)) {
                validWords.add(word);
            }
        }
        
        // 对每个有效单词，检查是否可以放置在锚点位置
        for (String word : validWords) {
            // 尝试单词的每个字母作为锚点位置的字母
            for (int i = 0; i < word.length(); i++) {
                int startRow = isHorizontal ? row : row - i;
                int startCol = isHorizontal ? col - i : col;
                
                // 检查单词是否可以从这个位置开始放置
                if (canPlaceWordHere(word, startRow, startCol, isHorizontal, grid, playerRack)) {
                    // 计算分数
                    int score = calculateWordScore(word, startRow, startCol, isHorizontal, grid);
                    
                    // 确定使用了哪些字母牌
                    List<Tile> tilesUsed = findTilesForWord(word, startRow, startCol, isHorizontal, grid, playerRack);
                    
                    // 添加到可能的放置方案中
                    placements.add(new WordPlacement(word, startRow, startCol, isHorizontal, score, tilesUsed));
                }
            }
        }
        
        return placements;
    }
    
    /**
     * 生成所有可能的单词组合
     * 
     * @param availableLetters 可用字母及其数量
     * @return 所有可能的单词集合
     */
    private Set<String> generatePossibleWords(Map<Character, Integer> availableLetters) {
        Set<String> result = new HashSet<>();
        generateWordsCombination("", availableLetters, result);
        return result;
    }
    
    /**
     * 递归生成所有可能的字母组合
     */
    private void generateWordsCombination(String prefix, Map<Character, Integer> letters, Set<String> result) {
        result.add(prefix);
        
        for (Map.Entry<Character, Integer> entry : letters.entrySet()) {
            if (entry.getValue() > 0) {
                char c = entry.getKey();
                Map<Character, Integer> newLetters = new HashMap<>(letters);
                newLetters.put(c, newLetters.get(c) - 1);
                generateWordsCombination(prefix + c, newLetters, result);
            }
        }
    }
    
    /**
     * 检查单词是否可以从指定位置开始放置
     */
    private boolean canPlaceWordHere(String word, int startRow, int startCol, boolean isHorizontal, 
                                   Cell[][] grid, List<Tile> playerRack) {
        // 检查位置是否在棋盘范围内
        if (startRow < 0 || startCol < 0) {
            return false;
        }
        
        int endRow = isHorizontal ? startRow : startRow + word.length() - 1;
        int endCol = isHorizontal ? startCol + word.length() - 1 : startCol;
        
        if (endRow >= Board.BOARD_SIZE || endCol >= Board.BOARD_SIZE) {
            return false;
        }
        
        // 检查是否至少使用一个棋盘上已有的字母
        boolean usesExistingTile = false;
        
        // 创建玩家手牌的副本用于检查
        Map<Character, Integer> availableTiles = new HashMap<>();
        for (Tile tile : playerRack) {
            char letter = Character.toUpperCase(tile.getLetter());
            availableTiles.put(letter, availableTiles.getOrDefault(letter, 0) + 1);
        }
        
        // 检查每个位置
        for (int i = 0; i < word.length(); i++) {
            int row = isHorizontal ? startRow : startRow + i;
            int col = isHorizontal ? startCol + i : startCol;
            char letterNeeded = word.charAt(i);
            
            if (grid[row][col].hasTile()) {
                // 位置已有字母，检查是否匹配
                char existingLetter = grid[row][col].getTile().getLetter();
                if (Character.toUpperCase(existingLetter) != Character.toUpperCase(letterNeeded)) {
                    return false;
                }
                usesExistingTile = true;
            } else {
                // 位置是空的，检查是否有可用的字母牌
                char upperLetter = Character.toUpperCase(letterNeeded);
                int available = availableTiles.getOrDefault(upperLetter, 0);
                if (available <= 0) {
                    // 没有足够的字母牌，检查是否有空白牌可用
                    int blanks = availableTiles.getOrDefault('*', 0);
                    if (blanks <= 0) {
                        return false;
                    }
                    // 使用一个空白牌
                    availableTiles.put('*', blanks - 1);
                } else {
                    // 使用一个普通字母牌
                    availableTiles.put(upperLetter, available - 1);
                }
            }
        }
        
        // 至少有一个交叉单词，或者是第一次放置
        return usesExistingTile || isFirstPlacement(grid);
    }
    
    /**
     * 计算单词放置的得分
     */
    private int calculateWordScore(String word, int startRow, int startCol, boolean isHorizontal, Cell[][] grid) {
        // 这里简单实现，实际应考虑加分格、交叉单词等
        int wordMultiplier = 1;
        int totalScore = 0;
        
        for (int i = 0; i < word.length(); i++) {
            int row = isHorizontal ? startRow : startRow + i;
            int col = isHorizontal ? startCol + i : startCol;
            char letter = word.charAt(i);
            
            if (!grid[row][col].hasTile()) {
                // 使用字母的基础分值
                int letterScore = getLetterScore(letter);
                
                // 考虑双倍字母、三倍字母等特殊格
                int letterMultiplier = grid[row][col].getLetterMultiplier();
                wordMultiplier *= grid[row][col].getWordMultiplier();
                
                totalScore += letterScore * letterMultiplier;
            } else {
                // 已有字母的分值直接计入
                totalScore += grid[row][col].getTile().getValue();
            }
        }
        
        // 应用单词乘数
        totalScore *= wordMultiplier;
        
        // 如果使用了所有7个字母牌，奖励50分
        if (word.length() == 7) {
            totalScore += 50;
        }
        
        return totalScore;
    }
    
    /**
     * 获取字母的基础分值
     */
    private int getLetterScore(char letter) {
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
                return 0; // 空白牌
        }
    }
    
    /**
     * 找出用于拼写单词的字母牌
     */
    private List<Tile> findTilesForWord(String word, int startRow, int startCol, boolean isHorizontal, 
                                     Cell[][] grid, List<Tile> playerRack) {
        List<Tile> tilesUsed = new ArrayList<>();
        Map<Character, List<Tile>> tilesByLetter = new HashMap<>();
        
        // 将玩家手牌按字母分类
        for (Tile tile : playerRack) {
            char letter = Character.toUpperCase(tile.getLetter());
            if (!tilesByLetter.containsKey(letter)) {
                tilesByLetter.put(letter, new ArrayList<>());
            }
            tilesByLetter.get(letter).add(tile);
        }
        
        // 找出每个位置需要使用的字母牌
        for (int i = 0; i < word.length(); i++) {
            int row = isHorizontal ? startRow : startRow + i;
            int col = isHorizontal ? startCol + i : startCol;
            char letter = Character.toUpperCase(word.charAt(i));
            
            if (!grid[row][col].hasTile()) {
                // 尝试找一个匹配的字母牌
                if (tilesByLetter.containsKey(letter) && !tilesByLetter.get(letter).isEmpty()) {
                    Tile tile = tilesByLetter.get(letter).remove(0);
                    tilesUsed.add(tile);
                } else {
                    // 使用空白牌
                    if (tilesByLetter.containsKey('*') && !tilesByLetter.get('*').isEmpty()) {
                        Tile blankTile = tilesByLetter.get('*').remove(0);
                        tilesUsed.add(blankTile);
                    }
                }
            }
        }
        
        return tilesUsed;
    }
    
    /**
     * 检查是否是首次在棋盘上放置字母
     */
    private boolean isFirstPlacement(Board board) {
        Cell[][] grid = board.getGrid();
        return isFirstPlacement(grid);
    }
    
    /**
     * 检查是否是首次在棋盘上放置字母
     */
    private boolean isFirstPlacement(Cell[][] grid) {
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
     * 放置首个单词（必须经过中心格）
     */
    private boolean placeFirstWord(List<Tile> playerRack, WordValidator wordValidator) {
        int centerRow = Board.BOARD_SIZE / 2;
        int centerCol = Board.BOARD_SIZE / 2;
        
        // 生成所有可能的单词
        Map<Character, Integer> availableLetters = new HashMap<>();
        for (Tile tile : playerRack) {
            char letter = Character.toUpperCase(tile.getLetter());
            availableLetters.put(letter, availableLetters.getOrDefault(letter, 0) + 1);
        }
        
        Set<String> possibleWords = generatePossibleWords(availableLetters);
        
        // 过滤出词典中的有效单词
        List<String> validWords = new ArrayList<>();
        for (String word : possibleWords) {
            if (word.length() >= 2 && wordValidator.isValidWord(word)) {
                validWords.add(word);
            }
        }
        
        // 按长度和得分排序单词
        validWords.sort((w1, w2) -> {
            // 首先按长度降序
            int lengthCompare = Integer.compare(w2.length(), w1.length());
            if (lengthCompare != 0) {
                return lengthCompare;
            }
            
            // 长度相同时按预估得分降序
            int score1 = estimateWordScore(w1);
            int score2 = estimateWordScore(w2);
            return Integer.compare(score2, score1);
        });
        
        // 尝试放置最优单词
        for (String word : validWords) {
            // 尝试单词的每个位置作为中心格
            for (int i = 0; i < word.length(); i++) {
                int startRow = centerRow;
                int startCol = centerCol - i;
                
                // 检查是否可以水平放置
                if (startCol >= 0 && startCol + word.length() <= Board.BOARD_SIZE) {
                    List<Tile> tilesUsed = findTilesForWord(word, startRow, startCol, true, 
                                                      game.getBoard().getGrid(), playerRack);
                    if (tilesUsed.size() == word.length()) {
                        int score = calculateWordScore(word, startRow, startCol, true, game.getBoard().getGrid());
                        WordPlacement placement = new WordPlacement(word, startRow, startCol, true, score, tilesUsed);
                        if (placeTilesForWord(placement)) {
                            return true;
                        }
                    }
                }
                
                // 尝试垂直放置
                startRow = centerRow - i;
                startCol = centerCol;
                if (startRow >= 0 && startRow + word.length() <= Board.BOARD_SIZE) {
                    List<Tile> tilesUsed = findTilesForWord(word, startRow, startCol, false, 
                                                      game.getBoard().getGrid(), playerRack);
                    if (tilesUsed.size() == word.length()) {
                        int score = calculateWordScore(word, startRow, startCol, false, game.getBoard().getGrid());
                        WordPlacement placement = new WordPlacement(word, startRow, startCol, false, score, tilesUsed);
                        if (placeTilesForWord(placement)) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * 估计单词的基础得分（不考虑特殊格子）
     */
    private int estimateWordScore(String word) {
        int score = 0;
        for (int i = 0; i < word.length(); i++) {
            score += getLetterScore(word.charAt(i));
        }
        return score;
    }
    
    /**
     * 实际在棋盘上放置单词
     */
    private boolean placeTilesForWord(WordPlacement placement) {
        PlaceTile placeTile = game.getPlaceTile();
        TileManager tileManager = game.getTileManager();
        
        logger.info("开始放置单词：{}", placement);
        
        // 清除当前选中状态
        tileManager.clearSelectedTiles(player);
        
        // 获取当前棋盘状态
        Cell[][] grid = game.getBoard().getGrid();
        String word = placement.getWord();
        int startRow = placement.getRow();
        int startCol = placement.getCol();
        boolean isHorizontal = placement.isHorizontal();
        List<Tile> tilesUsed = placement.getTilesUsed();
        
        int tileIndex = 0;
        
        // 逐个放置字母牌
        for (int i = 0; i < word.length(); i++) {
            int row = isHorizontal ? startRow : startRow + i;
            int col = isHorizontal ? startCol + i : startCol;
            
            // 跳过已有字母的位置
            if (grid[row][col].hasTile()) {
                logger.info("位置({},{})已有字母'{}'，跳过", row, col, grid[row][col].getTile().getLetter());
                continue;
            }
            
            // 获取下一个要使用的字母牌
            if (tileIndex < tilesUsed.size()) {
                Tile tile = tilesUsed.get(tileIndex++);
                
                // 如果是空白牌，需要设置字母
                if (tile.isBlank()) {
                    char letter = word.charAt(i);
                    logger.info("设置空白牌为字母'{}'", letter);
                    game.assignLetterToBlankTile(tile, letter);
                }
                
                // 标记字母牌为选中状态
                tileManager.markTileAsSelected(player, tile);
                
                // 放置字母牌
                logger.info("放置字母'{}' ({})到位置({},{})", 
                          tile.getLetter(), tile.getValue(), row, col);
                if (!placeTile.placeSelectedTileOnBoard(player, row, col)) {
                    // 放置失败，取消所有放置
                    logger.warn("字母'{}'放置失败，取消所有放置", tile.getLetter());
                    placeTile.cancelPlacements(player);
                    return false;
                }
            }
        }
        
        // 确认放置并计算分数
        logger.info("确认单词放置并计算分数");
        int score = game.confirmPlacement();
        if (score > 0) {
            logger.info("单词放置成功，得分：{}", score);
            return true;
        } else {
            logger.warn("单词放置验证失败");
            return false;
        }
    }
    
    /**
     * 处理字母架上的空白牌，为其设置字母
     * 
     * @param playerRack 玩家的字母牌架
     */
    private void processBlankTiles(List<Tile> playerRack) {
        // 空白牌不预先设置字母，而是在放置单词时根据需要设置
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
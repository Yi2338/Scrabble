package scrabble.AIPlayer;

import scrabble.Board.Board;
import scrabble.Board.Cell;
import scrabble.Board.CellType;
import scrabble.Game.Player;
import scrabble.Logging.GameLogger;
import scrabble.Logging.GameLoggerFactory;
import scrabble.Placement.BoardOperator;
import scrabble.Placement.PlaceTile;
import scrabble.Placement.PlaceTile.TilePlacement;
import scrabble.Score.ScoreCalculator;
import scrabble.Tile.Tile;
import scrabble.Tile.TileManager;
import scrabble.Validator.Dictionary;
import scrabble.Validator.DefaultPositionValidator;
import scrabble.Validator.PositionValidator;
import scrabble.Validator.WordFormer;

import java.util.*;

/**
 * DefaultAIPlayer - 提供AI玩家的实现
 * AI使用词典的单词列表，计算出当前局面的最佳移动
 */
public class DefaultAIPlayer implements AIPlayer {
    /** 用于验证单词的词典 */
    private final Dictionary dictionary;
    /** 棋盘操作器 */
    private final BoardOperator boardOperator;
    /** 单词形成器 */
    private final WordFormer wordFormer;
    /** 位置验证器 */
    private final PositionValidator positionValidator;
    /** 分数计算器 */
    private final ScoreCalculator scoreCalculator;
    /** 字母管理器 */
    private final TileManager tileManager;
    /** 随机数生成器（用于随机选择） */
    private final Random random;
    /** 关联的Player对象 */
    private final Player player;
    /** 字母放置工具 */
    private final PlaceTile placeTile;

    /**
     * 创建一个AI玩家
     * @param player 玩家对象
     * @param dictionary 词典
     * @param boardOperator 棋盘操作器
     * @param tileManager 字母管理器
     * @param placeTile 字母放置工具
     * @param scoreCalculator 分数计算器
     */
    public DefaultAIPlayer(Player player, Dictionary dictionary, BoardOperator boardOperator,
                           TileManager tileManager, PlaceTile placeTile,
                           ScoreCalculator scoreCalculator) {
        this.player = player;
        this.dictionary = dictionary;
        this.boardOperator = boardOperator;
        this.tileManager = tileManager;
        this.placeTile = placeTile;
        this.wordFormer = new WordFormer(boardOperator);
        this.positionValidator = new DefaultPositionValidator(boardOperator);
        this.scoreCalculator = scoreCalculator;
        this.random = new Random();
    }

    /**
     * 找出AI的最佳移动
     * @param board 当前游戏棋盘
     * @return 最佳移动的描述字符串
     */
    @Override
    public String findBestMove(Object board) {
        Board gameBoard = (Board) board;

        // 获取AI当前可用的字母牌
        List<Tile> availableTiles = tileManager.getPlayerRackList(player);

        // 如果没有可用字母牌，跳过回合
        if (availableTiles == null || availableTiles.isEmpty()) {
            return "PASS";
        }

        // 生成所有可能的单词
        List<String> possibleWords = generatePossibleWords(availableTiles);

        // 生成所有可能的有效移动
        List<Move> possibleMoves = generatePossibleMoves(gameBoard, availableTiles, possibleWords);

        // 如果没有有效移动，跳过回合
        if (possibleMoves.isEmpty()) {
            return "PASS";
        }

        // 选择最佳移动（得分最高）
        possibleMoves.sort(Comparator.comparingInt(move -> -move.score));
        Move selectedMove = possibleMoves.get(0);

        // 执行选定的移动
        executeMove(selectedMove);

        // 返回所选移动的描述
        return selectedMove.toString();
    }

    /**
     * 评估当前棋盘状态的分数
     * @param board 当前游戏棋盘
     * @return 评估分数，越高表示对AI越有利
     */
    @Override
    public int evaluatePosition(Object board) {
        Board gameBoard = (Board) board;

        // 基础评估：计算特殊格子的可用性
        int value = 0;

        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int col = 0; col < Board.BOARD_SIZE; col++) {
                if (!boardOperator.isCellOccupied(row, col)) {
                    Cell cell = boardOperator.getCell(row, col);

                    // 根据特殊格子类型赋予价值
                    switch (cell.getCellType()) {
                        case TRIPLE_WORD:
                            value += 5;
                            break;
                        case DOUBLE_WORD:
                            value += 3;
                            break;
                        case TRIPLE_LETTER:
                            value += 2;
                            break;
                        case DOUBLE_LETTER:
                            value += 1;
                            break;
                        case CENTER:
                            value += 3; // 中心格也有战略价值
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        // 额外评估策略价值
        value += evaluateStrategicValue(gameBoard);

        return value;
    }

    /**
     * 评估棋盘的战略价值
     * @param board 游戏棋盘
     * @return 战略价值评分
     */
    private int evaluateStrategicValue(Board board) {
        int value = 0;

        // 检查可用高分格子的可达性
        // 优先考虑能使用高分值字母牌的机会
        List<Tile> availableTiles = tileManager.getPlayerRackList(player);
        if (availableTiles != null) {
            // 计算高分字母的数量
            int highValueTiles = 0;
            for (Tile tile : availableTiles) {
                if (tile.getValue() >= 4) {
                    highValueTiles++;
                }
            }

            // 如果有高分字母牌，增加特殊格子的价值
            if (highValueTiles > 0) {
                for (int row = 0; row < Board.BOARD_SIZE; row++) {
                    for (int col = 0; col < Board.BOARD_SIZE; col++) {
                        if (!boardOperator.isCellOccupied(row, col)) {
                            Cell cell = boardOperator.getCell(row, col);
                            if (cell.getCellType() == CellType.DOUBLE_LETTER ||
                                    cell.getCellType() == CellType.TRIPLE_LETTER) {
                                value += highValueTiles * 2;
                            }
                        }
                    }
                }
            }
        }

        return value;
    }

    /**
     * 根据当前字母架和棋盘状态生成单词
     * @param tilesObj 当前可用的字母牌
     * @param board 当前游戏棋盘
     * @return 生成的单词和放置位置
     */
    @Override
    public String generateWord(List<Object> tilesObj, Object board) {
        // 将Object列表转换为Tile列表
        List<Tile> tiles = new ArrayList<>();
        for (Object obj : tilesObj) {
            tiles.add((Tile) obj);
        }

        Board gameBoard = (Board) board;

        // 生成可能的单词
        List<String> possibleWords = generatePossibleWords(tiles);

        // 如果没有可能的单词，返回空字符串
        if (possibleWords.isEmpty()) {
            return "";
        }

        // 选择得分最高的单词
        String selectedWord = findHighestScoringWord(possibleWords);

        return selectedWord;
    }

    /** 游戏日志记录器 */
    private final GameLogger logger = GameLoggerFactory.getLogger();

    /**
     * 执行选定的移动
     * @param move 要执行的移动
     */
    private void executeMove(Move move) {
        List<Tile> availableTiles = tileManager.getPlayerRackList(player);

        // 获取要放置的单词
        String word = move.word;
        int row = move.row;
        int col = move.col;
        boolean horizontal = move.horizontal;

        // 跟踪已放置的字母牌位置
        List<int[]> placedPositions = new ArrayList<>();

        logger.info("AI玩家 " + player.getPlayerIndex() + " 尝试放置单词: " + word +
                " 在位置 (" + row + "," + col + ") " + (horizontal ? "水平" : "垂直"));

        // 逐个字母放置
        for (int i = 0; i < word.length(); i++) {
            int currentRow = horizontal ? row : row + i;
            int currentCol = horizontal ? col + i : col;

            // 跳过已有字母牌的位置
            if (boardOperator.isCellOccupied(currentRow, currentCol)) {
                continue;
            }

            // 从字母架中找到匹配的字母牌
            char targetLetter = word.charAt(i);
            Tile tileToPlace = null;

            for (Tile tile : availableTiles) {
                if (Character.toLowerCase(tile.getLetter()) == Character.toLowerCase(targetLetter)) {
                    tileToPlace = tile;
                    break;
                }
            }

            if (tileToPlace != null) {
                // 选择字母牌
                tileManager.markTileAsSelected(player, tileToPlace);

                // 放置字母牌
                boolean placed = placeTile.placeSelectedTileOnBoard(player, currentRow, currentCol);

                if (placed) {
                    placedPositions.add(new int[]{currentRow, currentCol});

                    // 使用专用日志方法记录放置事件
                    logger.logTilePlacement(player, "rack", currentRow, currentCol, tileToPlace);

                    // 更新可用字母牌列表
                    availableTiles.remove(tileToPlace);
                } else {
                    logger.warn("AI玩家放置字母 " + targetLetter + " 在位置 (" +
                            currentRow + "," + currentCol + ") 失败");
                }
            }
        }
    }

    /**
     * 生成可以用给定字母牌形成的所有可能单词
     * @param tiles 可用的字母牌
     * @return 可能的单词列表
     */
    private List<String> generatePossibleWords(List<Tile> tiles) {
        // 如果没有可用字母牌，返回空列表
        if (tiles == null || tiles.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取字典中的所有单词
        Set<String> allWords = dictionary.getWords();
        List<String> possibleWords = new ArrayList<>();

        // 将字母牌转换为字符数组
        char[] availableLetters = new char[tiles.size()];
        for (int i = 0; i < tiles.size(); i++) {
            availableLetters[i] = tiles.get(i).getLetter();
        }

        // 检查每个词典单词是否可以用当前字母牌组成
        for (String word : allWords) {
            if (canFormWord(word, availableLetters)) {
                possibleWords.add(word);
            }
        }

        return possibleWords;
    }

    /**
     * 检查是否可以用给定字母牌形成特定单词
     * @param word 要检查的单词
     * @param availableLetters 可用的字母
     * @return 如果可以形成返回true，否则返回false
     */
    private boolean canFormWord(String word, char[] availableLetters) {
        // 创建一个可用字母的计数映射表
        Map<Character, Integer> letterCounts = new HashMap<>();
        for (char c : availableLetters) {
            char lowerC = Character.toLowerCase(c);
            letterCounts.put(lowerC, letterCounts.getOrDefault(lowerC, 0) + 1);
        }

        // 检查每个字母是否足够
        for (char c : word.toCharArray()) {
            char lowerC = Character.toLowerCase(c);
            int count = letterCounts.getOrDefault(lowerC, 0);
            if (count <= 0) {
                return false; // 字母不足
            }
            letterCounts.put(lowerC, count - 1);
        }

        return true;
    }

    /**
     * 生成所有可能的移动
     * @param board 游戏棋盘
     * @param availableTiles 可用的字母牌
     * @param possibleWords 可能形成的单词
     * @return 可能的移动列表
     */
    private List<Move> generatePossibleMoves(Board board, List<Tile> availableTiles, List<String> possibleWords) {
        List<Move> moves = new ArrayList<>();

        // 检查是否是游戏首次放置（棋盘为空）
        boolean isFirstPlacement = isBoardEmpty();

        // 对于游戏首次放置，只需考虑经过中心点的放置
        if (isFirstPlacement) {
            // 水平方向放置
            for (String word : possibleWords) {
                for (int offset = 0; offset < word.length(); offset++) {
                    int startCol = 7 - offset;
                    if (startCol >= 0 && startCol + word.length() <= Board.BOARD_SIZE) {
                        Move move = new Move(word, 7, startCol, true);
                        if (validateMove(move, availableTiles)) {
                            move.score = calculateMoveScore(move, availableTiles);
                            moves.add(move);
                        }
                    }
                }
            }

            // 垂直方向放置
            for (String word : possibleWords) {
                for (int offset = 0; offset < word.length(); offset++) {
                    int startRow = 7 - offset;
                    if (startRow >= 0 && startRow + word.length() <= Board.BOARD_SIZE) {
                        Move move = new Move(word, startRow, 7, false);
                        if (validateMove(move, availableTiles)) {
                            move.score = calculateMoveScore(move, availableTiles);
                            moves.add(move);
                        }
                    }
                }
            }

            return moves;
        }

        // 对于非首次放置，需要与棋盘上的字母连接
        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int col = 0; col < Board.BOARD_SIZE; col++) {
                // 如果当前格子已有字母，则尝试在其周围放置
                if (boardOperator.isCellOccupied(row, col)) {
                    // 尝试水平方向放置
                    tryHorizontalPlacements(row, col, possibleWords, availableTiles, moves);

                    // 尝试垂直方向放置
                    tryVerticalPlacements(row, col, possibleWords, availableTiles, moves);
                }
            }
        }

        return moves;
    }

    /**
     * 尝试水平方向的单词放置
     */
    private void tryHorizontalPlacements(int row, int col, List<String> words,
                                         List<Tile> availableTiles, List<Move> moves) {
        // 获取水平方向已有的连续字母
        int leftCol = col;
        while (leftCol > 0 && boardOperator.isCellOccupied(row, leftCol - 1)) {
            leftCol--;
        }

        int rightCol = col;
        while (rightCol < Board.BOARD_SIZE - 1 && boardOperator.isCellOccupied(row, rightCol + 1)) {
            rightCol++;
        }

        // 计算已有字母序列
        StringBuilder existingLetters = new StringBuilder();
        for (int c = leftCol; c <= rightCol; c++) {
            Cell cell = boardOperator.getCell(row, c);
            existingLetters.append(cell.getTile().getLetter());
        }

        String existing = existingLetters.toString().toLowerCase();

        // 尝试每个可能的单词
        for (String word : words) {
            String wordLower = word.toLowerCase();

            // 尝试向左扩展
            if (leftCol > 0) { // 确保左边有空间
                for (int prefixEnd = 1; prefixEnd < wordLower.length(); prefixEnd++) {
                    String prefix = wordLower.substring(0, prefixEnd);
                    if (existing.startsWith(prefix.substring(1))) {
                        int startCol = leftCol - 1;
                        if (startCol >= 0 && startCol + word.length() - prefix.length() + existing.length() <= Board.BOARD_SIZE) {
                            Move move = new Move(word, row, startCol, true);
                            if (validateMove(move, availableTiles)) {
                                move.score = calculateMoveScore(move, availableTiles);
                                moves.add(move);
                            }
                        }
                    }
                }
            }

            // 尝试向右扩展
            if (rightCol < Board.BOARD_SIZE - 1) { // 确保右边有空间
                for (int suffixStart = 0; suffixStart < wordLower.length() - 1; suffixStart++) {
                    String suffix = wordLower.substring(suffixStart);
                    if (existing.endsWith(suffix.substring(0, 1))) {
                        int startCol = leftCol - suffixStart;
                        if (startCol >= 0 && startCol + word.length() <= Board.BOARD_SIZE) {
                            Move move = new Move(word, row, startCol, true);
                            if (validateMove(move, availableTiles)) {
                                move.score = calculateMoveScore(move, availableTiles);
                                moves.add(move);
                            }
                        }
                    }
                }
            }

            // 尝试与现有字母交叉
            for (int c = leftCol; c <= rightCol; c++) {
                char existingChar = boardOperator.getCell(row, c).getTile().getLetter();

                for (int i = 0; i < wordLower.length(); i++) {
                    if (Character.toLowerCase(existingChar) == wordLower.charAt(i)) {
                        int startCol = c - i;
                        if (startCol >= 0 && startCol + word.length() <= Board.BOARD_SIZE) {
                            Move move = new Move(word, row, startCol, true);
                            if (validateMove(move, availableTiles)) {
                                move.score = calculateMoveScore(move, availableTiles);
                                moves.add(move);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 尝试垂直方向的单词放置
     */
    private void tryVerticalPlacements(int row, int col, List<String> words,
                                       List<Tile> availableTiles, List<Move> moves) {
        // 获取垂直方向已有的连续字母
        int topRow = row;
        while (topRow > 0 && boardOperator.isCellOccupied(topRow - 1, col)) {
            topRow--;
        }

        int bottomRow = row;
        while (bottomRow < Board.BOARD_SIZE - 1 && boardOperator.isCellOccupied(bottomRow + 1, col)) {
            bottomRow++;
        }

        // 计算已有字母序列
        StringBuilder existingLetters = new StringBuilder();
        for (int r = topRow; r <= bottomRow; r++) {
            Cell cell = boardOperator.getCell(r, col);
            existingLetters.append(cell.getTile().getLetter());
        }

        String existing = existingLetters.toString().toLowerCase();

        // 尝试每个可能的单词
        for (String word : words) {
            String wordLower = word.toLowerCase();

            // 尝试向上扩展
            if (topRow > 0) { // 确保上面有空间
                for (int prefixEnd = 1; prefixEnd < wordLower.length(); prefixEnd++) {
                    String prefix = wordLower.substring(0, prefixEnd);
                    if (existing.startsWith(prefix.substring(1))) {
                        int startRow = topRow - 1;
                        if (startRow >= 0 && startRow + word.length() - prefix.length() + existing.length() <= Board.BOARD_SIZE) {
                            Move move = new Move(word, startRow, col, false);
                            if (validateMove(move, availableTiles)) {
                                move.score = calculateMoveScore(move, availableTiles);
                                moves.add(move);
                            }
                        }
                    }
                }
            }

            // 尝试向下扩展
            if (bottomRow < Board.BOARD_SIZE - 1) { // 确保下面有空间
                for (int suffixStart = 0; suffixStart < wordLower.length() - 1; suffixStart++) {
                    String suffix = wordLower.substring(suffixStart);
                    if (existing.endsWith(suffix.substring(0, 1))) {
                        int startRow = topRow - suffixStart;
                        if (startRow >= 0 && startRow + word.length() <= Board.BOARD_SIZE) {
                            Move move = new Move(word, startRow, col, false);
                            if (validateMove(move, availableTiles)) {
                                move.score = calculateMoveScore(move, availableTiles);
                                moves.add(move);
                            }
                        }
                    }
                }
            }

            // 尝试与现有字母交叉
            for (int r = topRow; r <= bottomRow; r++) {
                char existingChar = boardOperator.getCell(r, col).getTile().getLetter();

                for (int i = 0; i < wordLower.length(); i++) {
                    if (Character.toLowerCase(existingChar) == wordLower.charAt(i)) {
                        int startRow = r - i;
                        if (startRow >= 0 && startRow + word.length() <= Board.BOARD_SIZE) {
                            Move move = new Move(word, startRow, col, false);
                            if (validateMove(move, availableTiles)) {
                                move.score = calculateMoveScore(move, availableTiles);
                                moves.add(move);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 验证一个移动是否有效
     * @param move 要验证的移动
     * @param availableTiles 可用的字母牌
     * @return 如果移动有效返回true，否则返回false
     */
    private boolean validateMove(Move move, List<Tile> availableTiles) {
        String word = move.word;
        int row = move.row;
        int col = move.col;
        boolean horizontal = move.horizontal;

        // 检查单词是否超出棋盘
        if ((horizontal && col + word.length() > Board.BOARD_SIZE) ||
                (!horizontal && row + word.length() > Board.BOARD_SIZE)) {
            return false;
        }

        // 创建模拟放置
        List<TilePlacement> simulatedPlacements = new ArrayList<>();

        // 跟踪已使用字母
        Map<Character, Integer> usedLetters = new HashMap<>();
        for (Tile tile : availableTiles) {
            char letter = Character.toLowerCase(tile.getLetter());
            usedLetters.put(letter, usedLetters.getOrDefault(letter, 0) + 1);
        }

        // 检查字母是否足够，并创建模拟放置
        for (int i = 0; i < word.length(); i++) {
            int currentRow = horizontal ? row : row + i;
            int currentCol = horizontal ? col + i : col;

            if (boardOperator.isCellOccupied(currentRow, currentCol)) {
                // 验证已有字母是否匹配
                char existingLetter = boardOperator.getCell(currentRow, currentCol).getTile().getLetter();
                if (Character.toLowerCase(existingLetter) != Character.toLowerCase(word.charAt(i))) {
                    return false;
                }
            } else {
                // 检查是否有足够的字母牌
                char neededLetter = Character.toLowerCase(word.charAt(i));
                int available = usedLetters.getOrDefault(neededLetter, 0);

                if (available <= 0) {
                    return false; // 字母不足
                }

                // 减少可用计数
                usedLetters.put(neededLetter, available - 1);

                // 为即将放置的字母牌创建模拟放置
                // 模拟一个Tile对象，实际放置时会使用真实的字母牌
                Tile dummyTile = new Tile(word.charAt(i), 1);
                simulatedPlacements.add(new TilePlacement(dummyTile, currentRow, currentCol, -1));
            }
        }

        // 如果没有新放置的字母牌，则无效
        if (simulatedPlacements.isEmpty()) {
            return false;
        }

        // 使用位置验证器检查放置是否符合规则
        return positionValidator.validatePositions(simulatedPlacements);
    }

    /**
     * 计算移动的得分
     * @param move 要计算的移动
     * @param availableTiles 可用的字母牌
     * @return 估计的得分
     */
    private int calculateMoveScore(Move move, List<Tile> availableTiles) {
        String word = move.word;
        int row = move.row;
        int col = move.col;
        boolean horizontal = move.horizontal;

        // 创建模拟放置列表
        List<TilePlacement> simulatedPlacements = new ArrayList<>();

        // 跟踪已使用字母
        Map<Character, Integer> letterToTile = new HashMap<>();
        for (Tile tile : availableTiles) {
            char letter = Character.toLowerCase(tile.getLetter());
            letterToTile.put(letter, tile.getValue());
        }

        // 创建模拟放置
        for (int i = 0; i < word.length(); i++) {
            int currentRow = horizontal ? row : row + i;
            int currentCol = horizontal ? col + i : col;

            if (!boardOperator.isCellOccupied(currentRow, currentCol)) {
                // 找到一个合适的字母牌进行模拟
                char letterNeeded = Character.toLowerCase(word.charAt(i));
                int value = letterToTile.getOrDefault(letterNeeded, 1);

                Tile dummyTile = new Tile(word.charAt(i), value);
                simulatedPlacements.add(new TilePlacement(dummyTile, currentRow, currentCol, -1));
            }
        }

        // 使用单词形成器识别会形成的所有单词
        List<String> formedWords = wordFormer.formWords(simulatedPlacements);

        // 使用分数计算器估算得分
        return scoreCalculator.calculateScore(formedWords, simulatedPlacements);
    }

    /**
     * 检查棋盘是否为空
     * @return 如果棋盘为空则返回true，否则返回false
     */
    private boolean isBoardEmpty() {
        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int col = 0; col < Board.BOARD_SIZE; col++) {
                if (boardOperator.isCellOccupied(row, col)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 找出得分最高的单词
     * @param words 单词列表
     * @return 得分最高的单词
     */
    private String findHighestScoringWord(List<String> words) {
        String bestWord = words.get(0);
        int highestScore = 0;

        for (String word : words) {
            // 简单估算：基于字母分值和字长
            int score = estimateWordScore(word);
            if (score > highestScore) {
                highestScore = score;
                bestWord = word;
            }
        }

        return bestWord;
    }

    /**
     * 估算单词的基础分值（不考虑放置位置和特殊格子）
     * @param word 要估算的单词
     * @return 估算分值
     */
    private int estimateWordScore(String word) {
        int score = 0;

        for (char c : word.toCharArray()) {
            // 根据Scrabble规则，不同字母有不同的分值
            // 这里使用简化的估算方法
            switch (Character.toLowerCase(c)) {
                case 'a': case 'e': case 'i': case 'o': case 'u': case 'l': case 'n': case 's': case 't': case 'r':
                    score += 1;
                    break;
                case 'd': case 'g':
                    score += 2;
                    break;
                case 'b': case 'c': case 'm': case 'p':
                    score += 3;
                    break;
                case 'f': case 'h': case 'v': case 'w': case 'y':
                    score += 4;
                    break;
                case 'k':
                    score += 5;
                    break;
                case 'j': case 'x':
                    score += 8;
                    break;
                case 'q': case 'z':
                    score += 10;
                    break;
                default:
                    score += 0;
            }
        }

        // 长单词通常能获得更高分数
        score += word.length();

        return score;
    }

    /**
     * Move内部类，表示一个可能的移动
     */
    private static class Move {
        /** 要放置的单词 */
        final String word;
        /** 起始行 */
        final int row;
        /** 起始列 */
        final int col;
        /** 是否水平放置 */
        final boolean horizontal;
        /** 移动的得分 */
        int score;

        /**
         * 创建一个移动
         * @param word 要放置的单词
         * @param row 起始行
         * @param col 起始列
         * @param horizontal 是否水平放置
         */
        public Move(String word, int row, int col, boolean horizontal) {
            this.word = word;
            this.row = row;
            this.col = col;
            this.horizontal = horizontal;
            this.score = 0;
        }

        @Override
        public String toString() {
            return String.format("Place \"%s\" at (%d,%d) %s with score %d",
                    word, row, col,
                    horizontal ? "horizontally" : "vertically",
                    score);
        }
    }
}
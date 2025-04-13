package scrabble.Validator;

import scrabble.Board.Board;
import scrabble.Tile.Tile;
import scrabble.Placement.BoardOperator;
import scrabble.Placement.PlaceTile.TilePlacement;

import java.util.*;

/**
 * WordFormer类负责识别字母牌放置形成的所有有效单词
 * 根据Scrabble规则：
 * 1. 单词必须连续完整（无缺口）
 * 2. 新单词必须包含至少一个新放置的字母
 * 3. 非首次放置时，新单词必须与至少一个已有字母相连
 * 4. 所有交叉形成的单词也必须是有效单词
 */
public class WordFormer {
    private final BoardOperator boardOperator;

    public WordFormer(BoardOperator boardOperator) {
        this.boardOperator = boardOperator;
    }

    /**
     * 识别由放置形成的所有单词，包括主单词和交叉单词
     *
     * @param placements 当前回合的字母牌放置列表
     * @return 形成的所有有效单词的列表
     */
    public List<String> formWords(List<TilePlacement> placements) {
        if (placements == null || placements.isEmpty()) {
                        return Collections.emptyList();
        }

        Set<String> formedWords = new HashSet<>();
        // 确定主方向（水平或垂直）
        boolean isHorizontal = determineMainDirection(placements);

        // 尝试形成主要单词
        String mainWord = formMainWord(placements, isHorizontal);
        if (mainWord != null && mainWord.length() > 1) {
            formedWords.add(mainWord);
        }

        // 检查每个放置位置是否形成交叉单词
        // 交叉单词方向与主单词方向相反
        for (TilePlacement placement : placements) {
            String crossWord = formCrossWord(placement, !isHorizontal);
            if (crossWord != null && crossWord.length() > 1) {
                formedWords.add(crossWord);
            }
        }

        return new ArrayList<>(formedWords);
    }

    /**
     * 根据放置和棋盘状态确定主要放置方向
     * 单个字母放置时，根据周围字母确定方向
     * 多个字母放置时，根据是否在同一行确定方向
     *
     * @param placements 字母牌放置列表
     * @return 如果主方向是水平则返回true，垂直则返回false
     */
    private boolean determineMainDirection(List<TilePlacement> placements) {
        if (placements.size() == 1) {
            // 单个放置时，需要检查周围情况来确定主方向
            TilePlacement placement = placements.get(0);
            int row = placement.getRow();
            int col = placement.getCol();

            // 检查水平方向是否有邻居字母
            boolean hasHorizontalNeighbor =
                    (col > 0 && boardOperator.isCellOccupied(row, col - 1)) ||
                            (col < Board.BOARD_SIZE - 1 && boardOperator.isCellOccupied(row, col + 1));

            // 检查垂直方向是否有邻居字母
            boolean hasVerticalNeighbor =
                    (row > 0 && boardOperator.isCellOccupied(row - 1, col)) ||
                            (row < Board.BOARD_SIZE - 1 && boardOperator.isCellOccupied(row + 1, col));

            // 如果只有垂直邻居，则为垂直方向
            // 如果两个方向都有邻居，或者都没有邻居，默认为水平方向
            if (hasVerticalNeighbor && !hasHorizontalNeighbor) {
                return false;
            }
            return true;
        }

        // 多个放置时，检查是否位于同一行
        int row = placements.get(0).getRow();
        for (int i = 1; i < placements.size(); i++) {
            if (placements.get(i).getRow() != row) {
                return false; // 不在同一行，是垂直放置
            }
        }
        return true; // 在同一行，是水平放置
    }

    /**
     * 形成主要单词（沿着放置方向）
     * 验证单词是连续的，且包含至少一个新放置的字母
     * 如果不是首次放置，还需包含至少一个已有字母
     *
     * @param placements 放置列表
     * @param isHorizontal 是否水平放置
     * @return 形成的有效主单词，如果没有则返回null
     */
    private String formMainWord(List<TilePlacement> placements, boolean isHorizontal) {
        if (placements.isEmpty()) {
            return null;
        }

        // 根据方向对放置进行排序，确保按顺序处理
        List<TilePlacement> sortedPlacements = new ArrayList<>(placements);
        if (isHorizontal) {
            Collections.sort(sortedPlacements, Comparator.comparingInt(TilePlacement::getCol));
        } else {
            Collections.sort(sortedPlacements, Comparator.comparingInt(TilePlacement::getRow));
        }

        // 确定单词的起始和结束位置
        int startRow, startCol, endRow, endCol;

        if (isHorizontal) {
            startRow = endRow = sortedPlacements.get(0).getRow();
            startCol = findWordStart(startRow, sortedPlacements.get(0).getCol(), true);
            endCol = findWordEnd(startRow, sortedPlacements.get(sortedPlacements.size() - 1).getCol(), true);
        } else {
            startCol = endCol = sortedPlacements.get(0).getCol();
            startRow = findWordStart(sortedPlacements.get(0).getRow(), startCol, false);
            endRow = findWordEnd(sortedPlacements.get(sortedPlacements.size() - 1).getRow(), startCol, false);
        }

        // 创建放置映射以便快速查找
        Map<String, TilePlacement> placementMap = createPlacementMap(placements);
        StringBuilder wordBuilder = new StringBuilder();
        boolean containsNewTile = false;       // 是否包含新放置的字母
        boolean containsExistingTile = false;  // 是否包含已有的字母

        // 根据方向遍历并构建单词
        if (isHorizontal) {
            for (int col = startCol; col <= endCol; col++) {
                char letter = getLetterAt(startRow, col, placementMap);
                if (letter == '\0') {
                    // 如果存在缺口，则无法形成有效单词
                    return null;
                }

                wordBuilder.append(letter);

                // 检查是否包含新放置的字母和已有的字母
                String key = startRow + "," + col;
                if (placementMap.containsKey(key)) {
                    containsNewTile = true;
                } else if (boardOperator.isCellOccupied(startRow, col)) {
                    containsExistingTile = true;
                }
            }
        } else {
            for (int row = startRow; row <= endRow; row++) {
                char letter = getLetterAt(row, startCol, placementMap);
                if (letter == '\0') {
                    // 如果存在缺口，则无法形成有效单词
                    return null;
                }

                wordBuilder.append(letter);

                // 检查是否包含新放置的字母和已有的字母
                String key = row + "," + startCol;
                if (placementMap.containsKey(key)) {
                    containsNewTile = true;
                } else if (boardOperator.isCellOccupied(row, startCol)) {
                    containsExistingTile = true;
                }
            }
        }

        // 验证单词有效性条件：
        // 1. 单词必须包含至少一个新放置的字母
        // 2. 如果不是首次放置，必须包含至少一个已有字母
        boolean isFirstPlacement = isBoardEmptyExceptPlacements(placements);
        if (!containsNewTile || (!containsExistingTile && !isFirstPlacement)) {
            // 首次放置特例：如果是首次放置且单词长度>1，则有效
            if (isFirstPlacement && wordBuilder.length() > 1) {
                return wordBuilder.toString();
            }
            return null;
        }

        return wordBuilder.toString();
    }

    /**
     * 形成交叉单词（垂直于主放置方向）
     * 交叉单词必须包含新放置的字母和至少一个已有的字母
     *
     * @param placement 字母牌放置
     * @param isHorizontal 交叉单词是否水平
     * @return 形成的交叉单词，无效则返回null
     */
    private String formCrossWord(TilePlacement placement, boolean isHorizontal) {
        int row = placement.getRow();
        int col = placement.getCol();

        // 确定单词的起始和结束位置
        int startRow, startCol, endRow, endCol;

        if (isHorizontal) {
            startRow = endRow = row;
            startCol = findWordStart(row, col, true);
            endCol = findWordEnd(row, col, true);
        } else {
            startCol = endCol = col;
            startRow = findWordStart(row, col, false);
            endRow = findWordEnd(row, col, false);
        }

        // 形成单词
        StringBuilder wordBuilder = new StringBuilder();
        boolean containsExistingTile = false;

        if (isHorizontal) {
            for (int c = startCol; c <= endCol; c++) {
                char letter;
                if (c == col) {
                    letter = placement.getTile().getLetter();
                } else {
                    Tile tile = boardOperator.getCell(row, c).getTile();
                    if (tile == null) {
                        return null; // 存在缺口，无法形成单词
                    }
                    letter = tile.getLetter();
                    containsExistingTile = true;
                }
                wordBuilder.append(letter);
            }
        } else {
            for (int r = startRow; r <= endRow; r++) {
                char letter;
                if (r == row) {
                    letter = placement.getTile().getLetter();
                } else {
                    Tile tile = boardOperator.getCell(r, col).getTile();
                    if (tile == null) {
                        return null; // 存在缺口，无法形成单词
                    }
                    letter = tile.getLetter();
                    containsExistingTile = true;
                }
                wordBuilder.append(letter);
            }
        }

        // 交叉单词必须包含至少一个已有的字母，且长度大于1
        if (!containsExistingTile || wordBuilder.length() <= 1) {
            return null;
        }

        return wordBuilder.toString();
    }

    /**
     * 获取指定位置的字母，优先使用当前放置的字母牌
     *
     * @param row 行坐标
     * @param col 列坐标
     * @param placementMap 当前回合字母牌放置映射
     * @return 位置上的字母，如果位置为空则返回'\0'
     */
    private char getLetterAt(int row, int col, Map<String, TilePlacement> placementMap) {
        String key = row + "," + col;
        if (placementMap.containsKey(key)) {
            // 优先使用当前回合放置的字母牌
            return placementMap.get(key).getTile().getLetter();
        } else if (boardOperator.isCellOccupied(row, col)) {
            // 然后使用棋盘上现有的字母牌
            return boardOperator.getCell(row, col).getTile().getLetter();
        }
        return '\0'; // 表示该位置没有字母
    }

    /**
     * 将放置列表转换为位置-放置映射，便于快速查找
     *
     * @param placements 字母牌放置列表
     * @return 位置字符串到放置对象的映射
     */
    private Map<String, TilePlacement> createPlacementMap(List<TilePlacement> placements) {
        Map<String, TilePlacement> map = new HashMap<>();
        for (TilePlacement placement : placements) {
            String key = placement.getRow() + "," + placement.getCol();
            map.put(key, placement);
        }
        return map;
    }

    /**
     * 检查棋盘是否为空（除了当前放置）
     * 用于确定是否为首次放置
     *
     * @param placements 当前回合的放置列表
     * @return 如果除了当前放置外棋盘为空则返回true
     */
    private boolean isBoardEmptyExceptPlacements(List<TilePlacement> placements) {
        Map<String, TilePlacement> placementMap = createPlacementMap(placements);

        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int col = 0; col < Board.BOARD_SIZE; col++) {
                String key = row + "," + col;
                if (boardOperator.isCellOccupied(row, col) && !placementMap.containsKey(key)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 查找单词的起始位置（向前搜索）
     */
    private int findWordStart(int row, int col, boolean isHorizontal) {
        int start = isHorizontal ? col : row;
        int prev = start - 1;

        while (prev >= 0) {
            boolean occupied = isHorizontal
                    ? boardOperator.isCellOccupied(row, prev)
                    : boardOperator.isCellOccupied(prev, col);

            if (!occupied) {
                break;
            }
            start = prev;
            prev--;
        }

        return start;
    }

    /**
     * 查找单词的结束位置（向后搜索）
     */
    private int findWordEnd(int row, int col, boolean isHorizontal) {
        int end = isHorizontal ? col : row;
        int next = end + 1;
        int limit = Board.BOARD_SIZE;

        while (next < limit) {
            boolean occupied = isHorizontal
                    ? boardOperator.isCellOccupied(row, next)
                    : boardOperator.isCellOccupied(next, col);

            if (!occupied) {
                break;
            }
            end = next;
            next++;
        }

        return end;
    }
}
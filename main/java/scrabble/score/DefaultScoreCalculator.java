package scrabble.score;

import scrabble.Board.Cell;
import scrabble.Tile.Tile;
import scrabble.placement.BoardOperator;
import scrabble.placement.PlaceTile.TilePlacement;

import java.util.*;

/**
 * DefaultScoreCalculator - 负责计算玩家放置字母牌的得分
 */
public class DefaultScoreCalculator implements ScoreCalculator {
    private final BoardOperator boardOperator;
    private boolean bingoEnabled = true;
    private int bingoBonusValue = 50;

    /**
     * 创建一个新的分数计算器
     * @param boardOperator 棋盘操作器
     */
    public DefaultScoreCalculator(BoardOperator boardOperator) {
        this.boardOperator = boardOperator;
    }

    /**
     * 计算给定单词列表的总分
     * @param words 单词列表
     * @param placements 字母牌放置列表
     * @return 总分
     */
    @Override
    public int calculateScore(List<String> words, List<TilePlacement> placements) {
        if (words == null || words.isEmpty() || placements == null || placements.isEmpty()) {
            return 0;
        }

        int totalScore = 0;
        boolean bingo = isBingo(placements);

        // 计算每个单词的分数
        for (String word : words) {
            totalScore += calculateWordScore(word, placements);
        }

        // 如果玩家使用了全部7个字母，额外奖励分
        if (bingo && bingoEnabled) {
            totalScore += bingoBonusValue;
        }

        return totalScore;
    }

    /**
     * 检查当前回合是否获得七字母奖励
     * @param placements 当前回合的放置列表
     * @return 是否获得奖励
     */
    @Override
    public boolean isBingo(List<TilePlacement> placements) {
        return placements != null && placements.size() == 7;
    }

    /**
     * 设置七字母奖励的分值
     * @param bonus 奖励分值
     */
    @Override
    public void setBingoBonusValue(int bonus) {
        if (bonus >= 0) {
            this.bingoBonusValue = bonus;
        }
    }

    /**
     * 获取当前设置的七字母奖励分值
     * @return 奖励分值
     */
    @Override
    public int getBingoBonusValue() {
        return this.bingoBonusValue;
    }

    /**
     * 设置是否启用七字母奖励
     * @param enabled 是否启用
     */
    @Override
    public void setBingoEnabled(boolean enabled) {
        this.bingoEnabled = enabled;
    }

    /**
     * 检查七字母奖励是否启用
     * @return 是否启用
     */
    @Override
    public boolean isBingoEnabled() {
        return this.bingoEnabled;
    }

    /**
     * 计算单个单词的分数
     * @param word 单词
     * @param placements 字母牌放置列表
     * @return 单词分数
     */
    private int calculateWordScore(String word, List<TilePlacement> placements) {
        if (word == null || word.isEmpty()) {
            return 0;
        }

        // 使用Map来跟踪所有放置的字母牌
        Map<String, TilePlacement> placementMap = new HashMap<>();
        for (TilePlacement placement : placements) {
            String key = placement.getRow() + "," + placement.getCol();
            placementMap.put(key, placement);
        }

        int wordScore = 0;
        int wordMultiplier = 1;

        // 根据单词的第一个字母确定方向
        boolean isHorizontal = determineDirection(word, placements);

        // 确定单词的起始位置
        List<int[]> wordPositions = findWordPositions(word, placements, isHorizontal);

        if (wordPositions.isEmpty()) {
            return 0;
        }

        // 计算单词分数
        for (int[] pos : wordPositions) {
            int row = pos[0];
            int col = pos[1];
            String key = row + "," + col;

            // 获取单元格和字母牌
            Cell cell = boardOperator.getCell(row, col);
            Tile tile;

            // 检查是否是新放置的字母牌
            boolean isNewPlacement = placementMap.containsKey(key);

            if (isNewPlacement) {
                // 使用本回合放置的字母牌
                tile = placementMap.get(key).getTile();

                // 获取单元格乘数
                int letterMultiplier = cell.getLetterMultiplier();
                int cellWordMultiplier = cell.getWordMultiplier();

                // 计算字母分数
                int letterScore = tile.getValue() * letterMultiplier;
                wordScore += letterScore;

                // 累积单词乘数（仅对新放置的字母牌有效）
                wordMultiplier *= cellWordMultiplier;
            } else {
                // 使用棋盘上已有的字母牌
                tile = cell.getTile();
                if (tile != null) {
                    wordScore += tile.getValue();
                }
            }
        }

        // 应用单词乘数
        return wordScore * wordMultiplier;
    }

    /**
     * 确定单词的方向
     * @param word 单词
     * @param placements 字母牌放置列表
     * @return 如果是水平放置返回true，否则返回false
     */
    private boolean determineDirection(String word, List<TilePlacement> placements) {
        if (placements.size() == 1) {
            // 单个字母牌放置，需要检查相邻位置确定方向
            TilePlacement placement = placements.get(0);
            int row = placement.getRow();
            int col = placement.getCol();

            // 检查左右是否有字母牌
            boolean hasHorizontalNeighbor =
                    (col > 0 && boardOperator.isCellOccupied(row, col - 1)) ||
                            (col < 14 && boardOperator.isCellOccupied(row, col + 1));

            return hasHorizontalNeighbor;
        } else {
            // 多个字母牌放置，比较它们的位置
            int firstRow = placements.get(0).getRow();
            for (int i = 1; i < placements.size(); i++) {
                if (placements.get(i).getRow() != firstRow) {
                    return false; // 不是同一行，所以是垂直放置
                }
            }
            return true; // 所有字母牌都在同一行，是水平放置
        }
    }

    /**
     * 查找单词在棋盘上的位置
     * @param word 单词
     * @param placements 字母牌放置列表
     * @param isHorizontal 是否水平放置
     * @return 单词每个字母的位置列表
     */
    private List<int[]> findWordPositions(String word, List<TilePlacement> placements, boolean isHorizontal) {
        List<int[]> positions = new ArrayList<>();

        if (placements.isEmpty()) {
            return positions;
        }

        // 根据方向对放置进行排序
        List<TilePlacement> sortedPlacements = new ArrayList<>(placements);
        if (isHorizontal) {
            Collections.sort(sortedPlacements, Comparator.comparingInt(TilePlacement::getCol));
        } else {
            Collections.sort(sortedPlacements, Comparator.comparingInt(TilePlacement::getRow));
        }

        // 确定单词的起始位置
        int startRow, startCol;
        if (isHorizontal) {
            startRow = sortedPlacements.get(0).getRow();
            startCol = findWordStart(startRow, sortedPlacements.get(0).getCol(), true);
        } else {
            startCol = sortedPlacements.get(0).getCol();
            startRow = findWordStart(sortedPlacements.get(0).getRow(), startCol, false);
        }

        // 构建单词位置
        if (isHorizontal) {
            for (int i = 0; i < word.length(); i++) {
                positions.add(new int[]{startRow, startCol + i});
            }
        } else {
            for (int i = 0; i < word.length(); i++) {
                positions.add(new int[]{startRow + i, startCol});
            }
        }

        return positions;
    }

    /**
     * 查找单词的起始位置
     * @param row 参考行
     * @param col 参考列
     * @param isHorizontal 是否水平查找
     * @return 单词的起始位置
     */
    private int findWordStart(int row, int col, boolean isHorizontal) {
        int start = isHorizontal ? col : row;
        int prev = start - 1;
        int min = 0;

        while (prev >= min) {
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
}
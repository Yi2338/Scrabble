package scrabble.validator;

import scrabble.Board.Board;
import scrabble.Tile.Tile;
import scrabble.placement.BoardOperator;
import scrabble.placement.PlaceTile.TilePlacement;

import java.util.*;

/**
 * WordFormer - 用于标识由字母牌放置形成的单词
 */
public class WordFormer {
    private final BoardOperator boardOperator;

    /**
     * 创建一个新的单词形成器
     * @param boardOperator 棋盘操作器
     */
    public WordFormer(BoardOperator boardOperator) {
        this.boardOperator = boardOperator;
    }

    /**
     * 识别由放置形成的所有单词
     * @param placements 当前回合的字母牌放置列表
     * @return 形成的所有单词的列表
     */
    public List<String> formWords(List<TilePlacement> placements) {
        if (placements == null || placements.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> formedWords = new HashSet<>();
        boolean isHorizontal = isHorizontalPlacement(placements);

        // 首先确定主要单词
        String mainWord = formMainWord(placements, isHorizontal);
        if (mainWord != null && mainWord.length() > 1) {
            formedWords.add(mainWord);
        }

        // 然后查找形成的交叉单词
        for (TilePlacement placement : placements) {
            // 交叉单词的方向与主要单词相反
            String crossWord = formCrossWord(placement, !isHorizontal);
            if (crossWord != null && crossWord.length() > 1) {
                formedWords.add(crossWord);
            }
        }

        return new ArrayList<>(formedWords);
    }

    /**
     * 检查所有放置是否水平（同一行）
     * @param placements 放置列表
     * @return 如果所有放置都在同一行返回true
     */
    private boolean isHorizontalPlacement(List<TilePlacement> placements) {
        if (placements.size() == 1) {
            // 单个放置，需要检查相邻字母确定方向
            return hasHorizontalAdjacent(placements.get(0));
        }

        int row = placements.get(0).getRow();
        for (int i = 1; i < placements.size(); i++) {
            if (placements.get(i).getRow() != row) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查指定位置是否有水平相邻的字母牌
     * @param placement 字母牌放置
     * @return 如果有水平相邻的字母牌返回true
     */
    private boolean hasHorizontalAdjacent(TilePlacement placement) {
        int row = placement.getRow();
        int col = placement.getCol();
        return (col > 0 && boardOperator.isCellOccupied(row, col - 1)) ||
                (col < Board.BOARD_SIZE - 1 && boardOperator.isCellOccupied(row, col + 1));
    }

    /**
     * 形成主要单词（沿着放置方向）
     * @param placements 放置列表
     * @param isHorizontal 是否水平放置
     * @return 形成的主要单词
     */
    private String formMainWord(List<TilePlacement> placements, boolean isHorizontal) {
        if (placements.isEmpty()) {
            return null;
        }

        // 排序放置的字母牌
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

        // 形成单词
        StringBuilder wordBuilder = new StringBuilder();
        if (isHorizontal) {
            for (int col = startCol; col <= endCol; col++) {
                Tile tile = boardOperator.getCell(startRow, col).getTile();
                if (tile == null) {
                    // 查找在当前位置放置的字母牌
                    for (TilePlacement placement : placements) {
                        if (placement.getRow() == startRow && placement.getCol() == col) {
                            tile = placement.getTile();
                            break;
                        }
                    }
                }
                if (tile != null) {
                    wordBuilder.append(tile.getLetter());
                }
            }
        } else {
            for (int row = startRow; row <= endRow; row++) {
                Tile tile = boardOperator.getCell(row, startCol).getTile();
                if (tile == null) {
                    // 查找在当前位置放置的字母牌
                    for (TilePlacement placement : placements) {
                        if (placement.getRow() == row && placement.getCol() == startCol) {
                            tile = placement.getTile();
                            break;
                        }
                    }
                }
                if (tile != null) {
                    wordBuilder.append(tile.getLetter());
                }
            }
        }

        return wordBuilder.toString();
    }

    /**
     * 形成交叉单词（垂直于放置方向）
     * @param placement 字母牌放置
     * @param isHorizontal 交叉单词是否水平
     * @return 形成的交叉单词
     */
    private String formCrossWord(TilePlacement placement, boolean isHorizontal) {
        int row = placement.getRow();
        int col = placement.getCol();

        // 如果周围没有字母牌，则不形成交叉单词
        boolean hasAdjacentTile = false;
        if (isHorizontal) {
            hasAdjacentTile = (col > 0 && boardOperator.isCellOccupied(row, col - 1)) ||
                    (col < Board.BOARD_SIZE - 1 && boardOperator.isCellOccupied(row, col + 1));
        } else {
            hasAdjacentTile = (row > 0 && boardOperator.isCellOccupied(row - 1, col)) ||
                    (row < Board.BOARD_SIZE - 1 && boardOperator.isCellOccupied(row + 1, col));
        }

        if (!hasAdjacentTile) {
            return null;
        }

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

        // 形成交叉单词
        StringBuilder wordBuilder = new StringBuilder();
        if (isHorizontal) {
            for (int c = startCol; c <= endCol; c++) {
                Tile tile = boardOperator.getCell(row, c).getTile();
                // 如果当前位置是放置位置，使用放置的字母牌
                if (tile == null && c == col) {
                    tile = placement.getTile();
                }
                if (tile != null) {
                    wordBuilder.append(tile.getLetter());
                }
            }
        } else {
            for (int r = startRow; r <= endRow; r++) {
                Tile tile = boardOperator.getCell(r, col).getTile();
                // 如果当前位置是放置位置，使用放置的字母牌
                if (tile == null && r == row) {
                    tile = placement.getTile();
                }
                if (tile != null) {
                    wordBuilder.append(tile.getLetter());
                }
            }
        }

        return wordBuilder.length() > 1 ? wordBuilder.toString() : null;
    }

    /**
     * 查找单词的起始位置
     * @param row 起始行
     * @param col 起始列
     * @param isHorizontal 是否水平查找
     * @return 单词的起始行或列
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
     * 查找单词的结束位置
     * @param row 起始行
     * @param col 起始列
     * @param isHorizontal 是否水平查找
     * @return 单词的结束行或列
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
package scrabble.validator;

import scrabble.placement.PlaceTile;
import java.util.List;

/**
 * 位置验证接口 - 用于验证字母牌的放置位置是否符合游戏规则
 */
public interface PositionValidator {
    /**
     * 根据游戏规则验证字母牌位置
     *
     * @param placements 当前回合的字母牌放置列表
     * @return 如果放置位置有效则返回true，否则返回false
     */
    boolean validatePositions(List<PlaceTile.TilePlacement> placements);
}
package scrabble.placement;

import scrabble.Tile.Tile;
import java.util.List;

/**
 * TileRackOperator接口 - 抽象字母架操作以减少耦合
 */
public interface TileRackOperator {
    /**
     * 在指定索引位置将字母牌添加到玩家的字母架
     */
    boolean addTileToRack(Object player, Tile tile, int index);

    /**
     * 从玩家的字母架中移除特定的字母牌
     */
    Tile removeTileFromRack(Object player, Tile tile);

    /**
     * 获取玩家当前选中的所有字母牌
     */
    List<Tile> getSelectedTiles(Object player);

    /**
     * 清除玩家选中的所有字母牌
     */
    void clearSelectedTiles(Object player);

    /**
     * 查找字母牌在玩家字母架中的索引位置
     */
    int findTileIndex(Object player, Tile tile);
}

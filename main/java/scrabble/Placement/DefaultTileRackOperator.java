package scrabble.Placement;

import scrabble.Tile.Tile;
import scrabble.Tile.TileManager;

import java.util.List;

/**
 * DefaultTileRackOperator - 提供TileRackOperator接口的具体实现
 */
public class DefaultTileRackOperator implements TileRackOperator {
    private final TileManager tileManager;

    public DefaultTileRackOperator(TileManager tileManager) {
        this.tileManager = tileManager;
    }

    /**
     * 将指定字母牌放到当前玩家的字母架上
     * @param player 玩家
     * @param tile 字母牌
     * @param index 字母架索引 (负数或超出范围则添加到末尾)
     * @return 成功则返回true，失败则返回false
     */
    @Override
    public boolean addTileToRack(Object player, Tile tile, int index) {
        // 参数验证
        if (tile == null) {
            return false; // 字母牌不能为空
        }

        // 获取玩家字母架
        List<Tile> rack = tileManager.getPlayerRackList(player);
        if (rack == null) {
            return false; // 玩家字母架不存在
        }

        int maxRackSize = tileManager.getMaxRackSize();

        // 判断是否为替换操作
        boolean isReplacement = (index >= 0 && index < rack.size() && rack.get(index) != null);

        // 容量检查：非替换操作且字母架已满时返回失败
        if (!isReplacement && rack.size() >= maxRackSize) {
            return false; // 字母架已满
        }

        // 处理添加到末尾的情况（索引为负或超出范围）
        if (index < 0 || index >= rack.size()) {
            rack.add(tile);
            return true;
        }

        // 处理放置到特定位置的情况
        Tile existingTile = rack.get(index);
        rack.set(index, tile);

        // 处理位置已被占用的情况
        if (existingTile != null) {
            // 字母架已满时需要为被替换的字母牌寻找空位
            if (rack.size() >= maxRackSize) {
                boolean foundEmptySlot = false;

                // 从替换位置之后开始搜索空位
                for (int i = index + 1; i < rack.size(); i++) {
                    if (rack.get(i) == null) {
                        rack.set(i, existingTile);
                        foundEmptySlot = true;
                        break;
                    }
                }

                // 若未找到，从替换位置之前搜索空位
                if (!foundEmptySlot) {
                    for (int i = 0; i < index; i++) {
                        if (rack.get(i) == null) {
                            rack.set(i, existingTile);
                            foundEmptySlot = true;
                            break;
                        }
                    }
                }

                // 无可用空位时返回失败
                if (!foundEmptySlot) {
                    return false; // 无法为被替换的字母牌找到空位
                }
            } else {
                // 字母架未满时，直接将被替换的字母牌添加到末尾
                rack.add(existingTile);
            }
        }

        return true;
    }

    /**
     * 从字母架上获取字母牌
     * @param player 玩家
     * @param tile 字母牌
     * @return 成功则返回字母牌，失败则返回null
     */
    @Override
    public Tile removeTileFromRack(Object player, Tile tile) {
        boolean removed = tileManager.removeTileFromRack(player, tile);
        return removed ? tile : null;
    }

    /**
     * 获取选中的所有字母牌
     * @param player 玩家
     * @return 返回选中的字母牌
     */
    @Override
    public List<Tile> getSelectedTiles(Object player) {
        return tileManager.getSelectedTile(player);
    }

    /**
     * 清除选中的所有字母牌
     * @param player 玩家
     */
    @Override
    public void clearSelectedTiles(Object player) {
        tileManager.clearSelectedTiles(player);
    }

    /**
     * 找到制定字母牌的索引
     * @param player 玩家
     * @param tile 字母牌
     * @return 返回指定字母牌的索引（失败则返回-1）
     */
    @Override
    public int findTileIndex(Object player, Tile tile) {
        List<Tile> rack = tileManager.getPlayerRackList(player);
        if (rack == null) {
            return -1;
        }

        for (int i = 0; i < rack.size(); i++) {
            if (rack.get(i) == tile) {
                return i;
            }
        }
        return -1;
    }
}
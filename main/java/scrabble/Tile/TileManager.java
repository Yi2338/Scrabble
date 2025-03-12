package scrabble.Tile;

import java.util.*;

/**
 * TileManager 类负责管理 Scrabble 游戏中的所有字母牌。
 * 包括字母牌的分布、生成、玩家字母架管理以及字母牌的选择状态管理。
 */
public class TileManager {

    /**
     * TileInfo 静态内部类用于存储字母牌的权重（数量）和分值信息
     */
    public static class TileInfo {
        /** 字母牌在游戏中的数量（权重） */
        private final int weight;
        /** 字母牌的分值 */
        private final int value;

        /**
         * 创建一个字母信息对象
         * @param weight 字母牌的数量
         * @param value 字母牌的分值
         */
        public TileInfo(int weight, int value) {
            this.weight = weight;
            this.value = value;
        }

        /**
         * 获取字母牌的数量
         * @return 字母牌的数量
         */
        public int getWeight() {
            return weight;
        }

        /**
         * 获取字母牌的分值
         * @return 字母牌的分值
         */
        public int getValue() {
            return value;
        }

        /**
         * 创建当前字母信息的副本
         * @return 一个新的具有相同属性的TileInfo对象
         */
        public TileInfo copy() {
            return new TileInfo(this.weight, this.value);
        }
    }

    /** 存储所有字母的分布信息（字母->{数量,分值}） */
    private final Map<Character, TileInfo> tileDistribution;
    /** 存储所有玩家的字母架 */
    public final Map<Object, List<Tile>> playerRacks;
    /** 存储所有玩家当前选中的字母牌 */
    public final Map<Object, List<Tile>> selectedTiles;
    /** 所有字母牌的总数量 */
    private int totalWeight;
    /** 每个玩家字母架的最大容量 */
    private int maxRackSize;
    /** 用于随机生成字母牌的随机数生成器 */
    private final Random random;

    /**
     * 创建一个字母管理器，默认字母架容量为7
     */
    public TileManager() {
        this.tileDistribution = new HashMap<>();
        this.playerRacks = new HashMap<>();
        this.selectedTiles = new HashMap<>();
        this.maxRackSize = 7;
        this.totalWeight = 100;
        this.random = new Random();
        initTileDistribution();
    }

    /**
     * 创建一个指定字母架容量的字母管理器
     * @param maxRackSize 字母架的最大容量
     */
    public TileManager(int maxRackSize) {
        this();
        this.maxRackSize = maxRackSize;
    }

    /**
     * 初始化字母分布表
     * 设置每个字母的数量和分值
     */
    private void initTileDistribution() {
        String[] tileData = {
                "A:9:1", "B:2:3", "C:2:3", "D:4:2", "E:12:1",
                "F:2:4", "G:3:2", "H:2:4", "I:9:1", "J:1:8",
                "K:1:5", "L:4:1", "M:2:3", "N:6:1", "O:8:1",
                "P:2:3", "Q:1:10", "R:6:1", "S:4:1", "T:6:1",
                "U:4:1", "V:2:4", "W:2:4", "X:1:8", "Y:2:4",
                "Z:1:10", "*:2:0"
        };
        for (String data : tileData) {
            String[] parts = data.split(":");
            char letter = parts[0].charAt(0);
            int weight = Integer.parseInt(parts[1]);
            int value = Integer.parseInt(parts[2]);
            tileDistribution.put(letter, new TileInfo(weight, value));
        }
    }

    /**
     * 随机生成一个字母牌
     * @return 生成的字母牌
     * @throws IllegalStateException 如果字母生成算法出错
     */
    private Tile generateTile() {
        int rand = random.nextInt(totalWeight) + 1;
        int weightSum = 0;

        for (Map.Entry<Character, TileInfo> entry : tileDistribution.entrySet()) {
            char letter = entry.getKey();
            TileInfo tileInfo = entry.getValue();
            weightSum += tileInfo.getWeight();

            if (rand <= weightSum) {
                if (letter == '*') {
                    return new Tile();
                } else {
                    return new Tile(letter, tileInfo.getValue());
                }
            }
        }
        throw new IllegalStateException("字母生成算法出错，请检查");
    }

    /**
     * 生成指定数量的字母牌
     * @param number 需要生成的字母牌数量
     * @return 生成的字母牌列表
     */
    private List<Tile> generateTiles(int number) {
        List<Tile> generatedTiles = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            generatedTiles.add(generateTile());
        }
        return generatedTiles;
    }

    /**
     * 验证玩家的注册状态
     * @param player 要验证的玩家
     * @param shouldBeRegistered 期望的注册状态（true表示应该已注册，false表示应该未注册）
     * @throws IllegalArgumentException 如果玩家的实际注册状态与期望不符
     */
    public void validatePlayer(Object player, boolean shouldBeRegistered) {
        boolean isRegistered = playerRacks.containsKey(player);
        if (!shouldBeRegistered && isRegistered) {
            throw new IllegalArgumentException("该用户已注册过");
        } else if (shouldBeRegistered && !isRegistered) {
            throw new IllegalArgumentException("该用户未注册");
        }
    }

    /**
     * 为玩家注册字母架
     * @param player 要注册的玩家
     * @throws IllegalArgumentException 如果玩家已经注册过
     */
    public void registerTileRackForPlayer(Object player) {
        validatePlayer(player, false);
        playerRacks.put(player, new ArrayList<>());
        selectedTiles.put(player, new ArrayList<>());
    }

    /**
     * 为玩家补充字母牌至字母架满
     * @param player 需要补充字母牌的玩家
     * @return 新补充的字母牌列表
     */
    public List<Tile> drawTilesForPlayer(Object player) {
        List<Tile> playerRack = playerRacks.get(player);
        int currentRackSize = playerRack.size();
        int actualCounts = maxRackSize - currentRackSize;

        if (actualCounts <= 0) {
            return new ArrayList<>();
        }

        List<Tile> drawnTiles = generateTiles(actualCounts);
        playerRack.addAll(drawnTiles);
        return drawnTiles;
    }

    /**
     * 从玩家字母架中选择指定字母的字母牌
     * @param player 玩家
     * @param letter 要选择的字母
     * @return 找到的字母牌，如果未找到则返回null
     */
    public Tile selectTileFromRack(Object player, char letter) {
        List<Tile> playerRack = playerRacks.get(player);
        for (Tile tile : playerRack) {
            if (tile.getLetter() == letter) {
                return tile;
            }
        }
        return null;
    }

    /**
     * 从玩家字母架中通过索引选择字母牌
     * @param player 玩家
     * @param index 字母牌的索引（从0开始）
     * @return 选中的字母牌
     * @throws IllegalArgumentException 如果索引无效
     */
    public Tile selectTileFromIndex(Object player, int index) {
        List<Tile> playerRack = playerRacks.get(player);
        if (index < 0 || index >= playerRack.size()) {
            throw new IllegalArgumentException("无效的字母索引");
        }
        return playerRack.get(index);
    }

    /**
     * 交换字母架中两个位置的字母
     * @param player 玩家
     * @param index1 选中的字母牌
     * @param index2 要交换的字母牌
     */
    public void swapTilesInRack(Object player, int index1, int index2) {
        List<Tile> playerRack = playerRacks.get(player);
        // 验证索引有效性
        if (index1 < 0 || index1 >= playerRack.size() ||
                index2 < 0 || index2 >= playerRack.size()) {
            throw new IllegalArgumentException("无效的字母索引");
        }

        // 交换位置
        Tile temp = playerRack.get(index1);
        playerRack.set(index1, playerRack.get(index2));
        playerRack.set(index2, temp);
    }

    /**
     * 标记字母牌为选中状态
     * @param player 玩家
     * @param tile 要标记的字母牌
     */
    public void markTileAsSelected(Object player, Tile tile) {
        List<Tile> selectedTile = selectedTiles.get(player);
        selectedTile.add(tile);
    }

    /**
     * 获取玩家当前选中的唯一字母牌的索引
     * 如果没有选中字母牌或选中了多个字母牌，则返回-1
     *
     * @param player 玩家对象
     * @return 选中字母牌在字母架中的索引，如果没有唯一选中则返回-1
     */
    public int getSelectedTileIndex(Object player) {
        // 获取玩家选中的字母牌列表
        List<Tile> selected = selectedTiles.get(player);

        // 检查是否只有一个字母牌被选中
        if (selected == null || selected.size() != 1) {
            return -1; // 没有选中字母牌或选中了多个字母牌
        }

        // 获取唯一选中的字母牌
        Tile selectedTile = selected.get(0);

        // 获取玩家的字母架
        List<Tile> playerRack = playerRacks.get(player);

        // 查找该字母牌在字母架中的索引
        for (int i = 0; i < playerRack.size(); i++) {
            if (playerRack.get(i) == selectedTile) {
                return i;
            }
        }

        // 如果没有找到（理论上不应该发生）
        return -1;
    }

    /**
     * 取消字母牌的选中状态
     * @param player 玩家
     * @param tile 要取消选中的字母牌
     */
    public void unmarkTileAsSelected(Object player, Tile tile) {
        List<Tile> selectedTile = selectedTiles.get(player);
        selectedTile.remove(tile);
    }

    /**
     * 获取玩家当前选中的所有字母牌
     * @param player 玩家
     * @return 选中的字母牌列表
     */
    public List<Tile> getSelectedTile(Object player) {
        if (!selectedTiles.containsKey(player)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(selectedTiles.get(player));
    }

    /**
     * 清除玩家选中的所有字母牌
     * @param player 玩家
     */
    public void clearSelectedTiles(Object player) {
        if (selectedTiles.containsKey(player)) {
            selectedTiles.get(player).clear();
        }
    }

    /**
     * 从玩家字母架中移除指定的字母牌
     * @param player 玩家
     * @param tile 要移除的字母牌
     * @return 如果成功移除返回true，否则返回false
     */
    public boolean removeTileFromRack(Object player, Tile tile) {
        if (playerRacks.get(player).remove(tile)) {
            if (selectedTiles.containsKey(player)) {
                selectedTiles.get(player).remove(tile);
            }
            return true;
        }
        return false;
    }

    /**
     * 从玩家字母架中移除所有选中的字母牌
     * @param player 玩家
     * @return 如果成功移除任何字母牌返回true，否则返回false
     */
    public boolean removeSelectedTiles(Object player) {
        if (selectedTiles.containsKey(player) && !selectedTiles.get(player).isEmpty()) {
            List<Tile> theTiles = selectedTiles.get(player);
            playerRacks.get(player).removeAll(theTiles);
            selectedTiles.get(player).clear();
            return true;
        }
        return false;
    }

    /**
     * 访问器1：获取每个玩家字母架的最大容量
     * @return 字母架的最大容量
     */
    public int getMaxRackSize() {
        return maxRackSize;
    }
}
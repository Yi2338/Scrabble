package scrabble.logging;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Scrabble游戏日志事件类
 * 用于封装游戏中发生的各种事件信息，包括事件类型、时间戳、玩家信息和相关数据
 */
public class GameLogEvent {
    /** 事件类型 */
    private final GameEventType eventType;
    /** 事件发生的时间戳 */
    private final Instant timestamp;
    /** 相关玩家对象 */
    private final Object player;
    /** 事件相关的额外数据 */
    private final Map<String, Object> eventData;
    
    /**
     * 创建新的游戏日志事件
     * @param eventType 事件类型
     * @param player 相关玩家对象
     */
    public GameLogEvent(GameEventType eventType, Object player) {
        this.eventType = eventType;
        this.player = player;
        this.timestamp = Instant.now();
        this.eventData = new HashMap<>();
    }
    
    /**
     * 添加事件相关的额外数据
     * @param key 数据的键
     * @param value 数据的值
     * @return 当前GameLogEvent对象（支持链式调用）
     */
    public GameLogEvent addData(String key, Object value) {
        eventData.put(key, value);
        return this;
    }
    
    /**
     * 获取事件类型
     * @return 事件类型枚举值
     */
    public GameEventType getEventType() {
        return eventType;
    }
    
    /**
     * 获取事件发生的时间戳
     * @return 事件时间戳
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    /**
     * 获取相关玩家对象
     * @return 玩家对象
     */
    public Object getPlayer() {
        return player;
    }
    
    /**
     * 获取事件相关的所有额外数据
     * @return 事件数据的副本
     */
    public Map<String, Object> getEventData() {
        return new HashMap<>(eventData);
    }
    
    /**
     * 将事件转换为字符串表示
     * @return 包含事件所有信息的格式化字符串
     */
    @Override
    public String toString() {
        return String.format("[%s] %s - Player: %s - Data: %s",
                timestamp, eventType, player, eventData);
    }
} 
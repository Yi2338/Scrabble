package scrabble.logging;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class GameLogEvent {
    private final GameEventType eventType;
    private final Instant timestamp;
    private final Object player;
    private final Map<String, Object> eventData;
    
    public GameLogEvent(GameEventType eventType, Object player) {
        this.eventType = eventType;
        this.player = player;
        this.timestamp = Instant.now();
        this.eventData = new HashMap<>();
    }
    
    public GameLogEvent addData(String key, Object value) {
        eventData.put(key, value);
        return this;
    }
    
    public GameEventType getEventType() {
        return eventType;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public Object getPlayer() {
        return player;
    }
    
    public Map<String, Object> getEventData() {
        return new HashMap<>(eventData);
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s - Player: %s - Data: %s",
                timestamp, eventType, player, eventData);
    }
} 
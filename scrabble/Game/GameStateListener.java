package scrabble.Game;

/**
 * 游戏状态监听器接口
 */
public interface GameStateListener {
    void onGameStateChanged(Game.GameState newState);

    void onTurnChanged(Player player, int remainingTurnTime);

    void onTurnTimeUpdated(int remainingTurnTime);

    void onGameTimeUpdated(int remainingGameTimeSeconds);

    void onGameOver(Player winner);
}
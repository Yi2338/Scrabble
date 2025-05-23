package scrabble.Game;

import scrabble.Board.Board;
import scrabble.Board.Cell;
import scrabble.Placement.BoardOperator;
import scrabble.Placement.DefaultBoardOperator;
import scrabble.Placement.DefaultTileRackOperator;
import scrabble.Placement.PlaceTile;
import scrabble.Placement.TileRackOperator;
import scrabble.Score.ScoreCalculator;
import scrabble.Tile.Tile;
import scrabble.Tile.TileManager;
import scrabble.Validator.Dictionary;
import scrabble.Validator.PositionValidator;
import scrabble.Validator.WordFormer;
import scrabble.Validator.WordValidator;
import scrabble.Logging.GameLogger;
import scrabble.Logging.GameLoggerFactory;
import scrabble.Validator.DefaultPositionValidator;
import scrabble.Validator.DefaultWordValidator;
import scrabble.AIPlayer.AIPlayerFactory;
import scrabble.AIPlayer.AIPlayer;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Scrabble游戏的主控制类，管理游戏流程、玩家、回合和游戏状态
 */
public class Game implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 游戏棋盘 */
    private final Board board;
    /** 游戏中的玩家列表 */
    private final List<Player> players;
    /** 当前回合的玩家索引 */
    private int currentPlayerIndex;
    /** 当前回合序号 */
    private int turnNumber;
    /** 字母管理器 */
    private final TileManager tileManager;
    /** 棋盘操作器 */
    private transient BoardOperator boardOperator;
    /** 字母架操作器 */
    private transient TileRackOperator tileRackOperator;
    /** 字母放置器 */
    private transient PlaceTile placeTile;
    /** 词典 */
    private transient Dictionary dictionary;
    /** 单词验证器 */
    private transient WordValidator wordValidator;
    /** 位置验证器 */
    private transient PositionValidator positionValidator;
    /** 游戏是否结束 */
    private boolean isGameOver;
    /** 游戏配置 */
    private final GameConfig config;
    /** 游戏开始时间 */
    private final LocalDateTime startTime;
    /** 游戏结束时间 */
    private LocalDateTime endTime;
    /** 游戏唯一ID */
    private final String gameId;
    /** 当前回合 */
    private Turn currentTurn;
    /** 回合历史记录 */
    private final List<Turn> turnHistory;
    /** 游戏日志记录器 */
    private transient GameLogger logger;
    /** 计时器服务 */
    private transient ScheduledExecutorService timerService;
    /** 游戏计时任务 */
    private transient ScheduledFuture<?> gameTimerTask;
    /** 回合计时任务 */
    private transient ScheduledFuture<?> turnTimerTask;
    /** 游戏剩余时间（秒） */
    private int remainingGameTime;
    /** 回合剩余时间（秒） */
    private int remainingTurnTime;
    /** 游戏状态监听器列表 */
    private transient List<GameStateListener> gameStateListeners;
    /** 游戏状态 */
    private GameState gameState;
    /** AI玩家工厂 */
    private transient AIPlayerFactory aiPlayerFactory;
    /** 存储跳过回合后需要保持字母架不变的玩家ID */
    private transient Set<Integer> keepRackPlayers = new HashSet<>();

    /**
     * 游戏状态枚举
     */
    public enum GameState {
        INITIALIZED,  // 游戏已初始化
        RUNNING,      // 游戏运行中
        PAUSED,       // 游戏暂停
        FINISHED      // 游戏结束
    }

    /**
     * 游戏状态监听器接口
     */
    public interface GameStateListener {
        void onGameStateChanged(GameState newState);
        void onTurnChanged(Player player, int remainingTurnTime);
        void onTurnTimeUpdated(int remainingTurnTime);
        void onGameTimeUpdated(int remainingGameTimeSeconds);
        void onGameOver(Player winner);
    }

    /**
     * 创建一个新的Scrabble游戏实例
     * @param players 参与游戏的玩家列表
     * @param config 游戏配置
     * @param dictionaryPath 词典文件路径
     * @throws IOException 如果词典加载失败
     */
    public Game(List<Player> players, GameConfig config, String dictionaryPath) throws IOException {
        this.gameId = UUID.randomUUID().toString();
        this.players = new ArrayList<>(players);
        this.config = config;
        this.startTime = LocalDateTime.now();
        this.turnHistory = new ArrayList<>();
        this.turnNumber = 1;
        this.gameStateListeners = new ArrayList<>();
        this.gameState = GameState.INITIALIZED;

        // 使用游戏ID初始化日志记录器
        this.logger = GameLoggerFactory.getLoggerForGame(this.gameId);

        // 初始化游戏组件
        this.board = new Board();
        this.tileManager = new TileManager();
        this.boardOperator = new DefaultBoardOperator(board);
        this.tileRackOperator = new DefaultTileRackOperator(tileManager);
        this.placeTile = new PlaceTile(boardOperator, tileRackOperator, this.logger);
        this.positionValidator = new DefaultPositionValidator(boardOperator);
        this.currentPlayerIndex = 0;
        this.isGameOver = false;

        // 初始化词典和验证器
        this.dictionary = new Dictionary();
        this.wordValidator = new DefaultWordValidator(dictionary);

        // 初始化计时器
        this.timerService = Executors.newSingleThreadScheduledExecutor();
        this.remainingGameTime = config.getTimeLimit() * 60; // 转换为秒
        this.remainingTurnTime = config.getTurnTimeLimit();

        // 初始化AI玩家工厂
        this.aiPlayerFactory = new AIPlayerFactory(this);
        // 设置默认AI难度
        this.aiPlayerFactory.setDefaultDifficulty(config.getAIDifficulty());

        logger.info("游戏配置已初始化: 目标分数={}, 游戏时间={}分钟({}秒), 回合时间={}秒, AI难度={}",
                config.getTargetScore(), config.getTimeLimit(), remainingGameTime, config.getTurnTimeLimit(), config.getAIDifficulty());
    }

    /**
     * 开始游戏
     */
    public void startGame() {
        if (gameState != GameState.INITIALIZED) {
            logger.warn("无法开始游戏: 游戏已经开始");
            return;
        }

        logger.info("开始游戏，共有 {} 名玩家", players.size());

        // 为每个玩家注册字母架并补充字母牌
        for (Player player : players) {
            tileManager.registerTileRackForPlayer(player);
            tileManager.drawTilesForPlayer(player);
        }

        // 记录游戏开始
        List<Object> playerObjects = new ArrayList<>(players);
        logger.logGameStart(playerObjects);

        // 更新游戏状态
        setGameState(GameState.RUNNING);

        // 启动游戏总时间计时器
        startGameTimer();

        // 开始第一个玩家的回合
        currentPlayerIndex = 0; // 确保从第一个玩家开始
        currentTurn = new Turn(getCurrentPlayer());
        
        // 初始化时不需要重新抽取字母牌，因为已经在上面抽取过了
        // 只记录玩家的字母架
        Player currentPlayer = getCurrentPlayer();
        List<Tile> playerRack = tileManager.getPlayerRackList(currentPlayer);
        logger.logPlayerRack(currentPlayer, playerRack, "TURN_START");
        
        // 重置回合剩余时间
        remainingTurnTime = config.getTurnTimeLimit();
        
        // 通知监听器回合开始
        for (GameStateListener listener : gameStateListeners) {
            listener.onTurnChanged(currentPlayer, remainingTurnTime);
        }
        
        // 启动回合计时器
        startTurnTimer();
    }

    /**
     * 启动游戏总时间计时器
     */
    private void startGameTimer() {
        // 取消之前的游戏计时任务（如果有）
        if (gameTimerTask != null && !gameTimerTask.isDone()) {
            gameTimerTask.cancel(false);
        }

        // 如果游戏时间已经为0或负数，立即触发游戏结束
        if (remainingGameTime <= 0) {
            endGameDueToTimeout();
            return;
        }

        // 每秒更新一次游戏剩余时间
        gameTimerTask = timerService.scheduleAtFixedRate(() -> {
            if (remainingGameTime > 0) {
                remainingGameTime--;

                // 通知监听器时间更新
                for (GameStateListener listener : gameStateListeners) {
                    listener.onGameTimeUpdated(remainingGameTime);
                }

                // 如果游戏时间到，结束游戏
                if (remainingGameTime <= 0) {
                    endGameDueToTimeout();
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * 由于游戏时间到而结束游戏
     */
    private void endGameDueToTimeout() {
        timerService.execute(() -> {
            logger.info("游戏时间已到，游戏结束");
            endGame();
        });
    }

    /**
     * 开始玩家回合
     */
    private void startPlayerTurn() {
        Player currentPlayer = getCurrentPlayer();
        currentTurn = new Turn(currentPlayer);

        // 使用特定的日志方法记录回合开始
        logger.logTurnStart(currentPlayer);
        
        // 检查是否需要保持字母架不变
        boolean keepRack = keepRackPlayers.contains(currentPlayer.getPlayerIndex());
        
        // 如果不需要保持字母架不变，则为玩家补充字母牌
        if (!keepRack) {
            tileManager.drawTilesForPlayer(currentPlayer);
        } else {
            // 已经使用了保持字母架的机会，从集合中移除
            keepRackPlayers.remove(currentPlayer.getPlayerIndex());
            logger.info("玩家 {} 保持字母架不变", currentPlayer);
        }
        
        // 记录玩家的字母架
        List<Tile> playerRack = tileManager.getPlayerRackList(currentPlayer);
        logger.logPlayerRack(currentPlayer, playerRack, "TURN_START");

        // 重置回合剩余时间
        remainingTurnTime = config.getTurnTimeLimit();

        // 通知监听器回合开始
        for (GameStateListener listener : gameStateListeners) {
            listener.onTurnChanged(currentPlayer, remainingTurnTime);
        }

        // 如果当前玩家由AI托管，则自动执行AI回合
        if (aiPlayerFactory.isAIControlled(getCurrentPlayer())) {
            // 使用线程执行AI回合，避免UI卡顿
            new Thread(() -> {
                try {
                    // 为AI回合添加一点延迟，使游戏更自然
                    Thread.sleep(1000);
                    
                    // 获取AI玩家实例
                    AIPlayer aiPlayer = aiPlayerFactory.getAIPlayer(getCurrentPlayer());
                    // 处理可能新抽到的空白牌
                    aiPlayer.processBlankTiles();
                    
                    // 执行AI回合
                    aiPlayerFactory.playAITurnIfControlled(getCurrentPlayer());
                } catch (InterruptedException e) {
                    logger.error("AI回合执行被中断", e);
                }
            }).start();
        }

        // 启动回合计时器
        startTurnTimer();
    }

    /**
     * 启动回合计时器
     */
    private void startTurnTimer() {
        // 取消之前的回合计时任务（如果有）
        if (turnTimerTask != null && !turnTimerTask.isDone()) {
            turnTimerTask.cancel(false);
        }

        // 如果回合时间已经为0或负数，立即触发回合结束
        if (remainingTurnTime <= 0) {
            endPlayerTurnDueToTimeout();
            return;
        }

        // 每秒更新一次剩余时间
        turnTimerTask = timerService.scheduleAtFixedRate(() -> {
            if (remainingTurnTime > 0) {
                remainingTurnTime--;

                // 通知监听器时间更新
                for (GameStateListener listener : gameStateListeners) {
                    listener.onTurnTimeUpdated(remainingTurnTime);
                }
            } else {
                // 时间到，强制结束回合
                endPlayerTurnDueToTimeout();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * 结束当前玩家的回合
     */
    public void endPlayerTurn() {
        if (gameState != GameState.RUNNING) {
            logger.warn("无法结束回合: 游戏未运行");
            return;
        }

        // 取消回合计时任务
        if (turnTimerTask != null && !turnTimerTask.isDone()) {
            turnTimerTask.cancel(false);
        }

        // 确认当前回合
        if (!currentTurn.isConfirmed()) {
            currentTurn.confirm();
        }

        Player currentPlayer = getCurrentPlayer();
        
        // 记录日志
        logger.logTurnEnd(currentPlayer);
        
        // 记录玩家字母架（在回合结束时，补牌后）
        List<Tile> playerRack = tileManager.getPlayerRackList(currentPlayer);
        logger.logPlayerRack(currentPlayer, playerRack, "TURN_END");

        // 添加到历史记录
        turnHistory.add(currentTurn);

        // 检查游戏是否应该结束
        if (checkGameEnd()) {
            endGame();
            return;
        }

        // 切换到下一个玩家
        moveToNextPlayer();

        // 开始新回合
        startPlayerTurn();
    }

    /**
     * 由于超时结束当前玩家的回合
     */
    private void endPlayerTurnDueToTimeout() {
        // 确保在游戏线程中执行
        timerService.execute(() -> {
            Player currentPlayer = getCurrentPlayer();
            logger.info("玩家 {} 回合超时", currentPlayer);

            // 将当前回合标记为pass
            currentTurn.markAsPass();
            currentTurn.confirm();
            
            // 标记该玩家的字母架需要保持不变（不重新抽取字母牌）
            keepRackPlayers.add(currentPlayer.getPlayerIndex());

            // 记录日志
            logger.logTurnEnd(currentPlayer);
            
            // 记录玩家字母架
            List<Tile> playerRack = tileManager.getPlayerRackList(currentPlayer);
            logger.logPlayerRack(currentPlayer, playerRack, "TURN_END");

            // 添加到历史记录
            turnHistory.add(currentTurn);

            // 检查游戏是否应该结束
            if (checkGameEnd()) {
                endGame();
                return;
            }

            // 切换到下一个玩家
            moveToNextPlayer();

            // 开始新回合
            startPlayerTurn();
        });
    }

    /**
     * 移动到下一个玩家
     */
    private void moveToNextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        turnNumber++;
    }

    /**
     * 检查游戏是否应该结束
     * @return 如果游戏应该结束则返回true，否则返回false
     */
    private boolean checkGameEnd() {
        // 检查是否有玩家达到目标分数
        for (Player player : players) {
            if (player.getScore() >= config.getTargetScore()) {
                logger.info("玩家 {} 达到目标分数 {}，游戏结束", player, player.getScore());
                return true;
            }
        }

        // 检查游戏时间是否已到
        if (remainingGameTime <= 0) {
            logger.info("游戏时间已到，游戏结束");
            return true;
        }

        return false;
    }

    /**
     * 结束游戏
     */
    public void endGame() {
        if (gameState == GameState.FINISHED) {
            logger.warn("游戏已经结束");
            return;
        }

        // 取消所有计时任务
        if (turnTimerTask != null) {
            turnTimerTask.cancel(false);
        }
        if (gameTimerTask != null) {
            gameTimerTask.cancel(false);
        }

        // 查找胜利者
        Player winner = determineWinner();

        // 设置游戏结束时间
        endTime = LocalDateTime.now();

        // 记录游戏结束
        logger.logGameEnd(winner, winner.getScore());

        // 更新游戏状态
        setGameState(GameState.FINISHED);

        // 设置游戏结束标志
        isGameOver = true;

        // 关闭计时器服务
        if (timerService != null && !timerService.isShutdown()) {
            timerService.shutdown();
        }

        // 通知监听器游戏结束
        for (GameStateListener listener : gameStateListeners) {
            listener.onGameOver(winner);
        }
        
    }

    /**
     * 确定游戏胜利者
     * @return 分数最高的玩家
     */
    private Player determineWinner() {
        Player winner = players.get(0);
        for (int i = 1; i < players.size(); i++) {
            if (players.get(i).getScore() > winner.getScore()) {
                winner = players.get(i);
            }
        }
        return winner;
    }

    /**
     * 确认放置字母牌并结束当前回合
     * @return 获得的分数
     */
    public int confirmPlacement() {
        if (gameState != GameState.RUNNING) {
            logger.warn("无法确认放置: 游戏未运行");
            return 0;
        }

        Player currentPlayer = getCurrentPlayer();
        
        // 在确认放置前获取当前放置的字母牌
        List<PlaceTile.TilePlacement> placements = placeTile.getCurrentPlacements(currentPlayer);
        
        // 获取WordFormer对象
        List<String> formedWords = getFormWords(placements);
        
        // 调用confirmPlacements验证并计算分数
        int score = placeTile.confirmPlacements(currentPlayer, wordValidator, positionValidator);

        if (score > 0) {
            // 记录玩家放置的字母牌和形成的单词
            for (PlaceTile.TilePlacement placement : placements) {
                currentTurn.recordPlacement(placement);
            }
            currentTurn.recordFormedWords(formedWords);
            
            // 记录得分
            currentTurn.setScore(score);

            // 向控制台输出有效单词
            System.out.println("======== 有效单词 ========");
            for (String word : formedWords) {
                System.out.println(word);
            }
            System.out.println("=========================");

            // 为玩家补充字母牌
            tileManager.drawTilesForPlayer(currentPlayer);
            
            // 由于成功放置了字母牌，取消该玩家的保持字母架标记
            keepRackPlayers.remove(currentPlayer.getPlayerIndex());

            // 使用特定的日志方法记录放置确认
            logger.logPlacementConfirmation(currentPlayer, formedWords, score);

            // 结束当前回合
            endPlayerTurn();
        } else {
            logger.info("玩家 {} 放置无效", currentPlayer);
        }

        return score;
    }
    
    /**
     * 辅助方法：根据放置信息获取形成的单词
     * @param placements 放置信息
     * @return 形成的单词列表
     */
    private List<String> getFormWords(List<PlaceTile.TilePlacement> placements) {
        if (placements == null || placements.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 我们可以借用placeTile内部的wordFormer来获取形成的单词
        WordFormer wordFormer = new WordFormer(boardOperator);
        return wordFormer.formWords(placements);
    }

    /**
     * 取消玩家当前的字母牌放置
     * @return 操作是否成功
     */
    public boolean cancelPlacement() {
        if (gameState != GameState.RUNNING) {
            logger.warn("无法取消放置: 游戏未运行");
            return false;
        }

        boolean result = placeTile.cancelPlacements(getCurrentPlayer());
        if (result) {
            // 使用特定的日志方法记录放置取消
            logger.logPlacementCancellation(getCurrentPlayer());
        } else {
            logger.warn("玩家 {} 尝试取消放置失败", getCurrentPlayer());
        }

        return result;
    }

    /**
     * 为当前玩家交换选中的字母牌并结束其回合
     * 这将计为玩家的一个回合，之后轮到下一位玩家
     *
     * @return 如果交换成功则返回true，否则返回false
     */
    public boolean exchangeTiles() {
        if (gameState != GameState.RUNNING) {
            logger.warn("无法交换字母牌：游戏未运行");
            return false;
        }

        Player currentPlayer = getCurrentPlayer();
        List<Tile> selectedTiles = tileManager.getSelectedTile(currentPlayer);

        if (selectedTiles.isEmpty()) {
            logger.info("玩家 {} 没有选中任何字母牌用于交换", currentPlayer);
            return false;
        }

        // 交换字母牌
        List<Tile> exchangedTiles = tileManager.exchangeSelectedTiles(currentPlayer);

        if (!exchangedTiles.isEmpty()) {
            // 记录交换日志
            logger.logTileExchange(currentPlayer, exchangedTiles);

            // 将当前回合标记为跳过，因为玩家没有放置任何字母牌
            currentTurn.markAsPass();

            // 确保选中状态被完全清除
            tileManager.clearSelectedTiles(currentPlayer);
            
            // 由于已经交换了字母牌，取消该玩家的保持字母架标记
            keepRackPlayers.remove(currentPlayer.getPlayerIndex());

            //由UI前端结束回合
            
            return true;
        }

        logger.warn("玩家 {} 交换字母牌失败", currentPlayer);
        return false;
    }

    /**
     * 为当前玩家指定空白牌的字母
     * @param tile 空白牌
     * @param letter 要指定的字母
     * @return 是否成功指定
     */
    public boolean assignLetterToBlankTile(Tile tile, char letter) {

        Player currentPlayer = getCurrentPlayer();

        // 指定空白牌字母
        boolean result = tileManager.assignLetterToBlankTile(currentPlayer, tile, letter);

        if (result) {
            // 记录日志
            logger.logBlankTileAssign(currentPlayer, tile, letter);
        } else {
            logger.warn("玩家 {} 指定空白牌字母 '{}' 失败", currentPlayer, letter);
        }

        return result;
    }

    /**
     * 玩家跳过当前回合
     */
    public void passTurn() {
        if (gameState != GameState.RUNNING) {
            logger.warn("无法跳过回合: 游戏未运行");
            return;
        }

        Player currentPlayer = getCurrentPlayer();
        logger.info("玩家 {} 主动跳过回合", currentPlayer);

        currentTurn.markAsPass();
        
        // 标记该玩家的字母架需要保持不变（不重新抽取字母牌）
        keepRackPlayers.add(currentPlayer.getPlayerIndex());
        
        endPlayerTurn();
    }

    /**
     * 暂停游戏
     */
    public void pauseGame() {
        if (gameState != GameState.RUNNING) {
            logger.warn("无法暂停游戏: 游戏未运行");
            return;
        }

        // 暂停计时器
        if (turnTimerTask != null && !turnTimerTask.isDone()) {
            turnTimerTask.cancel(false);
        }
        if (gameTimerTask != null && !gameTimerTask.isDone()) {
            gameTimerTask.cancel(false);
        }

        logger.info("游戏已暂停");

        // 更新游戏状态
        setGameState(GameState.PAUSED);
    }

    /**
     * 恢复游戏
     */
    public void resumeGame() {
        if (gameState != GameState.PAUSED) {
            logger.warn("无法恢复游戏: 游戏未暂停");
            return;
        }

        logger.info("游戏已恢复");

        // 更新游戏状态
        setGameState(GameState.RUNNING);

        // 重新启动计时器
        startGameTimer();
        startTurnTimer();
    }

    /**
     * 保存游戏到文件
     * @param filePath 文件路径
     * @throws IOException 如果保存失败
     */
    public void
    saveGame(String filePath) throws IOException {
        File file = new File(filePath);

        // 确保父目录存在
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("无法创建目录: " + parentDir.getAbsolutePath());
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            // 暂停游戏状态（如果正在运行）
            GameState previousState = gameState;
            if (gameState == GameState.RUNNING) {
                pauseGame();
            }

            // 保存游戏对象
            oos.writeObject(this);

            // 恢复原来的游戏状态
            if (previousState == GameState.RUNNING) {
                resumeGame();
            }

            logger.info("游戏已保存至: {}", filePath);
        }
    }

    /**
     * 从文件加载游戏
     * @param filePath 文件路径
     * @param dictionaryPath 词典路径
     * @return 加载的游戏实例
     * @throws IOException 如果加载失败
     * @throws ClassNotFoundException 如果类找不到
     */
    public static Game loadGame(String filePath, String dictionaryPath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            Game game = (Game) ois.readObject();

            // 重新初始化transient字段
            game.reinitializeTransientFields(dictionaryPath);

            game.logger.info("游戏已从 {} 加载", filePath);

            // 根据游戏状态自动恢复计时
            if (game.gameState == GameState.RUNNING) {
                // 自动重启游戏计时器和回合计时器
                game.startGameTimer();
                game.startTurnTimer();
                game.logger.info("游戏计时已自动恢复");
            }

            return game;
        }
    }

    /**
     * 重新初始化transient字段
     * @param dictionaryPath 词典路径
     * @throws IOException 如果词典加载失败
     */
    private void reinitializeTransientFields(String dictionaryPath) throws IOException {
        // 使用已有的游戏ID重新初始化日志记录器
        this.logger = GameLoggerFactory.getLoggerForGame(this.gameId);

        // 重新初始化游戏组件
        this.boardOperator = new DefaultBoardOperator(board);
        this.tileRackOperator = new DefaultTileRackOperator(tileManager);
        this.placeTile = new PlaceTile(boardOperator, tileRackOperator, this.logger);
        this.positionValidator = new DefaultPositionValidator(boardOperator);

        // 重新初始化词典和验证器
        this.dictionary = new Dictionary();
        this.wordValidator = new DefaultWordValidator(dictionary);

        // 重新初始化计时器
        this.timerService = Executors.newSingleThreadScheduledExecutor();

        // 重新初始化监听器列表
        this.gameStateListeners = new ArrayList<>();
        
        // 重新初始化保持字母架集合
        this.keepRackPlayers = new HashSet<>();

        // 重新初始化AI玩家工厂
        this.aiPlayerFactory = new AIPlayerFactory(this);
    }

    /**
     * 添加游戏状态监听器
     * @param listener 监听器
     */
    public void addGameStateListener(GameStateListener listener) {
        gameStateListeners.add(listener);

        // 如果游戏已经处于某个状态，立即通知新监听器
        listener.onGameStateChanged(gameState);

        if (gameState == GameState.RUNNING || gameState == GameState.PAUSED) {
            listener.onTurnChanged(getCurrentPlayer(), remainingTurnTime);
            listener.onGameTimeUpdated(remainingGameTime);
        }
    }

    /**
     * 移除游戏状态监听器
     * @param listener 监听器
     * @return 是否成功移除
     */
    public boolean removeGameStateListener(GameStateListener listener) {
        return gameStateListeners.remove(listener);
    }

    /**
     * 设置游戏状态并通知监听器
     * @param newState 新状态
     */
    private void setGameState(GameState newState) {
        if (this.gameState != newState) {
            this.gameState = newState;

            // 通知所有监听器
            for (GameStateListener listener : gameStateListeners) {
                listener.onGameStateChanged(newState);
            }
        }
    }

    /**
     * 获取当前玩家
     * @return 当前玩家
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /**
     * 获取所有玩家
     * @return 玩家列表
     */
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * 获取游戏配置
     * @return 游戏配置
     */
    public GameConfig getConfig() {
        return config;
    }

    /**
     * 获取游戏ID
     * @return 游戏ID
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * 获取游戏开始时间
     * @return 游戏开始时间
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * 获取游戏结束时间
     * @return 游戏结束时间，如果游戏未结束则返回null
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * 获取游戏持续时间
     * @return 游戏持续时间
     */
    public Duration getGameDuration() {
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return Duration.between(startTime, end);
    }

    /**
     * 获取当前回合号
     * @return 当前回合号
     */
    public int getTurnNumber() {
        return turnNumber;
    }

    /**
     * 获取游戏棋盘
     * @return 游戏棋盘
     */
    public Board getBoard() {
        return board;
    }

    /**
     * 获取字母管理器
     * @return 字母管理器
     */
    public TileManager getTileManager() {
        return tileManager;
    }

    /**
     * 获取棋盘操作器
     * @return 棋盘操作器
     */
    public BoardOperator getBoardOperator() {
        return boardOperator;
    }

    /**
     * 获取字母放置器
     * @return 字母放置器
     */
    public PlaceTile getPlaceTile() {
        return placeTile;
    }

    /**
     * 获取单词验证器
     * @return 单词验证器
     */
    public WordValidator getWordValidator() {
        return wordValidator;
    }

    /**
     * 获取游戏日志记录器
     * @return 游戏日志记录器
     */
    public GameLogger getLogger() {
        return logger;
    }

    /**
     * 获取当前回合
     * @return 当前回合
     */
    public Turn getCurrentTurn() {
        return currentTurn;
    }

    /**
     * 获取回合历史记录
     * @return 回合历史记录
     */
    public List<Turn> getTurnHistory() {
        return new ArrayList<>(turnHistory);
    }

    /**
     * 获取游戏剩余时间（秒）
     * @return 游戏剩余时间（秒）
     */
    public int getRemainingGameTime() {
        return remainingGameTime;
    }

    /**
     * 获取格式化的游戏剩余时间
     * @return 格式化为"分:秒"的游戏剩余时间
     */
    public String getFormattedGameTime() {
        int minutes = remainingGameTime / 60;
        int seconds = remainingGameTime % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * 获取回合剩余时间
     * @return 回合剩余时间（秒）
     */
    public int getRemainingTurnTime() {
        return remainingTurnTime;
    }

    /**
     * 获取格式化的回合剩余时间
     * @return 格式化为"分:秒"的回合剩余时间
     */
    public String getFormattedTurnTime() {
        int minutes = remainingTurnTime / 60;
        int seconds = remainingTurnTime % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * 获取游戏状态
     * @return 游戏状态
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * 游戏是否已结束
     * @return 如果游戏已结束则返回true，否则返回false
     */
    public boolean isGameOver() {
        return isGameOver;
    }

    /**
     * 将玩家切换为AI托管模式
     * 
     * @param player 要托管的玩家
     * @return 是否成功切换
     */
    public boolean enableAIControl(Player player) {
        return aiPlayerFactory.enableAIControl(player);
    }
    
    /**
     * 将玩家切换为指定难度的AI托管模式
     * 
     * @param player 要托管的玩家
     * @param difficulty AI难度级别
     * @return 是否成功切换
     */
    public boolean enableAIControl(Player player, AIPlayer.Difficulty difficulty) {
        return aiPlayerFactory.enableAIControl(player, difficulty);
    }
    
    /**
     * 取消玩家的AI托管模式
     * 
     * @param player 要取消托管的玩家
     * @return 是否成功取消
     */
    public boolean disableAIControl(Player player) {
        return aiPlayerFactory.disableAIControl(player);
    }
    
    /**
     * 检查玩家是否处于AI托管状态
     * 
     * @param player 要检查的玩家
     * @return 是否为AI托管
     */
    public boolean isAIControlled(Player player) {
        return aiPlayerFactory != null && aiPlayerFactory.isAIControlled(player);
    }
    
    /**
     * 获取AI玩家工厂
     * 
     * @return AI玩家工厂实例
     */
    public AIPlayerFactory getAIPlayerFactory() {
        return aiPlayerFactory;
    }

    /**
     * 处理对象销毁
     * @throws Throwable 如果发生错误
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            // 确保计时器服务被关闭
            if (timerService != null && !timerService.isShutdown()) {
                timerService.shutdownNow();
            }
        } finally {
            super.finalize();
        }
    }
}
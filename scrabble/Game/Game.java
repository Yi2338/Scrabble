package scrabble.Game;

import scrabble.Board.Board;
import scrabble.Logging.GameLogger;
import scrabble.Logging.GameLoggerFactory;
import scrabble.Placement.*;
import scrabble.Tile.Tile;
import scrabble.Tile.TileManager;
import scrabble.Validator.DefaultPositionValidator;
import scrabble.Validator.PositionValidator;
import scrabble.Validator.WordValidator;


import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


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
    private final transient BoardOperator boardOperator;
    /** 字母架操作器 */
    private final transient TileRackOperator tileRackOperator;
    /** 字母放置器 */
    private final transient PlaceTile placeTile;
    /** 词典 */
    private transient Dictionary dictionary;
    /** 单词验证器 */
    private transient WordValidator wordValidator;
    /** 位置验证器 */
    private final transient PositionValidator positionValidator;
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
    private final transient GameLogger logger;


    /**
     * 创建一个新的Scrabble游戏实例
     * @param players 参与游戏的玩家列表
     * @param config 游戏配置
     * @throws IOException 如果词典加载失败
     */
    public Game(List<Player> players, GameConfig config) throws IOException {
        this.gameId = UUID.randomUUID().toString();
        this.players = new ArrayList<>(players);
        this.config = config;
        this.startTime = LocalDateTime.now();
        this.turnHistory = new ArrayList<>();
        this.turnNumber = 1;
        this.logger = GameLoggerFactory.getLogger();

        // 初始化游戏组件
        this.board = new Board();
        this.tileManager = new TileManager();
        this.boardOperator = new DefaultBoardOperator(board);
        this.tileRackOperator = new DefaultTileRackOperator(tileManager);
        this.placeTile = new PlaceTile(boardOperator, tileRackOperator);
        this.positionValidator = new DefaultPositionValidator(boardOperator);
        this.currentPlayerIndex = 0;
        this.isGameOver = false;

    }

}

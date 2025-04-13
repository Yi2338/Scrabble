package scrabble.UI;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.net.URL;

/**
 * 音频管理类 - 负责处理游戏中的音频效果
 */
public class AudioManager {
    
    private static AudioManager instance;
    private Media clickSound;
    private Media failureSound;
    private Media slotMachineMusic;
    private final String CLICK_SOUND_PATH = "/audio/click.mp3";
    private final String FAILURE_SOUND_PATH = "/audio/failure_confirm.mp3";
    private final String SLOT_MACHINE_MUSIC_PATH = "/audio/Slot_machine_music.mp3";
    
    /**
     * 私有构造函数，初始化音频资源
     */
    private AudioManager() {
        try {
            // 初始化点击声音
            URL clickResource = getClass().getResource(CLICK_SOUND_PATH);
            if (clickResource != null) {
                clickSound = new Media(clickResource.toString());
            } else {
                System.err.println("无法加载音频文件: " + CLICK_SOUND_PATH);
            }
            
            // 初始化失败声音
            URL failureResource = getClass().getResource(FAILURE_SOUND_PATH);
            if (failureResource != null) {
                failureSound = new Media(failureResource.toString());
            } else {
                System.err.println("无法加载音频文件: " + FAILURE_SOUND_PATH);
            }
            
            // 初始化老虎机音乐
            URL slotMachineMusicResource = getClass().getResource(SLOT_MACHINE_MUSIC_PATH);
            if (slotMachineMusicResource != null) {
                slotMachineMusic = new Media(slotMachineMusicResource.toString());
            } else {
                System.err.println("无法加载音频文件: " + SLOT_MACHINE_MUSIC_PATH);
            }
        } catch (Exception e) {
            System.err.println("音频初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取单例实例
     */
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    /**
     * 播放点击音效
     */
    public void playClickSound() {
        if (clickSound != null) {
            try {
                MediaPlayer mediaPlayer = new MediaPlayer(clickSound);
                mediaPlayer.setOnEndOfMedia(() -> {
                    mediaPlayer.dispose();
                });
                mediaPlayer.play();
            } catch (Exception e) {
                System.err.println("播放音频失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 播放失败音效
     */
    public void playFailureSound() {
        if (failureSound != null) {
            try {
                MediaPlayer mediaPlayer = new MediaPlayer(failureSound);
                mediaPlayer.setOnEndOfMedia(() -> {
                    mediaPlayer.dispose();
                });
                mediaPlayer.play();
            } catch (Exception e) {
                System.err.println("播放失败音频失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 播放老虎机音乐
     */
    public void playSlotMachineMusic() {
        if (slotMachineMusic != null) {
            try {
                MediaPlayer mediaPlayer = new MediaPlayer(slotMachineMusic);
                mediaPlayer.setOnEndOfMedia(() -> {
                    mediaPlayer.dispose();
                });
                mediaPlayer.play();
            } catch (Exception e) {
                System.err.println("播放老虎机音乐失败: " + e.getMessage());
            }
        }
    }
} 
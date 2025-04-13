package scrabble.Tile;

import java.io.Serializable;

/**
 * Tile 类代表 Scrabble 游戏中的一个字母牌。
 * 每个字母牌包含一个字母和对应的分值。字母牌也可以是空白的（类似于万能牌）。
 */
public class Tile implements Serializable {
    /** 序列化ID */
    private static final long serialVersionUID = 1L;
    
    /** 字母牌上的字母 */
    private char letter;
    
    /** 字母对应的分值 */
    private int value;
    
    /** 标识是否为空白牌（万能牌） */
    private boolean isBlank;

    /**
     * 创建一个带有指定字母和分值的字母牌
     * @param letter 字母牌上的字母
     * @param value 字母对应的分值
     */
    public Tile(char letter, int value) {
        this.letter = letter;
        this.value = value;
        this.isBlank = false;
    }

    /**
     * 创建一个空白字母牌（万能牌）
     * 空白牌的字母为'\0'，分值为0
     */
    public Tile() {
        this('\0', 0);
        this.isBlank = true;
    }

    /**
     * 获取字母牌上的字母
     * @return 字母牌上的字母
     */
    public char getLetter() {
        return letter;
    }

    /**
     * 获取字母牌的分值
     * @return 字母牌的分值
     */
    public int getValue() {
        return value;
    }

    /**
     * 判断是否为空白牌（万能牌）
     * @return 如果是空白牌返回true，否则返回false
     */
    public boolean isBlank() {
        return isBlank;
    }

    /**
     * 设置空白牌的字母和分数
     * 只有空白牌可以使用此方法
     * @param letter 要设置的字母
     * @param newValue 要设置的分数值
     * @return 是否设置成功
     */
    public boolean setBlankLetter(char letter, int newValue) {
        if (!isBlank) {
            return false; // 只有空白牌可以设置字母
        }
        this.letter = letter;
        this.value = newValue;
        return true;
    }
    /**
     * 创建当前字母牌的一个副本
     * @return 一个新的具有相同属性的Tile对象
     */
    public Tile copy() {
        Tile copy = new Tile(this.letter, this.value);
        copy.isBlank = this.isBlank;
        return copy;
    }
}

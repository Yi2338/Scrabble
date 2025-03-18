package scrabble.Validator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 用于 Scrabble 游戏的字典类。
 * 从文本文件加载单词并提供单词验证。
 */
public class Dictionary {
    private final Set<String> words;
    private final String dictionaryPath;

    /**
     * 创建一个新的字典实例并从指定文件加载单词。
     *
     * @param dictionaryPath 字典文本文件的路径
     * @throws IOException 如果文件无法读取
     */
    public Dictionary(String dictionaryPath) throws IOException {
        this.dictionaryPath = dictionaryPath;
        this.words = new HashSet<>();

        // 先检查文件是否存在
        File file = new File(dictionaryPath);
        if (!file.exists()) {
            throw new IOException("词典文件不存在: " + dictionaryPath);
        }
        if (!file.isFile()) {
            throw new IOException("指定路径不是一个文件: " + dictionaryPath);
        }
        if (!file.canRead()) {
            throw new IOException("无法读取词典文件: " + dictionaryPath + " (权限不足,请注意：词典格式必须是txt)");
        }

        try {
            loadDictionary();
        } catch (IOException e) {
            throw new IOException("加载词典文件时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 从字典文件加载单词。
     *
     * @throws IOException 如果文件无法读取或格式错误
     */
    private void loadDictionary() throws IOException {
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(dictionaryPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    String word = line.trim().toLowerCase();
                    // 只加入长度大于或等于2且不包含 '.' 或 '-' 的单词
                    if (!word.isEmpty() && word.length() >= 2 && !word.contains(".") && !word.contains("-")) {
                        words.add(word);
                    }
                } catch (Exception e) {
                    throw new IOException("词典文件第 " + lineNumber + " 行格式错误: " + line, e);
                }
            }
        }

        if (words.isEmpty()) {
            throw new IOException("词典文件为空或没有有效单词: " + dictionaryPath);
        }
    }

    /**
     * 检查一个单词是否存在于字典中。
     *
     * @param word 要检查的单词
     * @return 如果单词存在返回 true，否则返回 false
     */
    public boolean isWordValid(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        return words.contains(word.trim().toLowerCase());
    }

    /**
     * 向字典中添加自定义单词。
     *
     * @param word 要添加的单词
     */
    public void addCustomWord(String word) {
        if (word != null && !word.isEmpty()) {
            words.add(word.trim().toLowerCase());
        }
    }

    /**
     * 获取字典中的单词数量。
     *
     * @return 单词的数量
     */
    public int getWordCount() {
        return words.size();
    }

    /**
     * 返回词典中的所有单词
     * @return 词典中单词
     */
    public Set<String> getWords() {
        return words;
    }
}
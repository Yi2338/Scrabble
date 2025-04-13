package scrabble.Validator;

/**
 * 单词验证接口 - 用于验证单词是否在字典中存在
 */
public interface WordValidator {
    /**
     * 根据游戏字典检查单词是否有效
     *
     * @param word 要验证的单词
     * @return 如果单词有效则返回true，否则返回false
     */
    boolean isValidWord(String word);

}

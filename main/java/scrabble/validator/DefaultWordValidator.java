package scrabble.validator;

/**
 * 使用 Dictionary 类的简单实现的 WordValidator。
 */
public class DefaultWordValidator implements WordValidator {
    private final Dictionary dictionary;

    /**
     * 使用指定的字典创建一个新的 SimpleDictionaryWordValidator。
     *
     * @param dictionary 用于单词验证的字典
     */
    public DefaultWordValidator(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * 验证一个单词是否存在于字典中。
     *
     * @param word 要验证的单词
     * @return 如果单词有效，返回 true，否则返回 false
     */
    @Override
    public boolean isValidWord(String word) {
        if (word == null || word.length() < 2) {
            return false;
        }

        return dictionary.isWordValid(word.trim().toLowerCase());
    }

    /**
     * 向字典中添加自定义单词。
     *
     * @param word 要添加的单词
     */
    public void addCustomWord(String word) {
        if (word != null && !word.isEmpty()) {
            dictionary.addCustomWord(word.trim().toLowerCase());
        }
    }
}

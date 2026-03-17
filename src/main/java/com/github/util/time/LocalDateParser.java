package com.github.util.time;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * 基于前缀树的日期解析工具类
 * 支持多种常见日期格式的高效匹配和解析
 */
public class LocalDateParser {

    /**
     * 前缀树节点
     */
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        DateTimeFormatter formatter; // 叶子节点存储对应的格式化器
        String pattern;               // 叶子节点存储原始模式字符串
    }

    private final TrieNode root;
    // 备用格式化器（用于前缀树匹配失败时的尝试）
//    private final DateTimeFormatter[] fallbackFormatters;

    /**
     * 预定义的常见日期格式
     */
    private static final String[] DEFAULT_PATTERNS = {
        // 年-月-日 格式
        "yyyy-MM-dd",
        "yyyy-M-d",
        "yyyy-M-dd",
        "yyyy-MM-d",
        // 年/月/日 格式
        "yyyy/MM/dd",
        "yyyy/M/d",
        "yyyy/M/dd",
        "yyyy/MM/d",
        // 年.月.日 格式
        "yyyy.MM.dd",
        "yyyy.M.d",
        "yyyy.M.dd",
        "yyyy.MM.d",
        // 年月日 无分隔符
        "yyyyMMdd",
        // 中文格式
        "yyyy年MM月dd日",
        "yyyy年M月d日"
    };

    private LocalDateParser(Builder builder) {
        this.root = new TrieNode();
        // 构建前缀树
        for (String pattern : builder.patterns) {
            insertPattern(pattern);
        }
        // 备用格式化器（用于前缀树匹配失败时的尝试）
//        this.fallbackFormatters = builder.patterns.stream()
//                .map(DateTimeFormatter::ofPattern)
//                .toArray(DateTimeFormatter[]::new);
    }

    /**
     * 将格式模式插入前缀树
     * 使用格式字符作为键，而非实际日期字符
     */
    private void insertPattern(String pattern) {
        TrieNode node = root;
        for (char c : pattern.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.formatter = DateTimeFormatter.ofPattern(pattern);
        node.pattern = pattern;
    }

    /**
     * 使用前缀树匹配日期字符串
     * @param dateStr 日期字符串
     * @return 匹配到的格式化器，如果未匹配到返回null
     */
    private DateTimeFormatter matchFormatter(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        // 收集所有可能的匹配路径
        return matchFormatterRecursive(root, dateStr, 0);
    }

    /**
     * 递归匹配格式化器
     */
    private DateTimeFormatter matchFormatterRecursive(TrieNode node, String dateStr, int index) {
        if (index == dateStr.length()) {
            return node.formatter;
        }

        char c = dateStr.charAt(index);
        DateTimeFormatter result = null;

        // 尝试精确匹配
        TrieNode childTrieNode = node.children.get(c);
        if (childTrieNode != null) {
            result = matchFormatterRecursive(childTrieNode, dateStr, index + 1);
        }

        // 尝试通配符匹配（y, M, d, H, m, s等格式字符可以匹配数字）
        if (result == null) {
            for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
                char patternChar = entry.getKey();
                if (isMatchForFormatChar(c, patternChar)) {
                    result = matchFormatterRecursive(entry.getValue(), dateStr, index + 1);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        return result;
    }

    /**
     * 判断字符是否为格式字符
     */
    private boolean isFormatChar(char c) {
        return c == 'y' || c == 'M' || c == 'd' || c == 'H' || c == 'm' || c == 's';
    }

    /**
     * 判断日期字符是否匹配格式字符
     */
    private boolean isMatchForFormatChar(char dateChar, char formatChar) {
        if (isFormatChar(formatChar)) {
            return Character.isDigit(dateChar);
        }
        return dateChar == formatChar;
    }

    /**
     * 解析日期字符串为LocalDate
     * @param dateStr 日期字符串
     * @return 解析后的LocalDate
     * @throws DateTimeParseException 如果无法解析
     */
    public LocalDate parse(String dateStr) {
        Objects.requireNonNull(dateStr, "日期字符串不能为null");

        String trimmed = dateStr.trim();

        // 首先尝试前缀树匹配
        DateTimeFormatter formatter = matchFormatter(trimmed);
        if (formatter != null) {
            try {
                LocalDate localDate = LocalDate.parse(trimmed, formatter);
                return localDate;
            } catch (DateTimeParseException ignored) {
                // 前缀树匹配失败，继续尝试备用格式
            }
        }

        // 尝试备用格式化器
//        for (DateTimeFormatter fallback : fallbackFormatters) {
//            try {
//                return LocalDate.parse(trimmed, fallback);
//            } catch (DateTimeParseException ignored) {
//                // 继续尝试下一个格式
//            }
//        }

        throw new DateTimeParseException("无法解析日期: " + dateStr, dateStr, 0);
    }

    /**
     * 安全解析日期字符串，解析失败返回null
     * @param dateStr 日期字符串
     * @return 解析后的LocalDate，失败返回null
     */
    public LocalDate parseSafely(String dateStr) {
        try {
            return parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断字符串是否为有效日期格式
     * @param dateStr 日期字符串
     * @return 是否可解析
     */
    public boolean isValidDate(String dateStr) {
        return parseSafely(dateStr) != null;
    }

    /**
     * 获取日期字符串的格式模式
     * @param dateStr 日期字符串
     * @return 格式模式，未匹配返回null
     */
    public String getPattern(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        String trimmed = dateStr.trim();
        return getPatternRecursive(root, trimmed, 0);
    }

    private String getPatternRecursive(TrieNode node, String dateStr, int index) {
        if (index == dateStr.length()) {
            return node.pattern;
        }

        char c = dateStr.charAt(index);
        String result = null;

        // 精确匹配
        if (node.children.containsKey(c)) {
            result = getPatternRecursive(node.children.get(c), dateStr, index + 1);
        }

        // 通配符匹配
        if (result == null) {
            for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
                char patternChar = entry.getKey();
                if (isFormatChar(patternChar) && isMatchForFormatChar(c, patternChar)) {
                    result = getPatternRecursive(entry.getValue(), dateStr, index + 1);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        return result;
    }

    /**
     * 创建Builder用于自定义日期格式
     */
    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String... patterns){
        return new Builder(patterns);
    }

    /**
     * 获取默认实例
     */
    public static LocalDateParser getDefault() {
        return DefaultHolder.INSTANCE;
    }

    /**
     * 静态工具方法：使用默认配置解析日期
     */
    public static LocalDate parseDate(String dateStr) {
        return getDefault().parse(dateStr);
    }

    /**
     * 静态工具方法：使用默认配置安全解析日期
     */
    public static LocalDate parseDateSafely(String dateStr) {
        return getDefault().parseSafely(dateStr);
    }

    /**
     * 单例持有者
     */
    private static class DefaultHolder {

        private static final LocalDateParser INSTANCE = new Builder().build();

    }

    /**
     * Builder类用于自定义日期格式
     */
    public static class Builder {

        private final Set<String> patterns = new LinkedHashSet<>();

        public Builder() {
            // 默认添加所有预定义格式
            for (String pattern : DEFAULT_PATTERNS) {
                patterns.add(pattern);
            }
        }

        public Builder (String ... patterns){
            this.patterns.addAll(Arrays.asList(patterns));
        }

        /**
         * 添加自定义格式
         */
        public Builder addPattern(String pattern) {
            Objects.requireNonNull(pattern, "格式模式不能为null");
            patterns.add(pattern);
            return this;
        }

        /**
         * 添加多个自定义格式
         */
        public Builder addPatterns(String... patterns) {
            for (String pattern : patterns) {
                addPattern(pattern);
            }
            return this;
        }

        /**
         * 移除格式
         */
        public Builder removePattern(String pattern) {
            patterns.remove(pattern);
            return this;
        }

        /**
         * 清空所有格式
         */
        public Builder clearPatterns() {
            patterns.clear();
            return this;
        }

        /**
         * 构建DateParser实例
         */
        public LocalDateParser build() {
            if (patterns.isEmpty()) {
                throw new IllegalStateException("至少需要添加一个日期格式");
            }
            return new LocalDateParser(this);
        }
    }
}
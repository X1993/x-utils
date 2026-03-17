package com.github.util.time;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * 基于前缀树的日期时间解析工具类
 * 支持多种常见日期时间格式的高效匹配和解析
 * @author X1993
 * @date 2026/03/15
 */
public class LocalDateTimeParser {

    /**
     * 字符匹配器接口
     */
    private interface CharMatcher {
        /**
         * 判断输入字符是否匹配
         * @param c 输入字符
         * @return 是否匹配
         */
        boolean matches(char c);
    }

    /**
     * 精确匹配器：匹配指定字符
     */
    private static class ExactMatcher implements CharMatcher {
        private final char target;

        ExactMatcher(char target) {
            this.target = target;
        }

        @Override
        public boolean matches(char c) {
            return c == target;
        }
    }

    /**
     * 数字匹配器：匹配任意数字
     */
    private static class DigitMatcher implements CharMatcher {
        static final DigitMatcher INSTANCE = new DigitMatcher();

        @Override
        public boolean matches(char c) {
            return Character.isDigit(c);
        }
    }

    /**
     * 前缀树节点
     */
    private static class TrieNode {
        // 子节点映射：key 为匹配器的标识字符（用于区分不同分支）
        Map<Character, TrieNode> children = new HashMap<>();
        // 该节点对应的匹配器
        CharMatcher matcher;
        // 叶子节点存储对应的格式化器
        DateTimeFormatter formatter;
        // 叶子节点存储原始模式字符串
        String pattern;
    }

    private final TrieNode root;

    /**
     * 格式字符集合（这些字符匹配任意数字）
     */
    private static final Set<Character> FORMAT_CHARS = new HashSet<>(
            Arrays.asList('y', 'M', 'd', 'H', 'm', 's', 'S')
    );

    /**
     * 预定义的常见日期时间格式
     */
    private static final String[] DEFAULT_PATTERNS = {
        // 标准格式
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd H:m:s",
        "yyyy-MM-dd H:m",
        // 斜杠分隔
        "yyyy/MM/dd HH:mm:ss",
        "yyyy/MM/dd HH:mm",
        "yyyy/MM/dd H:m:s",
        "yyyy/MM/dd H:m",
        // 点分隔
        "yyyy.MM.dd HH:mm:ss",
        "yyyy.MM.dd HH:mm",
        "yyyy.MM.dd H:m:s",
        "yyyy.MM.dd H:m",
        // 无分隔符
        "yyyyMMddHHmmss",
        "yyyyMMddHHmm",
        // ISO 格式
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm",
        // 带毫秒
        "yyyy-MM-dd HH:mm:ss.SSS",
        "yyyy-MM-dd HH:mm:ss.S",
        // 中文格式
        "yyyy年MM月dd日 HH时mm分ss秒",
        "yyyy年MM月dd日 HH时mm分",
        "yyyy年M月d日 H时m分s秒",
        "yyyy年M月d日 H时m分"
    };

    private LocalDateTimeParser(Builder builder) {
        this.root = new TrieNode();
        // 构建前缀树
        for (String pattern : builder.patterns) {
            insertPattern(pattern);
        }
    }

    /**
     * 将格式模式插入前缀树
     */
    private void insertPattern(String pattern) {
        TrieNode node = root;
        // 解析格式字符串，生成规范化字符序列和对应的匹配器
        List<ParseElement> elements = parsePattern(pattern);

        for (ParseElement element : elements) {
            // 使用标识字符作为 key
            char key = element.keyChar;
            TrieNode child = node.children.get(key);
            if (child == null) {
                child = new TrieNode();
                child.matcher = element.matcher;
                node.children.put(key, child);
            }
            node = child;
        }
        node.formatter = DateTimeFormatter.ofPattern(pattern);
        node.pattern = pattern;
    }

    /**
     * 解析元素：包含标识字符和匹配器
     */
    private static class ParseElement {
        final char keyChar;      // 用于区分分支的标识字符
        final CharMatcher matcher; // 匹配器

        ParseElement(char keyChar, CharMatcher matcher) {
            this.keyChar = keyChar;
            this.matcher = matcher;
        }
    }

    /**
     * 解析格式模式，生成解析元素列表
     * 处理字面量语法 'xxx'
     */
    private List<ParseElement> parsePattern(String pattern) {
        List<ParseElement> elements = new ArrayList<>();
        int i = 0;
        while (i < pattern.length()) {
            char c = pattern.charAt(i);
            if (c == '\'') {
                // 处理字面量 'xxx'
                int end = pattern.indexOf('\'', i + 1);
                if (end > i + 1) {
                    // 提取字面量内容
                    for (int j = i + 1; j < end; j++) {
                        char literal = pattern.charAt(j);
                        elements.add(new ParseElement(literal, new ExactMatcher(literal)));
                    }
                    i = end + 1;
                } else if (end == i + 1) {
                    // 空字面量 '' 表示单引号本身
                    elements.add(new ParseElement('\'', new ExactMatcher('\'')));
                    i = end + 1;
                } else {
                    // 没有结束的单引号
                    elements.add(new ParseElement(c, new ExactMatcher(c)));
                    i++;
                }
            } else if (FORMAT_CHARS.contains(c)) {
                // 格式字符：匹配任意数字
                elements.add(new ParseElement(c, DigitMatcher.INSTANCE));
                i++;
            } else {
                // 普通字符：精确匹配
                elements.add(new ParseElement(c, new ExactMatcher(c)));
                i++;
            }
        }
        return elements;
    }

    /**
     * 使用前缀树匹配日期时间字符串
     */
    private TrieNode matchNode(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return matchNodeRecursive(root, dateTimeStr, 0);
    }

    /**
     * 递归匹配节点
     */
    private TrieNode matchNodeRecursive(TrieNode node, String dateTimeStr, int index) {
        if (index == dateTimeStr.length()) {
            return node.formatter != null ? node : null;
        }

        char c = dateTimeStr.charAt(index);

        // 遍历所有子节点，找到匹配的分支
        for (TrieNode child : node.children.values()) {
            if (child.matcher != null && child.matcher.matches(c)) {
                TrieNode result = matchNodeRecursive(child, dateTimeStr, index + 1);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * 解析日期时间字符串为 LocalDateTime
     * @param dateTimeStr 日期时间字符串
     * @return 解析后的 LocalDateTime
     * @throws DateTimeParseException 如果无法解析
     */
    public LocalDateTime parse(String dateTimeStr) {
        Objects.requireNonNull(dateTimeStr, "日期时间字符串不能为null");

        String trimmed = dateTimeStr.trim();

        TrieNode matchedNode = matchNode(trimmed);
        if (matchedNode != null) {
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(trimmed, matchedNode.formatter);
                return localDateTime;
            } catch (DateTimeParseException ignored) {
                // 前缀树匹配失败
            }
        }

        throw new DateTimeParseException("无法解析日期时间: " + dateTimeStr, dateTimeStr, 0);
    }

    /**
     * 安全解析日期时间字符串，解析失败返回null
     * @param dateTimeStr 日期时间字符串
     * @return 解析后的 LocalDateTime，失败返回null
     */
    public LocalDateTime parseSafely(String dateTimeStr) {
        try {
            return parse(dateTimeStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断字符串是否为有效日期时间格式
     * @param dateTimeStr 日期时间字符串
     * @return 是否可解析
     */
    public boolean isValidDateTime(String dateTimeStr) {
        return parseSafely(dateTimeStr) != null;
    }

    /**
     * 获取日期时间字符串的格式模式
     * @param dateTimeStr 日期时间字符串
     * @return 格式模式，未匹配返回null
     */
    public String getPattern(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }

        TrieNode matchedNode = matchNode(dateTimeStr.trim());
        return matchedNode != null ? matchedNode.pattern : null;
    }

    /**
     * 创建 Builder 用于自定义格式
     */
    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String... patterns) {
        return new Builder(patterns);
    }

    /**
     * 获取默认实例
     */
    public static LocalDateTimeParser getDefault() {
        return DefaultHolder.INSTANCE;
    }

    /**
     * 静态工具方法：使用默认配置解析日期时间
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return getDefault().parse(dateTimeStr);
    }

    /**
     * 静态工具方法：使用默认配置安全解析日期时间
     */
    public static LocalDateTime parseDateTimeSafely(String dateTimeStr) {
        return getDefault().parseSafely(dateTimeStr);
    }

    /**
     * 单例持有者
     */
    private static class DefaultHolder {
        private static final LocalDateTimeParser INSTANCE = new Builder().build();
    }

    /**
     * Builder 类用于自定义日期时间格式
     */
    public static class Builder {

        private final Set<String> patterns = new LinkedHashSet<>();

        public Builder() {
            Collections.addAll(patterns, DEFAULT_PATTERNS);
        }

        public Builder(String... patterns) {
            Collections.addAll(this.patterns, patterns);
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
            Collections.addAll(this.patterns, patterns);
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
         * 构建 LocalDateTimeParser 实例
         */
        public LocalDateTimeParser build() {
            if (patterns.isEmpty()) {
                throw new IllegalStateException("至少需要添加一个日期时间格式");
            }
            return new LocalDateTimeParser(this);
        }
    }
}
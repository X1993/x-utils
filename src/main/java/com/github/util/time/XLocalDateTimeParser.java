package com.github.util.time;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * 基于前缀树的日期时间解析工具类
 * 支持多种常见日期时间格式的高效匹配和解析
 * 优化：连续格式字符合并为单节点，减少树深度
 * @author X1993
 */
public class XLocalDateTimeParser {

    /**
     * 字符匹配器接口：支持一次匹配多个字符
     */
    private interface CharMatcher {
        /**
         * 尝试从指定位置匹配字符
         * @param input 输入字符串
         * @param index 起始索引
         * @return 匹配的字符数，0 表示不匹配
         */
        int match(String input, int index);

        /**
         * 获取标识字符串（用于区分不同分支）
         */
        String key();
    }

    /**
     * 精确匹配器：匹配指定字符串
     */
    private static class ExactMatcher implements CharMatcher {
        private final String literal;

        ExactMatcher(String literal) {
            this.literal = literal;
        }

        @Override
        public int match(String input, int index) {
            int len = literal.length();
            if (index + len > input.length()) {
                return 0;
            }
            for (int i = 0; i < len; i++) {
                if (input.charAt(index + i) != literal.charAt(i)) {
                    return 0;
                }
            }
            return len;
        }

        @Override
        public String key() {
            return "'" + literal + "'";
        }
    }

    /**
     * 数字匹配器：匹配指定数量的数字
     */
    private static class DigitMatcher implements CharMatcher {
        private final int count;
        private final String formatKey;

        DigitMatcher(int count, char formatChar) {
            this.count = count;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < count; i++) {
                sb.append(formatChar);
            }
            this.formatKey = sb.toString();
        }

        @Override
        public int match(String input, int index) {
            if (index + count > input.length()) {
                return 0;
            }
            for (int i = 0; i < count; i++) {
                if (!Character.isDigit(input.charAt(index + i))) {
                    return 0;
                }
            }
            return count;
        }

        @Override
        public String key() {
            return formatKey;
        }
    }

    /**
     * 前缀树节点
     */
    private static class TrieNode {
        // 子节点映射
        Map<String, TrieNode> children = new HashMap<>();
        // 该节点对应的匹配器
        CharMatcher matcher;
        // 叶子节点存储对应的格式化器
        DateTimeFormatter formatter;
        // 叶子节点存储原始模式字符串
        String pattern;
        // 该节点及其子节点下的格式化器总数
        int formatterCount;
    }

    private final TrieNode root;

    /**
     * 格式字符集合
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
        // ISO 格式带毫秒
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss.S",
        // 中文格式
        "yyyy年MM月dd日 HH时mm分ss秒",
        "yyyy年MM月dd日 HH时mm分",
        "yyyy年M月d日 H时m分s秒",
        "yyyy年M月d日 H时m分"
    };

    private static final XLocalDateTimeParser INSTANCE = new Builder().addPatterns(DEFAULT_PATTERNS).build();

    private XLocalDateTimeParser(Builder builder) {
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
        // 解析格式字符串，生成匹配器列表
        List<CharMatcher> matchers = parsePattern(pattern);

        // 记录路径上的所有节点，用于更新 formatterCount
        List<TrieNode> path = new ArrayList<>();
        path.add(root);

        for (CharMatcher matcher : matchers) {
            String key = matcher.key();
            TrieNode child = node.children.get(key);
            if (child == null) {
                child = new TrieNode();
                child.matcher = matcher;
                node.children.put(key, child);
            }
            node = child;
            path.add(node);
        }
        node.formatter = DateTimeFormatter.ofPattern(pattern);
        node.pattern = pattern;

        // 更新路径上所有节点的 formatterCount
        for (TrieNode pathNode : path) {
            pathNode.formatterCount++;
        }
    }

    /**
     * 解析格式模式，生成匹配器列表
     * 将连续的格式字符合并为单个匹配器
     */
    private List<CharMatcher> parsePattern(String pattern) {
        List<CharMatcher> matchers = new ArrayList<>();
        int i = 0;
        while (i < pattern.length()) {
            char c = pattern.charAt(i);

            if (c == '\'') {
                // 处理字面量 'xxx'
                int end = pattern.indexOf('\'', i + 1);
                if (end > i + 1) {
                    // 提取字面量内容
                    String literal = pattern.substring(i + 1, end);
                    matchers.add(new ExactMatcher(literal));
                    i = end + 1;
                } else if (end == i + 1) {
                    // 空字面量 '' 表示单引号本身
                    matchers.add(new ExactMatcher("'"));
                    i = end + 1;
                } else {
                    // 没有结束的单引号
                    matchers.add(new ExactMatcher("'"));
                    i++;
                }
            } else if (FORMAT_CHARS.contains(c)) {
                // 统计连续相同格式字符的数量
                int start = i;
                while (i < pattern.length() && pattern.charAt(i) == c) {
                    i++;
                }
                int count = i - start;
                // 创建数字匹配器，匹配指定数量的数字
                matchers.add(new DigitMatcher(count, c));
            } else {
                // 统计连续普通字符
                int start = i;
                while (i < pattern.length() && !FORMAT_CHARS.contains(pattern.charAt(i)) && pattern.charAt(i) != '\'') {
                    i++;
                }
                String literal = pattern.substring(start, i);
                matchers.add(new ExactMatcher(literal));
            }
        }
        return matchers;
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

        // 遍历所有子节点，找到匹配的分支
        for (TrieNode child : node.children.values()) {
            // 优化：跳过没有格式化器的分支
            if (child.formatterCount == 0) {
                continue;
            }
            if (child.matcher != null) {
                int matchedLen = child.matcher.match(dateTimeStr, index);
                if (matchedLen > 0) {
                    TrieNode result = matchNodeRecursive(child, dateTimeStr, index + matchedLen);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        return null;
    }

    /**
     * 去除字符串末尾的时区偏移信息（LocalDateTime 不含时区，直接丢弃）
     * <p>支持格式：Z、+/-HHMM、+/-HH:MM、+/-HH</p>
     */
    private static String stripZoneOffset(String str) {
        if (str.endsWith("Z")) {
            return str.substring(0, str.length() - 1);
        }
        int len = str.length();
        // 从最长模式开始检查，避免歧义
        // +/-HH:MM (sign + 5 chars = 6)
        if (len >= 6) {
            char s = str.charAt(len - 6);
            if ((s == '+' || s == '-')
                    && Character.isDigit(str.charAt(len - 5)) && Character.isDigit(str.charAt(len - 4))
                    && str.charAt(len - 3) == ':'
                    && Character.isDigit(str.charAt(len - 2)) && Character.isDigit(str.charAt(len - 1))) {
                return str.substring(0, len - 6);
            }
        }
        // +/-HHMM (sign + 4 chars = 5)
        if (len >= 5) {
            char s = str.charAt(len - 5);
            if ((s == '+' || s == '-')
                    && Character.isDigit(str.charAt(len - 4)) && Character.isDigit(str.charAt(len - 3))
                    && Character.isDigit(str.charAt(len - 2)) && Character.isDigit(str.charAt(len - 1))) {
                return str.substring(0, len - 5);
            }
        }
        // +/-HH (sign + 2 chars = 3)
        if (len >= 3) {
            char s = str.charAt(len - 3);
            if ((s == '+' || s == '-')
                    && Character.isDigit(str.charAt(len - 2)) && Character.isDigit(str.charAt(len - 1))) {
                return str.substring(0, len - 3);
            }
        }
        return str;
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

        String normalized = stripZoneOffset(trimmed);

        TrieNode matchedNode = matchNode(normalized);
        if (matchedNode != null) {
            try {
                return LocalDateTime.parse(normalized, matchedNode.formatter);
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
        return new Builder().addPatterns(DEFAULT_PATTERNS);
    }

    public static Builder builder(String... patterns) {
        return new Builder().addPatterns(patterns);
    }

    /**
     * 获取默认实例
     */
    public static XLocalDateTimeParser getDefault() {
        return INSTANCE;
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
     * Builder 类用于自定义日期时间格式
     */
    public static class Builder {

        private final Set<String> patterns = new LinkedHashSet<>();

        public Builder() {
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
        public XLocalDateTimeParser build() {
            if (patterns.isEmpty()) {
                throw new IllegalStateException("至少需要添加一个日期时间格式");
            }
            return new XLocalDateTimeParser(this);
        }
    }
}
package com.github.util.time;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * 基于前缀树的日期解析工具类
 * 支持多种常见日期格式的高效匹配和解析
 * 优化：连续格式字符合并为单节点，减少树深度
 * @author X1993
 */
public class XLocalDateParser {

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
            Arrays.asList('y', 'M', 'd')
    );

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

    private static final XLocalDateParser INSTANCE = new Builder().addPatterns(DEFAULT_PATTERNS).build();

    private XLocalDateParser(Builder builder) {
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
     * 使用前缀树匹配日期字符串
     */
    private TrieNode matchNode(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return matchNodeRecursive(root, dateStr, 0);
    }

    /**
     * 递归匹配节点
     */
    private TrieNode matchNodeRecursive(TrieNode node, String dateStr, int index) {
        if (index == dateStr.length()) {
            return node.formatter != null ? node : null;
        }

        // 遍历所有子节点，找到匹配的分支
        for (TrieNode child : node.children.values()) {
            // 优化：跳过没有格式化器的分支
            if (child.formatterCount == 0) {
                continue;
            }
            if (child.matcher != null) {
                int matchedLen = child.matcher.match(dateStr, index);
                if (matchedLen > 0) {
                    TrieNode result = matchNodeRecursive(child, dateStr, index + matchedLen);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        return null;
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

        TrieNode matchedNode = matchNode(trimmed);
        if (matchedNode != null) {
            try {
                return LocalDate.parse(trimmed, matchedNode.formatter);
            } catch (DateTimeParseException ignored) {
                // 前缀树匹配失败
            }
        }

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

        TrieNode matchedNode = matchNode(dateStr.trim());
        return matchedNode != null ? matchedNode.pattern : null;
    }

    /**
     * 创建Builder用于自定义日期格式
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
    public static XLocalDateParser getDefault() {
        return INSTANCE;
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
     * Builder类用于自定义日期格式
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
         * 构建DateParser实例
         */
        public XLocalDateParser build() {
            if (patterns.isEmpty()) {
                throw new IllegalStateException("至少需要添加一个日期格式");
            }
            return new XLocalDateParser(this);
        }
    }
}
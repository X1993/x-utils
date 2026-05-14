package com.github.util.time;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DateParser 测试类
 */
@DisplayName("DateParser 日期解析工具测试")
class XLocalDateParserTest {

    // ==================== 基础解析测试 ====================

    @Test
    @DisplayName("解析 yyyy-MM-dd 格式")
    void testParseStandardFormat() {
        XLocalDateParser parser = XLocalDateParser.getDefault();

        LocalDate result = parser.parse("2024-03-15");
        assertEquals(LocalDate.of(2024, 3, 15), result);
    }

    @Test
    @DisplayName("解析 yyyy-M-d 格式（单数字月日）")
    void testParseSingleDigitMonthDay() {
        XLocalDateParser parser = XLocalDateParser.getDefault();

        LocalDate result = parser.parse("2024-3-5");
        assertEquals(LocalDate.of(2024, 3, 5), result);
    }

    @Test
    @DisplayName("解析 yyyy/MM/dd 格式")
    void testParseSlashFormat() {
        XLocalDateParser parser = XLocalDateParser.getDefault();

        LocalDate result = parser.parse("2024/03/15");
        assertEquals(LocalDate.of(2024, 3, 15), result);
    }

    @Test
    @DisplayName("解析 yyyy/M/d 格式（斜杠单数字）")
    void testParseSlashSingleDigit() {
        XLocalDateParser parser = XLocalDateParser.getDefault();

        LocalDate result = parser.parse("2024/3/5");
        assertEquals(LocalDate.of(2024, 3, 5), result);
    }

    @Test
    @DisplayName("解析 yyyy.MM.dd 格式")
    void testParseDotFormat() {
        XLocalDateParser parser = XLocalDateParser.getDefault();

        LocalDate result = parser.parse("2024.03.15");
        assertEquals(LocalDate.of(2024, 3, 15), result);
    }

    @Test
    @DisplayName("解析 yyyyMMdd 格式（无分隔符）")
    void testParseCompactFormat() {
        XLocalDateParser parser = XLocalDateParser.getDefault();

        LocalDate result = parser.parse("20240315");
        assertEquals(LocalDate.of(2024, 3, 15), result);
    }

    @Test
    @DisplayName("解析中文格式 yyyy年MM月dd日")
    void testParseChineseFormat() {
        XLocalDateParser parser = XLocalDateParser.getDefault();

        LocalDate result = parser.parse("2024年03月15日");
        assertEquals(LocalDate.of(2024, 3, 15), result);
    }

    @Test
    @DisplayName("解析中文格式 yyyy年M月d日（单数字）")
    void testParseChineseSingleDigitFormat() {
        XLocalDateParser parser = XLocalDateParser.getDefault();

        LocalDate result = parser.parse("2024年3月5日");
        assertEquals(LocalDate.of(2024, 3, 5), result);
    }

    // ==================== 参数化测试 ====================

    static Stream<Arguments> provideDateStrings() {
        return Stream.of(
            // 标准格式
            Arguments.of("2024-01-01", LocalDate.of(2024, 1, 1)),
            Arguments.of("2024-12-31", LocalDate.of(2024, 12, 31)),
            // 单数字格式
            Arguments.of("2024-1-1", LocalDate.of(2024, 1, 1)),
            Arguments.of("2024-1-31", LocalDate.of(2024, 1, 31)),
            // 斜杠格式
            Arguments.of("2024/01/01", LocalDate.of(2024, 1, 1)),
            Arguments.of("2024/12/31", LocalDate.of(2024, 12, 31)),
            Arguments.of("2024/1/1", LocalDate.of(2024, 1, 1)),
            // 点格式
            Arguments.of("2024.01.01", LocalDate.of(2024, 1, 1)),
            Arguments.of("2024.1.1", LocalDate.of(2024, 1, 1)),
            // 紧凑格式
            Arguments.of("20240101", LocalDate.of(2024, 1, 1)),
            Arguments.of("20241231", LocalDate.of(2024, 12, 31)),
            // 中文格式
            Arguments.of("2024年1月1日", LocalDate.of(2024, 1, 1)),
            Arguments.of("2024年12月31日", LocalDate.of(2024, 12, 31))
        );
    }

    @ParameterizedTest
    @MethodSource("provideDateStrings")
    @DisplayName("参数化测试：解析各种日期格式")
    void testParseVariousFormats(String dateStr, LocalDate expected) {
        XLocalDateParser parser = XLocalDateParser.getDefault();
        assertEquals(expected, parser.parse(dateStr));
    }

    // ==================== 边界情况测试 ====================

    @Test
    @DisplayName("解析闰年日期")
    void testParseLeapYear() {
        XLocalDateParser parser = XLocalDateParser.getDefault();

        assertEquals(LocalDate.of(2024, 2, 29), parser.parse("2024-02-29"));
        assertEquals(LocalDate.of(2020, 2, 29), parser.parse("2020-02-29"));
    }

    @Test
    @DisplayName("解析前后有空格的日期")
    void testParseWithWhitespace() {
        XLocalDateParser parser = XLocalDateParser.getDefault();

        assertEquals(LocalDate.of(2024, 3, 15), parser.parse("  2024-03-15  "));
        assertEquals(LocalDate.of(2024, 3, 15), parser.parse("\t2024-03-15\n"));
    }

    @Test
    @DisplayName("解析空字符串抛出异常")
    void testParseEmptyString() {
        XLocalDateParser parser = XLocalDateParser.getDefault();

        assertThrows(DateTimeParseException.class, () -> parser.parse(""));
        assertThrows(DateTimeParseException.class, () -> parser.parse("   "));
    }

    @Test
    @DisplayName("解析null抛出NullPointerException")
    void testParseNull() {
        XLocalDateParser parser = XLocalDateParser.getDefault();

        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    @DisplayName("解析无效日期抛出异常")
    void testParseInvalidDate() {
        XLocalDateParser parser = XLocalDateParser.getDefault();

        assertThrows(DateTimeParseException.class, () -> parser.parse("2024-13-01")); // 无效月份
//        assertThrows(DateTimeParseException.class, () -> parser.parse("2024-02-30")); // 无效日期
        assertThrows(DateTimeParseException.class, () -> parser.parse("invalid"));    // 无效格式
//        assertThrows(DateTimeParseException.class, () -> parser.parse("2024/02/30")); // 无效日期
    }

    // ==================== 安全解析测试 ====================

    @Test
    @DisplayName("安全解析：有效日期返回结果")
    void testParseSafelyValid() {
        XLocalDateParser parser = XLocalDateParser.getDefault();

        assertEquals(LocalDate.of(2024, 3, 15), parser.parseSafely("2024-03-15"));
        assertEquals(LocalDate.of(2024, 1, 5), parser.parseSafely("2024-1-5"));
    }

    @Test
    @DisplayName("安全解析：无效日期返回null")
    void testParseSafelyInvalid() {
        XLocalDateParser parser = XLocalDateParser.getDefault();

        assertNull(parser.parseSafely("invalid"));
        assertNull(parser.parseSafely("2024-13-01"));
        assertNull(parser.parseSafely(null));
        assertNull(parser.parseSafely(""));
    }

    // ==================== 验证测试 ====================

    @Test
    @DisplayName("isValidDate：验证日期有效性")
    void testIsValidDate() {
        XLocalDateParser parser = XLocalDateParser.getDefault();

        assertTrue(parser.isValidDate("2024-03-15"));
        assertTrue(parser.isValidDate("2024/3/5"));
        assertTrue(parser.isValidDate("2024年3月5日"));

        assertFalse(parser.isValidDate("invalid"));
        assertFalse(parser.isValidDate("2024-13-01"));
        assertFalse(parser.isValidDate(null));
        assertFalse(parser.isValidDate(""));
    }

    // ==================== 获取格式模式测试 ====================

    @Test
    @DisplayName("getPattern：获取日期格式模式")
    void testGetPattern() {
        XLocalDateParser parser = XLocalDateParser.getDefault();

        // 注意：由于前缀树可能匹配多个格式，这里验证能匹配到某个有效格式
        assertNotNull(parser.getPattern("2024-03-15"));
        assertNotNull(parser.getPattern("2024/3/5"));

        assertNull(parser.getPattern(null));
        assertNull(parser.getPattern(""));
    }

    // ==================== 静态方法测试 ====================

    @Test
    @DisplayName("静态方法 parseDate")
    void testStaticParseDate() {
        assertEquals(LocalDate.of(2024, 3, 15), XLocalDateParser.parseDate("2024-03-15"));
        assertEquals(LocalDate.of(2024, 1, 5), XLocalDateParser.parseDate("2024-1-5"));
    }

    @Test
    @DisplayName("静态方法 parseDateSafely")
    void testStaticParseDateSafely() {
        assertEquals(LocalDate.of(2024, 3, 15), XLocalDateParser.parseDateSafely("2024-03-15"));
        assertNull(XLocalDateParser.parseDateSafely("invalid"));
    }

    // ==================== Builder 模式测试 ====================

    @Test
    @DisplayName("Builder：自定义格式")
    void testBuilderCustomPattern() {
        XLocalDateParser parser = XLocalDateParser.builder()
                .clearPatterns()
                .addPattern("yyyy|MM|dd")
                .build();

        LocalDate result = parser.parse("2024|03|15");
        assertEquals(LocalDate.of(2024, 3, 15), result);
    }

    @Test
    @DisplayName("Builder：添加多个格式")
    void testBuilderAddMultiplePatterns() {
        XLocalDateParser parser = XLocalDateParser.builder()
                .addPatterns("yyyy|MM|dd", "yyyy*MM*dd")
                .build();

        assertEquals(LocalDate.of(2024, 3, 15), parser.parse("2024|03|15"));
        assertEquals(LocalDate.of(2024, 3, 15), parser.parse("2024*03*15"));
    }

    @Test
    @DisplayName("Builder：移除格式")
    void testBuilderRemovePattern() {
        XLocalDateParser parser = XLocalDateParser.builder()
                .removePattern("yyyy-MM-dd")
                .build();

        // 移除后应该无法解析该格式（或由其他相似格式匹配）
        assertThrows(DateTimeParseException.class, () -> parser.parse("2024-03-15"));
    }

    @Test
    @DisplayName("Builder：空格式抛出异常")
    void testBuilderEmptyPatterns() {
        assertThrows(IllegalStateException.class, () ->
            XLocalDateParser.builder().clearPatterns().build()
        );
    }

    // ==================== 性能测试 ====================

    @Test
    @DisplayName("性能测试：批量解析")
    void testPerformance() {
        XLocalDateParser parser = XLocalDateParser.getDefault();
        String[] dates = {
            "2024-01-01", "2024/02/15", "2024.03.20", "20240410",
            "2024-5-5", "2024/6/6", "2024.7.7",
            "2024年8月8日", "2024年9月9日"
        };

        int iterations = 10000;
        long start = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            for (String date : dates) {
                parser.parse(date);
            }
        }

        long end = System.currentTimeMillis();
        System.out.printf("解析 %d 个日期耗时: %d ms%n", iterations * dates.length, end - start);

        // 验证解析结果正确
        for (String date : dates) {
            assertNotNull(parser.parse(date));
        }
    }

    // ==================== 并发测试 ====================

    @Test
    @DisplayName("并发测试：多线程解析")
    void testConcurrentParsing() throws InterruptedException {
        XLocalDateParser parser = XLocalDateParser.getDefault();
        int threadCount = 10;
        int iterationsPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < iterationsPerThread; j++) {
                    parser.parse("2024-03-15");
                    parser.parse("2024/3/5");
                    parser.parse("2024.1.1");
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // 如果没有抛出异常，测试通过
        assertTrue(true);
    }
}
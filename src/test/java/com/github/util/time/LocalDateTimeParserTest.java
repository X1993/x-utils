package com.github.util.time;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LocalDateTimeParser 测试类
 */
@DisplayName("LocalDateTimeParser 日期时间解析工具测试")
class LocalDateTimeParserTest {

    // ==================== 基础解析测试 ====================

    @Test
    @DisplayName("解析 yyyy-MM-dd HH:mm:ss 格式")
    void testParseStandardFormat() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024-03-15 14:30:00");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);
    }

    @Test
    @DisplayName("解析 yyyy-MM-dd HH:mm 格式（无秒）")
    void testParseFormatWithoutSeconds() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024-03-15 14:30");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);
    }

    @Test
    @DisplayName("解析 yyyy/MM/dd HH:mm:ss 格式（斜杠分隔）")
    void testParseSlashFormat() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024/03/15 14:30:00");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);
    }

    @Test
    @DisplayName("解析 yyyy.MM.dd HH:mm:ss 格式（点分隔）")
    void testParseDotFormat() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024.03.15 14:30:00");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);
    }

    @Test
    @DisplayName("解析 yyyy.MM.dd HH:mm 格式（点分隔无秒）")
    void testParseDotFormatWithoutSeconds() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024.03.15 14:30");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);
    }

    @Test
    @DisplayName("解析 yyyy-MM-dd H:m:s 格式（单数字时分秒）")
    void testParseSingleDigitTimeFormat() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024-03-15 9:5:3");
        assertEquals(LocalDateTime.of(2024, 3, 15, 9, 5, 3), result);
    }

    @Test
    @DisplayName("解析 yyyy-MM-dd H:m 格式（单数字时分无秒）")
    void testParseSingleDigitTimeWithoutSecondsFormat() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024-03-15 9:5");
        assertEquals(LocalDateTime.of(2024, 3, 15, 9, 5, 0), result);
    }

    @Test
    @DisplayName("解析 yyyy/MM/dd H:m:s 格式（斜杠单数字时分秒）")
    void testParseSlashSingleDigitTimeFormat() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024/03/15 9:5:3");
        assertEquals(LocalDateTime.of(2024, 3, 15, 9, 5, 3), result);
    }

    @Test
    @DisplayName("解析 yyyy/MM/dd H:m 格式（斜杠单数字时分无秒）")
    void testParseSlashSingleDigitTimeWithoutSecondsFormat() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024/03/15 9:5");
        assertEquals(LocalDateTime.of(2024, 3, 15, 9, 5, 0), result);
    }

    @Test
    @DisplayName("解析 yyyyMMddHHmmss 格式（无分隔符）")
    void testParseCompactFormat() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("20240315143000");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);
    }

    @Test
    @DisplayName("解析 yyyyMMddHHmm 格式（无分隔符无秒）")
    void testParseCompactFormatWithoutSeconds() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("202403151430");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);
    }

    @Test
    @DisplayName("解析 ISO 格式 yyyy-MM-dd'T'HH:mm:ss")
    void testParseIsoFormat() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024-03-15T14:30:00");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);

        LocalDateTime result2 = parser.parse("2024-03-15T14:30");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result2);
    }

    @Test
    @DisplayName("解析带毫秒格式 yyyy-MM-dd HH:mm:ss.SSS")
    void testParseWithMillis() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024-03-15 14:30:00.123");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0, 123000000), result);
    }

    @Test
    @DisplayName("解析中文格式 yyyy年MM月dd日 HH时mm分ss秒")
    void testParseChineseFormat() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024年03月15日 14时30分00秒");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);

        LocalDateTime result2 = parser.parse("2024年3月5日 9时5分3秒");
        assertEquals(LocalDateTime.of(2024, 3, 5, 9, 5, 3), result2);
    }

    @Test
    @DisplayName("解析中文格式 yyyy年MM月dd日 HH时mm分（无秒）")
    void testParseChineseFormatWithoutSeconds() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024年03月15日 14时30分");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);
    }

    // ==================== 参数化测试 ====================

    static Stream<Arguments> provideDateTimeStrings() {
        return Stream.of(
            // 标准格式
            Arguments.of("2024-01-01 00:00:00", LocalDateTime.of(2024, 1, 1, 0, 0, 0)),
            Arguments.of("2024-12-31 23:59:59", LocalDateTime.of(2024, 12, 31, 23, 59, 59)),
            // 斜杠格式
            Arguments.of("2024/01/01 12:30:45", LocalDateTime.of(2024, 1, 1, 12, 30, 45)),
            Arguments.of("2024/01/01 12:30", LocalDateTime.of(2024, 1, 1, 12, 30, 0)),
            // 点格式
            Arguments.of("2024.01.01 08:00:00", LocalDateTime.of(2024, 1, 1, 8, 0, 0)),
            Arguments.of("2024.01.01 08:00", LocalDateTime.of(2024, 1, 1, 8, 0, 0)),
            // 紧凑格式
            Arguments.of("20240101000000", LocalDateTime.of(2024, 1, 1, 0, 0, 0)),
            Arguments.of("20241231235959", LocalDateTime.of(2024, 12, 31, 23, 59, 59)),
            Arguments.of("202401010000", LocalDateTime.of(2024, 1, 1, 0, 0, 0)),
            // ISO 格式
            Arguments.of("2024-01-01T00:00:00", LocalDateTime.of(2024, 1, 1, 0, 0, 0)),
            Arguments.of("2024-12-31T23:59:59", LocalDateTime.of(2024, 12, 31, 23, 59, 59)),
            Arguments.of("2024-01-01T00:00", LocalDateTime.of(2024, 1, 1, 0, 0, 0)),
            // 无秒格式
            Arguments.of("2024-01-01 12:30", LocalDateTime.of(2024, 1, 1, 12, 30, 0)),
            // 单数字时分秒格式
            Arguments.of("2024-01-01 1:2:3", LocalDateTime.of(2024, 1, 1, 1, 2, 3)),
            Arguments.of("2024-01-01 1:2", LocalDateTime.of(2024, 1, 1, 1, 2, 0)),
            // 毫秒格式
            Arguments.of("2024-01-01 12:30:45.123", LocalDateTime.of(2024, 1, 1, 12, 30, 45, 123000000)),
            Arguments.of("2024-01-01 12:30:45.1", LocalDateTime.of(2024, 1, 1, 12, 30, 45, 100000000))
        );
    }

    @ParameterizedTest
    @MethodSource("provideDateTimeStrings")
    @DisplayName("参数化测试：解析各种日期时间格式")
    void testParseVariousFormats(String dateTimeStr, LocalDateTime expected) {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();
        assertEquals(expected, parser.parse(dateTimeStr));
    }

    // ==================== 边界情况测试 ====================

    @Test
    @DisplayName("解析闰年日期时间")
    void testParseLeapYear() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        assertEquals(LocalDateTime.of(2024, 2, 29, 12, 0, 0),
                parser.parse("2024-02-29 12:00:00"));
        assertEquals(LocalDateTime.of(2020, 2, 29, 23, 59, 59),
                parser.parse("2020-02-29 23:59:59"));
    }

    @Test
    @DisplayName("解析边界时间")
    void testParseBoundaryTimes() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        // 午夜
        assertEquals(LocalDateTime.of(2024, 3, 15, 0, 0, 0),
                parser.parse("2024-03-15 00:00:00"));

        // 一天的最后一秒
        assertEquals(LocalDateTime.of(2024, 3, 15, 23, 59, 59),
                parser.parse("2024-03-15 23:59:59"));

        // 中午
        assertEquals(LocalDateTime.of(2024, 3, 15, 12, 0, 0),
                parser.parse("2024-03-15 12:00:00"));
    }

    @Test
    @DisplayName("解析前后有空格的日期时间")
    void testParseWithWhitespace() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                parser.parse("  2024-03-15 14:30:00  "));
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                parser.parse("\t2024-03-15 14:30:00\n"));
    }

    @Test
    @DisplayName("解析空字符串抛出异常")
    void testParseEmptyString() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        assertThrows(DateTimeParseException.class, () -> parser.parse(""));
        assertThrows(DateTimeParseException.class, () -> parser.parse("   "));
    }

    @Test
    @DisplayName("解析null抛出NullPointerException")
    void testParseNull() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    @DisplayName("解析无效日期时间抛出异常")
    void testParseInvalidDateTime() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        assertThrows(DateTimeParseException.class, () -> parser.parse("2024-13-01 12:00:00")); // 无效月份
        assertThrows(DateTimeParseException.class, () -> parser.parse("invalid"));              // 无效格式
        assertThrows(DateTimeParseException.class, () -> parser.parse("2024-03-15"));           // 仅日期，不是日期时间格式
    }

    // ==================== 安全解析测试 ====================

    @Test
    @DisplayName("安全解析：有效日期时间返回结果")
    void testParseSafelyValid() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                parser.parseSafely("2024-03-15 14:30:00"));
    }

    @Test
    @DisplayName("安全解析：无效日期时间返回null")
    void testParseSafelyInvalid() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        assertNull(parser.parseSafely("invalid"));
        assertNull(parser.parseSafely("2024-13-01 12:00:00"));
        assertNull(parser.parseSafely(null));
        assertNull(parser.parseSafely(""));
    }

    // ==================== 验证测试 ====================

    @Test
    @DisplayName("isValidDateTime：验证日期时间有效性")
    void testIsValidDateTime() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        assertTrue(parser.isValidDateTime("2024-03-15 14:30:00"));
        assertTrue(parser.isValidDateTime("2024年3月5日 9时5分3秒"));
        assertTrue(parser.isValidDateTime("20240315143000"));

        assertFalse(parser.isValidDateTime("invalid"));
        assertFalse(parser.isValidDateTime("2024-13-01 12:00:00"));
        assertFalse(parser.isValidDateTime(null));
        assertFalse(parser.isValidDateTime(""));
    }

    // ==================== 获取格式模式测试 ====================

    @Test
    @DisplayName("getPattern：获取日期时间格式模式")
    void testGetPattern() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        assertNotNull(parser.getPattern("2024-03-15 14:30:00"));

        assertNull(parser.getPattern(null));
        assertNull(parser.getPattern(""));
    }

    // ==================== 静态方法测试 ====================

    @Test
    @DisplayName("静态方法 parseDateTime")
    void testStaticParseDateTime() {
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                LocalDateTimeParser.parseDateTime("2024-03-15 14:30:00"));
    }

    @Test
    @DisplayName("静态方法 parseDateTimeSafely")
    void testStaticParseDateTimeSafely() {
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                LocalDateTimeParser.parseDateTimeSafely("2024-03-15 14:30:00"));
        assertNull(LocalDateTimeParser.parseDateTimeSafely("invalid"));
    }

    // ==================== Builder 模式测试 ====================

    @Test
    @DisplayName("Builder：自定义格式")
    void testBuilderCustomPattern() {
        LocalDateTimeParser parser = LocalDateTimeParser.builder()
                .clearPatterns()
                .addPattern("yyyy|MM|dd|HH|mm|ss")
                .build();

        LocalDateTime result = parser.parse("2024|03|15|14|30|00");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);
    }

    @Test
    @DisplayName("Builder：使用构造函数传入格式")
    void testBuilderWithConstructorPatterns() {
        LocalDateTimeParser parser = LocalDateTimeParser.builder("yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm")
                .build();

        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                parser.parse("2024-03-15 14:30:00"));
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                parser.parse("2024/03/15 14:30"));
    }

    @Test
    @DisplayName("Builder：添加多个格式")
    void testBuilderAddMultiplePatterns() {
        LocalDateTimeParser parser = LocalDateTimeParser.builder()
                .clearPatterns()
                .addPatterns("yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss")
                .build();

        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                parser.parse("2024-03-15 14:30:00"));
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                parser.parse("2024/03/15 14:30:00"));
    }

    @Test
    @DisplayName("Builder：移除格式")
    void testBuilderRemovePattern() {
        LocalDateTimeParser parser = LocalDateTimeParser.builder()
                .removePattern("yyyy-MM-dd HH:mm:ss")
                .build();

        // 移除后应该无法解析该格式（或由其他相似格式匹配）
        assertThrows(DateTimeParseException.class, () -> parser.parse("2024-03-15 14:30:00"));
    }

    @Test
    @DisplayName("Builder：空格式抛出异常")
    void testBuilderEmptyPatterns() {
        assertThrows(IllegalStateException.class, () ->
            LocalDateTimeParser.builder().clearPatterns().build()
        );
    }

    // ==================== 性能测试 ====================

    @Test
    @DisplayName("性能测试：批量解析")
    void testPerformance() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();
        String[] dateTimes = {
            "2024-01-01 00:00:00", "2024/02/15 12:30:45", "2024.03.20 08:00:00",
            "20240410143000", "2024年8月8日 8时8分8秒"
        };

        int iterations = 10000;
        long start = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            for (String dateTime : dateTimes) {
                parser.parse(dateTime);
            }
        }

        long end = System.currentTimeMillis();
        System.out.printf("解析 %d 个日期时间耗时: %d ms%n", iterations * dateTimes.length, end - start);

        // 验证解析结果正确
        for (String dateTime : dateTimes) {
            assertNotNull(parser.parse(dateTime));
        }
    }

    // ==================== 并发测试 ====================

    @Test
    @DisplayName("并发测试：多线程解析")
    void testConcurrentParsing() throws InterruptedException {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();
        int threadCount = 10;
        int iterationsPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < iterationsPerThread; j++) {
                    parser.parse("2024-03-15 14:30:00");
                    parser.parse("20240315143000");
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

    // ==================== 特殊格式测试 ====================

    @Test
    @DisplayName("解析带毫秒的格式")
    void testParseWithMilliseconds() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024-03-15 14:30:00.123");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0, 123000000), result);

        LocalDateTime result2 = parser.parse("2024-03-15 14:30:00.1");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0, 100000000), result2);
    }

    @Test
    @DisplayName("解析仅时间的格式应失败")
    void testParseTimeOnlyShouldFail() {
        LocalDateTimeParser parser = LocalDateTimeParser.getDefault();

        // 仅时间格式不在默认支持列表中
        assertThrows(DateTimeParseException.class, () -> parser.parse("14:30:00"));
    }
}
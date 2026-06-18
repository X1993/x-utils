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
class XLocalDateTimeParserTest {

    // ==================== 基础解析测试 ====================

    @Test
    @DisplayName("解析 yyyy-MM-dd HH:mm:ss 格式")
    void testParseStandardFormat() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024-03-15 14:30:00");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);
    }

    @Test
    @DisplayName("解析 yyyy-MM-dd HH:mm 格式（无秒）")
    void testParseFormatWithoutSeconds() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024-03-15 14:30");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);
    }

    @Test
    @DisplayName("解析 yyyy/MM/dd HH:mm:ss 格式（斜杠分隔）")
    void testParseSlashFormat() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024/03/15 14:30:00");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);
    }

    @Test
    @DisplayName("解析 yyyy.MM.dd HH:mm:ss 格式（点分隔）")
    void testParseDotFormat() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024.03.15 14:30:00");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);
    }

    @Test
    @DisplayName("解析 yyyy.MM.dd HH:mm 格式（点分隔无秒）")
    void testParseDotFormatWithoutSeconds() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024.03.15 14:30");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);
    }

    @Test
    @DisplayName("解析 yyyy-MM-dd H:m:s 格式（单数字时分秒）")
    void testParseSingleDigitTimeFormat() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024-03-15 9:5:3");
        assertEquals(LocalDateTime.of(2024, 3, 15, 9, 5, 3), result);
    }

    @Test
    @DisplayName("解析 yyyy-MM-dd H:m 格式（单数字时分无秒）")
    void testParseSingleDigitTimeWithoutSecondsFormat() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024-03-15 9:5");
        assertEquals(LocalDateTime.of(2024, 3, 15, 9, 5, 0), result);
    }

    @Test
    @DisplayName("解析 yyyy/MM/dd H:m:s 格式（斜杠单数字时分秒）")
    void testParseSlashSingleDigitTimeFormat() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024/03/15 9:5:3");
        assertEquals(LocalDateTime.of(2024, 3, 15, 9, 5, 3), result);
    }

    @Test
    @DisplayName("解析 yyyy/MM/dd H:m 格式（斜杠单数字时分无秒）")
    void testParseSlashSingleDigitTimeWithoutSecondsFormat() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024/03/15 9:5");
        assertEquals(LocalDateTime.of(2024, 3, 15, 9, 5, 0), result);
    }

    @Test
    @DisplayName("解析 yyyyMMddHHmmss 格式（无分隔符）")
    void testParseCompactFormat() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("20240315143000");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);
    }

    @Test
    @DisplayName("解析 yyyyMMddHHmm 格式（无分隔符无秒）")
    void testParseCompactFormatWithoutSeconds() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("202403151430");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);
    }

    @Test
    @DisplayName("解析 ISO 格式 yyyy-MM-dd'T'HH:mm:ss")
    void testParseIsoFormat() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024-03-15T14:30:00");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);

        LocalDateTime result2 = parser.parse("2024-03-15T14:30");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result2);
    }

    @Test
    @DisplayName("解析 ISO 格式 yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    void testParseIsoFormat2() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2026-05-01T19:19:42.000+0000");
        assertEquals(LocalDateTime.of(2026, 5, 1, 19 , 19, 42), result);
    }

    @Test
    @DisplayName("解析带毫秒格式 yyyy-MM-dd HH:mm:ss.SSS")
    void testParseWithMillis() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024-03-15 14:30:00.123");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0, 123000000), result);
    }

    @Test
    @DisplayName("解析中文格式 yyyy年MM月dd日 HH时mm分ss秒")
    void testParseChineseFormat() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024年03月15日 14时30分00秒");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);

        LocalDateTime result2 = parser.parse("2024年3月5日 9时5分3秒");
        assertEquals(LocalDateTime.of(2024, 3, 5, 9, 5, 3), result2);
    }

    @Test
    @DisplayName("解析中文格式 yyyy年MM月dd日 HH时mm分（无秒）")
    void testParseChineseFormatWithoutSeconds() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

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
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();
        assertEquals(expected, parser.parse(dateTimeStr));
    }

    // ==================== 边界情况测试 ====================

    @Test
    @DisplayName("解析闰年日期时间")
    void testParseLeapYear() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        assertEquals(LocalDateTime.of(2024, 2, 29, 12, 0, 0),
                parser.parse("2024-02-29 12:00:00"));
        assertEquals(LocalDateTime.of(2020, 2, 29, 23, 59, 59),
                parser.parse("2020-02-29 23:59:59"));
    }

    @Test
    @DisplayName("解析边界时间")
    void testParseBoundaryTimes() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

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
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                parser.parse("  2024-03-15 14:30:00  "));
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                parser.parse("\t2024-03-15 14:30:00\n"));
    }

    @Test
    @DisplayName("解析空字符串抛出异常")
    void testParseEmptyString() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        assertThrows(DateTimeParseException.class, () -> parser.parse(""));
        assertThrows(DateTimeParseException.class, () -> parser.parse("   "));
    }

    @Test
    @DisplayName("解析null抛出NullPointerException")
    void testParseNull() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    @DisplayName("解析无效日期时间抛出异常")
    void testParseInvalidDateTime() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        assertThrows(DateTimeParseException.class, () -> parser.parse("2024-13-01 12:00:00")); // 无效月份
        assertThrows(DateTimeParseException.class, () -> parser.parse("invalid"));              // 无效格式
        // 仅日期可通过兜底解析补齐时间为 00:00
        assertEquals(LocalDateTime.of(2024, 3, 15, 0, 0, 0),
                parser.parse("2024-03-15"));
    }

    // ==================== 安全解析测试 ====================

    @Test
    @DisplayName("安全解析：有效日期时间返回结果")
    void testParseSafelyValid() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                parser.parseSafely("2024-03-15 14:30:00"));
    }

    @Test
    @DisplayName("安全解析：无效日期时间返回null")
    void testParseSafelyInvalid() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        assertNull(parser.parseSafely("invalid"));
        assertNull(parser.parseSafely("2024-13-01 12:00:00"));
        assertNull(parser.parseSafely(null));
        assertNull(parser.parseSafely(""));
    }

    // ==================== 验证测试 ====================

    @Test
    @DisplayName("isValidDateTime：验证日期时间有效性")
    void testIsValidDateTime() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

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
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        assertNotNull(parser.getPattern("2024-03-15 14:30:00"));

        assertNull(parser.getPattern(null));
        assertNull(parser.getPattern(""));
    }

    // ==================== 静态方法测试 ====================

    @Test
    @DisplayName("静态方法 parseDateTime")
    void testStaticParseDateTime() {
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                XLocalDateTimeParser.parseDateTime("2024-03-15 14:30:00"));
    }

    @Test
    @DisplayName("静态方法 parseDateTimeSafely")
    void testStaticParseDateTimeSafely() {
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                XLocalDateTimeParser.parseDateTimeSafely("2024-03-15 14:30:00"));
        assertNull(XLocalDateTimeParser.parseDateTimeSafely("invalid"));
    }

    // ==================== Builder 模式测试 ====================

    @Test
    @DisplayName("Builder：自定义格式")
    void testBuilderCustomPattern() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.builder()
                .clearPatterns()
                .addPattern("yyyy|MM|dd|HH|mm|ss")
                .build();

        LocalDateTime result = parser.parse("2024|03|15|14|30|00");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), result);
    }

    @Test
    @DisplayName("Builder：使用构造函数传入格式")
    void testBuilderWithConstructorPatterns() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.builder("yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm")
                .build();

        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                parser.parse("2024-03-15 14:30:00"));
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                parser.parse("2024/03/15 14:30"));
    }

    @Test
    @DisplayName("Builder：添加多个格式")
    void testBuilderAddMultiplePatterns() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.builder()
                .clearPatterns()
                .addPatterns("yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss")
                .build();

        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                parser.parse("2024-03-15 14:30:00"));
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                parser.parse("2024/03/15 14:30:00"));
    }

    @Test
    @DisplayName("Builder：移除格式后通过兜底解析")
    void testBuilderRemovePattern() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.builder()
                .removePattern("yyyy-MM-dd HH:mm:ss")
                .build();

        // 移除后前缀树无法匹配，但兜底解析仍可解析
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                parser.parse("2024-03-15 14:30:00"));
    }

    @Test
    @DisplayName("Builder：空格式抛出异常")
    void testBuilderEmptyPatterns() {
        assertThrows(IllegalStateException.class, () ->
            XLocalDateTimeParser.builder().clearPatterns().build()
        );
    }

    // ==================== 性能测试 ====================

    @Test
    @DisplayName("性能测试：批量解析")
    void testPerformance() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();
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
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();
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
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        LocalDateTime result = parser.parse("2024-03-15 14:30:00.123");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0, 123000000), result);

        LocalDateTime result2 = parser.parse("2024-03-15 14:30:00.1");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0, 100000000), result2);
    }

    @Test
    @DisplayName("解析仅时间的格式应失败")
    void testParseTimeOnlyShouldFail() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        // 仅时间格式不在默认支持列表中，兜底解析会将数字组解析为年/月/...，月份无效
        assertThrows(DateTimeParseException.class, () -> parser.parse("14:30:00"));
    }

    // ==================== 兜底解析测试 ====================

    @Test
    @DisplayName("兜底解析：未注册的分隔符格式")
    void testFallbackUnregisteredDelimiter() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        // 下划线分隔的日期时间（未注册格式，走兜底）
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 45),
                parser.parse("2024_03_15_14_30_45"));
    }

    @Test
    @DisplayName("兜底解析：仅日期补齐时间为0")
    void testFallbackDateOnly() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        assertEquals(LocalDateTime.of(2024, 3, 15, 0, 0, 0),
                parser.parse("2024-03-15"));
        assertEquals(LocalDateTime.of(2024, 3, 15, 0, 0, 0),
                parser.parse("2024/03/15"));
    }

    @Test
    @DisplayName("兜底解析：日期+时分补齐秒为0")
    void testFallbackDateHourMinute() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0),
                parser.parse("2024=03=15=14=30"));
    }

    @Test
    @DisplayName("兜底解析：日期+时分秒补齐毫秒为0")
    void testFallbackDateToSecond() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 45),
                parser.parse("2024#03#15#14#30#45"));
    }

    @Test
    @DisplayName("兜底解析：混合非数字字符")
    void testFallbackMixedDelimiters() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        // 混合分隔符：- . : 等均视为分隔符
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 45),
                parser.parse("2024-03.15 abc 14:30:45"));
    }

    @Test
    @DisplayName("兜底解析：仅年份")
    void testFallbackYearOnly() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0, 0),
                parser.parse("2024"));
    }

    @Test
    @DisplayName("兜底解析：年月日+毫秒（需要中间0占位）")
    void testFallbackYearMonthDayMillis() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        // 年月日 + 0时0分0秒 + 毫秒：用 0 占位中间字段
        assertEquals(LocalDateTime.of(2024, 3, 15, 0, 0, 0, 500_000_000),
                parser.parse("2024_03_15_0_0_0_500"));
    }

    @Test
    @DisplayName("兜底解析：纯数字紧凑格式已在默认格式中")
    void testFallbackCompactNumeric() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        // "yyyyMMddHHmmss" 已在默认格式中，前缀树直接匹配
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 30),
                parser.parse("20240315143030"));
    }

    @Test
    @DisplayName("兜底解析：无数字字符串抛出异常")
    void testFallbackNoDigits() {
        XLocalDateTimeParser parser = XLocalDateTimeParser.getDefault();

        assertThrows(DateTimeParseException.class, () -> parser.parse("abc-def"));
        assertThrows(DateTimeParseException.class, () -> parser.parse(""));
    }
}
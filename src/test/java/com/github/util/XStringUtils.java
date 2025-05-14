package com.github.util;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author wangjj7
 * @date 2025/5/14
 * @description
 */
public class XStringUtils {

    /**
     * 替换文本中的关键字
     * 该方法通过指定的关键字替换函数来替换文本中由特定起始和结束字符包围的关键字
     * 还可以使用保留标识符函数来决定是否保留原始关键字
     *
     * @param content 待处理的文本
     * @param keywordReplaceFunction 关键字替换函数，用于生成新的关键字
     * @param startChar 关键字的起始字符
     * @param endChar 关键字的结束字符
     * @param reservedIdentifierFunction 保留标识符函数，决定是否保留原始关键字
     * @return 替换后的文本
     */
    public static String replaceKeyword(String content , Function<String ,String> keywordReplaceFunction ,
                                 char startChar , char endChar ,
                                 BiFunction<String ,String ,Boolean> reservedIdentifierFunction)
    {
        // 如果内容为空，则直接返回
        if (org.apache.commons.lang3.StringUtils.isBlank(content)){
            return content;
        }

        StringBuilder result = new StringBuilder();
        char[] chars = content.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            // 当找到起始字符时，开始处理关键字
            if (c == startChar){
                StringBuilder keyword = new StringBuilder();
                // 循环直到找到结束字符，收集关键字
                for (i++; i < chars.length; i++) {
                    c = chars[i];
                    // 如果找到结束字符，进行替换逻辑
                    if (c == endChar){
                        String oldKeyword = keyword.toString();
                        String replaceValue = keywordReplaceFunction.apply(oldKeyword);
                        boolean reservedIdentifier = reservedIdentifierFunction != null && Boolean.TRUE.equals(
                                reservedIdentifierFunction.apply(oldKeyword ,replaceValue));
                        // 如果是保留标识符，保留原始关键字
                        if (reservedIdentifier){
                            result.append(startChar);
                        }
                        // 如果替换值非空，添加到结果中
                        if (org.apache.commons.lang3.StringUtils.isNotBlank(replaceValue)){
                            result.append(replaceValue);
                        }
                        // 如果是保留标识符，添加结束字符
                        if (reservedIdentifier){
                            result.append(endChar);
                        }
                        break;
                    }else {
                        keyword.append(c);
                    }
                }
            } else {
                // 不是起始字符，直接添加到结果中
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 根据关键词替换内容中的特定部分
     * 此方法用于替换字符串content中的特定关键词，通过keywordReplaceFunction函数来转换这些关键词
     * 使用startChar和endChar来界定关键词的开始和结束
     *
     * @param content 原始内容，其中包含待替换的关键词
     * @param keywordReplaceFunction 函数，用于替换关键词
     * @param startChar 关键词开始的字符
     * @param endChar 关键词结束的字符
     * @return 替换后的字符串
     */
    public static String replaceKeyword(String content ,Function<String ,String> keywordReplaceFunction ,
                                        char startChar ,char endChar) {
        // 调用重载方法，添加一个默认的过滤条件，始终返回false，表示不过滤任何关键词
        return replaceKeyword(content ,keywordReplaceFunction ,startChar ,endChar ,(s0 ,s1) -> false);
    }

}

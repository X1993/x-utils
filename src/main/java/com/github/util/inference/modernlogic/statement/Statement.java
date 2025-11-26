package com.github.util.inference.modernlogic.statement;

import java.util.List;

/**
 * 陈述
 * @author wangjj7
 * @date 2025/11/24
 * @description
 */
public interface Statement {

    /**
     * 获取陈述的真值
     * @return
     */
    boolean isTrue();

    /**
     * 格式化输出
     * @return
     */
    String formatDescription();

    /**
     * 获取包含的所有简单陈述（去重）
     * @return
     */
    List<SimpleStatement> getSimpleStatements();

}

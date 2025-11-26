package com.github.util.inference.modernlogic;

import com.github.util.inference.modernlogic.statement.CombinationStatement;
import com.github.util.inference.modernlogic.statement.SimpleStatement;
import com.github.util.inference.modernlogic.statement.Statement;

import java.util.List;

/**
 * @author wangjj7
 * @date 2025/11/25
 * @description
 */
public interface Argumentation {

    /**
     * 解析论证涉及的所有简单论证
     * @return
     */
    List<SimpleStatement> getSimpleStatements();

    /**
     * 格式化输出
     * @return
     */
    String formatDescription();

    /**
     * 生成论证有效性逻辑等价的复合陈述（满足任一前提为假或结论为真）
     * @return
     */
    CombinationStatement equivalenceStatement();

    List<Statement> getPreconditions();

    Statement getConclusion();
}

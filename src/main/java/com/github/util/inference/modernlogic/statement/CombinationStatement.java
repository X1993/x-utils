package com.github.util.inference.modernlogic.statement;

import java.util.List;

/**
 * 复合陈述
 * @author wangjj7
 * @date 2025/11/24
 * @description
 */
public interface CombinationStatement extends Statement {

    /**
     * 分支陈述
     * @return
     */
    List<Statement> getBranchStatements();

    /**
     * 逻辑连接符
     * @return
     */
    String logicSymbol();

}

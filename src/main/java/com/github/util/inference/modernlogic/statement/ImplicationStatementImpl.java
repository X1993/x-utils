package com.github.util.inference.modernlogic.statement;

import com.github.util.inference.modernlogic.ModernLogicConstant;
import com.github.util.inference.modernlogic.StatementUtils;
import java.util.*;

/**
 * 蕴含陈述（如果...那么...）
 * @author wangjj7
 * @date 2025/11/24
 * @description
 */
public class ImplicationStatementImpl implements ImplicationStatement {

    final Statement beforeStatement;

    final Statement afterStatement;

    public ImplicationStatementImpl(Statement beforeStatement, Statement afterStatement) {
        this.beforeStatement = beforeStatement;
        this.afterStatement = afterStatement;
    }

    @Override
    public String logicSymbol() {
        return ModernLogicConstant.LogicSymbol.IF_THEN.getSymbol();
    }

    @Override
    public boolean isTrue() {
        return !beforeStatement.isTrue() || afterStatement.isTrue();
    }

    @Override
    public List<Statement> getBranchStatements() {
        return Arrays.asList(beforeStatement ,afterStatement);
    }

    @Override
    public String formatDescription() {
        return StatementUtils.formatDescription(beforeStatement)
                + logicSymbol() + StatementUtils.formatDescription(afterStatement);
    }

    @Override
    public List<SimpleStatement> getSimpleStatements() {
        Set<SimpleStatement> simpleStatements = new HashSet<>();
        simpleStatements.addAll(beforeStatement.getSimpleStatements());
        simpleStatements.addAll(afterStatement.getSimpleStatements());
        return new ArrayList<>(simpleStatements);
    }

    @Override
    public Statement getBeforeStatement() {
        return beforeStatement;
    }

    @Override
    public Statement getAfterStatement() {
        return afterStatement;
    }

    @Override
    public String toString() {
        return formatDescription();
    }
}

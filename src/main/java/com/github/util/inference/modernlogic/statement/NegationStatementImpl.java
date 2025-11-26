package com.github.util.inference.modernlogic.statement;

import com.github.util.inference.modernlogic.ModernLogicConstant;
import com.github.util.inference.modernlogic.StatementUtils;
import java.util.Collections;
import java.util.List;

/**
 * 否定陈述
 * @author wangjj7
 * @date 2025/11/24
 * @description
 */
public class NegationStatementImpl implements NegationStatement {

    //被否定的陈述
    final Statement deniedStatement;

    public NegationStatementImpl(Statement deniedStatement) {
        this.deniedStatement = deniedStatement;
    }

    @Override
    public List<Statement> getBranchStatements() {
        return Collections.singletonList(deniedStatement);
    }

    @Override
    public String logicSymbol() {
        return ModernLogicConstant.LogicSymbol.NOT.getSymbol();
    }

    @Override
    public boolean isTrue() {
        return !deniedStatement.isTrue();
    }

    @Override
    public String formatDescription() {
        return logicSymbol() + StatementUtils.formatDescription(deniedStatement);
    }

    @Override
    public List<SimpleStatement> getSimpleStatements() {
        return deniedStatement.getSimpleStatements();
    }

    @Override
    public Statement getDeniedStatement() {
        return deniedStatement;
    }

    @Override
    public String toString() {
        return formatDescription();
    }

}

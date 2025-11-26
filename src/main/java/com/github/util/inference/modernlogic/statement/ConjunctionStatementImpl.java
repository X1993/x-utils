package com.github.util.inference.modernlogic.statement;

import com.github.util.inference.modernlogic.ModernLogicConstant;
import com.github.util.inference.modernlogic.StatementUtils;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 合取陈述（与）
 * @author wangjj7
 * @date 2025/11/24
 * @description
 */
public class ConjunctionStatementImpl implements DisjunctionStatement {

    private final List<Statement> statements;

    public ConjunctionStatementImpl(List<Statement> statements) {
        this.statements = Collections.unmodifiableList(statements);
    }

    public ConjunctionStatementImpl(Statement... statements) {
        this.statements = Arrays.asList(statements);
    }

    @Override
    public String logicSymbol() {
        return ModernLogicConstant.LogicSymbol.AND.getSymbol();
    }

    @Override
    public boolean isTrue() {
        return statements.stream().allMatch(Statement::isTrue);
    }

    @Override
    public List<Statement> getBranchStatements() {
        return statements;
    }

    @Override
    public String formatDescription() {
        return statements.stream()
                .map(StatementUtils::formatDescription)
                .collect(Collectors.joining(logicSymbol()));
    }

    @Override
    public List<SimpleStatement> getSimpleStatements() {
        return statements.stream()
                .map(Statement::getSimpleStatements)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return formatDescription();
    }
}

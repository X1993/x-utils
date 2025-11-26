package com.github.util.inference.modernlogic;

import com.github.util.inference.modernlogic.statement.*;
import java.util.*;

/**
 * 论证
 * @author wangjj7
 * @date 2025/11/24
 * @description
 */
public class ArgumentationImpl implements Argumentation {

    //前提
    private final List<Statement> preconditions;

    //结论
    private final Statement conclusion;

    //包含的所有简单陈述（已去重）
    private final List<SimpleStatement> simpleStatements;

    public ArgumentationImpl(List<Statement> preconditions, Statement conclusion) {
        if (preconditions == null || preconditions.isEmpty()){
            throw new IllegalArgumentException("前提不能为空");
        }
        if (conclusion == null){
            throw new IllegalArgumentException("结论不能为空");
        }
        this.preconditions = Collections.unmodifiableList(preconditions);
        this.conclusion = conclusion;
        this.simpleStatements = getSimpleStatements();
        if (simpleStatements.size() > 31){
            throw new IllegalArgumentException("不支持超过31个简单陈述构成的论证");
        }
    }

    public ArgumentationImpl(Statement conclusion , Statement ... preconditions){
        this(Arrays.asList(preconditions) ,conclusion);
    }

    @Override
    public List<SimpleStatement> getSimpleStatements(){
        Set<SimpleStatement> simpleStatements = new HashSet<>();
        for (Statement precondition : preconditions) {
            simpleStatements.addAll(precondition.getSimpleStatements());
        }
        simpleStatements.addAll(conclusion.getSimpleStatements());
        return new ArrayList<>(simpleStatements);
    }

    @Override
    public String formatDescription(){
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= preconditions.size(); i++) {
            sb.append("P").append(i).append(": ")
                    .append(preconditions.get(i - 1).formatDescription())
                    .append("\n");
        }
        sb.append("∴ : ").append(conclusion.formatDescription());
        return sb.toString();
    }

    @Override
    public CombinationStatement equivalenceStatement() {
        List<Statement> statements = new ArrayList<>();
        for (int i = 0; i < preconditions.size(); i++) {
            statements.add(preconditions.get(i));
        }
        statements.add(new NegationStatementImpl(conclusion));
        return new NegationStatementImpl(new ConjunctionStatementImpl(statements));
    }

    @Override
    public List<Statement> getPreconditions() {
        return preconditions;
    }

    @Override
    public Statement getConclusion() {
        return conclusion;
    }

}

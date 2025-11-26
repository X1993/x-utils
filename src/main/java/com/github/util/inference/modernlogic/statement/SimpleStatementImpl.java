package com.github.util.inference.modernlogic.statement;

import java.util.Collections;
import java.util.List;

/**
 * 简单陈述
 * @author wangjj7
 * @date 2025/11/24
 * @description
 */
public class SimpleStatementImpl implements SimpleStatement {

    private final String description;

    //陈述真值
    private boolean isTrue;

    public SimpleStatementImpl(String description) {
        this.description = description;
    }

    @Override
    public List<SimpleStatement> getSimpleStatements() {
        return Collections.singletonList(this);
    }

    @Override
    public boolean isTrue() {
        return isTrue;
    }

    @Override
    public String formatDescription() {
        return description;
    }

    @Override
    public void setIsTrue(boolean isTrue) {
        this.isTrue = isTrue;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return formatDescription();
    }

}

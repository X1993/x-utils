package com.github.util.inference.modernlogic.statement;

/**
 * 否定陈述
 * @author wangjj7
 * @date 2025/11/26
 * @description
 */
public interface NegationStatement extends CombinationStatement{

    /**
     * 获取被否定的陈述
     * @return
     */
    Statement getDeniedStatement();

}

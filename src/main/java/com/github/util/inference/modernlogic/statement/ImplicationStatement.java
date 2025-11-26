package com.github.util.inference.modernlogic.statement;

/**
 * 蕴含陈述（如果...那么...）
 * @author wangjj7
 * @date 2025/11/26
 * @description
 */
public interface ImplicationStatement extends CombinationStatement{

    /**
     * 获取前件
     * @return
     */
    Statement getBeforeStatement();

    /**
     * 获取后件
     * @return
     */
    Statement getAfterStatement();

}

package com.github.util.inference.modernlogic.statement;

/**
 * 简单陈述
 * @author wangjj7
 * @date 2025/11/26
 * @description
 */
public interface SimpleStatement extends Statement{

    /**
     * 设置陈述真值
     * @param isTrue
     */
    void setIsTrue(boolean isTrue);

    /**
     * 获取陈述描述
     * @return
     */
    String getDescription();

}

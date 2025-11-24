package com.github.util.inference.syllogism.rule;

import com.github.util.inference.syllogism.entity.StandardOutspokenSyllogism;

/**
 * 标准直言三段论校验规则
 * @author wangjj7
 * @date 2025/11/12
 * @description
 */
public interface SyllogismValidateRule {

    /**
     * 校验内容
     * @return
     */
    String content();

    /**
     * 是否合归
     * @param standardOutspokenSyllogism
     * @return
     */
    boolean validate(StandardOutspokenSyllogism standardOutspokenSyllogism);

}

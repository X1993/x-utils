package com.github.util.inference.syllogism.rule;

import com.github.util.inference.syllogism.StandardOutspokenSyllogism;

/**
 * @author wangjj7
 * @date 2025/11/12
 * @description
 */
public interface SyllogismValidRule {

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

package com.github.util.inference.syllogism.rule;

import com.github.util.inference.syllogism.StandardOutspokenSyllogism;

/**
 * 两个前提不可以都是否定命题
 * @author wangjj7
 * @date 2025/11/12
 * @description
 */
public class NotHaveTwoNegativePremiseRule implements SyllogismValidateRule {

    @Override
    public String content() {
        return "两个前提不可以都是否定命题";
    }

    @Override
    public boolean validate(StandardOutspokenSyllogism standardOutspokenSyllogism) {
        return standardOutspokenSyllogism.getMajorPremise().isDefinitely()
                || standardOutspokenSyllogism.getMinorPremise().isDefinitely();
    }
}

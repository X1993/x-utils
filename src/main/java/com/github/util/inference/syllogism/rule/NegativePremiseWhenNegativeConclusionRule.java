package com.github.util.inference.syllogism.rule;

import com.github.util.inference.syllogism.entity.StandardOutspokenSyllogism;

/**
 * 结论是否定命题则前提中有且仅有一个否定命题
 * @author wangjj7
 * @date 2025/11/12
 * @description
 */
public class NegativePremiseWhenNegativeConclusionRule implements SyllogismValidateRule {

    @Override
    public String content() {
        return "结论是否定命题则前提中有且仅有一个否定命题";
    }

    @Override
    public boolean validate(StandardOutspokenSyllogism standardOutspokenSyllogism) {
        boolean conclusionIsDefinitely = standardOutspokenSyllogism.getConclusion().isDefinitely();
        if (conclusionIsDefinitely){
            return standardOutspokenSyllogism.getMajorPremise().isDefinitely()
                    && standardOutspokenSyllogism.getMinorPremise().isDefinitely();
        }else {
            return !standardOutspokenSyllogism.getMajorPremise().isDefinitely()
                    || !standardOutspokenSyllogism.getMinorPremise().isDefinitely();
        }
    }
}

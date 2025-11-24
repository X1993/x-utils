package com.github.util.inference.syllogism.rule;

import com.github.util.inference.syllogism.entity.StandardOutspokenSyllogism;

/**
 * 结论是特称命题前提不能都是全称命题（只适用布尔解释）
 * @author wangjj7
 * @date 2025/11/12
 * @description
 */
public class NoUniversal2SpecialRule implements SyllogismValidateRule {

    @Override
    public String content() {
        return "结论是特称命题前提不能都是全称命题";
    }

    @Override
    public boolean validate(StandardOutspokenSyllogism standardOutspokenSyllogism) {
        if (!standardOutspokenSyllogism.getConclusion().isUniversal()){
            return !standardOutspokenSyllogism.getMajorPremise().isUniversal()
                    || !standardOutspokenSyllogism.getMinorPremise().isUniversal();
        }
        return true;
    }

}

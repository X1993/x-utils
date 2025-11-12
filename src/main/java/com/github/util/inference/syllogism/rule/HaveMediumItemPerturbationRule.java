package com.github.util.inference.syllogism.rule;

import com.github.util.inference.syllogism.StandardOutspokenSyllogism;

/**
 * 标准直言三段论中项在前提中至少有一个周延
 * @author wangjj7
 * @date 2025/11/12
 * @description
 */
public class HaveMediumItemPerturbationRule implements SyllogismValidateRule {

    @Override
    public String content() {
        return "标准直言三段论中项在前提中至少有一个周延";
    }

    @Override
    public boolean validate(StandardOutspokenSyllogism standardOutspokenSyllogism) {
        return standardOutspokenSyllogism.isMiddlePremisePerturbation();
    }

}

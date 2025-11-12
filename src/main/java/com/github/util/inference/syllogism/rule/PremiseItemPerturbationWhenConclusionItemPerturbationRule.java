package com.github.util.inference.syllogism.rule;

import com.github.util.inference.syllogism.OutspokenProposition;
import com.github.util.inference.syllogism.StandardOutspokenSyllogism;

/**
 * 结论中周延的项在前提中也必须周延
 * @author wangjj7
 * @date 2025/11/12
 * @description
 */
public class PremiseItemPerturbationWhenConclusionItemPerturbationRule implements SyllogismValidRule{

    @Override
    public String content() {
        return "结论中周延的项在前提中也必须周延";
    }

    @Override
    public boolean validate(StandardOutspokenSyllogism standardOutspokenSyllogism) {
        OutspokenProposition conclusion = standardOutspokenSyllogism.getConclusion();
        if (conclusion.isPredicateItemPerturbation() && !standardOutspokenSyllogism.isMajorPremisePerturbation()){
            //结论中的大项周延且前提中的大项不周延
            return false;
        }
        if (conclusion.isPremiseItemPerturbation() && !standardOutspokenSyllogism.isMinorPremisePerturbation()){
            //结论中的小项周延且前提中的小项不周延
            return false;
        }
        return true;
    }

}

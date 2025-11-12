package com.github.util.inference.syllogism;

import com.github.util.inference.syllogism.rule.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wangjj7
 * @date 2025/11/12
 * @description
 */
public class SyllogismValidator {

    private final List<SyllogismValidRule> syllogismValidRules = new ArrayList<>();

    public SyllogismValidator()
    {
        syllogismValidRules.add(new HaveMediumItemPerturbationRule());
        syllogismValidRules.add(new PremiseItemPerturbationWhenConclusionItemPerturbationRule());
        syllogismValidRules.add(new NotHaveTwoNegativePremiseRule());
        syllogismValidRules.add(new NegativePremiseWhenNegativeConclusionRule());
        syllogismValidRules.add(new NoUniversal2SpecialRule());
    }

    public boolean validate(StandardOutspokenSyllogism standardOutspokenSyllogism){
        boolean result = true;
        for (SyllogismValidRule rule : syllogismValidRules) {
            boolean ruleResult = rule.validate(standardOutspokenSyllogism);
            result &= ruleResult;
            if (!ruleResult){
                System.out.println(rule.content());
            }
        }
        if (result){
            System.out.println("有效");
        }
        return result;
    }

    public boolean validate(OutspokenProposition premise1, OutspokenProposition premise2, OutspokenProposition conclusion)
    {
        StandardOutspokenSyllogism syllogism = StandardOutspokenSyllogism.create(premise1, premise2, conclusion);
        return validate(syllogism);
    }

}

package com.github.util.inference.syllogism;

import org.junit.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangjj7
 * @date 2025/11/12
 * @description
 */
public class SyllogismValidatorTest {

    SyllogismValidator syllogismValidator = new SyllogismValidator();

    @Test
    public void printAllValidSyllogism()
    {
        String largeItemValue = "A";
        String mediumItemValue = "B";
        String smallItemValue = "C";
        Map<SyllogismConstant.Item, String> itemMap = new HashMap<>();
        itemMap.put(SyllogismConstant.Item.LARGE ,largeItemValue);
        itemMap.put(SyllogismConstant.Item.MEDIUM ,mediumItemValue);
        itemMap.put(SyllogismConstant.Item.SMALL ,smallItemValue);

        List<StandardOutspokenSyllogism> validatedSyllogisms = new ArrayList<>();

        for (PropositionConstant.Type majorPremiseType : PropositionConstant.Type.values()) {
            for (PropositionConstant.Type minorPremiseType : PropositionConstant.Type.values()) {
                for (PropositionConstant.Type conclusionType : PropositionConstant.Type.values()) {
                    for (SyllogismConstant.Grid grid : SyllogismConstant.Grid.values()) {
                        OutspokenProposition majorPremise = new OutspokenProposition(majorPremiseType ,
                                itemMap.get(grid.getMajorPremiseMasterItem()) ,itemMap.get(grid.getMajorPremisePredicateItem()));
                        OutspokenProposition minorPremise = new OutspokenProposition(minorPremiseType ,
                                itemMap.get(grid.getMinorPremiseMasterItem()) ,itemMap.get(grid.getMinorPremisePredicateItem()));
                        OutspokenProposition conclusion = new OutspokenProposition(conclusionType ,
                                smallItemValue ,largeItemValue);

                        StandardOutspokenSyllogism syllogism = StandardOutspokenSyllogism.create(minorPremise, majorPremise, conclusion);
                        System.out.println("-----------------------校验" + syllogism.getFormName() + "----------------------");
                        if (syllogismValidator.validate(syllogism)){
                            validatedSyllogisms.add(syllogism);
                        }
                        System.out.println("-----------------------校验结束----------------------");
                    }
                }
            }
        }

        System.out.println("===============================================================");
        System.out.println("有效的三段论形式有" + validatedSyllogisms.size() + "个,如下:");

        for (StandardOutspokenSyllogism validatedSyllogism : validatedSyllogisms) {
            System.out.println(validatedSyllogism.getFormName());
        }
    }

    @Test
    public void demo(){
        OutspokenProposition majorPremise = new OutspokenProposition(PropositionConstant.Type.A ,
                "猫" ,"动物");
        OutspokenProposition minorPremise = new OutspokenProposition(PropositionConstant.Type.A ,
                "狗" ,"动物");
        OutspokenProposition conclusion = new OutspokenProposition(PropositionConstant.Type.A ,
                "猫" ,"狗");

        StandardOutspokenSyllogism syllogism = StandardOutspokenSyllogism.create(majorPremise, minorPremise, conclusion);
        System.out.println(syllogism.standardDescription());

        System.out.println(syllogismValidator.validate(syllogism) ? "有效" : "无效");
    }

    @Test
    public void demo1(){
        OutspokenProposition majorPremise = new OutspokenProposition(PropositionConstant.Type.A ,
                "黑妞" ,"狗");
        OutspokenProposition minorPremise = new OutspokenProposition(PropositionConstant.Type.A ,
                "狗" ,"动物");
        OutspokenProposition conclusion = new OutspokenProposition(PropositionConstant.Type.A ,
                "黑妞" ,"动物");

        StandardOutspokenSyllogism syllogism = StandardOutspokenSyllogism.create(majorPremise, minorPremise, conclusion);
        System.out.println(syllogism.standardDescription());

        System.out.println(syllogismValidator.validate(syllogism) ? "有效" : "无效");
    }

}
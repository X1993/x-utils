package com.github.util.inference.syllogism;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static com.github.util.inference.syllogism.SyllogismConstant.Item.*;

/**
 * 标准直言三段论
 * @author wangjj7
 * @date 2025/11/12
 * @description
 */
@Data
public class StandardOutspokenSyllogism {

    /**
     * 大前提
     */
    final OutspokenProposition majorPremise;

    /**
     * 小前提
     */
    final OutspokenProposition minorPremise;

    /**
     * 推论
     */
    final OutspokenProposition conclusion;

    /**
     * 大项
     */
    final String largeItem;

    /**
     * 中项
     */
    final String mediumItem;

    /**
     * 小项
     */
    final String smallItem;

    /**
     * 格
     */
    final SyllogismConstant.Grid grid;

    private StandardOutspokenSyllogism(OutspokenProposition majorPremise, OutspokenProposition minorPremise, OutspokenProposition conclusion, String largeItem, String mediumItem, String smallItem) {
        this.majorPremise = majorPremise;
        this.minorPremise = minorPremise;
        this.conclusion = conclusion;
        this.largeItem = largeItem;
        this.mediumItem = mediumItem;
        this.smallItem = smallItem;
        this.grid = SyllogismConstant.Grid.match(
                StringUtils.equals(largeItem ,majorPremise.getPremiseItem()) ? LARGE : MEDIUM ,
                StringUtils.equals(largeItem ,majorPremise.getPredicateItem()) ? LARGE : MEDIUM ,
                StringUtils.equals(smallItem ,minorPremise.getPremiseItem()) ? SMALL : MEDIUM ,
                StringUtils.equals(smallItem ,minorPremise.getPredicateItem()) ? SMALL : MEDIUM
        );
    }

    public static StandardOutspokenSyllogism create(OutspokenProposition outspokenProposition1, OutspokenProposition outspokenProposition2, OutspokenProposition conclusion)
    {
        //结论的谓项：大项
        String largeItem = conclusion.getPredicateItem();
        //结论的主项：小项
        String smallItem = conclusion.getPremiseItem();

        String mediumItem = outspokenProposition1.getPremiseItem();
        if (StringUtils.equals(mediumItem ,largeItem)){
            mediumItem = outspokenProposition1.getPredicateItem();
        }else if (StringUtils.equals(mediumItem, smallItem)){
            mediumItem = outspokenProposition1.getPredicateItem();
        }

        //大前提
        Set<String> largeItems = Stream.of(largeItem ,mediumItem).collect(Collectors.toSet());
        //小前提
        Set<String> smallItems = Stream.of(smallItem ,mediumItem).collect(Collectors.toSet());

        Set<String> proposition1Items = Stream.of(outspokenProposition1.getPremiseItem() ,
                outspokenProposition1.getPredicateItem()).collect(Collectors.toSet());
        Set<String> proposition2Items = Stream.of(outspokenProposition2.getPremiseItem() ,
                outspokenProposition2.getPredicateItem()).collect(Collectors.toSet());

        if (proposition1Items.equals(largeItems) && proposition2Items.equals(smallItems)){
            return new StandardOutspokenSyllogism(outspokenProposition1, outspokenProposition2, conclusion ,largeItem ,mediumItem ,smallItem);
        }else if (proposition1Items.equals(smallItems) && proposition2Items.equals(largeItems)){
            return new StandardOutspokenSyllogism(outspokenProposition2, outspokenProposition1, conclusion ,largeItem ,mediumItem ,smallItem);
        }

        throw new IllegalArgumentException("不是标准直言三段论");
    }

    /**
     * 大项在前提中是否周延
     */
    public boolean isMajorPremisePerturbation(){
        return majorPremise.isItemPerturbation(largeItem);
    }

    /**
     * 小项在前提中是否周延
     */
    public boolean isMinorPremisePerturbation(){
        return minorPremise.isItemPerturbation(smallItem);
    }

    /**
     * 是否至少有一个中项周延
     */
    public boolean isMiddlePremisePerturbation(){
        return majorPremise.isItemPerturbation(mediumItem) || minorPremise.isItemPerturbation(mediumItem);
    }



    /**
     * 获取三段论的式
     * @return
     */
    public String getFormula(){
        return majorPremise.getType().getSymbol() + minorPremise.getType().getSymbol() + conclusion.getType().getSymbol();
    }

    /**
     * 获取三段论的形式名称
     * @return
     */
    public String getFormName(){
        return getFormula() + "-" + grid.getSymbol();
    }

    /**
     * 标准化描述
     * <p>
     *     大前提;
     *     小前提;
     *     结论;
     * </p>
     * @return
     */
    public String standardDescription(){
        return majorPremise.description() + ";\n" + minorPremise.description() + ";\n" + conclusion.description();
    }

}

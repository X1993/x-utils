package com.github.util.inference.syllogism.entity;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 标准直言命题
 * @author wangjj7
 * @date 2025/11/12
 * @description
 */
@Data
public class OutspokenProposition {

    //命题类型
    final PropositionConstant.Type type;

    //主项
    final String premiseItem;

    //谓项
    final String predicateItem;

    public OutspokenProposition(PropositionConstant.Type type, String premiseItem, String predicateItem){
        if (type == null || premiseItem == null || predicateItem == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        this.type = type;
        this.premiseItem = premiseItem;
        this.predicateItem = predicateItem;
    }

    /**
     * 获取命题的量是否是全称（或特称）
     * @return
     */
    public boolean isUniversal() {
        return type.isUniversal();
    }

    /**
     * 获取命题的质肯定还是否定
     * @return
     */
    public boolean isDefinitely() {
        return type.isDefinitely();
    }

    /**
     * 主项是否周延
     * @return
     */
    public boolean isPremiseItemPerturbation() {
        return type.isPremiseItemPerturbation();
    }

    /**
     * 谓项是否周延
     * @return
     */
    public boolean isPredicateItemPerturbation() {
        return type.isPredicateItemPerturbation();
    }

    /**
     * 项是否周延
     * @param item
     * @return
     */
    public boolean isItemPerturbation(String item)
    {
        if (StringUtils.equals(item ,premiseItem)){
            return isPremiseItemPerturbation();
        }else if (StringUtils.equals(item ,predicateItem)){
            return isPredicateItemPerturbation();
        }
        throw new IllegalArgumentException("项不存在");
    }

    /**
     * 获取命题描述
     * @return
     */
    public String description(){
        return type.description(premiseItem ,predicateItem);
    }

}

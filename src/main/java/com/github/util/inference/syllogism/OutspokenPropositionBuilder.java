package com.github.util.inference.syllogism;

import com.github.util.inference.syllogism.entity.OutspokenProposition;
import com.github.util.inference.syllogism.entity.PropositionConstant;
import lombok.Data;

/**
 * 标准直言命题生成器
 * @author wangjj7
 * @date 2025/11/24
 * @description
 */
@Data
public class OutspokenPropositionBuilder {

    public static _A all(String premiseItem){
        return new _A(premiseItem);
    }

    public static _E none(String premiseItem){
        return new _E(premiseItem);
    }

    public static _I_O some(String premiseItem){
        return new _I_O(premiseItem);
    }

    public static class _A{

        final String premiseItem;

        _A(String premiseItem){
            this.premiseItem = premiseItem;
        }

        OutspokenProposition is(String predicateItem){
            return new OutspokenProposition(PropositionConstant.Type.A , premiseItem, predicateItem);
        }

    }

    public static class _E{

        final String premiseItem;

        _E(String premiseItem){
            this.premiseItem = premiseItem;
        }

        OutspokenProposition is(String predicateItem){
            return new OutspokenProposition(PropositionConstant.Type.E , premiseItem, predicateItem);
        }

    }

    public static class _I_O{

        final String premiseItem;

        _I_O(String premiseItem){
            this.premiseItem = premiseItem;
        }

        OutspokenProposition is(String predicateItem){
            return new OutspokenProposition(PropositionConstant.Type.I , premiseItem, predicateItem);
        }

        OutspokenProposition notIs(String predicateItem){
            return new OutspokenProposition(PropositionConstant.Type.O , premiseItem, predicateItem);
        }

    }

}

package com.github.util.inference.syllogism;

/**
 * 命题定义
 * @author wangjj7
 * @date 2025/11/12
 * @description
 */
public interface PropositionConstant {

    /**
     * 命题类型
     */
    enum Type
    {
        A("全称肯定命题" ,true ,true ,true ,false){
            @Override
            String description(String premiseItem, String predicateItem) {
                return "所有" + premiseItem + "都是" + predicateItem;
            }
        },
        E("全称否定命题" ,true ,false ,true ,true){
            @Override
            String description(String premiseItem, String predicateItem) {
                return "没有" + premiseItem + "是" + predicateItem;
            }
        },
        I("特称肯定命题" ,false ,true ,false ,false) {
            @Override
            String description(String premiseItem, String predicateItem) {
                return "有" + predicateItem + "是" + premiseItem;
            }
        },
        O("特称否定命题" ,false ,false ,false ,true) {
            @Override
            String description(String premiseItem, String predicateItem) {
                return "有" + predicateItem + "不是" + premiseItem;
            }
        },
        ;

        private final String typeName;

        //命题的量是否是全称（或特称）
        private final boolean universal;

        //命题的质肯定还是否定
        private final boolean definitely;

        //主项是否周延
        private final boolean premiseItemPerturbation;

        //谓项是否周延
        private final boolean predicateItemPerturbation;

        Type(String typeName, boolean universal, boolean definitely , boolean premiseItemPerturbation, boolean predicateItemPerturbation) {
            this.typeName = typeName;
            this.universal = universal;
            this.definitely = definitely;
            this.premiseItemPerturbation = premiseItemPerturbation;
            this.predicateItemPerturbation = predicateItemPerturbation;
        }

        /**
         * 完整描述
         * @param premiseItem 主项
         * @param predicateItem 谓项
         * @return
         */
        abstract String description(String premiseItem , String predicateItem);

        public String getTypeName(){
            return typeName;
        }

        public boolean isUniversal() {
            return universal;
        }

        public boolean isDefinitely() {
            return definitely;
        }

        public boolean isPremiseItemPerturbation() {
            return premiseItemPerturbation;
        }

        public boolean isPredicateItemPerturbation() {
            return predicateItemPerturbation;
        }

        public String getSymbol(){
            return name();
        }

    }



}

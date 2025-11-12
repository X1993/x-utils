package com.github.util.inference.syllogism;

/**
 * @author wangjj7
 * @date 2025/11/12
 * @description
 */
public interface SyllogismConstant {

    /**
     * 项
     */
    enum Item{

        LARGE("大项"),
        MEDIUM("中项"),
        SMALL("小项"),
        ;

        private final String name;

        Item(String name){
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * 格
     */
    enum Grid{

        ONE("1" ,Item.MEDIUM ,Item.LARGE ,Item.SMALL ,Item.MEDIUM),
        TWO("2" ,Item.LARGE ,Item.MEDIUM ,Item.SMALL ,Item.MEDIUM),
        THREE("3" ,Item.MEDIUM ,Item.LARGE ,Item.MEDIUM ,Item.SMALL),
        FOUR("4" ,Item.LARGE ,Item.MEDIUM ,Item.MEDIUM ,Item.SMALL),
        ;

        private final String symbol;

        //大前提主项
        private final Item majorPremiseMasterItem;

        //大前提谓项
        private final Item majorPremisePredicateItem;

        //小前提主项
        private final Item minorPremiseMasterItem;

        //小前提谓项
        private final Item minorPremisePredicateItem;

        Grid(String symbol, Item majorPremiseMasterItem, Item majorPremisePredicateItem, Item minorPremiseMasterItem, Item minorPremisePredicateItem) {
            this.symbol = symbol;
            this.majorPremiseMasterItem = majorPremiseMasterItem;
            this.majorPremisePredicateItem = majorPremisePredicateItem;
            this.minorPremiseMasterItem = minorPremiseMasterItem;
            this.minorPremisePredicateItem = minorPremisePredicateItem;
        }

        public static Grid match(Item majorPremiseMasterItem, Item majorPremisePredicateItem,
                                 Item minorPremiseMasterItem, Item minorPremisePredicateItem)
        {
            for (Grid grid : Grid.values()) {
                if (grid.getMajorPremiseMasterItem().equals(majorPremiseMasterItem)
                        && grid.getMajorPremisePredicateItem().equals(majorPremisePredicateItem)
                        && grid.getMinorPremiseMasterItem().equals(minorPremiseMasterItem)
                        && grid.getMinorPremisePredicateItem().equals(minorPremisePredicateItem)){
                    return grid;
                }
            }
            throw new IllegalArgumentException("非法的格");
        }

        public String getSymbol() {
            return symbol;
        }

        public Item getMajorPremiseMasterItem() {
            return majorPremiseMasterItem;
        }

        public Item getMajorPremisePredicateItem() {
            return majorPremisePredicateItem;
        }

        public Item getMinorPremiseMasterItem() {
            return minorPremiseMasterItem;
        }

        public Item getMinorPremisePredicateItem() {
            return minorPremisePredicateItem;
        }

    }

}

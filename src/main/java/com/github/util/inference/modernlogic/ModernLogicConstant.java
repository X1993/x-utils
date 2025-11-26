package com.github.util.inference.modernlogic;

/**
 * @author wangjj7
 * @date 2025/11/24
 * @description
 */
public interface ModernLogicConstant {

    /**
     * 逻辑连接符
     */
    enum LogicSymbol {

        AND("." ,"并且"),
        OR("∨" ,"或者"),
        NOT("~" ,"否定"),
        IF_THEN("⊃" ,"蕴含"),
        IFF("≡" ,"等值")
        ;

        private final String symbol;

        private final String name;

        LogicSymbol(String symbol ,String name) {
            this.symbol = symbol;
            this.name = name;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getName() {
            return name;
        }

    }

    enum Value {

        TRUE("T" ,"真"),
        FALSE("F" ,"假")
        ;

        private final String symbol;
        private final String name;

        Value(String symbol ,String name) {
            this.symbol = symbol;
            this.name = name;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getName() {
            return name;
        }

    }

}

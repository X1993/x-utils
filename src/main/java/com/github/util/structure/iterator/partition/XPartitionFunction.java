package com.github.util.structure.iterator.partition;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.List;

/**
 * @author X1993
 * @date 2023/4/27
 * @description
 */
@FunctionalInterface
public interface XPartitionFunction<T ,P> {

    /**
     * 查询当前分区数据
     * @param input 参数
     * @return 不允许为null
     */
    Output<T ,P> select(Input<T ,P> input);

    @Data
    @Accessors(chain = true)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    class Input<T ,P>{

        /**
         * 上一个分区查询结果
         */
        List<T> prePartition = Collections.EMPTY_LIST;

        /**
         * 当前分区查询参数
         */
        P currentParam;

        public Input(){

        }

        public Input(Output<T ,P> preOutput){
            prePartition = preOutput.partition;
            currentParam = preOutput.nextParam;
        }

    }

    @Data
    @Accessors(chain = true)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    class Output<T ,P>{

        /**
         * 分区查询结果
         */
        List<T> partition = Collections.EMPTY_LIST;

        /**
         * 是否有下一个分区
         */
        boolean hasNext = true;

        /**
         * 下一个分区查询参数
         */
        P nextParam;

    }

}

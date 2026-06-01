package com.github.util.structure.iterator.partition.fixed;

/**
 * 分片参数
 * @author wangjj7
 * @date 2023/8/9
 * @description
 * @param <V> 分片的维度（单位）
 */
public interface XFixedRatePartitionParam<V ,CHILD extends XFixedRatePartitionParam<V ,CHILD>> {

    /**
     * 获取开始边界
     * @return
     */
    V $readStartBorder();

    /**
     * 设置结束边界
     * @return
     */
    V $readEndBorder();

    /**
     * 获取分区参数
     * @return
     */
    CHILD $partitionParam(V partitionStartV, V partitionEndV);

    /**
     * 复制
     * @return
     */
    default CHILD copy(){
        return $partitionParam($readStartBorder() , $readEndBorder());
    }

}

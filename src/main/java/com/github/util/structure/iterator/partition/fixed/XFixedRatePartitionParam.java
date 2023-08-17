package com.github.util.structure.iterator.partition.fixed;

/**
 * 按时间分区的参数
 * @author wangjj7
 * @date 2023/8/9
 * @description
 * @param <V> 分片的维度（单位）
 */
public interface XFixedRatePartitionParam<V ,CHILD extends XFixedRatePartitionParam<V ,CHILD>> {

    /**
     * 获取开始时间
     * @return
     */
    V $readStartTime();

    /**
     * 设置结束时间
     * @return
     */
    V $readEndTime();

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
        return $partitionParam($readStartTime() ,$readEndTime());
    }

}

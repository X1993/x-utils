package com.github.util.structure.iterator.partition.time;

import java.time.LocalDateTime;

/**
 * 按时间分区的参数
 * @author wangjj7
 * @date 2023/8/9
 * @description
 */
public interface TimePartitionParam<CHILD extends TimePartitionParam<CHILD>> {

    /**
     * 获取开始时间
     * @return
     */
    LocalDateTime $readStartTime();

    /**
     * 设置结束时间
     * @return
     */
    LocalDateTime $readEndTime();

    /**
     * 获取分区参数
     * @return
     */
    CHILD $partitionParam(LocalDateTime partitionStartTime ,LocalDateTime partitionEndTime);

}

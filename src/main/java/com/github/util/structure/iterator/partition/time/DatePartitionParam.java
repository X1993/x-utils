package com.github.util.structure.iterator.partition.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 按时间分区的参数
 * @author wangjj7
 * @date 2023/8/9
 * @description
 */
public interface DatePartitionParam<CHILD extends DatePartitionParam<CHILD>> extends TimePartitionParam<CHILD> {

    /**
     * 获取开始日期
     * @return
     */
    LocalDate $readStartDate();

    /**
     * 设置结束日期
     * @return
     */
    LocalDate $readEndDate();

    /**
     * 获取分区参数
     * @return
     */
    CHILD $partitionParam(LocalDate partitionStartDate ,LocalDate partitionEndDate);

    @Override
    default LocalDateTime $readStartTime(){
        return $readEndDate().atTime(LocalTime.MIN);
    }

    @Override
    default LocalDateTime $readEndTime(){
        return $readEndDate().atTime(LocalTime.MIN);
    }

    @Override
    default CHILD $partitionParam(LocalDateTime partitionStartTime, LocalDateTime partitionEndTime){
        return $partitionParam(partitionStartTime.toLocalDate() ,partitionEndTime.toLocalDate());
    }
}

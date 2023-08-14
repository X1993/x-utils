package com.github.util.structure.iterator.partition.time;

import com.github.util.structure.iterator.partition.XPartitionFunction;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 按时间分区的实现函数
 * @author wangjj7
 * @date 2023/8/9
 * @description
 */
public class XTimePartitionFunction<T ,P extends TimePartitionParam<P>> implements XPartitionFunction<T ,P> {

    /**
     * 完整参数，包含整个时间区间
     */
    private final P sourceParam;

    /**
     * 分区时长
     */
    private final int partitionSize;

    /**
     * 分区时长单位
     */
    private final TemporalUnit intervalUnit;

    /**
     * 分区查询函数
     */
    private final Function<P ,List<T>> queryFunction;

    /**
     * true:升序加载
     * false:降序加载
     */
    private final boolean asc;


    public XTimePartitionFunction(P sourceParam,
                                  boolean asc,
                                  int partitionSize,
                                  TemporalUnit intervalUnit,
                                  Function<P, List<T>> queryFunction)
    {
        if (sourceParam == null || sourceParam.$readStartTime() == null || sourceParam.$readEndTime() == null
                || partitionSize < 1 || intervalUnit == null || queryFunction == null) {
            throw new IllegalArgumentException();
        }
        this.asc = asc;
        this.sourceParam = sourceParam;
        this.partitionSize = partitionSize;
        this.intervalUnit = intervalUnit;
        this.queryFunction = queryFunction;
    }

    @Override
    public Output<T, P> select(Input<T, P> input)
    {
        P currentParam = input.getCurrentParam();
        if (currentParam == null){
            throw new IllegalStateException();
        }

        if (currentParam.$readStartTime().compareTo(currentParam.$readEndTime()) >= 0)
        {
            return new XPartitionFunction.Output<T ,P>()
                    .setPartition(Collections.EMPTY_LIST)
                    .setHasNext(false);
        }

        List<T> resultList = queryFunction.apply(currentParam);
        P nextPartitionParam = nextParam(currentParam);

        return new XPartitionFunction.Output<T ,P>()
                .setPartition(resultList)
                .setHasNext(nextPartitionParam != null)
                .setNextParam(nextPartitionParam);
    }

    @Override
    public P firstInputParam()
    {
        LocalDateTime partitionStartTime = null;
        LocalDateTime partitionEndTime = null;
        if (asc) {
            partitionStartTime = sourceParam.$readStartTime();
            partitionEndTime = partitionStartTime.plus(partitionSize, intervalUnit);
            partitionEndTime = partitionEndTime.isBefore(sourceParam.$readEndTime()) ?
                    partitionEndTime : sourceParam.$readEndTime();
        }else {
            partitionEndTime = sourceParam.$readEndTime();
            partitionStartTime = partitionEndTime.plus(-partitionSize, intervalUnit);
            partitionStartTime = partitionStartTime.isAfter(sourceParam.$readStartTime()) ?
                    partitionStartTime : sourceParam.$readStartTime();
        }
        return sourceParam.$partitionParam(partitionStartTime ,partitionEndTime);
    }

    private P nextParam(P param0)
    {
        if (!hasNextPartition(param0)){
            return null;
        }

        LocalDateTime partitionStartTime = null;
        LocalDateTime partitionEndTime = null;
        if (asc) {
            partitionStartTime = param0.$readEndTime();
            partitionEndTime = partitionStartTime.plus(partitionSize, intervalUnit);
            partitionEndTime = partitionEndTime.isBefore(sourceParam.$readEndTime()) ?
                    partitionEndTime : sourceParam.$readEndTime();
        }else {
            partitionEndTime = param0.$readStartTime();
            partitionStartTime = partitionEndTime.plus(-partitionSize, intervalUnit);
            partitionStartTime = partitionStartTime.isAfter(sourceParam.$readStartTime()) ?
                    partitionStartTime : sourceParam.$readStartTime();
        }
        return sourceParam.$partitionParam(partitionStartTime ,partitionEndTime);
    }

    private boolean hasNextPartition(P param0){
        if (asc){
            return param0.$readEndTime().isBefore(sourceParam.$readEndTime());
        }else {
            return param0.$readStartTime().isAfter(sourceParam.$readStartTime());
        }
    }

}

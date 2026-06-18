package com.github.util.structure.iterator.partition.function;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * @author wangjj7
 * @date 2026/6/4
 */
public class XPartitionFunctionUtils {

    public static <T ,P extends XPartitionParam<LocalDate, P>> XFixedRatePartitionFunction<T ,LocalDate, P> toFixedLocalDateRatePartitionFunction(
            P sourceParam, int partitionSize, ChronoUnit chronoUnit, Function<P, List<T>> queryFunction)
    {
        return new XFixedRatePartitionFunction<>(sourceParam, partitionSize, queryFunction,
                (ld ,i) -> ld.plus(i ,chronoUnit), (ld0 ,ld1) -> ld0.compareTo(ld1));
    }

    public static <T ,P extends XPartitionParam<LocalDateTime, P>> XFixedRatePartitionFunction<T ,LocalDateTime, P> toFixedLocalDateTimeRatePartitionFunction(
            P sourceParam, int partitionSize ,ChronoUnit chronoUnit ,Function<P, List<T>> queryFunction)
    {
        return new XFixedRatePartitionFunction<>(sourceParam, partitionSize, queryFunction,
                (ld ,i) -> ld.plus(i ,chronoUnit), (ld0 ,ld1) -> ld0.compareTo(ld1));
    }

    public static <T ,P extends XPartitionParam<Date, P>> XFixedRatePartitionFunction<T ,Date, P> toFixedDateRatePartitionFunction(
            P sourceParam, int partitionSize ,ChronoUnit chronoUnit, Function<P, List<T>> queryFunction)
    {
        return new XFixedRatePartitionFunction<>(sourceParam, partitionSize, queryFunction,
                (date ,i) -> new Date(date.getTime() + i * chronoUnit.getDuration().toMillis()), (date0 ,date1) -> date0.compareTo(date1));
    }

    public static <T ,P extends XPartitionParam<LocalDate, P>> XOrderPartitionFunction<T ,LocalDate, P> toLocalDateOrderPartitionFunction(
            P sourceParam, Function<T ,LocalDate> borderFunction ,ChronoUnit chronoUnit, Function<P, List<T>> queryFunction ,int maxElementCnt)
    {
        return new XOrderPartitionFunction<>(sourceParam, borderFunction, queryFunction, maxElementCnt ,
                (ld ,i) -> ld.plus(i ,chronoUnit), (ld0 ,ld1) -> ld0.compareTo(ld1));
    }

    public static <T ,P extends XPartitionParam<LocalDateTime, P>> XOrderPartitionFunction<T ,LocalDateTime, P> toLocalDateTimeOrderPartitionFunction(
            P sourceParam, Function<T ,LocalDateTime> borderFunction ,ChronoUnit chronoUnit, Function<P, List<T>> queryFunction ,int maxElementCnt)
    {
        return new XOrderPartitionFunction<>(sourceParam, borderFunction, queryFunction, maxElementCnt ,
                (ld ,i) -> ld.plus(i ,chronoUnit), (ld0 ,ld1) -> ld0.compareTo(ld1));
    }

}

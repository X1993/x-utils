package com.github.util.structure.iterator.partition.fixed;

import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.function.Function;

/**
 * @author wangjj7
 * @date 2023/8/14
 * @description
 */
public class LocalDateTimePartitionFunction<T ,P extends XFixedRatePartitionParam<LocalDateTime, P>>
        extends XFixedRatePartitionFunction<T, LocalDateTime, P> {

    private final TemporalUnit unit;

    public LocalDateTimePartitionFunction(P sourceParam, int partitionSize,
                                          TemporalUnit unit, Function<P, List<T>> queryFunction)
    {
        super(sourceParam, partitionSize, queryFunction);
        if (unit == null){
            throw new IllegalArgumentException();
        }
        this.unit = unit;
    }

    /**
     *
     * @param v
     * @param amountToAdd
     * @return
     */
    @Override
    public LocalDateTime plus(LocalDateTime v ,long amountToAdd){
        return v.plus(amountToAdd ,unit);
    }

    /**
     *
     * @param v0
     * @param v1
     * @return
     */
    @Override
    public int compare(LocalDateTime v0 ,LocalDateTime v1){
        return v0.compareTo(v1);
    }

}

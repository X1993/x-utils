package com.github.util.structure.iterator.partition.fixed;

import java.time.LocalDate;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.function.Function;

/**
 * @author wangjj7
 * @date 2023/8/14
 * @description
 */
public class LocalDatePartitionFunction<T ,P extends XFixedRatePartitionParam<LocalDate, P>>
        extends XFixedRatePartitionFunction<T, LocalDate, P> {

    private final TemporalUnit unit;

    public LocalDatePartitionFunction(P sourceParam, int partitionSize,
                                      TemporalUnit unit, Function<P, List<T>> queryFunction) {
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
    public LocalDate plus(LocalDate v ,long amountToAdd){
        return v.plus(amountToAdd ,unit);
    }

    /**
     *
     * @param v0
     * @param v1
     * @return
     */
    @Override
    public int compare(LocalDate v0 ,LocalDate v1){
        return v0.compareTo(v1);
    }

}

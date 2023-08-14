package com.github.util.structure.iterator.partition.fixed;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author wangjj7
 * @date 2023/8/14
 * @description
 */
public class DatePartitionFunction<T ,P extends XFixedRatePartitionParam<Date, P>>
        extends XFixedRatePartitionFunction<T, Date, P> {

    private final TimeUnit timeUnit;

    public DatePartitionFunction(P sourceParam, int partitionSize ,TimeUnit timeUnit, Function<P, List<T>> queryFunction)
    {
        super(sourceParam, partitionSize, queryFunction);
        this.timeUnit = timeUnit;
    }

    @Override
    public Date plus(Date v ,long amountToAdd){
        return new Date(v.getTime() + timeUnit.toMillis(amountToAdd));
    }

    @Override
    public int compare(Date v0 ,Date v1){
        return v0.compareTo(v1);
    }

}

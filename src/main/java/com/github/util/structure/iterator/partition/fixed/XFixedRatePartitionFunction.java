package com.github.util.structure.iterator.partition.fixed;

import com.github.util.structure.iterator.partition.XPartitionFunction;
import lombok.Data;
import lombok.experimental.Accessors;
import java.util.List;
import java.util.function.Function;

/**
 * 以指定的大小将参数切分成多个分片参数后执行查询函数
 * @author wangjj7
 * @date 2023/8/9
 * @description
 * @param <P> 分片查询参数的类型
 * @param <T> 分片查询的数据类型
 * @param <V> 分片的大小的单位
 */
@Data
@Accessors(chain = true)
public abstract class XFixedRatePartitionFunction<T ,V ,P extends XFixedRatePartitionParam<V, P>>
        implements XPartitionFunction<T ,P> {

    /**
     * 完整参数，指定分片的边界
     */
    private final P sourceParam;

    /**
     * 分片大小
     */
    private final long partitionSize;

    /**
     * 分片查询函数
     */
    private final Function<P ,List<T>> queryFunction;

    /**
     * 分片间隔
     */
    private long partitionInterval = 0;

    /**
     * true:升序加载
     * false:降序加载
     */
    private boolean asc = true;


    public XFixedRatePartitionFunction(P sourceParam, int partitionSize, Function<P, List<T>> queryFunction)
    {
        if (sourceParam == null || sourceParam.$readStartTime() == null || sourceParam.$readEndTime() == null
                || partitionSize < 1 || queryFunction == null) {
            throw new IllegalArgumentException();
        }
        this.sourceParam = sourceParam;
        this.partitionSize = partitionSize;
        this.queryFunction = queryFunction;
    }

    /**
     * 维度增加若干个单位
     * @param v
     * @param amountToAdd
     * @return
     */
    abstract V plus(V v ,long amountToAdd);

    /**
     * 比较两个维度的大小
     * @param v0
     * @param v1
     * @return
     */
    abstract int compare(V v0 ,V v1);

    @Override
    public Output<T, P> select(Input<T, P> input)
    {
        P currentParam = input.getCurrentParam();
        if (currentParam == null){
            throw new IllegalStateException();
        }

        if (compare(currentParam.$readStartTime() ,currentParam.$readEndTime()) > 0)
        {
            throw new IllegalStateException("分片开始时间不能大于结束时间");
        }

        //复制一下避免查询的时候修改了原参数影响后续参数的创建
        P inputParam = currentParam.copy();
        if (inputParam == null){
            throw new IllegalStateException("copy实现异常");
        }
        List<T> resultList = queryFunction.apply(inputParam);

        return new XPartitionFunction.Output<T ,P>()
                .setPartition(resultList)
                .setHasNext(hasNextPartition(currentParam))
                .setNextParam(nextParam(currentParam));
    }

    @Override
    public P firstInputParam()
    {
        V partitionStart = null;
        V partitionEnd = null;
        if (asc) {
            partitionStart = sourceParam.$readStartTime();
            partitionEnd = plus(partitionStart ,partitionSize);
            partitionEnd = compare(partitionEnd ,sourceParam.$readEndTime()) < 0 ?
                    partitionEnd : sourceParam.$readEndTime();
        }else {
            partitionEnd = sourceParam.$readEndTime();
            partitionStart = plus(partitionEnd ,-partitionSize);
            partitionStart = compare(partitionStart ,sourceParam.$readStartTime()) > 0 ?
                    partitionStart : sourceParam.$readStartTime();
        }
        return sourceParam.$partitionParam(partitionStart ,partitionEnd);
    }

    //根据当分片参数获取下一个分片参数
    private P nextParam(P param0)
    {
        if (!hasNextPartition(param0)){
            return null;
        }

        V partitionStart = null;
        V partitionEnd = null;
        if (asc) {
            partitionStart = plus(param0.$readEndTime() ,partitionInterval);
            partitionStart = compare(partitionStart ,sourceParam.$readEndTime()) < 0 ?
                    partitionStart : sourceParam.$readEndTime();
            partitionEnd = plus(partitionStart ,partitionSize);
            partitionEnd = compare(partitionEnd ,sourceParam.$readEndTime()) < 0 ?
                    partitionEnd : sourceParam.$readEndTime();
        }else {
            partitionEnd = plus(param0.$readStartTime() ,-partitionInterval);
            partitionEnd = compare(partitionEnd ,sourceParam.$readStartTime()) > 0 ?
                    partitionEnd : sourceParam.$readStartTime();
            partitionStart = plus(partitionEnd ,-partitionSize);
            partitionStart = compare(partitionStart ,sourceParam.$readStartTime()) > 0 ?
                    partitionStart : sourceParam.$readStartTime();
        }
        return sourceParam.$partitionParam(partitionStart ,partitionEnd);
    }

    //是否有下一个分片
    private boolean hasNextPartition(P param0){
        if (asc){
            return compare(param0.$readEndTime() ,sourceParam.$readEndTime()) < 0;
        }else {
            return compare(param0.$readStartTime() ,sourceParam.$readStartTime()) > 0;
        }
    }

}

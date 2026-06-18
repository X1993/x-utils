package com.github.util.structure.iterator.partition.function;

import com.github.util.structure.iterator.partition.XPartitionFunction;
import lombok.Data;
import lombok.experimental.Accessors;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 以指定的大小将参数切分成多个分片参数后执行查询函数
 * @author wangjj7
 * @date 2023/8/9
 * @description
 * @param <P> 分片查询参数的类型
 * @param <T> 分片查询的数据类型
 * @param <P_BORDER> 分片的大小的单位
 */
@Data
@Accessors(chain = true)
public class XFixedRatePartitionFunction<T , P_BORDER, P extends XPartitionParam<P_BORDER, P>>
        implements XPartitionFunction<T ,P> {

    /**
     * 完整参数，指定数据的总查询范围
     */
    private final P sourceParam;

    /**
     * 分片大小
     */
    private final int partitionSize;

    /**
     * 分片查询函数
     */
    private final Function<P ,List<T>> queryFunction;

    /**
     * 分片大小增加函数
     */
    private final BiFunction<P_BORDER ,Integer ,P_BORDER> plusFunction;

    /**
     * 分片大小比较函数
     */
    private final Comparator<P_BORDER> comparator;

    /**
     * 分片间隔
     * 设计目的：有些接口是按维度范围[startBorder, endBorder]查询的且包含两端，如果不设置间隔边界值会重复查询
     */
    private int partitionInterval = 0;

    /**
     * true:升序加载
     * false:降序加载
     */
    private boolean asc = true;

    public XFixedRatePartitionFunction(P sourceParam, int partitionSize,
                                       Function<P, List<T>> queryFunction ,
                                       BiFunction<P_BORDER ,Integer ,P_BORDER> plusFunction,
                                       Comparator<P_BORDER> comparator)
    {
        if (sourceParam == null || sourceParam.$readStartBorder() == null || sourceParam.$readEndBorder() == null
                || partitionSize < 1 || queryFunction == null || plusFunction == null || comparator == null) {
            throw new IllegalArgumentException();
        }
        this.sourceParam = sourceParam;
        this.partitionSize = partitionSize;
        this.queryFunction = queryFunction;
        this.plusFunction = plusFunction;
        this.comparator = comparator;
    }

    @Override
    public Output<T, P> select(Input<T, P> input)
    {
        P currentParam = input.getCurrentParam();
        if (currentParam == null){
            throw new IllegalStateException();
        }

        if (comparator.compare(currentParam.$readStartBorder() ,currentParam.$readEndBorder()) > 0)
        {
            throw new IllegalStateException("分片开始时间不能大于结束时间");
        }

        //复制一下避免查询的时候修改了原参数影响后续参数的创建
        P inputParam = currentParam.copy();
        if (inputParam == null){
            throw new IllegalStateException("参数复制异常");
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
        P_BORDER partitionStart = null;
        P_BORDER partitionEnd = null;
        if (asc) {
            partitionStart = sourceParam.$readStartBorder();
            partitionEnd = plusFunction.apply(partitionStart ,partitionSize);
            partitionEnd = comparator.compare(partitionEnd ,sourceParam.$readEndBorder()) < 0 ?
                    partitionEnd : sourceParam.$readEndBorder();
        }else {
            partitionEnd = sourceParam.$readEndBorder();
            partitionStart = plusFunction.apply(partitionEnd ,-partitionSize);
            partitionStart = comparator.compare(partitionStart ,sourceParam.$readStartBorder()) > 0 ?
                    partitionStart : sourceParam.$readStartBorder();
        }
        return sourceParam.$partitionParam(partitionStart ,partitionEnd);
    }

    //根据当分片参数获取下一个分片参数
    private P nextParam(P param0)
    {
        if (!hasNextPartition(param0)){
            return null;
        }

        P_BORDER partitionStart = null;
        P_BORDER partitionEnd = null;
        if (asc) {
            partitionStart = plusFunction.apply(param0.$readEndBorder() ,partitionInterval);
            partitionStart = comparator.compare(partitionStart ,sourceParam.$readEndBorder()) < 0 ?
                    partitionStart : sourceParam.$readEndBorder();
            partitionEnd = plusFunction.apply(partitionStart ,partitionSize);
            partitionEnd = comparator.compare(partitionEnd ,sourceParam.$readEndBorder()) < 0 ?
                    partitionEnd : sourceParam.$readEndBorder();
        }else {
            partitionEnd = plusFunction.apply(param0.$readStartBorder() ,-partitionInterval);
            partitionEnd = comparator.compare(partitionEnd ,sourceParam.$readStartBorder()) > 0 ?
                    partitionEnd : sourceParam.$readStartBorder();
            partitionStart = plusFunction.apply(partitionEnd ,-partitionSize);
            partitionStart = comparator.compare(partitionStart ,sourceParam.$readStartBorder()) > 0 ?
                    partitionStart : sourceParam.$readStartBorder();
        }
        return sourceParam.$partitionParam(partitionStart ,partitionEnd);
    }

    //是否有下一个分片
    private boolean hasNextPartition(P param0){
        if (asc){
            return comparator.compare(param0.$readEndBorder() ,sourceParam.$readEndBorder()) < 0;
        }else {
            return comparator.compare(param0.$readStartBorder() ,sourceParam.$readStartBorder()) > 0;
        }
    }

}

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
 * @param <P_BORDER> 分片的大小的单位
 */
@Data
@Accessors(chain = true)
public abstract class XFixedRatePartitionFunction<T , P_BORDER, P extends XFixedRatePartitionFunction.Param<P_BORDER, P>>
        implements XPartitionFunction<T ,P> {

    /**
     * 完整参数，指定数据的总查询范围
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
     * 设计目的：有些接口是按维度范围[startBorder, endBorder]查询的且包含两端，如果不设置间隔边界值会重复查询
     */
    private long partitionInterval = 0;

    /**
     * true:升序加载
     * false:降序加载
     */
    private boolean asc = true;


    public XFixedRatePartitionFunction(P sourceParam, int partitionSize, Function<P, List<T>> queryFunction)
    {
        if (sourceParam == null || sourceParam.$readStartBorder() == null || sourceParam.$readEndBorder() == null
                || partitionSize < 1 || queryFunction == null) {
            throw new IllegalArgumentException();
        }
        this.sourceParam = sourceParam;
        this.partitionSize = partitionSize;
        this.queryFunction = queryFunction;
    }

    /**
     * 维度增加若干个单位
     * @param P_BORDER
     * @param amountToAdd
     * @return
     */
    abstract P_BORDER plus(P_BORDER P_BORDER, long amountToAdd);

    /**
     * 比较两个维度的大小
     * @param PBORDER0
     * @param PBORDER1
     * @return
     */
    abstract int compare(P_BORDER PBORDER0, P_BORDER PBORDER1);

    @Override
    public Output<T, P> select(Input<T, P> input)
    {
        P currentParam = input.getCurrentParam();
        if (currentParam == null){
            throw new IllegalStateException();
        }

        if (compare(currentParam.$readStartBorder() ,currentParam.$readEndBorder()) > 0)
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
            partitionEnd = plus(partitionStart ,partitionSize);
            partitionEnd = compare(partitionEnd ,sourceParam.$readEndBorder()) < 0 ?
                    partitionEnd : sourceParam.$readEndBorder();
        }else {
            partitionEnd = sourceParam.$readEndBorder();
            partitionStart = plus(partitionEnd ,-partitionSize);
            partitionStart = compare(partitionStart ,sourceParam.$readStartBorder()) > 0 ?
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
            partitionStart = plus(param0.$readEndBorder() ,partitionInterval);
            partitionStart = compare(partitionStart ,sourceParam.$readEndBorder()) < 0 ?
                    partitionStart : sourceParam.$readEndBorder();
            partitionEnd = plus(partitionStart ,partitionSize);
            partitionEnd = compare(partitionEnd ,sourceParam.$readEndBorder()) < 0 ?
                    partitionEnd : sourceParam.$readEndBorder();
        }else {
            partitionEnd = plus(param0.$readStartBorder() ,-partitionInterval);
            partitionEnd = compare(partitionEnd ,sourceParam.$readStartBorder()) > 0 ?
                    partitionEnd : sourceParam.$readStartBorder();
            partitionStart = plus(partitionEnd ,-partitionSize);
            partitionStart = compare(partitionStart ,sourceParam.$readStartBorder()) > 0 ?
                    partitionStart : sourceParam.$readStartBorder();
        }
        return sourceParam.$partitionParam(partitionStart ,partitionEnd);
    }

    //是否有下一个分片
    private boolean hasNextPartition(P param0){
        if (asc){
            return compare(param0.$readEndBorder() ,sourceParam.$readEndBorder()) < 0;
        }else {
            return compare(param0.$readStartBorder() ,sourceParam.$readStartBorder()) > 0;
        }
    }

    /**
     * 分片参数
     * @author wangjj7
     * @date 2023/8/9
     * @description
     * @param <P_BORDER> 分片的维度（单位）
     */
    public interface Param<P_BORDER,P extends Param<P_BORDER, P>> {

        /**
         * 获取开始边界
         * @return
         */
        P_BORDER $readStartBorder();

        /**
         * 设置结束边界
         * @return
         */
        P_BORDER $readEndBorder();

        /**
         * 获取分区参数
         * @return
         */
        P $partitionParam(P_BORDER partitionStartPBORDER, P_BORDER partitionEndPBORDER);

        /**
         * 复制
         * @return
         */
        default P copy(){
            return $partitionParam($readStartBorder() , $readEndBorder());
        }

    }

}

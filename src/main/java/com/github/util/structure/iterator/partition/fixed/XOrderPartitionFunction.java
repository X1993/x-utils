package com.github.util.structure.iterator.partition.fixed;

import com.github.util.structure.iterator.partition.XPartitionFunction;
import lombok.Data;
import lombok.experimental.Accessors;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 基于游标/偏移量的有序分片函数。
 * <p>
 * 与 {@link XFixedRatePartitionFunction}（按固定步长切分参数范围）不同，
 * 本类通过查询结果中元素本身的维度值来动态确定下一个分片的边界，
 * 适用于游标式分页或基于偏移量的深分页场景。
 * </p>
 *
 * <h3>工作原理</h3>
 * <ol>
 *   <li>每次查询最多返回 maxElementCnt 条按维度排序的数据</li>
 *   <li>如果结果数量 &lt; maxElementCnt，说明没有更多数据</li>
 *   <li>如果结果数量 == maxElementCnt，从末尾向前移除与末尾元素维度值相同的元素，
 *       以剩余最后一个元素的维度值作为下一个分片的起始边界</li>
 *   <li>校验每次查询结果的维度值必须单调递增/递减，否则抛出异常</li>
 * </ol>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 1. 定义分片参数（实现 Param 接口）
 * record QueryParam(Long startId, Long endId)
 *         implements XPartitionParam<Long, QueryParam> {
 *     public Long $readStartBorder() { return startId; }
 *     public Long $readEndBorder() { return endId; }
 *     public QueryParam $partitionParam(Long s, Long e) { return new QueryParam(s, e); }
 * }
 *
 * // 2. 创建分片函数（每次最多查 100 条）
 * XOrderPartitionFunction<Order, Long, QueryParam> pf =
 *     new XOrderPartitionFunction<>(
 *         new QueryParam(0L, 999999L),
 *         Order::getId,
 *         param -> orderMapper.selectByRange(param),
 *         100
 *     ) {
 *         Long plus(Long id, long amount) { return id + amount; }
 *         int compare(Long a, Long b) { return a.compareTo(b); }
 *     };
 *
 * // 3. 包装为迭代器，透明遍历全量数据
 * for (Order order : new XPartitionIterable<>(pf)) {
 *     // 处理订单
 * }
 * }</pre>
 *
 * <p><b>注意</b>：maxElementCnt 最小值为 2，因为算法需要至少一个元素用于确定下一片边界。</p>
 *
 * @author wangjj7
 * @date 2026/6/1
 * @param <T> 分片查询的数据类型
 * @param <P_BORDER> 分片维度类型（如 Long、LocalDateTime）
 * @param <P> 分片查询参数的类型，必须实现 {@link XPartitionParam}
 * @see XFixedRatePartitionFunction
 * @see com.github.util.structure.iterator.partition.XPartitionIterable
 */
@Data
@Accessors(chain = true)
public abstract class XOrderPartitionFunction<T ,P_BORDER, P extends XPartitionParam<P_BORDER, P>>
        implements XPartitionFunction<T ,P> {

    /**
     * 完整参数，指定数据的总查询范围
     */
    private final P sourceParam;

    /**
     * 从元素中提取维度值的函数，用于确定分片边界
     */
    private final Function<T , P_BORDER> borderFunction;

    /**
     * 分片查询函数
     */
    private final Function<P ,List<T>> queryFunction;

    /**
     * 单次查询最大返回数量（最小值为 2，算法需要至少一个元素用于确定下一片边界）
     */
    private final int maxElementCnt;

    /**
     * 分片间隔
     * <p>设计目的：有些接口是按维度范围 [startBorder, endBorder] 查询的且包含两端，
     * 如果不设置间隔，边界值会重复查询。设置后下一片的起始边界会偏移该间隔。</p>
     */
    private long partitionInterval = 0;

    /**
     * true:升序加载
     * false:降序加载
     */
    private boolean asc = true;

    public XOrderPartitionFunction(P sourceParam, Function<T , P_BORDER> borderFunction ,
                                   Function<P, List<T>> queryFunction ,int maxElementCnt)
    {
        if (sourceParam == null || sourceParam.$readStartBorder() == null || sourceParam.$readEndBorder() == null
                || borderFunction == null || queryFunction == null || maxElementCnt < 2) {
            throw new IllegalArgumentException();
        }
        this.sourceParam = sourceParam;
        this.borderFunction = borderFunction;
        this.queryFunction = queryFunction;
        this.maxElementCnt = maxElementCnt;
    }

    /**
     * 维度增加若干个单位
     * @param P_BORDER 原始维度值
     * @param amountToAdd 增加量（可为负数）
     * @return 增加后的维度值
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
            throw new IllegalStateException("分片开始边界不能大于结束边界");
        }

        //复制一下避免查询的时候修改了原参数影响后续参数的创建
        P inputParam = currentParam.copy();
        if (inputParam == null){
            throw new IllegalStateException("copy实现异常");
        }

        List<T> partitionList = new ArrayList<>(queryFunction.apply(inputParam));
        validateOrder(partitionList);

        int partitionListSize = partitionList.size();
        if (partitionListSize > maxElementCnt){
            throw new IllegalStateException("分片查询结果数量超过最大限制");
        }
        if (partitionListSize < maxElementCnt){
            //没有更多了
            return new Output<T ,P>()
                    .setPartition(partitionList)
                    .setHasNext(false)
                    .setNextParam(null);
        }

        //从末尾向前找到与末尾元素不同边界值的分界点，跳过重复边界值的元素
        //这些元素将在下一片中重新查询，避免遗漏
        P_BORDER endBorder = borderFunction.apply(partitionList.remove(partitionList.size() - 1));
        while (!partitionList.isEmpty()){
            T lastElement = partitionList.remove(partitionList.size() - 1);
            P_BORDER border = borderFunction.apply(lastElement);
            if (compare(border ,endBorder) != 0){
                partitionList.add(lastElement);
                break;
            }
        }
        if (partitionList.isEmpty()){
            throw new IllegalStateException("具有相同边界值的元素超过最大数量,考虑增大最大限制");
        }

        return new Output<T ,P>()
                .setPartition(partitionList)
                .setHasNext(true)
                .setNextParam(inputParam.$partitionParam(plus(endBorder, partitionInterval) ,sourceParam.$readEndBorder()));
    }

    @Override
    public P firstInputParam()
    {
        return sourceParam;
    }

    //校验是否单调
    private void validateOrder(List<T> resultList){
        if (asc) {
            for (int i = 1; i < resultList.size(); i++) {
                if (compare(borderFunction.apply(resultList.get(i - 1)) ,borderFunction.apply(resultList.get(i))) > 0) {
                    throw new IllegalStateException("分片查询结果非单调递增");
                }
            }
        }else {
            for (int i = 1; i < resultList.size(); i++) {
                if (compare(borderFunction.apply(resultList.get(i - 1)) ,borderFunction.apply(resultList.get(i))) < 0) {
                    throw new IllegalStateException("分片查询结果非单调递减");
                }
            }
        }
    }

}

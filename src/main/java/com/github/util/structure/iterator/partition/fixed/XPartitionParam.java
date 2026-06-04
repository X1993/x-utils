package com.github.util.structure.iterator.partition.fixed;


/**
 * 分片参数
 * @author wangjj7
 * @date 2026/6/1
 * @description
 * @param <BORDER> 分片的维度（单位）
 */
public interface XPartitionParam<BORDER, P extends XPartitionParam<BORDER, P>> {

    /**
     * 获取开始边界
     * @return
     */
    BORDER $readStartBorder();

    /**
     * 获取结束边界
     * @return
     */
    BORDER $readEndBorder();

    /**
     * 获取分区参数
     * @return
     */
    P $partitionParam(BORDER partitionStartBORDER, BORDER partitionEndBORDER);

    /**
     * 复制
     * @return
     */
    default P copy(){
        return $partitionParam($readStartBorder() , $readEndBorder());
    }

}
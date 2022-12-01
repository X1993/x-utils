package com.github.util.concurrent;

/**
 * 指针键操作
 * @author X1993
 * @date 2022/11/28
 * @description
 */
public interface PointKeyOperator {

    /**
     * 获取指针偏移量
     * @param pointerKey 指针键
     * @return
     */
    Integer getOffset(String pointerKey);

    /**
     * 设置指定偏移量
     * @param pointerKey 指针键
     * @param offset 偏移量
     */
    void setOffset(String pointerKey ,Integer offset);

}

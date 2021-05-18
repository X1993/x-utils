package com.github.util.concurrent;

/**
 * 元素分配策略
 * @see ThreadGroupBlockingQueue#elementAllocationStrategy
 * @Author: X1993
 * @Date: 2021/5/14
 */
public interface ElementAllocationStrategy<E> {

    /**
     * 获取队列下标
     * @param element 元素
     * @param queueCount 队列数量
     * @return
     */
    int index(E element ,int queueCount);

}

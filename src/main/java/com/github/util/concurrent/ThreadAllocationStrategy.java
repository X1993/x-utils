package com.github.util.concurrent;

/**
 * 线程分配的队列获取策略
 * @see ThreadGroupBlockingQueue#threadIndexStrategy
 * @Author: X1993
 * @Date: 2021/5/14
 */
public interface ThreadAllocationStrategy {

    /**
     * 获取队列下标
     * @param thread 线程
     * @param queueCount 队列数量
     * @return
     */
    int index(Thread thread ,int queueCount);

}

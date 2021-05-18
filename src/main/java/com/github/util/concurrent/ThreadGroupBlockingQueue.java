package com.github.util.concurrent;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

/**
 * 线程分组队列，内部是一个队列集合，每个队列由特定线程负责（先抢{@link #getThreadQueue()}先分配，数量有限，抢完为止）
 *
 * 注意！不能直接作为{@link ThreadPoolExecutor#workQueue}，因为线程池的机制是没有可用队列才会加入到队列中
 * @Author: X1993
 * @Date: 2021/5/13
 */
public class ThreadGroupBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>
{
    /**
     * 通道组
     */
    private final BlockingQueue<E>[] threadQueueGroup;

    /**
     * 线程分配的队列下标计算策略
     */
    private final ThreadAllocationStrategy threadIndexStrategy;

    /**
     * 元素的hashCode值计算策略
     */
    private final ElementAllocationStrategy<E> elementAllocationStrategy;

    public ThreadGroupBlockingQueue(BlockingQueue<E>[] threadQueueGroup,
                                    ThreadAllocationStrategy threadIndexStrategy,
                                    ElementAllocationStrategy<E> elementAllocationStrategy)
    {
        if (threadQueueGroup == null || threadQueueGroup.length == 0
                || elementAllocationStrategy == null || threadIndexStrategy == null){
            throw new IllegalArgumentException();
        }
        this.threadQueueGroup = threadQueueGroup;
        this.threadIndexStrategy = threadIndexStrategy;
        this.elementAllocationStrategy = elementAllocationStrategy;
    }

    public ThreadGroupBlockingQueue(BlockingQueue<E>[] threadQueueGroup, ElementAllocationStrategy<E> elementAllocationStrategy)
    {
        this(threadQueueGroup ,defaultThreadAllocationStrategy() ,elementAllocationStrategy);
    }

    public ThreadGroupBlockingQueue(BlockingQueue<E>[] threadQueueGroup)
    {
        this(threadQueueGroup ,defaultThreadAllocationStrategy() ,(element, queueCount) -> element.hashCode() % queueCount);
    }

    /**
     * 获取数据保存队列
     * @param e
     * @return
     */
    private BlockingQueue<E> getThreadQueue(E e)
    {
        int index = elementAllocationStrategy.index(e, threadQueueGroup.length);
        if (index >= 0 && index < threadQueueGroup.length){
            return threadQueueGroup[index];
        }
        return null;
    }

    /**
     * 获取线程分配的队列
     * @return
     */
    private BlockingQueue<E> getThreadQueue()
    {
        int index = threadIndexStrategy.index(Thread.currentThread() ,threadQueueGroup.length);
        if (index >= 0 && index < threadQueueGroup.length)
        {
            return threadQueueGroup[index];
        }
        return null;
    }

    @Override
    public Iterator<E> iterator()
    {
        List<E> list = new ArrayList<>(size() + 1);
        for (BlockingQueue<E> threadQueue : threadQueueGroup) {
            Iterator<E> iterator = threadQueue.iterator();
            while (iterator.hasNext()){
                list.add(iterator.next());
            }
        }
        return list.iterator();
    }

    @Override
    public int size()
    {
        return Stream.of(threadQueueGroup).mapToInt(BlockingQueue::size).sum();
    }

    @Override
    public void put(E e) throws InterruptedException
    {
        getThreadQueue(e).put(e);
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException
    {
        return getThreadQueue(e).offer(e ,timeout ,unit);
    }

    @Override
    public E take() throws InterruptedException {
        BlockingQueue<E> threadQueue = getThreadQueue();
        if (threadQueue == null){
            return null;
        }
        E take = threadQueue.take();
        return take;
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        BlockingQueue<E> threadQueue = getThreadQueue();
        if (threadQueue == null){
            return null;
        }
        return threadQueue.poll(timeout, unit);
    }

    @Override
    public int remainingCapacity()
    {
        int remainingCapacity = 0;
        for (BlockingQueue<E> blockingQueue : threadQueueGroup) {
            int i = remainingCapacity + blockingQueue.remainingCapacity();
            if (i < remainingCapacity){
                //越界
                return Integer.MAX_VALUE;
            }
            remainingCapacity = i;
        }
        return remainingCapacity;
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        int max = maxElements;
        for (BlockingQueue<E> blockingQueue : threadQueueGroup) {
            max -= blockingQueue.drainTo(c ,max);
            if (max <= 0){
                return maxElements;
            }
        }
        return maxElements - max;
    }

    @Override
    public boolean offer(E e) {
        return getThreadQueue(e).offer(e);
    }

    @Override
    public E poll() {
        BlockingQueue<E> threadQueue = getThreadQueue();
        if (threadQueue == null){
            return null;
        }
        return threadQueue.poll();
    }

    @Override
    public E peek() {
        BlockingQueue<E> threadQueue = getThreadQueue();
        if (threadQueue == null){
            return null;
        }
        return threadQueue.peek();
    }

    /**
     * 线程分配的队列获取策略
     * @return
     */
    public static ThreadAllocationStrategy defaultThreadAllocationStrategy() {
        return new SeizeAllocationStrategy();
    }

    /**
     * 抢占式策略
     * @Author: junjie
     * @Date: 2021/5/14
     */
    static class SeizeAllocationStrategy implements ThreadAllocationStrategy {

        /**
         * 线程负责的队列下标
         */
        private final Map<Thread ,Integer> threadIndexMap = new HashMap<>();

        private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        @Override
        public int index(Thread thread ,int queueCount)
        {
            Thread currentThread = Thread.currentThread();

            Integer index = null;
            readWriteLock.readLock().lock();
            try {
                index = threadIndexMap.get(currentThread);
            }finally {
                readWriteLock.readLock().unlock();
            }

            if (index == null){
                readWriteLock.writeLock().lock();
                try {
                    index = threadIndexMap.size();
                    threadIndexMap.put(currentThread ,index);
                    System.out.println(Thread.currentThread() + ":" + index);
                }finally {
                    readWriteLock.writeLock().unlock();
                }
            }

            //所有队列被分配完了
            return index < queueCount ? index : -1;
        }

    }


}

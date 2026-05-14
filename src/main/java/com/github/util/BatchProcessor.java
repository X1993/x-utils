package com.github.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 分批处理器（单线程版本）
 * <p>
 * 通过 add 方法接收元素，达到指定数量时批量执行特定操作
 * </p>
 *
 * @param <T> 元素类型
 */
public class BatchProcessor<T> {

    private final List<T> buffer;
    private final int batchSize;
    private final Consumer<List<T>> handler;

    /**
     * 创建分批处理器
     *
     * @param batchSize 批量大小
     * @param handler    批量处理函数
     */
    public BatchProcessor(int batchSize, Consumer<List<T>> handler) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be positive");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }
        this.batchSize = batchSize;
        this.buffer = new ArrayList<>(batchSize);
        this.handler = handler;
    }

    /**
     * 添加元素，达到批量大小时自动触发处理
     */
    public void add(T element) {
        buffer.add(element);
        if (buffer.size() >= batchSize) {
            flush();
        }
    }

    /**
     * 手动刷新缓冲区
     */
    public void flush() {
        if (!buffer.isEmpty()) {
            handler.accept(new ArrayList<>(buffer));
            buffer.clear();
        }
    }

    /**
     * 获取当前缓冲区大小
     */
    public int size() {
        return buffer.size();
    }

    /**
     * 判断缓冲区是否为空
     */
    public boolean isEmpty() {
        return buffer.isEmpty();
    }
}
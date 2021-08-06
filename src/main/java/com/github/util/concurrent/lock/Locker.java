package com.github.util.concurrent.lock;

import lombok.Data;

/**
 * 加锁者
 * @Author: jie
 * @Date: 2021/8/6
 */
@Data
public class Locker<K> {

    /**
     * 抢占者唯一标识
     */
    private K key;

    /**
     * 优先级
     */
    private int priority;

}

package com.github.util.concurrent;

import java.util.function.Consumer;

/**
 * 读写切换操作
 * 定义两个指针key，一个用于读，一个用于新的写入，每次写入成功之后切换两个指针的标识
 *
 * 开发背景： 之前工作中遇到一个场景，redis中有数据量比较大的map，需要支持全量覆盖且过程中不能影响原先的map读取
 *    遇到的问题：1.redis的map只能先删除后添加；2.写入耗时较长
 *
 * @author X1993
 * @date 2022/11/28
 * @description
 */
public interface ReadWriteSwitchOperator {

    /**
     * 获取映射的读键
     * 不直接支持数据读取是因为某些读取操作比较特殊，比如redis读取map数据结构里面的某个key值
     * @param key 键
     * @return
     */
    String getReadKey(String key);

    /**
     * 写入新数据，如果当前key有其他写入任务正在进行中则阻塞等待
     * @param key 键
     * @param maxBlockingMS 最大阻塞等待时间，毫秒； null:不阻塞
     * @param lockExpiredMS 锁超时时间，避免死锁
     * @param writeOperator 将数据写入映射的写键操作
     * @param delOperator 删除过期的读键操作，非必须
     */
    boolean write(String key, Long maxBlockingMS, Long lockExpiredMS,
                  Consumer<String> writeOperator, Consumer<String> delOperator);

}

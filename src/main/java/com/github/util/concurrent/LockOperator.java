package com.github.util.concurrent;

/**
 * 锁操作
 * @author X1993
 * @date 2022/12/1
 * @description
 */
public interface LockOperator {

    /**
     * 如果当前锁无人占用，尝试占用指定锁
     * 原子操作
     * @param lockKey 锁标识
     * @param occupierId 占用者
     * @param lockExpireMS 锁过期时间，毫秒（避免死锁）；如果<=0，不过期，必须通过{@link #releaseLock(String, String)}释放
     * @return 是否占用成功，重复占用返回true，刷新过期时间
     */
    boolean tryLock(String lockKey, String occupierId, Long lockExpireMS);

    /**
     * 如果当前锁无人占用，占用指定锁，否则阻塞等待锁释放
     * @param lockKey 锁标识
     * @param occupierId 占用者
     * @param lockExpireMS 锁过期时间，毫秒（避免死锁）；<=0：不过期，只能通过{@link #releaseLock(String, String)}释放锁
     * @param maxBlockingMS 最大阻塞等待时间，毫秒； null:不阻塞
     * @param blockIntervalMS 阻塞唤醒间隔，毫秒
     * @return 是否占用成功，重复占用返回true，刷新过期时间
     */
    default boolean tryLock(String lockKey, String occupierId, Long lockExpireMS ,Long maxBlockingMS ,long blockIntervalMS){
        long startTimeMillis = System.currentTimeMillis();
        while (!tryLock(lockKey, occupierId, lockExpireMS)){
            //获取写锁失败
            if (maxBlockingMS == null || System.currentTimeMillis() - startTimeMillis >= maxBlockingMS) {
                return false;
            }
            if (blockIntervalMS >= 0) {
                //自旋等待
                try {
                    Thread.sleep(blockIntervalMS);
                } catch (InterruptedException e) {

                }
            }
        }
        return true;
    }

    default boolean tryLock(String lockKey, String occupierId, Long lockExpireMS ,Long maxBlockingMS){
        return tryLock(lockKey, occupierId, lockExpireMS, maxBlockingMS ,100L);
    }

    /**
     * 如果当前锁占用者匹配，则释放锁
     * 原子操作
     * @param lockKey 锁标识
     * @param occupierId 占用者
     * @return 释放释放锁成功，如果锁没有被占用或者占用者不匹配，返回false
     */
    boolean releaseLock(String lockKey, String occupierId);

}

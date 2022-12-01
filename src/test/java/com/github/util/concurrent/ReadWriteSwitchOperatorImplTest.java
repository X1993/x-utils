package com.github.util.concurrent;

import org.junit.Assert;
import org.junit.Test;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author X1993
 * @date 2022/11/29
 * @description
 */
public class ReadWriteSwitchOperatorImplTest {

    RedisMock redisMock = new RedisMock();

    ReadWriteSwitchOperator readWriteSwitchRedisMock = new ReadWriteSwitchOperatorImpl(redisMock ,redisMock);

    private boolean write(String key ,String value ,long writeTime ,long maxBlockingMS)
    {
        return readWriteSwitchRedisMock.write(key ,maxBlockingMS ,5000L ,
                s -> {
                    System.out.println(MessageFormat.format("{0},{1}开始写入key:{2},value:{3}" ,
                            LocalDateTime.now() ,Thread.currentThread() ,s ,value));
                    redisMock.put(s ,value);
                    if (writeTime > 0) {
                        try {
                            //模拟写入过程耗时较长
                            Thread.sleep(writeTime);
                        } catch (InterruptedException e) {

                        }
                    }
                    System.out.println(MessageFormat.format("{0},{1}成功写入key:{2},value:{3}" ,
                            LocalDateTime.now() ,Thread.currentThread() ,s ,value));
                },
                s -> redisMock.remove(s));
    }

    private String read(String key)
    {
        String readKey = readWriteSwitchRedisMock.getReadKey(key);
        String value = redisMock.get(readKey);
        System.out.println(MessageFormat.format("{0},读取key:{1},value:{2}" ,
                LocalDateTime.now() ,readKey ,value));
        return value;
    }

    @Test
    public void test(){
        String key = "key";
        String value0 = "value0";
        String value1 = "value1";

        boolean writeResult = write(key ,value0 ,-1 ,30L * 1000);
        Assert.assertTrue(writeResult);

        new Thread(() -> write(key ,value1 ,500 ,30L * 1000)).start();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {

        }
        Assert.assertEquals(read(key) ,value0);;

        writeResult = write(key ,value0 ,-1 ,-1);
        Assert.assertFalse(writeResult);

        new Thread(() -> write(key ,value0 ,500 ,30L * 1000)).start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {

        }
        //第二次异步写应该已经完成
        Assert.assertEquals(read(key) ,value1);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {

        }
        //第二次异步写应该已经完成
        Assert.assertEquals(read(key) ,value0);
    }

    /**
     * 模拟redis，用redis实现PointKeyOperator和LockOperator
     */
    static class RedisMock extends ConcurrentHashMap<String ,String> implements PointKeyOperator ,LockOperator{

        @Override
        public Integer getOffset(String pointerKey) {
            String s = get(pointerKey);
            if (s != null){
                return Integer.valueOf(s);
            }
            return null;
        }

        @Override
        public void setOffset(String pointerKey, Integer offset) {
            put(pointerKey ,String.valueOf(offset));
        }

        @Override
        public boolean tryLock(String lockKey, String occupierId, Long lockExpireMS) {
            return occupierId.equals(computeIfAbsent(lockKey, k -> occupierId));
        }

        @Override
        public boolean releaseLock(String lockKey, String occupierId) {
            return remove(lockKey ,occupierId);
        }

    }

}
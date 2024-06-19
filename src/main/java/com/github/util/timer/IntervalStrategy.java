package com.github.util.timer;

/**
 * 间隔计算策略
 * @author wangjj7
 * @date 2024/6/19
 * @description
 */
@FunctionalInterface
public interface IntervalStrategy {

    /**
     * 获取下次轮询间隔（毫秒）
     * @param count 轮询次数
     * @param beforeIntervalMS 上次轮询间隔（毫秒）
     * @param startTimestamp 第一次轮询时间戳
     * @return 小于0，不再轮询
     */
    long nextInterval(Integer count ,long beforeIntervalMS ,long startTimestamp);

    /**
     * 以固定间隔轮询直至超时
     * @param intervalMS
     * @param timeoutMS
     * @return
     */
    static IntervalStrategy fixedInterval(long intervalMS ,long timeoutMS){
        return (count ,beforeInterval ,startTimestamp) -> {
            if (System.currentTimeMillis() - startTimestamp >= timeoutMS){
                return -1;
            }
            return intervalMS;
        };
    }

}

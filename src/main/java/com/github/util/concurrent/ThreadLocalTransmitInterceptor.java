package com.github.util.concurrent;

/**
 * 线程池异步执行时负责线程变量的传递
 * @author wangjj7
 * @date 2023/8/22
 * @description
 */
public class ThreadLocalTransmitInterceptor implements TaskInterceptor{

    private final ThreadLocal[] threadLocals;

    private final Object[] values;

    public ThreadLocalTransmitInterceptor(ThreadLocal ... threadLocals) {
        this.threadLocals = threadLocals;
        this.values = new Object[threadLocals.length];
    }

    @Override
    public void postInit() {
        for (int i = 0; i < threadLocals.length; i++) {
            values[i] = threadLocals[i].get();
        }
    }

    @Override
    public void preExe() {
        for (int i = 0; i < threadLocals.length; i++) {
            threadLocals[i].set(values[i]);
        }
    }

    @Override
    public void postExe() {
        for (ThreadLocal threadLocal : threadLocals) {
            threadLocal.remove();
        }
    }
}

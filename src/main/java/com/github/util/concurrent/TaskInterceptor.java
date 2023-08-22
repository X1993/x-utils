package com.github.util.concurrent;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 线程池任务拦截
 * @author wangjj7
 * @date 2023/8/18
 * @description
 */
public interface TaskInterceptor {

    /**
     * 代理对象初始化后执行
     * @return
     */
    void postInit();

    /**
     * 代理对象执行前执行
     */
    void preExe();

    /**
     * 代理对象执行后执行
     */
    void postExe();

    class EmptyInterceptor implements TaskInterceptor {

        @Override
        public void postInit() {

        }

        @Override
        public void preExe() {

        }

        @Override
        public void postExe() {

        }
    }

    /**
     * 简单实现
     */
    @Data
    @Accessors(chain = true)
    class DefaultTaskInterceptor implements TaskInterceptor {

        private Runnable postInit;

        private Runnable preExe;

        private Runnable postExe;

        @Override
        public void postInit() {
            if (postInit != null) {
                postInit.run();
            }
        }

        @Override
        public void preExe() {
            if (preExe != null){
                preExe.run();
            }
        }

        @Override
        public void postExe() {
            if (postExe != null){
                postExe.run();
            }
        }

    }

}

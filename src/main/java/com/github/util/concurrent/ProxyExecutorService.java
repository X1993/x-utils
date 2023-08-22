package com.github.util.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 支持自定义拦截的的执行器
 * @author wangjj7
 * @date 2023/8/18
 * @description
 */
public class ProxyExecutorService implements ExecutorService {

    private ExecutorService executorService;

    private Supplier<TaskInterceptor> interceptorFactory;

    public ProxyExecutorService(ExecutorService executorService, Supplier<TaskInterceptor> interceptorFactory) {
        Objects.requireNonNull(executorService);
        Objects.requireNonNull(interceptorFactory);

        this.executorService = executorService;
        this.interceptorFactory = interceptorFactory;
    }

    @Override
    public void execute(Runnable task) {
        executorService.execute(callableProxy(task));
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout ,unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(callableProxy(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return executorService.submit(callableProxy(task) ,result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executorService.submit(callableProxy(task));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return executorService.invokeAll(tasks.stream()
                .map(this::callableProxy)
                .collect(Collectors.toList()));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.invokeAll(tasks.stream()
                .map(this::callableProxy)
                .collect(Collectors.toList()) ,timeout ,unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return executorService.invokeAny(tasks.stream()
                .map(this::callableProxy)
                .collect(Collectors.toList()));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return executorService.invokeAny(tasks.stream()
                .map(this::callableProxy)
                .collect(Collectors.toList()) ,timeout ,unit);
    }

    private <T> ProxyCallable<T> callableProxy(Callable<T> task){
        return new ProxyCallable<T>(task ,interceptorFactory.get());
    }

    private ProxyRunnable callableProxy(Runnable task){
        return new ProxyRunnable(task ,interceptorFactory.get());
    }

    private class ProxyRunnable implements Runnable{

        private final Runnable target;

        private final TaskInterceptor taskInterceptor;

        public ProxyRunnable(Runnable target , TaskInterceptor taskInterceptor) {
            this.target = target;
            this.taskInterceptor = taskInterceptor;
            taskInterceptor.postInit();
        }

        @Override
        public void run() {
            taskInterceptor.preExe();
            target.run();
            taskInterceptor.postExe();
        }
    }

    private class ProxyCallable<V> implements Callable<V>{

        private final Callable<V> target;

        private final TaskInterceptor taskInterceptor;

        public ProxyCallable(Callable<V> target , TaskInterceptor taskInterceptor) {
            this.target = target;
            this.taskInterceptor = taskInterceptor;
            taskInterceptor.postInit();
        }

        @Override
        public V call() throws Exception {
            taskInterceptor.preExe();
            V call = target.call();
            taskInterceptor.postExe();
            return call;
        }

    }

}

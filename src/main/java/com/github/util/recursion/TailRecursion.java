package com.github.util.recursion;

import java.util.stream.Stream;

/**
 * 尾递归函数接口
 *
 * Java编译器不支持尾递归优化，看到一篇文章通过模拟栈 + Stream的延迟加载来实现尾递归，学习下。 ∑( 口 ||
 *
 * 使用方式可以参考单元测试
 *
 * 参考：https://www.cnblogs.com/invoker-/p/7723420.html
 *
 * @author X1993
 * @date 2022/4/20
 */
@FunctionalInterface
public interface TailRecursion<T> {

    /**
     * 用于递归栈帧之间的连接,惰性求值
     * @return 下一个递归栈帧
     */
    TailRecursion<T> apply();

    /**
     * 判断当前递归是否结束
     * @return 默认为false,因为正常的递归过程中都还未结束
     */
    default boolean isFinished(){
        return false;
    }

    /**
     * 获得递归结果,只有在递归结束才能调用,这里默认给出异常,通过工具类的重写来获得值
     * @return 递归最终结果
     */
    default T getResult()  {
        throw new IllegalStateException("递归还没有结束,调用获得结果异常!");
    }

    /**
     * 及早求值,执行者一系列的递归,因为栈帧只有一个,所以使用findFirst获得最终的栈帧,接着调用getResult方法获得最终递归值
     * @return 及早求值,获得最终递归结果
     */
    default T invoke() {
        return Stream.iterate(this, TailRecursion::apply)
                .filter(TailRecursion::isFinished)
                .findFirst()
                .get()
                .getResult();
    }

    /**
     * 结束当前递归，重写对应的默认方法的值,完成状态改为true,设置最终返回结果,设置非法递归调用
     *
     * @param result 最终递归值
     * @param <T>   T
     * @return 一个isFinished状态true的尾递归, 外部通过调用接口的invoke方法及早求值, 启动递归求值。
     */
    static <T> TailRecursion<T> done(T result){
        return new TailRecursion<T>() {
            @Override
            public TailRecursion<T> apply() {
                throw new IllegalStateException("recursion task already finish");
            }

            @Override
            public T getResult() {
                return result;
            }

            @Override
            public boolean isFinished() {
                return true;
            }
        };
    }

}

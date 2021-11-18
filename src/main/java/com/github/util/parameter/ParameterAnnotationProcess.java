package com.github.util.parameter;

import java.lang.annotation.Annotation;

/**
 * 参数注解绑定参数值处理
 * @param <A> 支持@Target({ElementType.FIELD ,ElementType.PARAMETER})
 * @param <T>
 * @author jie
 * @date 2021/11/18
 */
public interface ParameterAnnotationProcess<A extends Annotation ,T> {

    /**
     * 执行
     * @param t
     * @param annotation
     * @return
     */
    T execute(T t ,A annotation);

    /**
     * 执行顺序，值小优先执行
     * @return
     */
    default int order(){
        return 0;
    }

}

package com.github.util.reflect.invoke;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * 方法句柄工具类
 * @author X1993
 * @date 2022/4/20
 * @description
 */
public class MethodHandleUtils {

    private static final int ALLOWED_MODES = MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
            | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC;

    private static final Constructor<MethodHandles.Lookup> lookupConstructor;

    private static final Method privateLookupInMethod;

    static {
        Method privateLookupIn;
        try {
            privateLookupIn = MethodHandles.class.getMethod(
                    "privateLookupIn", Class.class, MethodHandles.Lookup.class);
        } catch (NoSuchMethodException e) {
            privateLookupIn = null;
        }
        privateLookupInMethod = privateLookupIn;

        Constructor<MethodHandles.Lookup> lookup = null;
        if (privateLookupInMethod == null) {
            // JDK 1.8
            try {
                lookup = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
                lookup.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(
                        "There is neither 'privateLookupIn(Class, Lookup)' nor 'Lookup(Class, int)' method in java.lang.invoke.MethodHandles.",
                        e);
            } catch (Throwable t) {
                lookup = null;
            }
        }
        lookupConstructor = lookup;
    }

    /**
     * 执行默认方法
     *
     * 使用场景：接口做动态代理，不存在被代理对象，所有逻辑都实现在拦截层
     * （{@link java.lang.reflect.InvocationHandler#invoke(Object, Method, Object[])}）。
     * 对于默认方法不可以通过{@link Method#invoke(Object, Object...)}的方式执行，因为不存在被代理对象，
     * 如果使用代理对象执行会陷入死循环。此时可以通过这种方式实现
     *
     * 参考 mybatis源码MapperProxy
     *
     * @param target 实现了定义默认方法的接口的实现类对象
     * @param method 默认方法 {@link Method#isDefault()} == true
     * @param args 执行参数
     * @return
     * @throws Throwable
     */
    public static Object invokeDefaultMethod(Object target, Method method, Object ... args) throws Throwable {
        if (!method.isDefault()){
            throw new IllegalArgumentException("must be default method");
        }
        if (!method.getDeclaringClass().isAssignableFrom(target.getClass())){
            throw new IllegalArgumentException("target class must implement " + method.getDeclaringClass());
        }
        try {
            if (privateLookupInMethod == null) {
                return invokeDefaultMethodJava8(target, method, args);
            } else {
                return invokeDefaultMethodJava9(target, method, args);
            }
        } catch (Throwable t) {
            throw unwrapThrowable(t);
        }
    }

    private static Throwable unwrapThrowable(Throwable wrapped) {
        Throwable unwrapped = wrapped;
        while (true) {
            if (unwrapped instanceof InvocationTargetException) {
                unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
            } else if (unwrapped instanceof UndeclaredThrowableException) {
                unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
            } else {
                return unwrapped;
            }
        }
    }

    private static Object invokeDefaultMethodJava9(Object target, Method method, Object[] args)
            throws Throwable {
        final Class<?> declaringClass = method.getDeclaringClass();
        return ((MethodHandles.Lookup) privateLookupInMethod.invoke(null, declaringClass, MethodHandles.lookup()))
                .findSpecial(declaringClass, method.getName(),
                        MethodType.methodType(method.getReturnType(), method.getParameterTypes()), declaringClass)
                .bindTo(target).invokeWithArguments(args);
    }

    private static Object invokeDefaultMethodJava8(Object target, Method method, Object[] args)
            throws Throwable {
        final Class<?> declaringClass = method.getDeclaringClass();
        return lookupConstructor.newInstance(declaringClass, ALLOWED_MODES).unreflectSpecial(method, declaringClass)
                .bindTo(target).invokeWithArguments(args);
    }

}

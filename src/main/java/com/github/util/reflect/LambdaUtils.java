package com.github.util.reflect;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * @author X1993
 * @date 2022/1/26
 * @description
 */
public class LambdaUtils {

    /**
     * 获取字段名
     * 避免直接引用字符串，利用JavaBean规范提供编译检查
     * @param getFunction 根据JavaBean规范定义的get方法 lambda形式
     * @return
     */
    public static <T> String getFieldName(GetFunction<T ,?> getFunction)
    {
        Method writeReplace = null;
        Boolean accessible = null;
        try {
            //实现Serializable接口的lambda对象字节码里有这个方法描述lambda方法
            writeReplace = getFunction.getClass().getDeclaredMethod("writeReplace");
            accessible = writeReplace.isAccessible();
            if (!accessible) {
                writeReplace.setAccessible(true);
            }
            SerializedLambda serializedLambda = (SerializedLambda) writeReplace.invoke(getFunction);
            String implMethodName = serializedLambda.getImplMethodName();
            int subLength = 0;
            if (implMethodName.startsWith("get") && implMethodName.length() > 3){
                subLength = 3;
            }else if (implMethodName.startsWith("is") && implMethodName.length() > 2){
                subLength = 2;
            }else {
                throw new IllegalArgumentException("Method names do not comply with the JavaBean GET specification");
            }
            char firstChar = implMethodName.charAt(subLength);
            firstChar = (firstChar >= 'A' && firstChar <= 'Z') ? (char) (firstChar + ('a' - 'A')) : firstChar;
            return firstChar + implMethodName.substring(subLength + 1);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            if (writeReplace != null && accessible != null){
                writeReplace.setAccessible(accessible);
            }
        }
    }

    @FunctionalInterface
    public interface GetFunction<T ,R> extends Serializable ,Function<T ,R> {

    }

}

package com.github.util.reflect;

import com.github.util.lamdba.LambdaUtils;
import org.junit.Assert;
import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * @author X1993
 * @date 2022/1/26
 * @description
 */
public class LambdaUtilsTest {

    static class User{

        private String userName;

        public String getUserName() {
            return userName;
        }

        private String findInfo(){
            return "a";
        }

    }

    @Test
    public void getFieldNameTest()
    {
        Assert.assertEquals(LambdaUtils.getFieldName(User::getUserName), "userName");

        boolean exception = false;
        try {
            LambdaUtils.getFieldName(User::findInfo);
        }catch (RuntimeException e){
            exception = true;
        }
        Assert.assertTrue(exception);
    }

    @Test
    public void cacheFunctionTest()
    {
        Function<Integer ,Integer> function = new Function<Integer, Integer>() {

            final Set<Integer> set = new HashSet<>();

            @Override
            public Integer apply(Integer integer) {
                if (set.contains(integer)){
                    //重复执行报错
                    throw new IllegalStateException();
                }
                set.add(integer);
                return integer;
            }
        };

        Function<Integer, Integer> cacheFunction = LambdaUtils.cache(function);

        cacheFunction.apply(1);
        cacheFunction.apply(1);

        cacheFunction.apply(2);
    }

}
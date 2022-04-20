package com.github.util.reflect.invoke;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author wangjj7
 * @date 2022/4/20
 * @description
 */
public class MethodHandleUtilsTest {

    interface Interface0{

        default int defaultOrder(){
            return 0;
        }

    }

    class Child0 implements Interface0{

        @Override
        public int defaultOrder() {
            return 2;
        }

    }

    @Test
    public void testInvokeDefaultMethod() throws Throwable
    {
        Child0 child0 = new Child0();
        //默认方法结果是0，重写的结果是2
        Assert.assertTrue(Integer.valueOf(0).equals(MethodHandleUtils.invokeDefaultMethod(
                child0 ,Interface0.class.getMethod("defaultOrder"))));
        Assert.assertTrue(Integer.valueOf(2).equals(Interface0.class.getMethod("defaultOrder").invoke(child0)));
    }
}
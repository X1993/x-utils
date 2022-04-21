package com.github.util.recursion;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author X1993
 * @date 2022/4/20
 * @description
 */
public class TailRecursionTest {

    @Test
    public void totalTailRecursionTest() {
        int num = 5;
        Assert.assertTrue(totalTailRecursion(0 ,num)
                == totalTailRecursion1(0, num).invoke());

        num = 100000;
        boolean stackOverflowError = false;
        try {
            totalTailRecursion(0 ,num);
        } catch (StackOverflowError error){
            //java不支持尾递归，堆栈溢出
            stackOverflowError = true;
        }
        Assert.assertTrue(stackOverflowError);

        totalTailRecursion1(0 ,num).invoke();
    }

    /**
     * 计算[1-N]中所有自然数的总和，递归方式实现
     * @param factorial 上一轮递归保存的值
     * @param number    当前累加需要计算的数值
     * @return number
     */
    public static int totalTailRecursion(final int factorial, final int number) {
        if (number == 1){
            return factorial;
        }
        return totalTailRecursion(factorial + number, number - 1);
    }

    /**
     * 计算[1-N]中所有自然数的总和，TailRecursion方式实现
     * @param factorial 上一轮递归保存的值
     * @param number    当前累加需要计算的数值
     * @return number
     */
    public static TailRecursion<Integer> totalTailRecursion1(final int factorial, final int number) {
        if (number == 1){
            return TailRecursion.done(factorial);
        }
        return () -> totalTailRecursion1(factorial + number, number - 1);
    }

}
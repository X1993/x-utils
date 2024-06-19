package com.github.util.lamdba;

import org.junit.Test;
import java.util.function.Function;

/**
 * @author wangjj7
 * @date 2024/6/19
 * @description
 */
public class XSupplierTest {

    @Test
    public void test(){
        XSupplier<Integer> xSupplier = XSupplier.start(new XSupplier<Integer>() {
                    int count = 1;
                    @Override
                    public Integer get() {
                        System.out.println("执行节点1");
                        if (count-- > 0) {
                            return null;
                        }
                        System.out.println("获取节点1结果");
                        return 1;
                    }
                })
                .cache()
                .doThen(new Function<Integer, Integer>() {
                    int count = 1;
                    @Override
                    public Integer apply(Integer param) {
                        System.out.println("执行节点2，参数：" + param);
                        if (count-- > 0) {
                            return null;
                        }
                        System.out.println("获取节点2结果");
                        return 2;
                    }
                })
                .cache()
                .doThen(new Function<Integer, Integer>() {
                    int count = 1;
                    @Override
                    public Integer apply(Integer param) {
                        System.out.println("执行节点3，参数：" + param);
                        if (count-- > 0) {
                            return null;
                        }
                        System.out.println("获取节点3结果");
                        return 3;
                    }
                });


        while (xSupplier.get() == null){
            System.out.println("===================");
        }
    }

}
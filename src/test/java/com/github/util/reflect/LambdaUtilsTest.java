package com.github.util.reflect;

import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author wangjj7
 * @date 2022/1/26
 * @description
 */
public class LambdaUtilsTest {

    @Data
    static class User{

        private String userName;

        private boolean vip;

        private String findInfo(){
            return "a";
        }

    }

    @Test
    public void testGetFieldName() {
        Assert.assertEquals(LambdaUtils.getFieldName(User::getUserName), "userName");
        Assert.assertEquals(LambdaUtils.getFieldName(User::isVip), "vip");
        boolean exeception = false;
        try {
            LambdaUtils.getFieldName(User::findInfo);
        }catch (RuntimeException e){
            exeception = true;
        }
        Assert.assertTrue(exeception);
    }

}
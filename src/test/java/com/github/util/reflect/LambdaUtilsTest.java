package com.github.util.reflect;

import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

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

}
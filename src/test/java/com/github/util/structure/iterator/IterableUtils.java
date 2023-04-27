package com.github.util.structure.iterator;

import org.junit.Assert;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author wangjj7
 * @date 2023/4/27
 * @description
 */
public class IterableUtils {

    public static void validation(int minVal , int maxVal , Iterator<Integer> iterator){
        for (int i = minVal; i <= maxVal; i++) {
            Assert.assertTrue(iterator.hasNext());
            Assert.assertTrue(Integer.valueOf(i).equals(iterator.next()));
        }

        boolean noSuchElementException = false;
        Assert.assertFalse(iterator.hasNext());
        try {
            iterator.next();
        }catch (NoSuchElementException e){
            noSuchElementException = true;
        }

        if (!noSuchElementException){
            throw new IllegalStateException();
        }
    }

}

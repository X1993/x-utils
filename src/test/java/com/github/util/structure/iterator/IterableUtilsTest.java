package com.github.util.structure.iterator;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author wangjj7
 * @date 2023/4/27
 * @description
 */
public class IterableUtilsTest {

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

    @Test
    public void splitTest()
    {
        int minVal = 1;
        int maxVal = 101;
        List<Integer> list = IntStream.range(minVal ,maxVal)
                .mapToObj(Integer::valueOf)
                .collect(Collectors.toList());

        Iterable<List<Integer>> iterable = IterableUtils.split(list, 10);
        int beforeItem = -1;
        for (List<Integer> itemList : iterable) {
            for (Integer item : itemList) {
                Assert.assertTrue(item > beforeItem);
                beforeItem = item;
            }
            Assert.assertTrue(itemList.size() == 10);
        }
    }

}

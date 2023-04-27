package com.github.util.structure.iterator.page;

import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author wangjj7
 * @date 2023/4/27
 * @description
 */
public class XPageIterableTest {

    @Test
    public void iteratorTest(){
        iteratorTest(1 ,100 ,10);
        iteratorTest(4 ,70 ,7);
    }

    private void iteratorTest(int minVal ,int maxVal ,int pageSize)
    {
        int[] mockTable = IntStream.range(minVal ,maxVal + 1).toArray();

        XPageFunction<Integer> pageFunction = param -> {
            int startIndex = (param.pageIndex() - 1) * param.pageSize();
            int endIndex = startIndex + param.pageSize() - 1;
            int maxIndex = mockTable.length - 1;
            if (startIndex > maxIndex){
                return new XPageResultImpl<>(param , Collections.emptyList());
            }
            return new XPageResultImpl<>(param ,Arrays.stream(
                            Arrays.copyOfRange(mockTable, Math.min(startIndex ,maxIndex), Math.min(endIndex ,maxIndex) + 1))
                    .boxed()
                    .collect(Collectors.toList()));
        };

        XPageIterable<Integer> pageIterable = new XPageIterable<>(pageFunction ,pageSize);

        Iterator<Integer> iterator = pageIterable.iterator();
        for (int i = minVal; i <= maxVal; i++) {
            Assert.assertTrue(iterator.hasNext());
            Assert.assertTrue(Integer.valueOf(i).equals(iterator.next()));
        }

        Assert.assertFalse(iterator.hasNext());
        try {
            iterator.next();
        }catch (NoSuchElementException e){
            return;
        }

        throw new IllegalStateException();
    }

}
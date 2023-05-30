package com.github.util.structure.iterator.partition;

import com.github.util.structure.iterator.XIterableUtilsTest;
import org.junit.Test;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author X1993
 * @date 2023/4/27
 * @description
 */
public class XPartitionIteratorTest {

    @Test
    public void iteratorTest()
    {
        int minVal = 1;
        int maxVal = 100;
        int[] mockTable = IntStream.range(minVal ,maxVal + 1).unordered().toArray();

        XPartitionIterable<Integer> iterable = new XPartitionIterable<>(preElement ->
            IntStream.of(mockTable)
                    .sorted()
                    .filter(x -> preElement == null || x > preElement)
                    .limit(10)
                    .mapToObj(Integer::valueOf)
                    .collect(Collectors.toList()));

        XIterableUtilsTest.validation(minVal ,maxVal ,iterable.iterator());
    }

}
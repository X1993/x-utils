package com.github.util.structure.iterator.partition;

import com.github.util.structure.iterator.XIterableUtilsTest;
import org.junit.Test;
import java.util.List;
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

        XPartitionIterable<Integer ,Void> iterable = new XPartitionIterable<>(input ->
        {
            List<Integer> values = IntStream.of(mockTable)
                    .sorted()
                    .filter(x -> {
                        List<Integer> prePartitions = input.getPrePartition();
                        Integer preElement = (prePartitions == null || prePartitions.size() == 0) ?
                                null : prePartitions.get(prePartitions.size() - 1);
                        return preElement == null || x > preElement;
                    })
                    .limit(10)
                    .mapToObj(Integer::valueOf)
                    .collect(Collectors.toList());

            return new XPartitionFunction.Output<Integer ,Void>()
                    .setHasNext(values != null && values.size() > 0)
                    .setPartition(values);
        });

        XIterableUtilsTest.validation(minVal ,maxVal ,iterable.iterator());
    }

}
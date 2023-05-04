package com.github.util.structure.iterator.page;

import com.github.util.structure.iterator.IterableUtils;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
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
                return Collections.emptyList();
            }
            return Arrays.stream(
                            Arrays.copyOfRange(mockTable, Math.min(startIndex ,maxIndex), Math.min(endIndex ,maxIndex) + 1))
                    .boxed()
                    .collect(Collectors.toList());
        };

        XPageIterable<Integer> pageIterable = new XPageIterable<>(pageFunction ,pageSize);

        Iterator<Integer> iterator = pageIterable.iterator();
        IterableUtils.validation(minVal ,maxVal ,iterator);

        //测试重置
        int resetVal = ThreadLocalRandom.current().nextInt(minVal, maxVal) + minVal;
        XPageIterator<Integer> pageIterator = (XPageIterator<Integer>) iterator;
        pageIterator.resetIndex(resetVal - 1);
        IterableUtils.validation(minVal + resetVal - 1 ,maxVal ,iterator);
    }

}
package com.github.util.structure.iterator.limit;

import com.github.util.structure.iterator.XIterableUtilsTest;
import org.junit.Test;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author wangjj7
 * @date 2023/4/27
 * @description
 */
public class XLimitIterableTest {

    @Test
    public void iteratorTest(){
        int minVal = 1;
        int maxVal = 100;
        List<Integer> list = IntStream.range(minVal ,maxVal).mapToObj(Integer::valueOf).collect(Collectors.toList());
        int limit = ThreadLocalRandom.current().nextInt(minVal, maxVal);
        Iterator<Integer> iterator = new XLimitIterable<>(list, limit).iterator();
        XIterableUtilsTest.validation(minVal ,limit ,iterator);
    }

}
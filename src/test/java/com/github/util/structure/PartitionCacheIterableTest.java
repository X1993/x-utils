package com.github.util.structure;

import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author wangjj7
 * @date 2023/2/3
 * @description
 */
public class PartitionCacheIterableTest {

    //模拟行数据
    @Accessors(chain = true)
    @Data
    static class Row{

        //模拟自增主键
        private Long pkId;

        //...

    }

    final static int ROW_SIZE = 100;

    //模拟数据库表
    final static Row[] TABLE = new Row[ROW_SIZE];

    static {
        int rowSize = 100;
        for (int i = 0; i < rowSize; i++) {
            TABLE[i] = new Row().setPkId(Long.valueOf(rowSize - i));
        }
    }

    @Test
    public void partitionCacheIteratorTest()
    {
        int pageSize = 10;

        //模拟分区查询
        Function<Row ,List<Row>> partitionQueryFunction = lastedRow -> Arrays.stream(TABLE)
                .filter(row1 -> lastedRow == null || row1.getPkId() > lastedRow.getPkId())
                .sorted(Comparator.comparing(Row::getPkId))
                .limit(pageSize)
                .collect(Collectors.toList());

        int expectPkId = 1;
        for (Row row : new PartitionCacheIterable<>(partitionQueryFunction)) {
            Assert.assertTrue(row.getPkId() == expectPkId++);
        }
    }

    @Test
    public void pageCacheIteratorTest()
    {
        int pageSize = 10;

        //模拟分页查询
        PartitionCacheIterable<Row> pageIterable = PartitionCacheIterable.pageIterable(
                pageIndex -> Arrays.stream(TABLE)
                        .sorted(Comparator.comparing(Row::getPkId))
                        .skip(pageIndex * pageSize)
                        .limit(pageSize)
                        .collect(Collectors.toList())
        );

        int expectPkId = 1;
        for (Row row : pageIterable) {
            Assert.assertTrue(row.getPkId() == expectPkId++);
        }
    }

}
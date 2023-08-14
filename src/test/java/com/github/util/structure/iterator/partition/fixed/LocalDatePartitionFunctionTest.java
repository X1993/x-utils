package com.github.util.structure.iterator.partition.fixed;

import com.github.util.structure.iterator.partition.XPartitionFunction;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.Assert;
import org.junit.Test;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

/**
 * @author wangjj7
 * @date 2023/8/14
 * @description
 */
public class LocalDatePartitionFunctionTest {

    @Data
    @Accessors(chain = true)
    class LocalDateParam implements XFixedRatePartitionParam<LocalDate ,LocalDateParam> {

        private LocalDate startTime;

        private LocalDate endTime;

        @Override
        public LocalDate $readStartTime() {
            return startTime;
        }

        @Override
        public LocalDate $readEndTime() {
            return endTime;
        }

        @Override
        public LocalDateParam $partitionParam(LocalDate partitionStartTime, LocalDate partitionEndTime) {
            return new LocalDateParam().setStartTime(partitionStartTime).setEndTime(partitionEndTime);
        }
    }

    @Test
    public void test()
    {
        LocalDate startTime = LocalDate.of(2023 ,1,1);
        LocalDate endTime = LocalDate.of(2023 ,1,10);

        XFixedRatePartitionFunction<Void ,LocalDate ,LocalDateParam> partitionFunction =
                new LocalDatePartitionFunction<>(
                        new LocalDateParam().setStartTime(startTime).setEndTime(endTime),
                        1 , ChronoUnit.DAYS ,p -> {
                    System.out.println(p);
                    return Collections.EMPTY_LIST;
        });

        XPartitionFunction.Input<Void, LocalDateParam> input =
                new XPartitionFunction.Input<Void, LocalDateParam>()
                        .setCurrentParam(partitionFunction.firstInputParam());

        while (true){
            XPartitionFunction.Output<Void, LocalDateParam> output = partitionFunction.select(input);
            if (!output.isHasNext()){
                break;
            }
            XPartitionFunction.Input<Void, LocalDateParam> nextInput =
                    new XPartitionFunction.Input<Void, LocalDateParam>()
                    .setCurrentParam(output.getNextParam());
            Assert.assertEquals(input.getCurrentParam().getEndTime() ,
                    nextInput.getCurrentParam().getStartTime());
            input = nextInput;
        }

        System.out.println("------------------ 测试分区间隔 --------------------");

        partitionFunction.setPartitionInterval(1);

        input = new XPartitionFunction.Input<Void, LocalDateParam>()
                        .setCurrentParam(partitionFunction.firstInputParam());

        while (true){
            XPartitionFunction.Output<Void, LocalDateParam> output = partitionFunction.select(input);
            if (!output.isHasNext()){
                break;
            }
            XPartitionFunction.Input<Void, LocalDateParam> nextInput =
                    new XPartitionFunction.Input<Void, LocalDateParam>()
                            .setCurrentParam(output.getNextParam());
            Assert.assertEquals(input.getCurrentParam().getEndTime().plusDays(1) ,
                    nextInput.getCurrentParam().getStartTime());
            input = nextInput;
        }
    }

}
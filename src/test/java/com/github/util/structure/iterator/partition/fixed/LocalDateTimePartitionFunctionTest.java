package com.github.util.structure.iterator.partition.fixed;


import com.github.util.structure.iterator.partition.XPartitionFunction;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.Assert;
import org.junit.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

/**
 * @author wangjj7
 * @date 2023/8/14
 * @description
 */
public class LocalDateTimePartitionFunctionTest {

    @Data
    @Accessors(chain = true)
    class LocalDateTimeParam implements XFixedRatePartitionParam<LocalDateTime ,LocalDateTimeParam> {

        private LocalDateTime startTime;

        private LocalDateTime endTime;

        @Override
        public LocalDateTime $readStartTime() {
            return startTime;
        }

        @Override
        public LocalDateTime $readEndTime() {
            return endTime;
        }

        @Override
        public LocalDateTimeParam $partitionParam(LocalDateTime partitionStartTime, LocalDateTime partitionEndTime) {
            return new LocalDateTimeParam().setStartTime(partitionStartTime).setEndTime(partitionEndTime);
        }
    }

    @Test
    public void testTimePartitionFunction(){
        LocalDateTime startTime = LocalDate.of(2023 ,1,1).atTime(LocalTime.MIN);
        LocalDateTime endTime = LocalDate.of(2023 ,1,10).atTime(LocalTime.MIN);

        XFixedRatePartitionFunction<Void ,LocalDateTime ,LocalDateTimeParam> partitionFunction =
                new LocalDateTimePartitionFunction<>(
                        new LocalDateTimeParam().setStartTime(startTime).setEndTime(endTime),
                        1 , ChronoUnit.DAYS ,p -> {
                    System.out.println(p);
                    return Collections.EMPTY_LIST;
        });


        XPartitionFunction.Input<Void, LocalDateTimeParam> input =
                new XPartitionFunction.Input<Void, LocalDateTimeParam>()
                        .setCurrentParam(partitionFunction.firstInputParam());

        while (true){
            XPartitionFunction.Output<Void, LocalDateTimeParam> output = partitionFunction.select(input);
            if (!output.isHasNext()){
                break;
            }
            XPartitionFunction.Input<Void, LocalDateTimeParam> nextInput =
                    new XPartitionFunction.Input<Void, LocalDateTimeParam>()
                    .setCurrentParam(output.getNextParam());
            Assert.assertEquals(input.getCurrentParam().getEndTime() ,
                    nextInput.getCurrentParam().getStartTime());
            input = nextInput;
        }
    }

}
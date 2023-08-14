package com.github.util.structure.iterator.partition.time;

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
public class XTimePartitionFunctionTest {

    @Data
    @Accessors(chain = true)
    class TimeParam implements TimePartitionParam<TimeParam>{

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
        public TimeParam $partitionParam(LocalDateTime partitionStartTime, LocalDateTime partitionEndTime) {
            return new TimeParam().setStartTime(partitionStartTime).setEndTime(partitionEndTime);
        }
    }

    @Test
    public void testTimePartitionFunction(){
        LocalDateTime startTime = LocalDate.of(2023 ,1,1).atTime(LocalTime.MIN);
        LocalDateTime endTime = LocalDate.of(2023 ,1,10).atTime(LocalTime.MIN);

        XTimePartitionFunction<Void , TimeParam> partitionFunction = new XTimePartitionFunction<>(
                new TimeParam().setStartTime(startTime).setEndTime(endTime) ,true ,
                1 ,ChronoUnit.DAYS ,p -> {
                    System.out.println(p);
                    return Collections.EMPTY_LIST;
        });


        XPartitionFunction.Input<Void, TimeParam> input = new XPartitionFunction.Input<Void, TimeParam>()
                .setCurrentParam(partitionFunction.firstInputParam());
        while (true){
            XPartitionFunction.Output<Void, TimeParam> output = partitionFunction.select(input);
            if (!output.isHasNext()){
                break;
            }
            XPartitionFunction.Input<Void, TimeParam> nextInput = new XPartitionFunction.Input<Void, TimeParam>()
                    .setCurrentParam(output.getNextParam());
            Assert.assertEquals(input.getCurrentParam().getEndTime() ,
                    nextInput.getCurrentParam().getStartTime());
            input = nextInput;
        }
    }


    @Data
    @Accessors(chain = true)
    class DateParam implements DatePartitionParam<DateParam>{

        private LocalDate startDate;

        private LocalDate endDate;

        @Override
        public LocalDate $readStartDate() {
            return startDate;
        }

        @Override
        public LocalDate $readEndDate() {
            return endDate;
        }

        @Override
        public DateParam $partitionParam(LocalDate partitionStartDate, LocalDate partitionEndDate) {
            return new DateParam().setStartDate(partitionStartDate).setEndDate(partitionEndDate);
        }

    }

    @Test
    public void testDatePartitionFunction(){
        LocalDate startDate = LocalDate.of(2023 ,1,1);
        LocalDate endDate = LocalDate.of(2023 ,1,10);

        XTimePartitionFunction<Void , DateParam> partitionFunction = new XTimePartitionFunction<>(
                new DateParam().setStartDate(startDate).setEndDate(endDate) ,true ,
                1 ,ChronoUnit.DAYS ,p -> {
            System.out.println(p);
            return Collections.EMPTY_LIST;
        });


        XPartitionFunction.Input<Void, DateParam> input = new XPartitionFunction.Input<Void, DateParam>()
                .setCurrentParam(partitionFunction.firstInputParam());
        while (true){
            XPartitionFunction.Output<Void, DateParam> output = partitionFunction.select(input);
            if (!output.isHasNext()){
                break;
            }
            XPartitionFunction.Input<Void, DateParam> nextInput = new XPartitionFunction.Input<Void, DateParam>()
                    .setCurrentParam(output.getNextParam());
            Assert.assertEquals(input.getCurrentParam().getEndDate() ,
                    nextInput.getCurrentParam().getStartDate());
            input = nextInput;
        }
    }

}
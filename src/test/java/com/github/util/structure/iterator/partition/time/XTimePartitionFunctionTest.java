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
    class Param implements TimePartitionParam<Param>{

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
        public Param $partitionParam(LocalDateTime partitionStartTime, LocalDateTime partitionEndTime) {
            return new Param().setStartTime(partitionStartTime).setEndTime(partitionEndTime);
        }

    }

    @Test
    public void test(){
        LocalDateTime startTime = LocalDate.of(2023 ,1,1).atTime(LocalTime.MIN);
        LocalDateTime endTime = LocalDate.of(2023 ,1,10).atTime(LocalTime.MIN);

        XTimePartitionFunction<Void ,Param> partitionFunction = new XTimePartitionFunction<>(
                new Param().setStartTime(startTime).setEndTime(endTime) ,true ,
                1 ,ChronoUnit.DAYS ,p -> {
                    System.out.println(p);
                    return Collections.EMPTY_LIST;
        });


        XPartitionFunction.Input<Void, Param> input = new XPartitionFunction.Input<Void, Param>()
                .setCurrentParam(partitionFunction.firstInputParam());
        while (true){
            XPartitionFunction.Output<Void, Param> output = partitionFunction.select(input);
            if (!output.isHasNext()){
                break;
            }
            XPartitionFunction.Input<Void, Param> nextInput = new XPartitionFunction.Input<Void, Param>()
                    .setCurrentParam(output.getNextParam());
            Assert.assertEquals(input.getCurrentParam().getEndTime() ,
                    nextInput.getCurrentParam().getStartTime());
            input = nextInput;
        }
    }

}
package com.github.util.structure.iterator.partition.fixed;

import com.github.util.structure.iterator.partition.XPartitionFunction;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author wangjj7
 * @date 2023/8/14
 * @description
 */
public class DatePartitionFunctionTest {

    @Data
    @Accessors(chain = true)
    class DateParam implements XFixedRatePartitionParam<Date ,DateParam> {

        private Date startTime;

        private Date endTime;

        @Override
        public Date $readStartTime() {
            return startTime;
        }

        @Override
        public Date $readEndTime() {
            return endTime;
        }

        @Override
        public DateParam $partitionParam(Date partitionStartTime, Date partitionEndTime) {
            return new DateParam().setStartTime(partitionStartTime).setEndTime(partitionEndTime);
        }
    }

    @Test
    public void test() throws ParseException {
        Date startTime = new SimpleDateFormat("yyyy-MM-dd").parse("2023-01-01");
        Date endTime = new SimpleDateFormat("yyyy-MM-dd").parse("2023-01-10");

        XFixedRatePartitionFunction<Void ,Date ,DateParam> partitionFunction = new DatePartitionFunction(
                new DateParam().setStartTime(startTime).setEndTime(endTime), 1 ,TimeUnit.DAYS ,
                p -> {
                    System.out.println(p);
                    return Collections.EMPTY_LIST;
                }
        );


        XPartitionFunction.Input<Void, DateParam> input =
                new XPartitionFunction.Input<Void, DateParam>()
                        .setCurrentParam(partitionFunction.firstInputParam());

        while (true){
            XPartitionFunction.Output<Void, DateParam> output = partitionFunction.select(input);
            if (!output.isHasNext()){
                break;
            }
            XPartitionFunction.Input<Void, DateParam> nextInput =
                    new XPartitionFunction.Input<Void, DateParam>()
                    .setCurrentParam(output.getNextParam());
            Assert.assertEquals(input.getCurrentParam().getEndTime() ,
                    nextInput.getCurrentParam().getStartTime());
            input = nextInput;
        }
    }

}
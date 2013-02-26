package uk.ac.cam.cl.groupproject12.lima.hadoop;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class DosJobTest {
    MapDriver<LongWritable,Text,BytesWritable,FlowRecord> mapDriver1;
    ReduceDriver<BytesWritable,FlowRecord,BytesWritable,BytesWritable> reduceDriver1;
    MapReduceDriver<LongWritable,Text,BytesWritable,FlowRecord,BytesWritable,BytesWritable> mapReduceDriver1;

    MapDriver<BytesWritable,BytesWritable,BytesWritable,DosJob.DoSAttack> mapDriver2;
    ReduceDriver<BytesWritable,DosJob.DoSAttack,BytesWritable,DosJob.DoSAttack> reduceDriver2;
    MapReduceDriver<BytesWritable,BytesWritable,BytesWritable,DosJob.DoSAttack,BytesWritable,DosJob.DoSAttack> mapReduceDriver2;

    @BeforeClass
    public void setUp(){
        DosJob.Map1 mapper1 = new DosJob.Map1();
        DosJob.Reduce1 reducer1 = new DosJob.Reduce1();
        DosJob.Map2 mapper2 = new DosJob.Map2();
        DosJob.Reduce2 reducer2 = new DosJob.Reduce2();

        mapDriver1 = MapDriver.newMapDriver(mapper1);
        reduceDriver1 = ReduceDriver.newReduceDriver(reducer1);
        mapReduceDriver1 = MapReduceDriver.newMapReduceDriver(mapper1,reducer1);

        mapDriver2 = MapDriver.newMapDriver(mapper2);
        reduceDriver2 = ReduceDriver.newReduceDriver(reducer2);
        mapReduceDriver2 = MapReduceDriver.newMapReduceDriver(mapper2,reducer2);
    }

    @Test
    public void testReduce1SingleValue(){
        List<FlowRecord> values = new ArrayList<FlowRecord>();
        FlowRecord rec = new FlowRecord(new IP("1.1.1.1"),0L,1L,6,new IP("2.2.2.2"),new IP("0.0.0.0"),2,3,4,5,"a","a");
        values.add(rec);
        BytesWritable key = SerializationUtils.asBytesWritable(new IP("0.0.0.0"), new LongWritable(0), new IP("2.2.2.2"));
        reduceDriver1.withInput(key,values);
        reduceDriver1.withOutput(key,SerializationUtils.asBytesWritable(new DosJob.DoSAttack(new IP("1.1.1.1"), new LongWritable(0), new LongWritable(1), new IP("0.0.0.0"), new IntWritable(4), new LongWritable(5), new IntWritable(1), new IntWritable(1))));
        reduceDriver1.runTest();
    }

    @Test
    public void testReduce1MultipleValuesAggregation(){
//        FlowRecord(IP routerId, long startTime, long endTime, String protocol,
//                IP srcAddress, IP destAddress, int srcPort, int destPort,
//        int packets, long bytes, String tcpFlags, String typeOfService)

        List<FlowRecord> values = new ArrayList<FlowRecord>();
        values.add(new FlowRecord(new IP("1.1.1.1"),40L,1L,6,new IP("2.2.2.2"),new IP("0.0.0.0"),2,3,5,6,"",""));
        values.add(new FlowRecord(new IP("1.1.1.1"),50L,100L,6,new IP("2.2.2.2"),new IP("0.0.0.0"),2,3,4,7,"",""));
        values.add(new FlowRecord(new IP("1.1.1.1"),30L,3L,6,new IP("2.2.2.2"),new IP("0.0.0.0"),2,3,1,1,"",""));
        values.add(new FlowRecord(new IP("1.1.1.1"),20L,86L,6,new IP("2.2.2.2"),new IP("0.0.0.0"),2,3,1,1,"",""));
        reduceDriver1.withInput(SerializationUtils.asBytesWritable(new IP("0.0.0.0"), new LongWritable(0), new IP("2.2.2.2")),values);
        reduceDriver1.withOutput(SerializationUtils.asBytesWritable(new IP("0.0.0.0"), new LongWritable(0), new IP("2.2.2.2")),
                SerializationUtils.asBytesWritable(new DosJob.DoSAttack(new IP("1.1.1.1"), new LongWritable(20), new LongWritable(100), new IP("0.0.0.0"), new IntWritable(11), new LongWritable(15), new IntWritable(4), new IntWritable(1))));
        reduceDriver1.runTest();
    }
}

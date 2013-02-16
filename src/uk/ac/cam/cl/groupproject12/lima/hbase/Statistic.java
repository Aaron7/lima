package uk.ac.cam.cl.groupproject12.lima.hbase;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.Writable;

import uk.ac.cam.cl.groupproject12.lima.hadoop.FlowRecord;
import uk.ac.cam.cl.groupproject12.lima.hadoop.IP;

public class Statistic implements Writable
{
	private static final byte[] QUANTIFIER = "data".getBytes();
	private static final byte[] FAMILY = "family".getBytes(); 
	
	public static class Key extends AutoWritable
	{
		IP routerId;
		long timeFrame;
		
		public Key(IP routerId, long timeFrame) {
			super();
			this.routerId = routerId;
			this.timeFrame = timeFrame;
		}
		
		public Key() {
			super(); // constructor for deserialization
		}
	}
	
	Key key;
	int flowCount;
	int packetCount;
	long totalDataSize;
	int TCPCount;
	int UDPCount;
	int ICMPCount;
	
	public Statistic(IP routerId, Long timeframe) 
	{
		this.key = new Key(routerId, timeframe);
	}
	
	public void addFlowRecord(FlowRecord record)
	{
		this.flowCount ++;
		this.packetCount += record.packets;
		this.totalDataSize += record.bytes;
		
		if ("TCP".equals(record.protocol))
		{
			this.TCPCount ++;
		}
		else if ("UDP".equals(record.protocol))
		{
			this.UDPCount ++;
		}
		else if ("ICMP".equals(record.protocol))
		{
			this.ICMPCount ++;
		}
		else
		{
			//TODO log error?
		}
	}
	private byte[] getByteValue()
	{
		try {
			DataOutputBuffer out = new DataOutputBuffer();
			this.write(out);
			return out.getData();
		} catch (IOException e) 
		{
			throw new RuntimeException("Unexpected IO Exception",e);
		}
	}
	
	public void putToHbase()
	{
		Put put = new Put(this.key.getByteValue());
		put.add(FAMILY, QUANTIFIER, this.getByteValue());
		//HTable statistics = TODO
		// statistics.put(put);
	}
	
	private static Key readKey(DataInput in) throws IOException
	{
		Key key = new Key();
		key.readFields(in);
		return key;
	}
	
	@Override
	public void readFields(DataInput in) throws IOException {
		this.key = readKey(in);
		this.flowCount = in.readInt();
		this.packetCount = in.readInt();
		this.totalDataSize = in.readLong();
		this.TCPCount = in.readInt();
		this.UDPCount = in.readInt();
		this.ICMPCount = in.readInt();
	}
	

	@Override
	public void write(DataOutput out) throws IOException {
		this.key.write(out);
		out.writeInt(flowCount);
		out.writeInt(packetCount);
		out.writeLong(totalDataSize);
		out.writeInt(TCPCount);
		out.writeInt(UDPCount);
		out.writeInt(ICMPCount);
	}
}

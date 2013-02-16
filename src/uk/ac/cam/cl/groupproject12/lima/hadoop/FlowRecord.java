package uk.ac.cam.cl.groupproject12.lima.hadoop;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

/**
 * @author ernest
 *
 *	A class which acts as a container for information about a flow.
 */
public class FlowRecord implements Writable{
	
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public IP routerId;
	public long startTime;  	//in ms
	public long endTime;		//in ms
	public String protocol;
	public IP srcAddress;
	public IP destAddress;
	public int srcPort;
	public int destPort;
	public int packets;
	public long bytes;
	public String tcpFlags;
	public String typeOfService;
	
	
	
	public FlowRecord(IP routerId, long startTime, long endTime, String protocol,
			IP srcAddress, IP destAddress, int srcPort, int destPort,
			int packets, long bytes, String tcpFlags, String typeOfService) {
		super();
		this.routerId = routerId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.protocol = protocol;
		this.srcAddress = srcAddress;
		this.destAddress = destAddress;
		this.srcPort = srcPort;
		this.destPort = destPort;
		this.packets = packets;
		this.bytes = bytes;
		this.tcpFlags = tcpFlags;
		this.typeOfService = typeOfService;
	}

	static long valueOfDate(String string) throws ParseException
	{
		Date date = dateFormat.parse(string);
		return date.getTime();
	}
	
	
	static long valueOfBytes(String string)
	{
		if (string.matches("\\d\\+ M"))
		{
			String[] tokens = string.split(" ");
			Double prefix = Double.valueOf(tokens[0]);
			return (long)(prefix*1000000);
		}
		return Integer.valueOf(string);
	}
	
	public static FlowRecord valueOf(String str) throws ParseException
	{
		String[] tokens = str.split(" *, *");
		return new FlowRecord(
				IP.valueOf(tokens[0]),
				valueOfDate(tokens[1]),
				valueOfDate(tokens[2]),
				tokens[3],
				IP.valueOf(tokens[4]),
				IP.valueOf(tokens[5]),
				Integer.valueOf(tokens[6]),
				Integer.valueOf(tokens[7]),
				Integer.valueOf(tokens[8]),
				valueOfBytes(tokens[9]),
				tokens[10],
				tokens[11]);
	}

	@Override
	public void readFields(DataInput input) throws IOException 
	{
		this.routerId = IP.read(input);
		this.startTime = input.readLong();
		this.endTime = input.readLong();
		this.protocol = Text.readString(input);
		this.srcAddress.readFields(input);
		this.destAddress.readFields(input);
		this.srcPort = input.readInt();
		this.destPort = input.readInt();
		this.packets = input.readInt();
		this.bytes = input.readLong();
		this.tcpFlags = Text.readString(input);
		this.typeOfService = Text.readString(input);
		
	}

	@Override
	public void write(DataOutput output) throws IOException {
		routerId.write(output);
		output.writeLong(startTime);  	
		output.writeLong(endTime);				
		Text.writeString(output, protocol);
		srcAddress.write(output);
		destAddress.write(output);		
		output.writeInt(srcPort);
		output.writeInt(destPort);
		output.writeInt(packets);
		output.writeLong(bytes);
		Text.writeString(output, this.tcpFlags);
		Text.writeString(output, typeOfService);
	}
}

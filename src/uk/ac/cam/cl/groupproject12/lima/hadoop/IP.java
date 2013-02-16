package uk.ac.cam.cl.groupproject12.lima.hadoop;

import java.io.DataInput;
import java.io.IOException;

import org.apache.hadoop.io.Text;

public class IP extends AutoWritable {

	public Text value;
	
	public IP() 
	{
		// constructor for deserializing
	}
	
	public IP(String value) 
	{
		this.value = new Text(value);
	}
	
	public boolean isValid()
	{
		
		String[] tokens = this.value.toString().split("\\.");
		if (tokens.length != 4)
		{
			return false;
		}
		for(int i =0;i<tokens.length;i++)
		{
			try {
				int a = Integer.valueOf(tokens[i]);
				
				if (a < 0 || a > 255)
				{
					return false;
				}
			} catch (NumberFormatException e) 
			{
				return false;
			}
		}
		return true;
	}

	public static IP valueOf(String string) 
	{
		return new IP(string);
	}

	public static IP read(DataInput input) throws IOException
	{
		IP ip = new IP();
		ip.readFields(input);
		return ip;
	}
}

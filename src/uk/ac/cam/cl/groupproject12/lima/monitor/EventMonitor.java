package uk.ac.cam.cl.groupproject12.lima.monitor;

import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

/**
 * Manages the replication of data between HBase and PostgreSQL on completion of
 * a Hadoop M-R job.
 * 
 * @author Team Lima
 * 
 */
public class EventMonitor {

	Configuration hbaseConfig = HBaseConfiguration.create();
	Connection jdbcPGSQL = null;

	public EventMonitor() {
		hbaseConfig.set(Constants.HBASE_CONFIGURATION_ZOOKEEPER_QUORUM,
				"localhost");
		hbaseConfig.setInt(Constants.HBASE_CONFIGURATION_ZOOKEEPER_CLIENTPORT,
				2182);
		
		// Set up PGSQL connection
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			System.err.println(Constants.ERROR_POSTGRESQL_DRIVER_MISSING);
			System.exit(1);
		}
		
		Connection this.jdbcPGSQL = DriverManager.getConnection("jdbc:postgresql://hostname:port/dbname", username, password);)
	}

}

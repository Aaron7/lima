package uk.ac.cam.cl.groupproject12.lima.monitor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;

public class ThreatSynchroniser implements IDataSynchroniser {
	private int routerID;

	/**
	 * Constructs an instance of a threat synchroniser.
	 * 
	 * @param routerID
	 *            The router ID we are to synchronise threats for.
	 */
	public ThreatSynchroniser(int routerID) {
		this.routerID = routerID;
	}

    /**
     * Reads threats directly from HBase, pushing them into PostgreSQL for further visualisation via the web UI.
     *
     * @param monitor
     *            instance of the EventMonitor which contains the state required
     *            to interact with the various databases.
     *
     * @return A boolean indicating if the entire procedure was successful.
     * @throws SQLException
     */

	@Override
	public boolean synchroniseTables(EventMonitor monitor) throws SQLException {
		HTable table = null;
		try {
			table = new HTable(monitor.getHBaseConfig(), "Threat");

			// Row filter based on the router ID in the key. Substitutes in the
			// key separator.
			Filter routerIDFilter = new RowFilter(
					CompareFilter.CompareOp.EQUAL, new RegexStringComparator(
							String.format(this.routerID + "%s",
									Constants.HBASE_KEY_SEPARATOR)));

			Scan scan = new Scan();
			FilterList fl = new FilterList();
			fl.addFilter(routerIDFilter);
			ResultScanner scanner = table.getScanner(scan);

			for (Result r : scanner) {
				System.out.println("getRow:" + Bytes.toString(r.getRow()));
				for (KeyValue kv : r.raw()) {
					System.out.println("kv:" + kv + ", Key: "
							+ Bytes.toString(kv.getRow()));
				}
			}

			Connection c = monitor.jdbcPGSQL;

			String stmt = "INSERT INTO MESSAGES(eventID, routerIP, ip, type, status, message, createTS) VALUES (?,?,?,?,?,?,?)";
			PreparedStatement ps = c.prepareStatement(stmt);
			try {
				ps.setInt(1, 0);
				ps.setInt(2, 0);
				ps.setInt(3, 0);
				ps.setString(4, "");
				ps.setString(5, "");
				ps.setString(6, "");
				ps.setLong(7, 0L);

				ps.executeUpdate();
			} finally {
				ps.close();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (table != null)
					table.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return false;
	}
}
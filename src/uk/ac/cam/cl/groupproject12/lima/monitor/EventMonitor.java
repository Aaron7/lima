package uk.ac.cam.cl.groupproject12.lima.monitor;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.hbase.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import uk.ac.cam.cl.groupproject12.lima.hadoop.*;
import uk.ac.cam.cl.groupproject12.lima.hbase.*;
import uk.ac.cam.cl.groupproject12.lima.monitor.database.*;
import uk.ac.cam.cl.groupproject12.lima.web.*;

import javax.xml.parsers.*;
import java.io.*;
import java.sql.*;

/**
 * Manages the replication of data between HBase and PostgreSQL on completion of a Hadoop MapReduce job.
 */
public class EventMonitor implements Runnable {
    /*
	 * TODO: Call Web.updateJob(routerIp, timestamp, true); when we have updated
	 * stuff to postgreSQL after the set of map reduce jobs for that router
	 */

    Configuration hbaseConfig = HBaseConfiguration.create();
    Connection jdbcPGSQL = null;

    // Instance of the synchroniser for this monitor to run
    IDataSynchroniser synchroniser = null;

    private EventMonitor(HBaseConnectionDetails hbaseConf,
                         IDataSynchroniser synchroniser) throws PostgreSQLConfigurationException,
            SQLException {

        this.synchroniser = synchroniser;

        hbaseConfig.set(HBaseConstants.HBASE_CONFIGURATION_ZOOKEEPER_QUORUM,
                hbaseConf.getHost());
        hbaseConfig.setInt(HBaseConstants.HBASE_CONFIGURATION_ZOOKEEPER_CLIENTPORT,
                hbaseConf.getPort());

        // Set up PGSQL connection
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println(MonitorConstants.ERROR_POSTGRESQL_DRIVER_MISSING);
            System.exit(1);
        }

        try {
            PostgreSQLConnectionDetails pgsqlConn = getPostgresConnection();

            this.jdbcPGSQL = DriverManager.getConnection(
                    String.format(MonitorConstants.PGSQL_CONNECTION_STRING,
                            pgsqlConn.getHost(), pgsqlConn.getPort(),
                            pgsqlConn.getDbname()), pgsqlConn.getUsername(),
                    pgsqlConn.getPassword());
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    Configuration getHBaseConfig() {
        return this.hbaseConfig;
    }

    Connection getPGSQLConnection() {
        return this.jdbcPGSQL;
    }

    // Parses the XML file in a well-defined location to obtain PGSQL connection
    // information. Returns a PostgreSQLConnectionDetails object containing such
    // details.
    private static PostgreSQLConnectionDetails getPostgresConnection()
            throws PostgreSQLConfigurationException {
        File fXmlFile = new File(String.format(
                MonitorConstants.PGSQL_CONNECTION_XML_LOCATION,
                System.getProperty("user.dir")));
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();

            // Get the PGSQL connection details
            NodeList PGSQLConnectionInfo = doc.getElementsByTagName("pgsql");

            // Too many configuration entries! Cannot decide on one particular
            // one deterministically... which PGSQL are we to use? Abort and
            // report this error.
            if (PGSQLConnectionInfo.getLength() != 1) {
                throw new PostgreSQLConfigurationException(
                        MonitorConstants.ERROR_POSTGRESQL_CONFIG_NOT_ONE);
            }

            Node node = PGSQLConnectionInfo.item(0);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element nodeElement = (Element) node;

                String hostname = nodeElement.getElementsByTagName("host")
                        .item(0).getTextContent();
                int port = Integer.parseInt(nodeElement
                        .getElementsByTagName("port").item(0).getTextContent());
                String username = nodeElement.getElementsByTagName("username")
                        .item(0).getTextContent();
                String password = nodeElement.getElementsByTagName("password")
                        .item(0).getTextContent();
                String dbName = nodeElement.getElementsByTagName("dbName")
                        .item(0).getTextContent();

                if (hostname == null || port == 0 || username == null
                        || password == null || dbName == null) {
                    throw new PostgreSQLConfigurationException(
                            MonitorConstants.ERROR_POSTGRESQL_CONFIG_MALFORMED);
                } else {
                    return new PostgreSQLConnectionDetails(hostname, port,
                            dbName, username, password);
                }
            }

            throw new PostgreSQLConfigurationException(
                    MonitorConstants.ERROR_POSTGRESQL_CONFIG_MALFORMED);
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // If it gets to this point without returning, something's horribly
        // wrong.
        return null;
    }

    public static void main(String[] args) throws PostgreSQLConfigurationException,
            SQLException {
        // long time = System.currentTimeMillis();
        // Threat t = new Threat(new LongWritable(time), new IP("1.2.3.4"),
        // EventType.landAttack, new LongWritable(444L));
        // t.setDestIP(new IP("6.7.8.9"));
        // t.setEndTime(new LongWritable(667L));
        // t.setFlowCount(new IntWritable(678));
        // t.setPacketCount(new IntWritable(11123));
        // t.setFlowDataTotal(new LongWritable(622L));
        // t.setSrcIP(new IP("66.22.11.55"));
        //
        // try {
        // HBaseAutoWriter.put(t);
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        //
        // new EventMonitor(new HBaseConnectionDetails("localhost", 2182),
        // new ThreatSynchroniser("1.2.3.4", time));

        // for (int n = 0; n < 50; n++) {
        // Statistic s = new Statistic(new IP("1.2.3.4"), new
        // LongWritable(System.currentTimeMillis()), new IntWritable(1), new
        // IntWritable(1), new LongWritable(1), new IntWritable(0), new
        // IntWritable(0), new IntWritable(0));
        // try {
        // HBaseAutoWriter.put(s);
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // try {
        // //Thread.sleep(100);
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // System.out.println(n);
        // }

        new EventMonitor(new HBaseConnectionDetails("localhost", 2182),
                new StatisticsSynchroniser("1.2.3.4"));
    }

    /**
     * Invokes and runs synchronisers on a hardcoded list of tables.
     *
     * @param routerIP
     *         Router to synchronise.
     * @param hbaseConf
     *         Configuration data to be able to connect to HBase
     * @param timeProcessed
     *         Timestamp carried from the start of the processing phase.
     */
    public static void doSynchronise(IP routerIP,
                                     HBaseConnectionDetails hbaseConf, long timeProcessed) {

        EventMonitor threat = null;
        EventMonitor stats = null;

        // Thread for Threat synchronisation
        try {
            threat = new EventMonitor(hbaseConf, new ThreatSynchroniser(
                    routerIP.getValue().toString(), timeProcessed));
        } catch (PostgreSQLConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO
            e.printStackTrace();
        }

        try {
            stats = new EventMonitor(hbaseConf, new StatisticsSynchroniser(
                    routerIP.getValue().toString()));
        } catch (PostgreSQLConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO
            e.printStackTrace();
        }

        // Invoke threads
        Thread threatThread = new Thread(threat);
        Thread statsThread = new Thread(stats);
        threatThread.start();
        statsThread.start();

        // Wait for both to terminate
        try {
            threatThread.join();
            statsThread.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Call the web GUI to notify it of the new data
        Web.updateJob(routerIP.getValue().toString(), timeProcessed, true);

        // Done!
    }

    /**
     * Invokes the synchronisation method of the synchroniser.
     */
    @Override
    public void run() {
        try {
            this.synchroniser.synchroniseTables(this);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
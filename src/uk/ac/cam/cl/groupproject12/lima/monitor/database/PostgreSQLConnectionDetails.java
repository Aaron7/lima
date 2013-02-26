package uk.ac.cam.cl.groupproject12.lima.monitor.database;

/**
 * Stores information required to invoke a connection to PGSQL.
 * 
 * @author Team Lima
 */
public class PostgreSQLConnectionDetails {

	private String host;
	private int port;
	private String dbname;
	private String username;
	private String password;

	/**
	 * Construct a new PostgreSQLConnectionDetails with the information about
	 * connecting to PGSQL.
     *
     * @param host  Hostname of the PostgreSQL server.
     * @param port  Port number on which PostgreSQL is listening.
	 * @param username  Username used to log into PostgreSQL.
	 * @param password  Password for the PostgreSQL user.
	 */
	public PostgreSQLConnectionDetails(String host, int port, String dbname,
			String username, String password) {
		super();
		this.host = host;
		this.port = port;
		this.dbname = dbname;
		this.username = username;
		this.password = password;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the dbname
	 */
	public String getDbname() {
		return dbname;
	}
}

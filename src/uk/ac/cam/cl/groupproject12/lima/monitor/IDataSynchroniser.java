package uk.ac.cam.cl.groupproject12.lima.monitor;

/**
 * An interface to describe classes which implement the logic to replicate data
 * from HBase to PostgreSQL.
 * 
 * @author Team Lima
 * 
 */
public interface IDataSynchroniser {

	/**
	 * Synchronises tables from HBase to PostgreSQL in accordance with the rules
	 * for a particular implementation (one implementation per table / dataset
	 * to replicate).
	 * 
	 * @return true on success, false on failure
	 */
	boolean synchroniseTables();

}
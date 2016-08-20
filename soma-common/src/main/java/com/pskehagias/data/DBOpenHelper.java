package com.pskehagias.data;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by pkcyr on 8/10/2016.
 * A base class to facilitate opening jdbc connections, based in part on how Android
 * handles SQLite connections.
 */
public abstract class DBOpenHelper {

    protected int mVersion;
    protected String mName;
    protected boolean mAllowVersionChange;

    /**
     * Constructs a DBOpenHelper for connecting to the database indicated by the name and version parameters.
     * @param version The target database schema version that should be returned by
     *                getConnection.
     * @param name The name of the database to open a connection for.
     * @param forceVersionChange If set to true, getConnection will automatically upgrade/downgrade
     *                           as required by the requested version number.  If false,
     *                           getConnection will throw SQLException when there is a version mismatch.
     *                           This parameter is ignored on the initial creation of the database.
     */
    public DBOpenHelper(int version, String name, boolean forceVersionChange){
        mVersion = version;
        mName = name;
        mAllowVersionChange = forceVersionChange;
    }

    /**
     * Returns a connection to the implementation specific RDBMS system, i.e. SQLite, MySQL, etc.
     * @return A Connection object to the database.
     * @throws SQLException
     */
    protected abstract Connection establishConnection() throws SQLException;

    /**
     * Returns a java.sql.Connection object to the requested database, after performing any necessary
     * upgrade/downgrades and calling both onConfigure and onOpen.
     * @return The Connection object for the requested database.
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException{
        Connection connection = establishConnection();

        int currentVersion = getVersion(connection);
        if(currentVersion == 0){
            onCreate(connection);
            setVersion(connection, mVersion);
        }else if(currentVersion < mVersion){
            onUpgrade(connection, currentVersion, mVersion);
            setVersion(connection, mVersion);
        }else if(currentVersion > mVersion){
            onDowngrade(connection, currentVersion, mVersion);
            setVersion(connection, mVersion);
        }

        onConfigure(connection);
        onOpen(connection);

        return connection;
    }

    /**
     * Return the current schema version for the connected database.
     * This must be overwritten based on how your RDBMS will be used to store the version.
     * A value of 0 should be returned for a new un-versioned database.
     * @param db The database connection being prepared.
     * @return The current schema version for the connected database
     * @throws SQLException
     */
    public abstract int getVersion(Connection db) throws SQLException;

    /**
     * Set the current schema version for the connected database after performing an upgrade/downgrade.
     * This must be overwritten based on how your RDBMS will be used to store the version.
     * A value of 0 is reserved for a new un-versioned database.
     * @param db The database connection being prepared.
     * @param version The new schema version for the database.
     * @throws SQLException
     */
    public abstract void setVersion(Connection db, int version) throws SQLException;

    /**
     * Executed after the database is upgraded/downgraded.
     * Override to set default parameters on the database connection.
     * @param db The database connection being prepared.
     * @throws SQLException
     */
    public void onConfigure(Connection db) throws SQLException{}

    /**
     * Executed after a connection is established, and the database has been upgraded/downgraded.
     * Override to execute SQL before db is returned to the caller.
     * @param db The database connection being prepared.
     * @throws SQLException
     */
    protected void onOpen(Connection db) throws SQLException{}

    /**
     * Executed on a new database to create the schema.
     * @param db The database being created.
     * @throws SQLException
     */
    protected abstract void onCreate(Connection db) throws SQLException;

    /**
     * Executed on a database when the requested version is greater than the existing version.
     * @param db The database connection being upgraded.
     * @param oldVersion The current version of the database schema.
     * @param newVersion The requested version of the database schema.
     * @throws SQLException
     */
    protected abstract void onUpgrade(Connection db, int oldVersion, int newVersion) throws SQLException;

    /**
     * Executed on a database when the requested version is less than the existing version.
     * By default this call throws an SQLException.
     * @param db The database being downgraded.
     * @param oldVersion The current version of the database schema.
     * @param newVersion The requested version of the database schema.
     * @throws SQLException Thrown by default unless overrode by child class.
     */
    protected void onDowngrade(Connection db, int oldVersion, int newVersion) throws SQLException
    {
        throw new SQLException("Database can't be downgraded.");
    }

}

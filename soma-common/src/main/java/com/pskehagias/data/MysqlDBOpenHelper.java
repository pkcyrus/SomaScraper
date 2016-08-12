package com.pskehagias.data;

import java.sql.*;
import java.util.Properties;

/**
 * Created by pkcyr on 8/10/2016.
 * A MySQL specific implementation of DBOpenHelper.  This requires a valid MySQL driver to utilize.
 */
public abstract class MysqlDBOpenHelper extends DBOpenHelper {
    protected String mHost;
    protected String mPort;
    private boolean attemptedToCreatePropertiesTable = false;

    /**
     * Constructs a MysqlDBOpenHelper for connecting to the database indicated by the parameters.
     * This is a convenience constructor for defaulting to allowing upgrade/downgrade on the schema.
     *
     * @param version            The target database schema version that should be returned by
     *                           getConnection.
     * @param name               The name of the database to open a connection for.
     * @param host               The hostname or ip address of the requested database.
     * @param port               The port for the requested database.
     */
    public MysqlDBOpenHelper(int version, String name, String host, String port) {
        this(version, name, true, host, port);
    }

    /**
     * Constructs a MysqlDBOpenHelper for connecting to the database indicated by the parameters.
     *
     * @param version            The target database schema version that should be returned by
     *                           getConnection.
     * @param name               The name of the database to open a connection for.
     * @param forceVersionChange If set to true, getConnection will automatically upgrade/downgrade
     *                           as required by the requested version number.  If false,
     *                           getConnection will throw SQLException when there is a version mismatch.
     */
    public MysqlDBOpenHelper(int version, String name, boolean forceVersionChange, String host, String port) {
        super(version, name, forceVersionChange);
        mHost = host;
        mPort = port;
    }

    protected abstract Properties getCredentials();

    @Override
    protected Connection establishConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" + mHost + ':' + mPort + '/' + mName, getCredentials());
    }

    private void createPropertiesTable(Connection db) throws SQLException{
        String create_properties_table =
                "CREATE TABLE IF NOT EXISTS db_properties (\n" +
                        "_id INTEGER PRIMARY KEY AUTO_INCREMENT, \n" +
                        "kee TEXT NOT NULL, \n" +
                        "value TEXT)";
        String set_version_zero =
                "INSERT IGNORE INTO db_properties (kee,value) VALUES ('db_version','0')";

        try(PreparedStatement statement = db.prepareStatement(create_properties_table)){
            statement.executeUpdate();
        }
        try(PreparedStatement statement = db.prepareStatement(set_version_zero)){
            statement.executeUpdate();
        }
    }

    @Override
    public int getVersion(Connection db) throws SQLException {
        String select_version_property =
                "SELECT value FROM db_properties WHERE kee='db_version'";
        int rval = 0;
        try(PreparedStatement statement = db.prepareStatement(select_version_property);
            ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                rval = Integer.parseInt(resultSet.getString(1));
            }
        }catch (SQLException e){
            // If the first query failed, attempt to create the db_properties table and try again
            // If we've already attempted to create db_properties, throw the exception up the chain.
            if(!attemptedToCreatePropertiesTable) {
                attemptedToCreatePropertiesTable = true;
                createPropertiesTable(db);
                rval = getVersion(db);
            }else {
                throw e;
            }
        }
        return rval;
    }

    @Override
    public void setVersion(Connection db, int version) throws SQLException {
        String set_version_property =
                "UPDATE db_properties SET value=? WHERE kee='db_version'";
        try(PreparedStatement statement = db.prepareStatement(set_version_property)){
            statement.setString(1,((Integer)version).toString());
            statement.executeUpdate();
        }
    }
}

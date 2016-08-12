package com.pskehagias.data;

import java.sql.*;

/**
 * Created by Peter on 5/11/2016.
 * An SQLite3 specific implementation of DBOpenHelper.  This requires a valid SQLite3 driver to utilize.
 */
public abstract class SQLiteDBOpenHelper extends DBOpenHelper {
    /**
     * Constructs a SQLiteDBOpenHelper for connecting to the database indicated by the name and version parameters.
     * This is a convenience constructor for defaulting to allowing upgrade/downgrade on the schema.
     * @param version            The target database schema version that should be returned by
     *                           getConnection.
     * @param name               The name of the database to open a connection for.
     */
    public SQLiteDBOpenHelper(int version, String name){
        super(version, name, true);
    }

    /**
     * Constructs a SQLiteDBOpenHelper for connecting to the database indicated by the name and version parameters.
     *
     * @param version            The target database schema version that should be returned by
     *                           getConnection.
     * @param name               The name of the database to open a connection for.
     * @param forceVersionChange If set to true, getConnection will automatically upgrade/downgrade
     *                           as required by the requested version number.  If false,
     *                           getConnection will throw SQLException when there is a version mismatch.
     */
    public SQLiteDBOpenHelper(int version, String name, boolean forceVersionChange){
        super(version, name, forceVersionChange);
    }


    @Override
    public Connection establishConnection() throws SQLException{
        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+mName);
        return connection;
    }

    @Override
    public int getVersion(Connection db) throws SQLException{
        int result = -1;
        Statement statement = db.createStatement();
        ResultSet resultSet = statement.executeQuery("PRAGMA user_version");
        result = resultSet.getInt(1);
        resultSet.close();
        statement.close();
        return result;
    }

    @Override
    public void setVersion(Connection db, int version) throws SQLException{
        Statement statement = db.createStatement();
        statement.executeUpdate("PRAGMA user_version = "+version);
        statement.close();
    }
}

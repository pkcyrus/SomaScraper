package soma.data;

import java.sql.*;

/**
 * Created by Peter on 5/11/2016.
 */
public abstract class DBOpenHelper {
    private int mVersion;
    private String mName;

    public DBOpenHelper(int version, String name){
        mVersion = version;
        mName = name;
    }

    public Connection getConnection() throws SQLException{
        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+mName);

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

    public int getVersion(Connection db) throws SQLException{
        int result = -1;
        Statement statement = db.createStatement();
        ResultSet resultSet = statement.executeQuery("PRAGMA user_version");
        result = resultSet.getInt(1);
        resultSet.close();
        statement.close();
        return result;
    }

    public void setVersion(Connection db, int version) throws SQLException{
        Statement statement = db.createStatement();
        statement.executeUpdate("PRAGMA user_version = "+version);
        statement.close();
    }

    public void onConfigure(Connection db) throws SQLException{}
    public void onOpen(Connection db) throws SQLException{}

    public abstract void onCreate(Connection db) throws SQLException;
    public abstract void onUpgrade(Connection db, int oldVersion, int newVersion) throws SQLException;

    public void onDowngrade(Connection db, int oldVersion, int newVersion) throws SQLException {
        throw new SQLException("Database can't be downgraded");
    }
}

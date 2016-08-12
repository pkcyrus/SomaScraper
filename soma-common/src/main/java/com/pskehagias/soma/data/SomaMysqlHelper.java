package com.pskehagias.soma.data;

import com.pskehagias.data.MysqlDBOpenHelper;
import com.pskehagias.soma.data.schema.SomaMysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Created by pkcyr on 8/10/2016.
 */
public class SomaMysqlHelper extends MysqlDBOpenHelper {
    private Properties credentials;

    /**
     * Constructs a SomaMysqlHelper which returns connections to host:port/dbName
     * @param host The hostname or ip address of the mysql server, i.e. "localhost" or "192.168.1.20".
     * @param port The port of the mysql server, i.e. "3306"
     */
    public SomaMysqlHelper(String host, String port){
        super(1, "somascraper", host, port);
        credentials = new Properties();
    }

    public void setCredentials(String user, String password){
        credentials.put("user", user);
        credentials.put("password", password);
    }

    @Override
    protected Properties getCredentials() {
        return credentials;
    }

    @Override
    protected void onCreate(Connection db) throws SQLException {
        db.setAutoCommit(false);
        Statement statement = db.createStatement();
        statement.executeUpdate(SomaMysql.T_ARTIST);
        statement.executeUpdate(SomaMysql.T_ALBUM);
        statement.executeUpdate(SomaMysql.T_SONG);
        statement.executeUpdate(SomaMysql.T_CHANNEL);
        statement.executeUpdate(SomaMysql.T_PLAY);
        statement.close();
        db.commit();
        db.setAutoCommit(true);
    }

    @Override
    protected void onUpgrade(Connection db, int oldVersion, int newVersion) throws SQLException {

    }
}

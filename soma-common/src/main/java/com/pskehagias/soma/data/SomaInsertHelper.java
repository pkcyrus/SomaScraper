package com.pskehagias.soma.data;

import com.pskehagias.data.DBOpenHelper;
import com.pskehagias.soma.common.Channel;
import com.pskehagias.soma.common.Play;

import java.io.Closeable;
import java.sql.*;

/**
 * Created by pkcyr on 8/10/2016.
 *
 *
 */
public class SomaInsertHelper implements Closeable{
    private DBOpenHelper dbOpenHelper;
    private Connection connection;

    public SomaInsertHelper(DBOpenHelper openHelper){
        this.dbOpenHelper = openHelper;
        this.connection = null;
    }

    public void begin() throws SQLException{
        if(connection != null){
            return;
        }

        connection = dbOpenHelper.getConnection();
        connection.setAutoCommit(false);
    }

    public void commit() throws SQLException{
        if(connection == null){
            return;
        }
        connection.commit();
    }

    public void close(){
        try {
            connection.commit();
            connection.close();
            connection = null;
        } catch (SQLException e) {
            System.err.println("Error closing database connection.");
            e.printStackTrace();
        }
    }

    public static final String ADD_PLAY =
            "INSERT  IGNORE INTO plays (timestamp, song_id, channel_id) " +
                    "SELECT ?, songs._id, channels._id " +
                    "FROM songs inner join channels where channels.name = ? and songs.name = ?";
    public static final String ADD_SONG =
            "INSERT  IGNORE INTO songs (name, album_id, artist_id) " +
                    "SELECT ?, albums._id,artists._id  " +
                    "from albums inner join artists on albums.artist_id = artists._id " +
                    "where albums.name=? and artists.name=?";
    public static final String ADD_ALBUM =
            "INSERT  IGNORE INTO albums ( artist_id, name ) " +
                    "values ((SELECT _id FROM artists WHERE name=?),? )";
    public static final String ADD_ARTIST =
            "INSERT  IGNORE INTO artists ( name ) values ( ? )";
    public static final String ADD_CHANNEL =
            "INSERT  IGNORE INTO channels ( name, pl_url ) values ( ?,? )";

    public long addChannel(Channel c) throws SQLException{
        if(connection == null){
            throw new SQLException("No connection established to the database");
        }

        try(PreparedStatement statement = connection.prepareStatement(ADD_CHANNEL, Statement.RETURN_GENERATED_KEYS)){
            statement.setString(1,c.getName());
            statement.setString(2,c.getUrl());
            return executeReturnId(statement);
        }
    }

    public long addPlay(Play play) throws SQLException{
        if(connection == null){
            throw new SQLException("No connection established to the database");
        }

        try(PreparedStatement statement = connection.prepareStatement(ADD_PLAY, Statement.RETURN_GENERATED_KEYS)){
            statement.setLong(1, play.getTimestamp());
            statement.setString(2, play.getChannel());
            statement.setString(3, play.getSong());
            System.out.println(statement);
            return executeReturnId(statement);
        }
    }

    public long addSong(String name, String album, String artist) throws SQLException{
        if(connection == null){
            throw new SQLException("No connection established to the database");
        }

        try(PreparedStatement statement = connection.prepareStatement(ADD_SONG, Statement.RETURN_GENERATED_KEYS)){
            statement.setString(1, name);
            statement.setString(2, album);
            statement.setString(3, artist);
            return executeReturnId(statement);
        }
    }

    public long addAlbum(String name, String artist) throws SQLException{
        if(connection == null){
            throw new SQLException("No connection established to the database");
        }

        try(PreparedStatement statement = connection.prepareStatement(ADD_ALBUM, Statement.RETURN_GENERATED_KEYS)){
            statement.setString(1,artist);
            statement.setString(2,name);
            return executeReturnId(statement);
        }
    }

    public long addArtist(String name) throws SQLException{
        if(connection == null){
            throw new SQLException("No connection established to the database");
        }

        try(PreparedStatement statement = connection.prepareStatement(ADD_ARTIST, Statement.RETURN_GENERATED_KEYS)){
            statement.setString(1, name);
            return  executeReturnId(statement);
        }
    }

    private long executeReturnId(PreparedStatement statement) throws SQLException{
        long result = 0;
        int rowsAffected = statement.executeUpdate();
        if(rowsAffected == 0){
            result = -1;
        }else{
            try(ResultSet generatedKeys = statement.getGeneratedKeys()){
                if(generatedKeys.next()){
                    result = generatedKeys.getLong(1);
                }
            }
        }
        return result;
    }
}

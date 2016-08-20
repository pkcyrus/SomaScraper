package com.pskehagias.soma.data;

import com.pskehagias.data.DBOpenHelper;
import com.pskehagias.soma.common.Channel;
import com.pskehagias.soma.common.Play;

import java.sql.*;

/**
 * Created by pkcyr on 8/10/2016.
 *
 *
 */
public class SomaInsertHelper implements InsertHelper{
    protected DBOpenHelper dbOpenHelper;
    protected Connection connection;

    public SomaInsertHelper(DBOpenHelper openHelper) throws SQLException{
        this.dbOpenHelper = openHelper;
        this.connection = dbOpenHelper.getConnection();
    }

    @Override
    public void close() throws SQLException{
        if(connection != null) {
            connection.close();
            connection = null;
        }
    }

    public String addSongQuery() {
        return "INSERT  IGNORE INTO songs (name, album_id, artist_id) " +
                "SELECT ?, albums._id,artists._id  " +
                "from albums inner join artists on albums.artist_id = artists._id " +
                "where albums.name=? and artists.name=?";
    }

    public String addAlbumQuery() {
        return "INSERT  IGNORE INTO albums ( artist_id, name ) " +
                "values ((SELECT _id FROM artists WHERE name=?),? )";
    }

    public String addArtistQuery() {
        return "INSERT  IGNORE INTO artists ( name ) values ( ? )";
    }

    public String addChannelQuery() {
        return "INSERT  IGNORE INTO channels ( name, pl_url ) values ( ?,? )";
    }

    public String addPlayQuery(){
        return "INSERT  IGNORE INTO plays (timestamp, song_id, channel_id) " +
                "SELECT ?, songs._id, channels._id " +
                "FROM songs inner join channels where channels.name = ? and songs.name = ?";
    }

    @Override
    public long addChannel(String name, String pl_url) throws SQLException{
        if(connection == null){
            throw new SQLException("No connection established to the database");
        }

        try(PreparedStatement statement = prepareStatement(addChannelQuery())){//connection.prepareStatement(addChannelQuery(), Statement.RETURN_GENERATED_KEYS)){
            statement.setString(1,name);
            statement.setString(2,pl_url);
            return executeReturnId(statement);
        }
    }

    @Override
    public long addChannel(Channel c) throws SQLException{
        return addChannel(c.getName(), c.getUrl());
    }

    @Override
    public long addPlay(String song, String channel, long timestamp) throws SQLException{
        if(connection == null){
            throw new SQLException("No connection established to the database");
        }

        try(PreparedStatement statement = prepareStatement(addPlayQuery())){//connection.prepareStatement(addPlayQuery(), Statement.RETURN_GENERATED_KEYS)){
            statement.setLong(1, timestamp);
            statement.setString(2, channel);
            statement.setString(3, song);
            return executeReturnId(statement);
        }
    }

    @Override
    public long addPlay(Play play) throws SQLException{
        return addPlay(play.getSong(), play.getChannel(), play.getTimestamp());
    }

    @Override
    public long addSong(String name, String album, String artist) throws SQLException{
        if(connection == null){
            throw new SQLException("No connection established to the database");
        }

        try(PreparedStatement statement = prepareStatement(addSongQuery())){//connection.prepareStatement(addSongQuery(), Statement.RETURN_GENERATED_KEYS)){
            statement.setString(1, name);
            statement.setString(2, album);
            statement.setString(3, artist);
            return executeReturnId(statement);
        }
    }

    @Override
    public long addAlbum(String name, String artist) throws SQLException{
        if(connection == null){
            throw new SQLException("No connection established to the database");
        }

        try(PreparedStatement statement = prepareStatement(addAlbumQuery())){//connection.prepareStatement(addAlbumQuery(), Statement.RETURN_GENERATED_KEYS)){
            statement.setString(1,artist);
            statement.setString(2,name);
            return executeReturnId(statement);
        }
    }

    @Override
    public long addArtist(String name) throws SQLException{
        if(connection == null){
            throw new SQLException("No connection established to the database");
        }

        try(PreparedStatement statement = prepareStatement(addArtistQuery())){//connection.prepareStatement(addArtistQuery(), Statement.RETURN_GENERATED_KEYS)){
            statement.setString(1, name);
            return  executeReturnId(statement);
        }
    }

    protected PreparedStatement prepareStatement(String query) throws SQLException{
        return connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
    }

    protected long executeReturnId(PreparedStatement statement) throws SQLException{
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

package com.pskehagias.soma.data;

import com.pskehagias.soma.common.Play;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter on 6/10/2016.
 */
public class SomaDBManager extends SomaInsertHelper{
    public SomaDBManager() throws SQLException {
        super(new SomaSQLiteHelper());
    }

    @Override
    public String addPlayQuery() {
        return "INSERT INTO plays (timestamp, song_id, channel_id) " +
                "SELECT ?, songs._id, channels._id " +
                "FROM songs inner join channels where channels.name = ? and songs.name = ?";
    }

    @Override
    public String addChannelQuery() {
        return "INSERT INTO channels ( name, pl_url ) values ( ?,? )";
    }

    @Override
    public String addArtistQuery() {
        return "INSERT INTO artists ( name ) values ( ? )";
    }

    @Override
    public String addAlbumQuery() {
        return "INSERT INTO albums ( artist_id, name ) "+
                "values ((SELECT _id FROM artists WHERE name=?),? )";
    }

    @Override
    public String addSongQuery() {
        return "INSERT INTO songs (name, album_id, artist_id) " +
                "SELECT ?, albums._id,artists._id  " +
                "from albums inner join artists on albums.artist_id = artists._id " +
                "where albums.name=? and artists.name=?";
    }

    final String SELECT_PLAYLIST =
            "SELECT timestamp, artists.name, albums.name, songs.name, song_ratings.rating " +
                    ", songs._id, albums._id, artists._id, channel_id "
                    + "FROM plays "
                    + "INNER JOIN songs ON plays.song_id = songs._id "
                    + "INNER JOIN albums ON songs.album_id = albums._id "
                    + "INNER JOIN artists ON songs.artist_id = artists._id "
                    + "INNER JOIN song_ratings ON song_ratings._id = songs._id "
                    + "WHERE channel_id = (SELECT _id FROM channels WHERE name=?) "
                    + "AND song_ratings.rating >= ?";

    public List<Play> getPlaylist(String channel) {
        return getPlaylist(channel, 0);
    }

    public List<Play> getPlaylist(String channel, int minRating) {
        List<Play> result = new ArrayList<>(128);
        try (PreparedStatement statement = connection.prepareStatement(SELECT_PLAYLIST)) {
            statement.setString(1, channel);
            statement.setInt(2, minRating);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(resultSetToPlay(resultSet, channel));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Play resultSetToPlay(ResultSet resultSet, String channel) throws SQLException {
        long time = resultSet.getLong(1);
        String artist = resultSet.getString(2);
        String album = resultSet.getString(3);
        String song = resultSet.getString(4);
        int rating = resultSet.getInt(5);
        long songId = resultSet.getLong(6);
        long albumId = resultSet.getLong(7);
        long artistId = resultSet.getLong(8);
        long channelId = resultSet.getLong(9);
        return new Play(time, artist, album, song, channel, rating, songId, artistId, albumId, channelId);
    }

    final String SELECT_PLAYLIST_DATE_RANGE =
            "SELECT timestamp, artists.name, albums.name, songs.name, song_ratings.rating " +
                    ", songs._id, albums._id, artists._id, channel_id "
                    + "FROM plays "
                    + "INNER JOIN songs ON plays.song_id = songs._id "
                    + "INNER JOIN albums ON songs.album_id = albums._id "
                    + "INNER JOIN artists ON songs.artist_id = artists._id "
                    + "INNER JOIN song_ratings ON song_ratings._id = songs._id "
                    + "WHERE channel_id = (SELECT _id FROM channels WHERE name=?) "
                    + "AND timestamp > ? AND timestamp <= ? "
                    + "AND song_ratings.rating >= ?";

    public List<Play> getPlaylist(String channel, long min_stamp, long max_stamp) throws SQLException{
        return getPlaylist(channel, min_stamp, max_stamp, 0);
    }

    public List<Play> getPlaylist(String channel, long min_stamp, long max_stamp, int minRating) throws SQLException{
        if(connection == null){
            throw new SQLException("No connection established to the database");
        }

        List<Play> result = new ArrayList<>(128);
        try (PreparedStatement statement = connection.prepareStatement(SELECT_PLAYLIST_DATE_RANGE)) {
            statement.setString(1, channel);
            statement.setLong(2, min_stamp);
            statement.setLong(3, max_stamp);
            statement.setInt(4, minRating);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(resultSetToPlay(resultSet, channel));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static final String ADD_SONG_RATING =
            "INSERT INTO song_ratings (_id,rating) "
            +"VALUES(?,?)";
    public static final String ADD_SONG_FTS =
            "INSERT INTO fts_tracks (docid,artist,album,song)" +
            "VALUES(?,?,?,?)";

    @Override
    public long addSong(String song, String album, String artist) throws SQLException{
        long song_id = super.addSong(song, album, artist);

        if(song_id > 0) {
            try (PreparedStatement ftsStatement = connection.prepareStatement(ADD_SONG_FTS)) {
                ftsStatement.setLong(1, song_id);
                ftsStatement.setString(2, song);
                ftsStatement.setString(3, album);
                ftsStatement.setString(4, artist);
                ftsStatement.executeUpdate();
            }
            try (PreparedStatement ratingStatement = connection.prepareStatement(ADD_SONG_RATING)) {
                ratingStatement.setLong(1, song_id);
                ratingStatement.setInt(2, 0);
                ratingStatement.executeUpdate();
            }
        }

        return song_id;
    }

    public static final String UPDATE_RATING =
            "UPDATE song_ratings SET rating = ? WHERE _id= ?";

    public long updateRating(Play item) throws SQLException{
        if(connection == null){
            throw new SQLException("No connection established to the database");
        }
        long result = -1;
        try(PreparedStatement statement = connection.prepareStatement(UPDATE_RATING)){
            statement.setInt(1,item.getRating());
            statement.setLong(2,item.getSongId());
            result = executeReturnId(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }



    public static final String SELECT_CHANNEL =
            "SELECT _id,name,pl_url FROM channels WHERE name=?";

    public String getChannelURL(String channel) throws SQLException{
        if(connection == null){
            throw new SQLException("No connection established to the database");
        }

        String result = null;
        try (PreparedStatement statement = connection.prepareStatement(SELECT_CHANNEL)) {
            statement.setString(1, channel);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next())
                    throw new IllegalArgumentException("Channel not found: " + channel);
                result = resultSet.getString(3);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static final String RATE_SONG =
            "INSERT INTO song_ratings(_id,rating) VALUES(?,?)";
    public long rateSong(int _id, int rating)throws SQLException{
        if(connection == null){
            throw new SQLException("No connection established to the database");
        }

        try(PreparedStatement statement = connection.prepareStatement(RATE_SONG, Statement.RETURN_GENERATED_KEYS)){
            statement.setInt(1,_id);
            statement.setInt(2,rating);
            return statement.executeUpdate();
        }
    }


    @Override
    protected PreparedStatement prepareStatement(String query) throws SQLException {
        return connection.prepareStatement(query);
    }
}

package soma.data;

import soma.Play;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter on 6/10/2016.
 */
public class SomaDBManager implements Closeable{
    private Connection connection;

    public SomaDBManager()throws SQLException{
        connection = new SomaDBOpenHelper().getConnection();
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
    public List<Play> getPlaylist(String channel){
        return getPlaylist(channel, 0);
    }

    public List<Play> getPlaylist(String channel, int minRating) {
        List<Play> result = new ArrayList<>(128);
        try (PreparedStatement statement = connection.prepareStatement(SELECT_PLAYLIST)) {
            statement.setString(1, channel);
            statement.setInt(2,minRating);
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

    private Play resultSetToPlay(ResultSet resultSet, String channel)throws SQLException{
        long time = resultSet.getLong(1);
        String artist = resultSet.getString(2);
        String album = resultSet.getString(3);
        String song = resultSet.getString(4);
        int rating = resultSet.getInt(5);
        long songId = resultSet.getLong(6);
        long albumId = resultSet.getLong(7);
        long artistId = resultSet.getLong(8);
        long channelId = resultSet.getLong(9);
        return new Play(time, artist, album, song, channel, rating,songId, artistId, albumId, channelId);
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
                    + "AND timestamp > ? AND timestamp < ? "
                    + "AND song_ratings.rating >= ?";
    public List<Play> getPlaylist(String channel, long min_stamp, long max_stamp){
        return getPlaylist(channel, min_stamp, max_stamp, 0);
    }

    public List<Play> getPlaylist(String channel, long min_stamp, long max_stamp, int minRating) {
        List<Play> result = new ArrayList<>(128);
        try (PreparedStatement statement = connection.prepareStatement(SELECT_PLAYLIST_DATE_RANGE)) {
            statement.setString(1, channel);
            statement.setLong(2,min_stamp);
            statement.setLong(3,max_stamp);
            statement.setInt(4,minRating);
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

    public static final String ADD_CHANNEL =
            "INSERT INTO channels ( name, pl_url ) values ( ?,? )";

    public void addChannel(String name, String pl_url) {
        try (PreparedStatement statement = connection.prepareStatement(ADD_CHANNEL)) {
            statement.setString(1, name);
            statement.setString(2, pl_url);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static final String ADD_ARTIST =
            "INSERT INTO artists ( name ) values ( ? )";

    public void addArtist(String name) {
        try (PreparedStatement statement = connection.prepareStatement(ADD_ARTIST)) {
            statement.setString(1, name);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static final String ADD_ALBUM =
            "INSERT INTO albums ( artist_id, name ) " +
                    "values ((SELECT _id FROM artists WHERE name=?),? )";

    public void addAlbum(String album, String artist) {
        try (PreparedStatement statement = connection.prepareStatement(ADD_ALBUM)) {
            statement.setString(1, artist);
            statement.setString(2, album);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static final String ADD_SONG =
            "INSERT INTO songs (name, album_id, artist_id) " +
                    "SELECT ?, albums._id,artists._id  " +
                    "from albums inner join artists on albums.artist_id = artists._id " +
                    "where albums.name=? and artists.name=?";
    public static final String ADD_SONG_RATING =
            "INSERT INTO song_ratings (_id,rating) "
            +"VALUES(?,?)";
    public static final String ADD_SONG_FTS =
            "INSERT INTO fts_tracks (docid,artist,album,song)" +
            "VALUES(?,?,?,?)";
    public void addSong(String song, String album, String artist) {
        try (PreparedStatement statement = connection.prepareStatement(ADD_SONG)) {
            statement.setString(1, song);
            statement.setString(2, album);
            statement.setString(3, artist);
            long result = statement.executeUpdate();
            if(result > 0) {
                ResultSet rs = statement.getGeneratedKeys();
                rs.next();
                try (PreparedStatement ftsStatement = connection.prepareStatement(ADD_SONG_FTS)) {
                    ftsStatement.setLong(1, rs.getLong(1));
                    ftsStatement.setString(2, song);
                    ftsStatement.setString(3, album);
                    ftsStatement.setString(4, artist);
                    ftsStatement.executeUpdate();
                }
                try (PreparedStatement ratingStatement = connection.prepareStatement(ADD_SONG_RATING)){
                    ratingStatement.setLong(1,rs.getLong(1));
                    ratingStatement.setInt(2,0);
                    ratingStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static final String UPDATE_RATING =
            "UPDATE song_ratings SET rating = ? WHERE _id= ?";

    public void updateRating(Play item){
        try(PreparedStatement statement = connection.prepareStatement(UPDATE_RATING)){
            statement.setInt(1,item.getRating());
            statement.setLong(2,item.getSongId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static final String ADD_PLAY =
            "INSERT INTO plays (timestamp, song_id, channel_id) " +
                    "SELECT ?, songs._id, channels._id " +
                    "FROM songs inner join channels where channels.name = ? and songs.name = ?";

    public void addPlay(String song, String channel, long timestamp) {
        try (PreparedStatement statement = connection.prepareStatement(ADD_PLAY)) {
            statement.setLong(1, timestamp);
            statement.setString(2, channel);
            statement.setString(3, song);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static final String SELECT_CHANNEL =
            "SELECT _id,name,pl_url FROM channels WHERE name=?";

    public String getChannelURL(String channel) {
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

    public static final String SELECT_PLAY =
            "SELECT * FROM plays WHERE channel_id=(SELECT _id FROM channels WHERE name=? LIMIT 1) AND timestamp=?";
    public static final String SELECT_SONG =
            "SELECT * FROM songs WHERE name=? AND album_id=(SELECT _id FROM albums WHERE name=? LIMIT 1)";
    public static final String SELECT_ALBUM =
            "SELECT * FROM albums WHERE name=? AND artist_id=(SELECT _id FROM artists WHERE name=? LIMIT 1)";
    public static final String SELECT_ARTIST =
            "SELECT * FROM artists WHERE name=? LIMIT 1";


    public boolean checkIfPlayExists(Play play) {
        boolean result = false;
        try (PreparedStatement statement = connection.prepareStatement(SELECT_PLAY)) {
            statement.setString(1, play.getChannel());
            statement.setLong(2, play.getTimestamp());
            ResultSet resultSet = statement.executeQuery();
            result = resultSet.isBeforeFirst();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean checkIfArtistExists(String artist) {
        boolean result = false;
        try (PreparedStatement statement = connection.prepareStatement(SELECT_ARTIST)) {
            statement.setString(1, artist);
            ResultSet resultSet = statement.executeQuery();
            result = resultSet.isBeforeFirst();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean checkIfAlbumExists(String artist, String album) {
        boolean result = false;
        try (PreparedStatement statement = connection.prepareStatement(SELECT_ALBUM)) {
            statement.setString(1, album);
            statement.setString(2, artist);
            ResultSet resultSet = statement.executeQuery();
            result = resultSet.isBeforeFirst();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean checkIfSongExists(String album, String song) {
        boolean result = false;
        try (PreparedStatement statement = connection.prepareStatement(SELECT_SONG)) {
            statement.setString(1, song);
            statement.setString(2, album);
            ResultSet resultSet = statement.executeQuery();
            result = resultSet.isBeforeFirst();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static final String INCREMENT_PLAYS =
            "UPDATE songs SET plays=(plays+1) WHERE name=? AND album_id=(SELECT _id FROM albums WHERE name=? LIMIT 1)";

    public void incrementPlayCount(Play play) {
        try (PreparedStatement statement = connection.prepareStatement(INCREMENT_PLAYS)) {
            statement.setString(1, play.getSong());
            statement.setString(2, play.getAlbum());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addPlay(Play play) throws SQLException {
        connection.setAutoCommit(false);

        if (!checkIfArtistExists(play.getArtist())) {
            addArtist(play.getArtist());
            addAlbum(play.getAlbum(), play.getArtist());
            addSong(play.getSong(), play.getAlbum(), play.getArtist());
            addPlay(play.getSong(), play.getChannel(), play.getTimestamp());
            incrementPlayCount(play);
        }
        if (!checkIfAlbumExists(play.getArtist(), play.getAlbum())) {
            addAlbum(play.getAlbum(), play.getArtist());
            addSong(play.getSong(), play.getAlbum(), play.getArtist());
            addPlay(play.getSong(), play.getChannel(), play.getTimestamp());
            incrementPlayCount(play);
        }
        if (!checkIfSongExists(play.getAlbum(), play.getSong())) {
            addSong(play.getSong(), play.getAlbum(), play.getArtist());
            addPlay(play.getSong(), play.getChannel(), play.getTimestamp());
            incrementPlayCount(play);
        }
        if (!checkIfPlayExists(play)) {
            addPlay(play.getSong(), play.getChannel(), play.getTimestamp());
            incrementPlayCount(play);
        }

        connection.commit();
        connection.setAutoCommit(true);

    }

    public void addAllPlays(List<Play> plays) throws SQLException {
        connection.setAutoCommit(false);

        for (Play play : plays) {

            if (!checkIfArtistExists(play.getArtist())) {
                addArtist(play.getArtist());
                addAlbum(play.getAlbum(), play.getArtist());
                addSong(play.getSong(), play.getAlbum(), play.getArtist());
                addPlay(play.getSong(), play.getChannel(), play.getTimestamp());
                incrementPlayCount(play);
            }
            if (!checkIfAlbumExists(play.getArtist(), play.getAlbum())) {
                addAlbum(play.getAlbum(), play.getArtist());
                addSong(play.getSong(), play.getAlbum(), play.getArtist());
                addPlay(play.getSong(), play.getChannel(), play.getTimestamp());
                incrementPlayCount(play);
            }
            if (!checkIfSongExists(play.getAlbum(), play.getSong())) {
                addSong(play.getSong(), play.getAlbum(), play.getArtist());
                addPlay(play.getSong(), play.getChannel(), play.getTimestamp());
                incrementPlayCount(play);
            }
            if (!checkIfPlayExists(play)) {
                addPlay(play.getSong(), play.getChannel(), play.getTimestamp());
                incrementPlayCount(play);
            }
        }
        connection.commit();
        connection.setAutoCommit(true);

    }

    public static final String RATE_SONG =
            "INSERT INTO song_ratings(_id,rating) VALUES(?,?)";
    public void rateSong(int _id, int rating)throws SQLException{
        try(PreparedStatement statement = connection.prepareStatement(RATE_SONG)){
            statement.setInt(1,_id);
            statement.setInt(2,rating);
            statement.executeUpdate();
        }
    }

}

package soma.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Peter on 6/9/2016.
 */
public class SomaDBOpenHelper extends DBOpenHelper {
    public static final String DATABASE_NAME = "somafm.db";
    public static final int DATABASE_VERSION = 4;

    public SomaDBOpenHelper() {
        super(DATABASE_VERSION, DATABASE_NAME);
    }

    private final String SCHEMA_RATING =
            "CREATE TABLE song_ratings ( \n"
            +"_id INTEGER PRIMARY KEY ON CONFLICT REPLACE, \n"
            +"rating INTEGER NOT NULL, \n"
            +"FOREIGN KEY (_id) REFERENCES songs (_id) );";

    private final String SCHEMA_TRACK_VIEW =
            "CREATE VIEW tracks AS \n"
            +"SELECT songs._id AS _id,artists.name AS artist,albums.name AS album,songs.name AS song \n"
            +"FROM songs INNER JOIN albums ON songs.album_id = albums._id \n"
            +"INNER JOIN artists ON songs.artist_id = artists._id";

    private final String SCHEMA_RATED_TRACK_VIEW =
            "CREATE VIEW rated_tracks AS \n"
            +"SELECT songs._id AS _id,artists.name AS artist,albums.name AS album,songs.name AS song,rating \n"
            +"FROM songs INNER JOIN albums ON songs.album_id = albums._id \n"
            +"INNER JOIN artists ON songs.artist_id = artists._id \n"
            +"INNER JOIN song_ratings ON songs._id = song_ratings._id";

    private final String SCHEMA_FTS_TRACK_SEARCH =
            "CREATE VIRTUAL TABLE fts_tracks USING FTS4(content='tracks',artist,album,song)";

    private final String SCHEMA_CHANNEL =
            "CREATE TABLE channels ( \n"
                    +"_id INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                    +"name TEXT NOT NULL UNIQUE ON CONFLICT IGNORE, \n"
                    +"pl_url TEXT NOT NULL )";
    private final String SCHEMA_SONG =
            "CREATE TABLE songs ( \n"
                    +"_id INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                    +"album_id INTEGER NOT NULL, \n"
                    +"artist_id INTEGER NOT NULL, \n"
                    +"name TEXT NOT NULL, \n"
                    +"plays INTEGER NOT NULL DEFAULT 0, \n"
                    +"UNIQUE (album_id,artist_id,name) ON CONFLICT IGNORE, \n"
                    +"FOREIGN KEY (album_id) REFERENCES albums (_id), \n"
                    +"FOREIGN KEY (artist_id) REFERENCES artists (_id) )";
    private final String SCHEMA_ALBUM =
            "CREATE TABLE albums ( \n"
                    +"_id INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                    +"artist_id INTEGER NOT NULL, \n"
                    +"name TEXT NOT NULL, \n"
                    +"UNIQUE (artist_id,name) ON CONFLICT IGNORE, \n"
                    +"FOREIGN KEY (artist_id) REFERENCES artists (_id) )";
    private final String SCHEMA_ARTIST =
            "CREATE TABLE artists ( \n"
                    +"_id INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                    +"name TEXT NOT NULL UNIQUE ON CONFLICT IGNORE)";
    private final String SCHEMA_PLAY =
            "CREATE TABLE plays ( \n"
                    +"_id INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                    +"song_id INTEGER NOT NULL, \n"
                    +"channel_id INTEGER NOT NULL, \n"
                    +"timestamp INTEGER NOT NULL, \n"
                    +"UNIQUE (channel_id,timestamp) ON CONFLICT IGNORE, \n"
                    +"FOREIGN KEY (channel_id) REFERENCES channels (_id), \n"
                    +"FOREIGN KEY (song_id) REFERENCES songs (_id) )";

    @Override
    public void onCreate(Connection db) throws SQLException {
        Statement statement = db.createStatement();
        statement.executeUpdate(SCHEMA_CHANNEL);
        statement.executeUpdate(SCHEMA_ARTIST);
        statement.executeUpdate(SCHEMA_ALBUM);
        statement.executeUpdate(SCHEMA_SONG);
        statement.executeUpdate(SCHEMA_PLAY);
        statement.executeUpdate(SCHEMA_RATING);
        statement.executeUpdate(SCHEMA_TRACK_VIEW);
        statement.executeUpdate(SCHEMA_RATED_TRACK_VIEW);
        statement.executeUpdate(SCHEMA_FTS_TRACK_SEARCH);
        statement.close();
    }

    @Override
    public void onUpgrade(Connection db, int oldVersion, int newVersion) throws SQLException {
        final String FTS_UPGRADE_SYNC =
                "INSERT INTO fts_tracks(docid,artist,album,song) "
                +"SELECT _id,artist,album,song FROM tracks;";
        final String RATING_UPGRADE_SYNC =
                "INSERT INTO song_ratings(_id,rating) SELECT songs._id,0 FROM songs";


        final String DROP_PLAYS = "DROP TABLE IF EXISTS plays";
        final String DROP_ARTISTS = "DROP TABLE IF EXISTS artists";
        final String DROP_ALBUMS = "DROP TABLE IF EXISTS albums";
        final String DROP_SONGS = "DROP TABLE IF EXISTS songs";
        final String DROP_CHANNELS = "DROP TABLE IF EXISTS channels";
        final String DROP_RATINGS = "DROP TABLE IF EXISTS ratings";
        final String DROP_TRACK_VIEW = "DROP VIEW IF EXISTS tracks";
        Statement statement = db.createStatement();
        if (oldVersion == 2) {
            if(newVersion == 3) {
                statement.executeUpdate(SCHEMA_RATING);
                statement.executeUpdate(RATING_UPGRADE_SYNC);
            }else if(newVersion == 4){
                statement.executeUpdate(SCHEMA_RATING);
                statement.executeUpdate(RATING_UPGRADE_SYNC);
                statement.executeUpdate(SCHEMA_TRACK_VIEW);
                statement.executeUpdate(SCHEMA_RATED_TRACK_VIEW);
                statement.executeUpdate(SCHEMA_FTS_TRACK_SEARCH);
                statement.executeUpdate(FTS_UPGRADE_SYNC);
            }
            statement.close();
        }else if(oldVersion == 3 && newVersion == 4){
            statement.executeUpdate(SCHEMA_TRACK_VIEW);
            statement.executeUpdate(SCHEMA_RATED_TRACK_VIEW);
            statement.executeUpdate(SCHEMA_FTS_TRACK_SEARCH);
            statement.executeUpdate(FTS_UPGRADE_SYNC);
            statement.close();
        }else {
            statement.executeUpdate(DROP_PLAYS);
            statement.executeUpdate(DROP_SONGS);
            statement.executeUpdate(DROP_ALBUMS);
            statement.executeUpdate(DROP_ARTISTS);
            statement.executeUpdate(DROP_CHANNELS);
            statement.executeUpdate(DROP_RATINGS);
            statement.executeUpdate(DROP_TRACK_VIEW);
            statement.close();
            onCreate(db);
        }
    }
}

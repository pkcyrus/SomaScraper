package com.pskehagias.soma.data;

import com.pskehagias.data.SQLiteDBOpenHelper;
import com.pskehagias.soma.data.schema.SomaSQLite;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Peter on 6/9/2016.
 */
public class SomaSQLiteHelper extends SQLiteDBOpenHelper {
    public static final String DATABASE_NAME = "somafm.db";
    public static final int DATABASE_VERSION = 4;

    public SomaSQLiteHelper() {
        super(DATABASE_VERSION, DATABASE_NAME);
    }

    @Override
    public void onCreate(Connection db) throws SQLException {
        Statement statement = db.createStatement();
        statement.executeUpdate(SomaSQLite.SCHEMA_CHANNEL);
        statement.executeUpdate(SomaSQLite.SCHEMA_ARTIST);
        statement.executeUpdate(SomaSQLite.SCHEMA_ALBUM);
        statement.executeUpdate(SomaSQLite.SCHEMA_SONG);
        statement.executeUpdate(SomaSQLite.SCHEMA_PLAY);
        statement.executeUpdate(SomaSQLite.SCHEMA_RATING);
        statement.executeUpdate(SomaSQLite.SCHEMA_TRACK_VIEW);
        statement.executeUpdate(SomaSQLite.SCHEMA_RATED_TRACK_VIEW);
        statement.executeUpdate(SomaSQLite.SCHEMA_FTS_TRACK_SEARCH);
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
                statement.executeUpdate(SomaSQLite.SCHEMA_RATING);
                statement.executeUpdate(RATING_UPGRADE_SYNC);
            }else if(newVersion == 4){
                statement.executeUpdate(SomaSQLite.SCHEMA_RATING);
                statement.executeUpdate(RATING_UPGRADE_SYNC);
                statement.executeUpdate(SomaSQLite.SCHEMA_TRACK_VIEW);
                statement.executeUpdate(SomaSQLite.SCHEMA_RATED_TRACK_VIEW);
                statement.executeUpdate(SomaSQLite.SCHEMA_FTS_TRACK_SEARCH);
                statement.executeUpdate(FTS_UPGRADE_SYNC);
            }
            statement.close();
        }else if(oldVersion == 3 && newVersion == 4){
            statement.executeUpdate(SomaSQLite.SCHEMA_TRACK_VIEW);
            statement.executeUpdate(SomaSQLite.SCHEMA_RATED_TRACK_VIEW);
            statement.executeUpdate(SomaSQLite.SCHEMA_FTS_TRACK_SEARCH);
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

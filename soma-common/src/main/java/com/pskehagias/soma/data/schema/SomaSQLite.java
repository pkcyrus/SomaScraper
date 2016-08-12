package com.pskehagias.soma.data.schema;

/**
 * Created by pkcyr on 8/10/2016.
 * The SCHEMA used to construct the database on SQLite3
 */
public class SomaSQLite {

    public static final String SCHEMA_RATING =
            "CREATE TABLE song_ratings ( \n"
            +"_id INTEGER PRIMARY KEY ON CONFLICT REPLACE, \n"
            +"rating INTEGER NOT NULL, \n"
            +"FOREIGN KEY (_id) REFERENCES songs (_id) );";
    public static final String SCHEMA_TRACK_VIEW =
            "CREATE VIEW tracks AS \n"
            +"SELECT songs._id AS _id,artists.name AS artist,albums.name AS album,songs.name AS song \n"
            +"FROM songs INNER JOIN albums ON songs.album_id = albums._id \n"
            +"INNER JOIN artists ON songs.artist_id = artists._id";
    public static final String SCHEMA_RATED_TRACK_VIEW =
            "CREATE VIEW rated_tracks AS \n"
            +"SELECT songs._id AS _id,artists.name AS artist,albums.name AS album,songs.name AS song,rating \n"
            +"FROM songs INNER JOIN albums ON songs.album_id = albums._id \n"
            +"INNER JOIN artists ON songs.artist_id = artists._id \n"
            +"INNER JOIN song_ratings ON songs._id = song_ratings._id";
    public static final String SCHEMA_FTS_TRACK_SEARCH =
            "CREATE VIRTUAL TABLE fts_tracks USING FTS4(content='tracks',artist,album,song)";
    public static final String SCHEMA_CHANNEL =
            "CREATE TABLE channels ( \n"
                    +"_id INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                    +"name TEXT NOT NULL UNIQUE ON CONFLICT IGNORE, \n"
                    +"pl_url TEXT NOT NULL )";
    public static final String SCHEMA_SONG =
            "CREATE TABLE songs ( \n"
                    +"_id INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                    +"album_id INTEGER NOT NULL, \n"
                    +"artist_id INTEGER NOT NULL, \n"
                    +"name TEXT NOT NULL, \n"
                    +"plays INTEGER NOT NULL DEFAULT 0, \n"
                    +"UNIQUE (album_id,artist_id,name) ON CONFLICT IGNORE, \n"
                    +"FOREIGN KEY (album_id) REFERENCES albums (_id), \n"
                    +"FOREIGN KEY (artist_id) REFERENCES artists (_id) )";
    public static final String SCHEMA_ALBUM =
            "CREATE TABLE albums ( \n"
                    +"_id INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                    +"artist_id INTEGER NOT NULL, \n"
                    +"name TEXT NOT NULL, \n"
                    +"UNIQUE (artist_id,name) ON CONFLICT IGNORE, \n"
                    +"FOREIGN KEY (artist_id) REFERENCES artists (_id) )";
    public static final String SCHEMA_ARTIST =
            "CREATE TABLE artists ( \n"
                    +"_id INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                    +"name TEXT NOT NULL UNIQUE ON CONFLICT IGNORE)";
    public static final String SCHEMA_PLAY =
            "CREATE TABLE plays ( \n"
                    +"_id INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                    +"song_id INTEGER NOT NULL, \n"
                    +"channel_id INTEGER NOT NULL, \n"
                    +"timestamp INTEGER NOT NULL, \n"
                    +"UNIQUE (channel_id,timestamp) ON CONFLICT IGNORE, \n"
                    +"FOREIGN KEY (channel_id) REFERENCES channels (_id), \n"
                    +"FOREIGN KEY (song_id) REFERENCES songs (_id) )";
}

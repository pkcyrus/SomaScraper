package com.pskehagias.soma.data.schema;

/**
 * Created by pkcyr on 8/9/2016.
 * The SCHEMA used to construct the database on Mysql (requires 5.6+ for FULLTEXT)
 */
public class SomaMysql {
    public static final String T_CHANNEL =
            "CREATE TABLE channels ( \n" +
                    "_id INTEGER PRIMARY KEY AUTO_INCREMENT, \n" +
                    "name VARCHAR(64) NOT NULL UNIQUE, \n" +
                    "pl_url VARCHAR(255) NOT NULL ) ";

    public static final String T_SONG =
            "CREATE TABLE songs ( \n" +
                    "_id INTEGER UNSIGNED AUTO_INCREMENT, \n" +
                    "album_id INTEGER UNSIGNED NOT NULL REFERENCES albums(_id), \n" +
                    "artist_id INTEGER UNSIGNED NOT NULL REFERENCES artists(_id), \n" +
                    "name VARCHAR(128) NOT NULL, \n" +
                    "plays INTEGER NOT NULL DEFAULT 1, \n" +
                    "PRIMARY KEY (_id), \n" +
                    "KEY (album_id), \n" +
                    "KEY (artist_id), \n" +
                    "CONSTRAINT uc_song UNIQUE (album_id, artist_id, name), \n" +
                    "FULLTEXT ft_song (name) )";

    public static final String T_ALBUM =
            "CREATE TABLE albums ( \n" +
                    "_id INTEGER UNSIGNED AUTO_INCREMENT, \n" +
                    "artist_id INTEGER UNSIGNED NOT NULL REFERENCES artists(_id), \n" +
                    "name VARCHAR(128) NOT NULL, \n" +
                    "PRIMARY KEY (_id), \n" +
                    "KEY (artist_id), \n" +
                    "CONSTRAINT uc_album UNIQUE (artist_id, name), \n" +
                    "FULLTEXT ft_album (name) )";

    public static final String T_ARTIST =
            "CREATE TABLE artists ( \n" +
                    "_id INTEGER UNSIGNED AUTO_INCREMENT, \n" +
                    "name VARCHAR(128) NOT NULL, \n" +
                    "PRIMARY KEY (_id), \n" +
                    "UNIQUE (name), \n" +
                    "FULLTEXT ft_artist (name) )";

    public static final String T_PLAY =
            "CREATE TABLE plays ( \n" +
                    "_id INTEGER UNSIGNED AUTO_INCREMENT, \n" +
                    "song_id INTEGER UNSIGNED NOT NULL, \n" +
                    "channel_id INTEGER NOT NULL REFERENCES channels(_id), \n" +
                    "timestamp BIGINT NOT NULL, \n" +
                    "CONSTRAINT uc_play UNIQUE (channel_id,timestamp), \n" +
                    "PRIMARY KEY (_id), \n" +
                    "KEY (song_id), \n" +
                    "KEY (channel_id) )";

}

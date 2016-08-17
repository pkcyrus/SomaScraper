package com.pskehagias.soma.data;

import com.pskehagias.soma.common.Channel;
import com.pskehagias.soma.common.Play;

import java.io.Closeable;
import java.sql.SQLException;

/**
 * Created by pkcyr on 8/17/2016.
 */
public interface InsertHelper{
    void begin() throws SQLException;
    void commit() throws SQLException;
    void close() throws SQLException;

    long addPlay(Play play) throws SQLException;
    long addChannel(Channel channel) throws SQLException;
    long addSong(String name, String album, String artist) throws SQLException;
    long addAlbum(String name, String artist) throws SQLException;
    long addArtist(String name) throws SQLException;
}

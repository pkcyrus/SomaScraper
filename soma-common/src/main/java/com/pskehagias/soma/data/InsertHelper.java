package com.pskehagias.soma.data;

import com.pskehagias.soma.common.Channel;
import com.pskehagias.soma.common.Play;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * Created by pkcyr on 8/17/2016.
 */
public interface InsertHelper{
    void close() throws SQLException;

    long addPlayAndDependents(List<Play> plays) throws SQLException;
    long addPlay(Play play) throws SQLException;
    long addPlay(String song, String channel, long timestamp) throws SQLException;
    long addChannel(Channel channel) throws SQLException;
    long addChannel(String name, String pl_url) throws SQLException;
    long addSong(String name, String album, String artist) throws SQLException;
    long addAlbum(String name, String artist) throws SQLException;
    long addArtist(String name) throws SQLException;
    boolean incrementPlayCount(Play play) throws SQLException;
}

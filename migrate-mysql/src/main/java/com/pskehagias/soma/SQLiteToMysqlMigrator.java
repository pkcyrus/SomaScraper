package com.pskehagias.soma;

import com.pskehagias.soma.common.Play;
import com.pskehagias.soma.data.InsertHelper;
import com.pskehagias.soma.data.SomaInsertHelper;
import com.pskehagias.soma.data.SomaMysqlHelper;
import com.pskehagias.soma.data.SomaSQLiteHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by pkcyr on 8/11/2016.
 */
public class SQLiteToMysqlMigrator {
    private static final String SELECT_ALL_PLAYS =
            "SELECT timestamp, artists.name, albums.name, songs.name, channels.name " +
                    "FROM plays INNER JOIN songs ON plays.song_id = songs._id " +
                    "INNER JOIN albums ON songs.album_id = albums._id " +
                    "INNER JOIN artists ON songs.artist_id = artists._id " +
                    "INNER JOIN channels ON plays.channel_id = channels._id " +
                    "ORDER BY timestamp ASC";

    public static void main(String[] args){
        if(args.length < 4){
            System.out.println("Usage:\nsoma-mysql <mysql hostname> <mysql port> <username> <password>");
            return;
        }

        try{
            SomaSQLiteHelper sqLiteHelper = new SomaSQLiteHelper();
            SomaMysqlHelper mysqlHelper = new SomaMysqlHelper(args[0],args[1]);
            mysqlHelper.setCredentials(args[2],args[3]);

            try(Connection sqlite = sqLiteHelper.getConnection();
                PreparedStatement getPlays = sqlite.prepareStatement(SELECT_ALL_PLAYS);
                ResultSet allPlays = getPlays.executeQuery()) {
                InsertHelper inserter = new SomaInsertHelper(mysqlHelper);

                inserter.begin();
                while (allPlays.next()) {
                    Play play = new Play(allPlays.getLong(1), allPlays.getString(2), allPlays.getString(3), allPlays.getString(4),
                            allPlays.getString(5), 0, 0, 0, 0, 0);
                    inserter.addArtist(play.getArtist());
                    inserter.addAlbum(play.getAlbum(), play.getArtist());
                    inserter.addSong(play.getSong(), play.getAlbum(), play.getArtist());
                    inserter.addPlay(play);
                }
                inserter.commit();
                inserter.close();
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
}

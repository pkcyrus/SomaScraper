package com.pskehagias.soma.async;

import javafx.concurrent.Task;
import com.pskehagias.soma.common.Play;
import com.pskehagias.soma.data.SomaDBManager;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by pkcyr on 6/20/2016.
 */
public class WritePlaylistTask extends Task<Void> {
    SomaDBManager dbManager;
    private List<Play> playList;

    public WritePlaylistTask(List<Play> playList){
        this.playList = playList;
        try{
            dbManager = new SomaDBManager();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Void call() throws Exception {
        dbManager.addAllPlays(playList);
        return null;
    }
}

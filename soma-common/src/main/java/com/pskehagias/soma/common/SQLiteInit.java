package com.pskehagias.soma.common;

import com.pskehagias.soma.common.Channel;
import com.pskehagias.soma.common.Configuration;
import com.pskehagias.soma.data.SomaDBManager;

import javax.xml.transform.TransformerException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by pkcyr on 8/12/2016.
 */
public class SQLiteInit implements Configuration.InitializerDelegate {
    @Override
    public void initialize(Configuration config) {
        try {
            SomaDBManager scraper = new SomaDBManager();

            List<Channel> channels = config.getChannels();
            for (Channel c : channels) {
                scraper.addChannel(c.getName(), c.getUrl());
            }
            channels.stream().forEach(it -> scraper.addChannel(it.getName(), it.getUrl()));
            config.setInitialized(true);
            config.saveConfiguration();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}

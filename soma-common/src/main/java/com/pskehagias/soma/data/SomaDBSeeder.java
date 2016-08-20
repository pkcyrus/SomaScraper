package com.pskehagias.soma.data;

import com.pskehagias.soma.common.Channel;
import com.pskehagias.soma.common.Configuration;
import com.pskehagias.soma.data.InsertHelper;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by pkcyr on 8/17/2016.
 */
public class SomaDBSeeder implements com.pskehagias.data.DBSeeder {
    private InsertHelper insertHelper;

    public SomaDBSeeder(InsertHelper dbHelper) {
        insertHelper = dbHelper;
    }

    @Override
    public void seed() {
        try {
            Configuration configuration = new Configuration(Configuration.FILE_USER);
            if (!configuration.isInitialized()) {
                List<Channel> channels = configuration.getChannels();
                insertHelper.begin();
                for (Channel c : channels) {
                    insertHelper.addChannel(c);
                }
                insertHelper.commit();
                insertHelper.close();
                configuration.setInitialized(true);
                configuration.saveConfiguration();
            }
        } catch (IOException | SAXException e) {
            throw new RuntimeException("Can't load the configuration file.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't insert channels into the database.", e);
        } catch (TransformerException e) {
            throw new RuntimeException("Couldn't save the configuration post initialization.", e);
        }
    }
}

package com.pskehagias.soma;

import com.pskehagias.soma.common.Configuration;
import com.pskehagias.soma.common.Play;
import com.pskehagias.soma.data.SomaInsertHelper;
import com.pskehagias.soma.data.SomaMysqlHelper;
import com.pskehagias.soma.scraper.Scraper;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by pkcyr on 8/16/2016.
 */
public class ScrapeToMysql {
    public static void main(String[] args){
        if(args.length < 4){
            System.out.println("Usage:\nsoma-mysql <mysql hostname> <mysql port> <username> <password>");
            return;
        }

        SomaMysqlHelper helper = new SomaMysqlHelper(args[0], args[1]);
        helper.setCredentials(args[2], args[3]);

        Scraper scraper = new Scraper(null);
        try (SomaInsertHelper inserter = new SomaInsertHelper(helper)){
            Configuration configuration = new Configuration(Configuration.FILE_USER, null);
            List<Play> result = scraper.scrapeAllNow(configuration.getChannels());

            inserter.begin();
            for(Play p : result){
                inserter.addArtist(p.getArtist());
                inserter.addAlbum(p.getAlbum(), p.getArtist());
                inserter.addSong(p.getSong(), p.getAlbum(), p.getArtist());
                inserter.addPlay(p);
            }
            inserter.commit();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
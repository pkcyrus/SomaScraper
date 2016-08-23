package com.pskehagias.soma;

import com.pskehagias.soma.common.Configuration;
import com.pskehagias.soma.common.Play;
import com.pskehagias.soma.data.SomaDBManager;
import com.pskehagias.soma.data.SomaDBSeeder;
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
public class ScrapeCommandTool {
    public static void scrapesqlite(List<Play> plays) throws SQLException {
        SomaDBManager sdb = new SomaDBManager();

        try {
            SomaDBSeeder dbSeeder = new SomaDBSeeder(sdb);
            dbSeeder.seed();
        }catch (RuntimeException e){
            System.err.println(e.getMessage());
            System.err.println(e.getCause().getMessage());
        }

        sdb.addPlayAndDependents(plays);
        sdb.close();
    }

    public static void scrapemysql(String[] args, List<Play> plays) throws SQLException {
        if (args.length < 5) {
            printusage();
            return;
        }

        SomaMysqlHelper helper = new SomaMysqlHelper(args[0], args[1]);
        helper.setCredentials(args[2], args[3]);
        SomaInsertHelper inserter = new SomaInsertHelper(helper);

        try {
            SomaDBSeeder dbSeeder = new SomaDBSeeder(inserter);
            dbSeeder.seed();
        }catch (RuntimeException e){
            System.err.println(e.getMessage());
            System.err.println(e.getCause().getMessage());
        }

        inserter.addPlayAndDependents(plays);
        inserter.close();
    }

    public static void printusage(){
        System.out.println("Usage:\nsoma-mysql [mysql|sqlite] <mysql hostname> <mysql port> <username> <password>");
    }

    public static void main(String[] args){
        if(args.length < 1){
            printusage();
            return;
        }

        Scraper scraper = new Scraper(null);
        Configuration configuration;
        List<Play> result = null;

        try{
            configuration = new Configuration(Configuration.FILE_USER);
            result = scraper.scrapeAllNow(configuration.getChannels());
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }

        try {
            if (args[0].equalsIgnoreCase("sqlite")) {
                scrapesqlite(result);
            } else if (args[1].equalsIgnoreCase("mysql")) {
                scrapemysql(args, result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
package com.pskehagias.soma;

import com.pskehagias.soma.common.Channel;
import com.pskehagias.soma.common.Configuration;
import com.pskehagias.soma.common.Play;
import com.pskehagias.soma.common.SQLiteInit;
import com.pskehagias.soma.data.SomaDBSeeder;
import com.pskehagias.soma.scraper.Scraper;
import com.pskehagias.soma.util.ScrapeUrlToFilename;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.xml.sax.SAXException;
import com.pskehagias.soma.data.SomaDBManager;
import com.pskehagias.soma.export.CSVPlaylistFormatter;
import com.pskehagias.soma.export.PlaylistExporter;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Peter on 6/10/2016.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/layout/layout_launch.fxml"));
        primaryStage.setTitle("SomaScraper Config");
        primaryStage.setScene(new Scene(root, 800 , 600));
        primaryStage.show();
    }

    public static void printUsage(){
        System.out.println("Usage:\njava -jar SomaScraper.jar [action] [params...]");
        System.out.println("Valid actions:\nscrape\nparams:list of channels to scrape\n\nScrape all named playlists. If no playlists are names, the config file is used\n");
        System.out.println("config\nparams:none\n\nOpens the configuration interface\n");
        System.out.println("export\nparams:a list of channel names to export, separated by spaces\nif no channel is specified, all channels will be exported\n");
    }

    public static void main(String[] args){

        try {
            SomaDBSeeder dbSeeder = new SomaDBSeeder(new SomaDBManager());
            dbSeeder.seed();
        } catch (SQLException e) {
            System.err.println("Failed to open a connection to the sqlite database.");
            System.err.println(e.getMessage());
            return;
        } catch (RuntimeException e){
            System.err.println(e.getMessage());
            System.err.println(e.getCause().getMessage());
        }

        if (args.length == 0 || args[0].equals("config")) {
            //config
            launch(args);
        }else if(args[0].equals("export")){
            //export
            startExport(args);
        }else if(args[0].equals("scrape")){
            //scrape
            startScrape(args);
        }else{
            printUsage();
        }
        System.exit(0);
    }

    public static void export(SomaDBManager db, File file, String channel){
        try(PlaylistExporter exporter = new PlaylistExporter(new CSVPlaylistFormatter(";"), file, null)){
            exporter.write(db.getPlaylist(channel));
        } catch (IOException e) {
            System.err.println("Failed to export channel: "+channel);
            System.out.println(e.getMessage());
        }
    }

    public static void startExport(String[] args){
        try {
            SomaDBManager dbManager = new SomaDBManager();
            if(args.length > 1){
                for(int idx = 1; idx < args.length; idx++){
                    String url = ScrapeUrlToFilename.filenameFromUrl(dbManager.getChannelURL(args[idx]));
                    export(dbManager, new File("./playlist/"+url), args[idx]);
                }
            }else{
                try {
                    Configuration configuration = new Configuration(Configuration.FILE_USER, new SQLiteInit());
                    List<Channel> channelList = configuration.getChannels();
                    for(Channel c : channelList){
                        export(dbManager, new File("./playlist/"+ ScrapeUrlToFilename.filenameFromUrl(c.getUrl())), c.getName());
                    }
                } catch (IOException | SAXException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void startScrape(String[] args){
        try {
            SomaDBManager dbManager = new SomaDBManager();
            Scraper scraper = new Scraper(null);
            Configuration configuration = new Configuration(Configuration.FILE_USER, new SQLiteInit());
            List<Play> result = scraper.scrapeAllNow(configuration.getChannels());
            dbManager.addPlayAndDependents(result);
            return;
        }catch (SAXException | SQLException | IOException e) {
            e.printStackTrace();
        }
        System.exit(1);
    }
}
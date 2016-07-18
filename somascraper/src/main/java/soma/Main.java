package soma;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.xml.sax.SAXException;
import soma.data.SomaDBManager;
import soma.export.CSVPlaylistFormatter;
import soma.export.GmailSmtpSender;
import soma.export.PlaylistExporter;
import soma.export.ZipPlaylistExporter;

import javax.mail.MessagingException;
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
        System.out.println("mail\nparams: [gmail acct] [gmail passwd] [destination acct] [channel list]\nexports playlists to zip and mails to target address\n");
    }

    public static void main(String[] args){
        if (args.length == 0 || args[0].equals("config")) {
            //config
            launch(args);
        }else if(args[0].equals("export")){
            //export
            startExport(args);
        }else if(args[0].equals("scrape")){
            //scrape
            startScrape(args);
        }else if(args[0].equals("mail") && args.length >= 4){
            //mail
            startMail(args);
        }else{
            printUsage();
        }
        System.exit(0);
    }

    public static void startMail(String[] args){
        String sendAcct = args[1];
        String sendPass = args[2];
        String destAcct = args[3];
        try {
            SomaDBManager dbManager = new SomaDBManager();
            try(ZipPlaylistExporter exporter = new ZipPlaylistExporter(new CSVPlaylistFormatter(";"), new File("./playlist/playlist.zip"), null)) {
                if (args.length > 4) {
                    for(int idx = 4; idx < args.length; idx++) {
                        String url = filenameFromUrl(dbManager.getChannelURL(args[idx]));
                        exporter.write(url, dbManager.getPlaylist(args[idx]));
                    }
                } else {
                    try {
                        Configuration configuration = new Configuration();
                        List<Channel> channelList = configuration.getChannels();
                        for(Channel c : channelList){
                            exporter.write(filenameFromUrl(c.getUrl()),dbManager.getPlaylist(c.getName()));
                        }
                    }catch (SAXException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            GmailSmtpSender sender = new GmailSmtpSender();
            sender.sendMail(sendAcct, sendPass, destAcct, "./playlist/playlist.zip");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public static String filenameFromUrl(String url){
        url = url.substring(0,url.lastIndexOf("/"));
        return url.substring(url.lastIndexOf("/")+1)+".csv";
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
                    String url = filenameFromUrl(dbManager.getChannelURL(args[idx]));
                    export(dbManager, new File("./playlist/"+url), args[idx]);
                }
            }else{
                try {
                    Configuration configuration = new Configuration();
                    List<Channel> channelList = configuration.getChannels();
                    for(Channel c : channelList){
                        export(dbManager, new File("./playlist/"+filenameFromUrl(c.getUrl())), c.getName());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
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
            List<Play> result = scraper.scrapeAllNow();
            dbManager.addAllPlays(result);
            return;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.exit(1);
    }
}
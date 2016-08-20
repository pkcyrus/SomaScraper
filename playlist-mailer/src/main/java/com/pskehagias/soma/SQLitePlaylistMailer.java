package com.pskehagias.soma;

import com.pskehagias.soma.common.Channel;
import com.pskehagias.soma.common.Configuration;
import com.pskehagias.soma.common.SQLiteInit;
import com.pskehagias.soma.data.SomaDBManager;
import com.pskehagias.soma.export.CSVPlaylistFormatter;
import com.pskehagias.soma.export.GmailSmtpSender;
import com.pskehagias.soma.export.ZipPlaylistExporter;
import com.pskehagias.soma.util.ScrapeUrlToFilename;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by pkcyr on 8/12/2016.
 */
public class SQLitePlaylistMailer {
    public static void main(String[] args){
        String sendAcct = args[0];
        String sendPass = args[1];
        String destAcct = args[2];
        try {
            SomaDBManager dbManager = new SomaDBManager();
            try(ZipPlaylistExporter exporter = new ZipPlaylistExporter(new CSVPlaylistFormatter(";"), new File("playlist/playlist.zip"), null)) {
                if (args.length > 3) {
                    for(int idx = 4; idx < args.length; idx++) {
                        String url = ScrapeUrlToFilename.filenameFromUrl(dbManager.getChannelURL(args[idx]));
                        exporter.write(url, dbManager.getPlaylist(args[idx]));
                    }
                } else {
                    try {
                        Configuration configuration = new Configuration(Configuration.FILE_USER, new SQLiteInit());
                        List<Channel> channelList = configuration.getChannels();
                        for(Channel c : channelList){
                            exporter.write(ScrapeUrlToFilename.filenameFromUrl(c.getUrl()),dbManager.getPlaylist(c.getName()));
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
        } catch (SQLException | javax.mail.MessagingException e) {
            e.printStackTrace();
        }
    }
}

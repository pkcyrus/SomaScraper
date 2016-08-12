package com.pskehagias.soma.util;

/**
 * Created by pkcyr on 8/12/2016.
 * Helper methods for choosing a filename based on the url of the playlist.
 */
public class ScrapeUrlToFilename {
    /**
     * Takes the URL of a given playlist, and returns a filename usable for exporting to .csv files
     * @param url The URL of a somafm playlist, i.e. "http://somafm.com/channel/songhistory.html"
     * @return A filename to use for .csv files, i.e. "channel.csv"
     */
    public static String filenameFromUrl(String url){
        url = url.substring(0,url.lastIndexOf("/"));
        return url.substring(url.lastIndexOf("/")+1)+".csv";
    }
}

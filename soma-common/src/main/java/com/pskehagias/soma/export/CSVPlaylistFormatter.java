package com.pskehagias.soma.export;

import com.pskehagias.soma.common.Play;

import java.util.Calendar;

/**
 * Created by pkcyr on 6/13/2016.
 */
public class CSVPlaylistFormatter implements PlaylistFormatter {
    private String delimiter;
    private StringBuilder builder;
    private Calendar convertMillis;

    public CSVPlaylistFormatter(String delimiter) {
        this.delimiter = delimiter;
        builder = new StringBuilder(256);
        convertMillis = Calendar.getInstance();
    }

    @Override
    public String header() {
        builder.setLength(0);
        builder.append("Timestamp").append(delimiter).append("Artist").append(delimiter).append("Album").append(delimiter).append("Song\n");
        return builder.toString();
    }

    @Override
    public String format(Play p) {
        builder.setLength(0);
        convertMillis.setTimeInMillis(p.getTimestamp());
        builder.append(convertMillis.getTime()).append(delimiter).append(p.getArtist()).append(delimiter).append(p.getAlbum()).append(delimiter).append(p.getSong()).append("\n");
        return builder.toString();
    }
}

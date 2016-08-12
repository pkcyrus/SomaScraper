package com.pskehagias.soma.common;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by Peter on 6/10/2016.
 */
public class Play {
    private final SimpleLongProperty timestamp;
    private final SimpleStringProperty artist;
    private final SimpleStringProperty album;
    private final SimpleStringProperty song;
    private final SimpleStringProperty channel;
    private final SimpleIntegerProperty rating;

    private final SimpleLongProperty artistId;
    private final SimpleLongProperty albumId;
    private final SimpleLongProperty songId;
    private final SimpleLongProperty channelId;

    public Play(){
        this(0,"","","","",0,0,0,0,0);
    }

    public Play(long time, String artist, String album, String song, String channel, int rating,
                long songId, long artistId, long albumId, long channelId) {
        this.timestamp = new SimpleLongProperty(time);
        this.artist = new SimpleStringProperty(artist);
        this.album = new SimpleStringProperty(album);
        this.song = new SimpleStringProperty(song);
        this.channel = new SimpleStringProperty(channel);
        this.rating = new SimpleIntegerProperty(rating);

        this.artistId = new SimpleLongProperty(artistId);
        this.albumId = new SimpleLongProperty(albumId);
        this.songId = new SimpleLongProperty(songId);
        this.channelId = new SimpleLongProperty(channelId);
    }

    public SimpleLongProperty artistIdProperty(){return artistId;}
    public long getArtistId(){return artistId.get();}
    public void setArtistId(long value){artistId.set(value);}

    public SimpleLongProperty albumIdProperty(){return albumId;}
    public long getAlbumId(){return albumId.get();}
    public void setAlbumId(long value){albumId.set(value);}

    public SimpleLongProperty songIdProperty(){return songId;}
    public long getSongId(){return songId.get();}
    public void setSongId(long value){songId.set(value);}

    public SimpleLongProperty channelIdProperty(){return channelId;}
    public long getChannelId(){return channelId.get();}
    public void setChannelId(long value){channelId.set(value);}

    public SimpleIntegerProperty ratingProperty(){return rating;}
    public int getRating(){return rating.get();}
    public void setRating(int rating){this.rating.set(rating);}

    public SimpleLongProperty timestampProperty(){return timestamp;}
    public long getTimestamp() {
        return timestamp.get();
    }
    public void setTimestamp(long timestamp){ this.timestamp.set(timestamp);}

    public SimpleStringProperty artistProperty(){return artist;}
    public String getArtist() {
        return artist.get();
    }
    public void setArtist(String artist){this.artist.set(artist);}

    public SimpleStringProperty albumProperty(){return album;}
    public String getAlbum() {
        return album.get();
    }
    public void setAlbum(String album){this.album.set(album);}

    public SimpleStringProperty songProperty(){return song;}
    public String getSong() {
        return song.get();
    }
    public void setSong(String song){this.song.set(song);}

    public SimpleStringProperty channelProperty(){return channel;}
    public String getChannel() {
        return channel.get();
    }
    public void setChannel(String channel){this.channel.set(channel);}
}

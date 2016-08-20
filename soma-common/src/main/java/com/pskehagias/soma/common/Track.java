package com.pskehagias.soma.common;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by pkcyr on 6/22/2016.
 */
public class Track implements RatingSource {
    private final SimpleStringProperty album;
    private final SimpleStringProperty artist;
    private final SimpleStringProperty song;
    private final SimpleIntegerProperty rating;

    private final SimpleLongProperty songId;

    public SimpleStringProperty albumProperty(){return album;}
    public SimpleStringProperty artistProperty(){return artist;}
    public SimpleStringProperty songProperty(){return song;}
    @Override
    public SimpleIntegerProperty ratingProperty(){return rating;}
    public SimpleLongProperty songIdProperty(){return songId;}

    public String getAlbum(){return album.get();}
    public String getArtist(){return artist.get();}
    public String getSong(){return song.get();}
    @Override
    public int getRating(){return rating.get();}
    public long getSongId(){return songId.get();}

    public void setAlbum(String album){this.album.set(album);}
    public void setArtist(String artist){this.artist.set(artist);}
    public void setSong(String song){this.song.set(song);}
    @Override
    public void setRating(int rating){this.rating.set(rating);}
    public void setSongId(long value){this.songId.set(value);}

    public Track(){
        this("","","",-1, -1);
    }

    public Track(String album, String artist, String song, int rating, long songId){
        this.album = new SimpleStringProperty(album);
        this.artist = new SimpleStringProperty(artist);
        this.song = new SimpleStringProperty(song);
        this.rating = new SimpleIntegerProperty(rating);
        this.songId = new SimpleLongProperty(songId);
    }
}

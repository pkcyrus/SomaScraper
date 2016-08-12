package com.pskehagias.soma.ui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import com.pskehagias.soma.common.Channel;
import com.pskehagias.soma.common.Configuration;
import com.pskehagias.soma.data.SomaSQLiteHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by pkcyr on 6/21/2016.
 */
public class ControllerChannelInfo {
    @FXML private GridPane main_root;

    @FXML private Label label_channel_name;
    @FXML private ImageView icon_image;
    @FXML private Label label_channel_artists;
    @FXML private Label label_channel_albums;
    @FXML private Label label_channel_songs;

    private final SimpleStringProperty chan_name;
    private final SimpleIntegerProperty chan_artists;
    private final SimpleIntegerProperty chan_albums;
    private final SimpleIntegerProperty chan_songs;

    private Configuration configuration;
    private SomaSQLiteHelper dbHelper;

    public ControllerChannelInfo(){
        configuration = null;
        dbHelper = new SomaSQLiteHelper();

        chan_name = new SimpleStringProperty("");
        chan_artists = new SimpleIntegerProperty(0);
        chan_albums = new SimpleIntegerProperty(0);
        chan_songs = new SimpleIntegerProperty(0);
    }

    public void initialize(){
        label_channel_name.textProperty().bind(chan_name);
        label_channel_artists.textProperty().bind(chan_artists.asString());
        label_channel_albums.textProperty().bind(chan_albums.asString());
        label_channel_songs.textProperty().bind(chan_songs.asString());
    }

    public void setConfiguration(Configuration configuration){
        this.configuration = configuration;
    }

    public void changeChannel(Channel channel){
        chan_name.set(channel.getName());

        if(configuration != null)
            icon_image.setImage(new Image(ControllerChannelInfo.class.getResourceAsStream(configuration.getImage(channel.getName()))));

        final String SELECT_CHAN_STATS =
                "SELECT COUNT(DISTINCT artist_id),COUNT(DISTINCT album_id),COUNT(DISTINCT songs._id) "
                        +"FROM songs INNER JOIN plays ON songs._id = plays.song_id "
                        +"WHERE plays.channel_id = (SELECT _id FROM channels WHERE name=?)";
        try(Connection connection = dbHelper.getConnection();
            PreparedStatement statement = connection.prepareStatement(SELECT_CHAN_STATS)){
            statement.setString(1,channel.getName());
            try(ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()){
                    chan_artists.set(resultSet.getInt(1));
                    chan_albums.set(resultSet.getInt(2));
                    chan_songs.set(resultSet.getInt(3));
                }
            }
        }catch(SQLException e){
            System.err.println(e.getMessage());
        }
    }
}

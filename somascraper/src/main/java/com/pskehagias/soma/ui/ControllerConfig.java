package com.pskehagias.soma.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.xml.sax.SAXException;
import com.pskehagias.soma.common.Channel;
import com.pskehagias.soma.common.Configuration;
import com.pskehagias.soma.common.SQLiteInit;

import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * Created by Peter on 6/28/2016.
 */
public class ControllerConfig implements Controller, ControllerLaunch.ChannelSelectionProvider {

    private HBox main_root;

    @FXML private TextField buffer_size;
    @FXML private TextField recording_directory;
    @FXML private CheckBox  record_streams;

    private Configuration configuration;
    private ObservableList<Channel> channels;
    private ControllerChannelsCheck controllerChannelsCheck;

    public ControllerConfig(){
        channels = FXCollections.observableArrayList();
        try {
            configuration = new Configuration(Configuration.FILE_USER, new SQLiteInit());
            channels.addAll(configuration.getChannels());
            for(Channel c: channels){
                c.doScrapeProperty().addListener((observable, oldValue, newValue) -> {
                    configuration.setChannelConfig(c.getName(), newValue);
                    onSaveConfiguration();
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        FXMLLoader loader = new FXMLLoader();
        loader.setController(this);
        try {
            main_root = loader.load(getClass().getResourceAsStream("/layout/layout_config.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadCheckList();
    }

    public HBox getNode(){
        return main_root;
    }

    public Configuration getConfiguration(){
        return configuration;
    }

    public void onSaveConfiguration(){
        try {
            configuration.saveConfiguration();
        } catch (TransformerException e) {
            System.err.println("Couldn't save configuration.");
            e.printStackTrace();
        }
    }

    public void loadCheckList(){
        FXMLLoader loader = new FXMLLoader();
        try {
            Node channels_check = loader.load(getClass().getResourceAsStream("/layout/layout_channels_check.fxml"));
            controllerChannelsCheck = loader.getController();
            controllerChannelsCheck.setChannels(channels);
            main_root.getChildren().add(0,channels_check);
        } catch (IOException e) {
            System.err.println("Error, layout_channels_check.fxml is missing!");
            e.printStackTrace();
        }
    }

    public void initialize(){
        buffer_size.setText(configuration.getString(Configuration.PLAYER_BUFFER));
        recording_directory.setText(configuration.getString(Configuration.PLAYER_DIRECTORY));
        record_streams.setSelected(Boolean.parseBoolean(configuration.getString(Configuration.PLAYER_RECORD)));

        buffer_size.focusedProperty().addListener((observable, oldValue, newValue) -> {
            configuration.setString(Configuration.PLAYER_BUFFER, buffer_size.getText());
            onSaveConfiguration();
        });
        recording_directory.focusedProperty().addListener((observable, oldValue, newValue) -> {
            configuration.setString(Configuration.PLAYER_DIRECTORY, recording_directory.getText());
            onSaveConfiguration();
        });
        record_streams.selectedProperty().addListener((observable, oldValue, newValue) -> {
            configuration.setString(Configuration.PLAYER_RECORD, newValue.toString());
            onSaveConfiguration();
        });
    }

    public Channel getSelectedChannel() {
        return controllerChannelsCheck.getSelectedChannel();
    }
}

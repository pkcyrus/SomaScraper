package com.pskehagias.soma.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import com.pskehagias.soma.common.Channel;
import com.pskehagias.soma.common.Configuration;
import com.pskehagias.soma.async.PlaylistExportTask;
import com.pskehagias.soma.async.ScrapeChannelsTask;
import com.pskehagias.soma.async.WritePlaylistTask;
import com.pskehagias.soma.async.ZipPlaylistExportTask;
import com.pskehagias.soma.data.SomaSQLiteHelper;

import java.io.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.pskehagias.soma.ui.ControllerLaunch.ControllerState.BROWSE;

/**
 * Created by pkcyr on 6/14/2016.
 */
public class ControllerLaunch {
    public enum ControllerState{
        BROWSE,SEARCH,CONFIG
    }
    public interface ChannelSelectionProvider{
        Channel getSelectedChannel();
    }


    private ControllerState state;
    private ChannelSelectionProvider channelSelector;
    private ControllerBrowse browseController;
    private ControllerSearch searchController;
    private ControllerConfig configController;
    private Node musicNode;
    private ControllerStreamMusic musicController;

    private Configuration configuration;
    private ObservableList<Channel> channels;

    private final SimpleIntegerProperty plays;
    private final SimpleIntegerProperty tracks;

    @FXML private BorderPane main_root;

    @FXML private VBox work_bar_box;
    @FXML private ProgressBar work_bar;
    @FXML private Label work_label;

    private final int MAX_THREADS = 4;
    private final Executor executor;

    public ControllerLaunch(){
        plays = new SimpleIntegerProperty(0);
        tracks = new SimpleIntegerProperty(0);

        executor = Executors.newFixedThreadPool(MAX_THREADS, run -> {
            Thread t = new Thread(run);
            t.setDaemon(true);
            return t;
        });

        try {
            configController = new ControllerConfig();
            configuration = configController.getConfiguration();
            loadMusicNode();
            searchController = new ControllerSearch();
            searchController.setChannels(configuration.getChannels());
            browseController = new ControllerBrowse();
            browseController.setChannels(configuration.getChannels());

            channels = FXCollections.observableArrayList(configuration.getChannels());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Couldn't create Configuration");
        }
    }

    private void loadMusicNode() throws IOException {
        FXMLLoader musicLoader = new FXMLLoader();
        musicNode = musicLoader.load(getClass().getResourceAsStream("/layout/layout_stream_music.fxml"));
        musicController = musicLoader.getController();
        musicController.setConfiguration(configuration);
    }

    public void initialize(){
        ((VBox)main_root.getTop()).getChildren().add(musicNode);
        changeState(BROWSE);
    }

    public void onConfigure(ActionEvent event){
        changeState(ControllerState.CONFIG);
    }

    public void onExit(ActionEvent actionEvent) {
        /*Should prompt to save changes first*/Platform.exit();
    }

    public File chooseSaveFilename(String channelFileName, boolean forceZip){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select playlist filename");
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.setInitialFileName(channelFileName + (forceZip ? ".zip" :".csv"));
        if(!forceZip)
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Playlist Spreadsheet", "*.csv"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Zip Archive", "*.zip"));
        return fileChooser.showSaveDialog(main_root.getScene().getWindow());
    }

    public static String filenameFromUrl(String url){
        url = url.substring(0,url.lastIndexOf("/"));
        return url.substring(url.lastIndexOf("/")+1);
    }

    public void onExportAllPlaylist(ActionEvent actionEvent){
        File target = chooseSaveFilename("soma_playlists",true);
        if(target != null){
            try {
                Map<String, File> targets = new HashMap<>();
                for (Channel c : channels) {
                    targets.put(c.getName(), new File(filenameFromUrl(c.getUrl()) + ".csv"));
                }
                ZipPlaylistExportTask zpet = new ZipPlaylistExportTask(target, targets, ";");
                work_bar.progressProperty().bind(zpet.progressProperty());
                work_label.setText("Exporting playlists...");
                executor.execute(zpet);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Channel getSelectedChannel(){
        return channelSelector != null ? channelSelector.getSelectedChannel() : null;
    }

    public void changeState(ControllerState newState){
        switch(newState){
            case BROWSE:
                main_root.setCenter(browseController.getNode());
                channelSelector = browseController;
                break;
            case CONFIG:
                main_root.setCenter(configController.getNode());
                channelSelector = configController;
                break;
            case SEARCH:
                main_root.setCenter(searchController.getNode());
                channelSelector = searchController;
                break;
            default:
                main_root.setCenter(new Label("Critical Error!"));
                channelSelector = null;
                throw new IllegalArgumentException("Launch controller cannot be set to a null state!");
        }
        state = newState;
    }

    public void onExportPlaylist(ActionEvent actionEvent) {
        Channel c = getSelectedChannel();
        if(c == null){
            return;
        }
        File target = chooseSaveFilename(filenameFromUrl(c.getUrl()),false);
        if(target != null){
            try {
                String name = target.getName();
                String filetype = name.substring(name.length()-4);
                if(filetype.equals(".csv")) {
                    Map<String,File> targets = new HashMap<>();
                    targets.put(c.getName(), target);
                    PlaylistExportTask exporter = new PlaylistExportTask(targets, ";");
                    work_bar.progressProperty().bind(exporter.progressProperty());
                    work_label.setText("Exporting playlists...");
                    executor.execute(exporter);
                }else if(filetype.equals(".zip")){
                    Map<String,File> targets = new HashMap<>();
                    System.out.println(name + ":::" + name.substring(0, name.length()-4)+".csv");
                    targets.put(c.getName(), new File(name.substring(0, name.length()-4)+".csv"));
                    ZipPlaylistExportTask exporter = new ZipPlaylistExportTask(target, targets, ";");
                    work_bar.progressProperty().bind(exporter.progressProperty());
                    work_label.setText("Exporting playlists...");
                    executor.execute(exporter);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void startScrapeTask(Channel channel){
        ScrapeChannelsTask sct;
        if(channel == null)
            sct = new ScrapeChannelsTask(configuration.getChannels());
        else
            sct = new ScrapeChannelsTask(channel);

        sct.valueProperty().addListener((observable, oldValue, newValue) -> {
            WritePlaylistTask wpt = new WritePlaylistTask(newValue);
            Thread th = new Thread(wpt);
            th.setDaemon(true);
            th.start();
        });

        executor.execute(sct);
    }

    public void onScrape(ActionEvent actionEvent) {
        Channel c = getSelectedChannel();
        if(c != null)
            startScrapeTask(c);
    }

    public void onAllScrape(ActionEvent actionEvent) {
        startScrapeTask(null);
    }

    public void onBrowse(ActionEvent actionEvent) {
        changeState(BROWSE);
    }

    public void onSearch(ActionEvent actionEvent) {
        changeState(ControllerState.SEARCH);
    }
}

package com.pskehagias.soma.ui;

import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import com.pskehagias.soma.common.Channel;
import com.pskehagias.soma.common.Play;
import com.pskehagias.soma.data.SomaDBManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Created by pkcyr on 6/22/2016.
 */
public class ControllerBrowse implements Controller, RatingCell.RatingUpdateCallback, ControllerLaunch.ChannelSelectionProvider {
    private GridPane main_root;
    @FXML private ListView<Channel> channel_list;
    @FXML private CheckBox check_use_date_range;
    @FXML private DatePicker date_range_min;
    @FXML private DatePicker date_range_max;
    @FXML private ComboBox<Integer> rating_cutoff;

    @FXML private TableView result_table;
    @FXML private TableColumn timestamp_column;
    @FXML private TableColumn rating_column;
    @FXML private Label label_result_count;

    private final SimpleLongProperty min_date;
    private final SimpleLongProperty max_date;

    private SomaDBManager dbManager;
    private ObservableList<Channel> channels;
    private ObservableList<Play> results;

    public ControllerBrowse(){
        min_date = new SimpleLongProperty(0);
        max_date = new SimpleLongProperty(0);

        channels = FXCollections.observableArrayList();
        results = FXCollections.observableArrayList();
        try {
            dbManager = new SomaDBManager();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        FXMLLoader loader = new FXMLLoader();
        loader.setController(this);
        try {
            main_root = loader.load(getClass().getResourceAsStream("/layout/layout_browse.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GridPane getNode(){
        return main_root;
    }

    public void setChannels(List<Channel> channels){
        this.channels.addAll(channels);
    }

    public void updateRating(int rowIndex, int value){
        Play item = results.get(rowIndex);
        item.setRating(value);
        dbManager.updateRating(item);
    }

    public void initialize(){
        channel_list.setItems(channels);
        result_table.setItems(results);

        timestamp_column.setCellFactory(new Callback<TableColumn, TableCell>() {

            @Override
            public TableCell call(TableColumn param) {
                return new TimestampCell();
            }
        });

        rating_column.setEditable(true);
        rating_column.setCellFactory(new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new RatingCell(ControllerBrowse.this);
            }
        });

        date_range_min.valueProperty().addListener((observable1, oldValue1, newValue1) -> {
            min_date.setValue(date_range_min.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond()*1000);
        });

        date_range_max.valueProperty().addListener((observable1, oldValue1, newValue1) -> {
            max_date.setValue(date_range_max.getValue().plus(1,ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toEpochSecond()*1000);
        });

        date_range_min.setValue(LocalDate.now().minus(1, ChronoUnit.MONTHS));
        date_range_max.setValue(LocalDate.now());

        date_range_min.valueProperty().addListener((observable2, oldValue2, newValue2) -> {
            loadPlaylist();
        });
        date_range_max.valueProperty().addListener((observable2, oldValue2, newValue2) -> {
            loadPlaylist();
        });

        rating_cutoff.valueProperty().addListener((observable1, oldValue1, newValue1) -> {
            loadPlaylist();
        });

        channel_list.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.equals(oldValue))
                return;

            loadPlaylist(newValue.intValue());
        });

    }

    public void loadPlaylist(){
        int index = channel_list.getSelectionModel().getSelectedIndex();
        if(index >= 0)
            loadPlaylist(index);
    }

    public void loadPlaylist(int index){
        loadPlaylist(channel_list.getItems().get(index).getName());
    }

    public void loadPlaylist(String channelName){
        List<Play> result = null;
        int minRating = rating_cutoff.getValue();
        if(check_use_date_range.isSelected()){
            result = dbManager.getPlaylist(channelName, min_date.get(), max_date.get(), minRating);
        }else {
            result = dbManager.getPlaylist(channelName, minRating);
        }
        results.clear();
        results.addAll(result);
        result_table.scrollTo(0);
        label_result_count.setText( ((Integer)results.size()).toString());
    }

    public Channel getSelectedChannel(){
        return (Channel)channel_list.getSelectionModel().getSelectedItem();
    }
}

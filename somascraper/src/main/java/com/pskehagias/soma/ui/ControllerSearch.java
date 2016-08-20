package com.pskehagias.soma.ui;

import com.pskehagias.soma.common.RatingSource;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import com.pskehagias.soma.common.Channel;
import com.pskehagias.soma.common.Track;
import com.pskehagias.soma.data.SomaSQLiteHelper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by pkcyr on 6/22/2016.
 */
public class ControllerSearch implements Controller, RatingCell.RatingUpdateCallback, ControllerLaunch.ChannelSelectionProvider {
    private GridPane main_root;

    @FXML private TextField search_box;
    @FXML private ListView<Channel> channel_list;
    @FXML private ComboBox<Integer> rating_cutoff;

    @FXML private TableView<Track> result_table;
    @FXML private TableColumn<RatingSource,Integer> rating_column;

    @FXML private Label label_count;
    @FXML private Label label_result_count;

    private final SimpleIntegerProperty resultCount;
    private final SimpleIntegerProperty minRating;
    private SomaSQLiteHelper dbHelper;
    private ObservableList<Track> results;
    private ObservableList<Channel> channels;

    public ControllerSearch(){
        resultCount = new SimpleIntegerProperty(0);
        minRating = new SimpleIntegerProperty(0);
        dbHelper = new SomaSQLiteHelper();
        results = FXCollections.observableArrayList();
        channels = FXCollections.observableArrayList();

        FXMLLoader loader = new FXMLLoader();
        loader.setController(this);
        try {
            main_root = loader.load(getClass().getResourceAsStream("/layout/layout_search.fxml"));
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

    public void initialize(){
        channel_list.setItems(channels);
        rating_column.setCellFactory(new Callback<TableColumn<RatingSource, Integer>, TableCell<RatingSource,Integer>>() {
            @Override
            public RatingCell call(TableColumn param) {
                return new RatingCell(ControllerSearch.this);
            }
        });
        result_table.setItems(results);
        label_result_count.textProperty().bind(resultCount.asString());
        minRating.bind(rating_cutoff.valueProperty());

        rating_cutoff.valueProperty().addListener((observable, oldValue, newValue) -> loadTrackData());
        channel_list.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> loadTrackData());

        search_box.setOnAction((actionEvent)->onSearch(null));
    }

    public void onSearch(ActionEvent event){
        loadTrackData();
    }

    private void loadTrackData(){
        String searchQuery = search_box.getText();
        Channel selectedChannel = channel_list.getSelectionModel().getSelectedItem();
        boolean withChannel = selectedChannel != null;
        boolean withSearch = searchQuery.length() > 0;

        StringBuilder queryBuilder = new StringBuilder(1024);
        queryBuilder.append("SELECT ").append(withChannel?"DISTINCT ":"")
                .append("rated_tracks._id,rated_tracks.artist,rated_tracks.album,rated_tracks.song, rating ")
                .append("FROM rated_tracks ")
                .append(withSearch?"INNER JOIN fts_tracks ON rated_tracks._id = fts_tracks.docid ":"")
                .append(withChannel?"INNER JOIN plays ON rated_tracks._id = plays.song_id ":"")
                .append("WHERE rating >= ? ").append(withSearch?"AND fts_tracks MATCH ? ":"")
                .append(withChannel?"AND  plays.channel_id = (SELECT _id FROM channels WHERE name=? )":"");
        try(Connection connection = dbHelper.getConnection();
            PreparedStatement statement = connection.prepareStatement(queryBuilder.toString())){
            statement.setInt(1,minRating.get());
            int position = 2;
            if(withSearch) {
                statement.setString(position++, searchQuery);
            }
            if(withChannel){
                statement.setString(position, selectedChannel.getName());
            }
            try(ResultSet rs = statement.executeQuery()){
                replaceTableWithResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void replaceTableWithResultSet(ResultSet resultSet) throws SQLException{
        results.clear();
        resultCount.set(0);
        while(resultSet.next()){
            results.add(new Track(resultSet.getString(3), resultSet.getString(2), resultSet.getString(4), resultSet.getInt(5), resultSet.getLong(1)));
            resultCount.set(resultCount.get()+1);
        }
        result_table.scrollTo(0);
    }

    @Override
    public void updateRating(int rowIndex, int value) {
        final String UPDATE_RATING =
                "UPDATE song_ratings SET rating=? WHERE song_ratings._id = ?";

        Track item = results.get(rowIndex);
        item.setRating(value);
        try(Connection connection = dbHelper.getConnection();
            PreparedStatement statement = connection.prepareStatement(UPDATE_RATING)){
            statement.setInt(1,item.getRating());
            statement.setLong(2,item.getSongId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Channel getSelectedChannel() {
        return channel_list.getSelectionModel().getSelectedItem();
    }
}

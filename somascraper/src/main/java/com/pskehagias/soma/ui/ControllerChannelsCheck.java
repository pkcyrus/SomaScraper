package com.pskehagias.soma.ui;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import com.pskehagias.soma.common.Channel;

/**
 * Created by Peter on 6/10/2016.
 */
public class ControllerChannelsCheck {
    @FXML   public GridPane main_root;
    @FXML   private ListView    channels_list;
    private ObservableList<Channel> channels;

    public ControllerChannelsCheck(){
        channels = FXCollections.emptyObservableList();
    }

    @FXML   void initialize(){
        channels_list.setCellFactory(CheckBoxListCell.forListView(new Callback<Channel, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(Channel param) {
                return param.doScrapeProperty();
            }
        }));
    }

    public ReadOnlyObjectProperty<Channel> setChannels(ObservableList<Channel> c){
        channels_list.setItems(c);
        this.channels = c;
        return channels_list.getSelectionModel().selectedItemProperty();
    }

    public void setSelectionIndex(int idx){
        channels_list.getSelectionModel().select(idx);
    }

    public Channel getSelectedChannel(){
        return (Channel)channels_list.getSelectionModel().getSelectedItem();
    }

    public void onToggleSelection(ActionEvent actionEvent) {
        for(Channel c : channels){
            c.setDoScrape(!c.getDoScrape());
        }
    }

    public void onCheckUncheck(ActionEvent actionEvent) {
        int count  = 0;
        for(Channel c : channels)
            if(c.getDoScrape())
                count++;
        boolean setTo = count < (channels.size()/2);
        for(Channel c : channels){
            c.setDoScrape(setTo);
        }
    }

}

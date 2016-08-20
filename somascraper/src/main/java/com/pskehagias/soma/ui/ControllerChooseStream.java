package com.pskehagias.soma.ui;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.xml.sax.SAXException;
import com.pskehagias.soma.common.Configuration;
import com.pskehagias.soma.common.Stream;
import com.pskehagias.soma.common.SQLiteInit;

import java.io.IOException;

/**
 * Created by pkcyr on 6/22/2016.
 */
public class ControllerChooseStream {

    @FXML public Button button_play;

    public void onPlay(ActionEvent actionEvent) {
        Stage stage = (Stage)stream_select.getScene().getWindow();
        stage.close();
    }

    private Configuration configuration;
    private ObservableList<Stream> streams;

    @FXML private ListView<Stream> stream_select;

    public ControllerChooseStream(){
        try {
            configuration = new Configuration(Configuration.FILE_USER, new SQLiteInit());
            streams = FXCollections.observableArrayList();
            streams.addAll(configuration.getStreams());
        } catch (IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    public void initialize(){
        stream_select.setItems(streams);
    }

    public ReadOnlyObjectProperty<Stream> selectionProperty(){
        return stream_select.getSelectionModel().selectedItemProperty();
    }

    public void onClicks(MouseEvent event) {
        if(event.getClickCount() == 2){
            onPlay(null);
        }
    }
}

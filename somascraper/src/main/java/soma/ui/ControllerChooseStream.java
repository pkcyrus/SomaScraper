package soma.ui;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.xml.sax.SAXException;
import soma.Configuration;
import soma.Stream;

import java.io.IOException;

/**
 * Created by pkcyr on 6/22/2016.
 */
public class ControllerChooseStream {

    public void onPlay(ActionEvent actionEvent) {
        Stage stage = (Stage)stream_select.getScene().getWindow();
        stage.close();
    }

    private Configuration configuration;
    private ObservableList<Stream> streams;

    @FXML private ListView<Stream> stream_select;

    public ControllerChooseStream(){
        try {
            configuration = new Configuration();
            streams = FXCollections.observableArrayList();
            streams.addAll(configuration.getStreams());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
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

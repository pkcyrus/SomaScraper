package soma.ui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Modality;
import javafx.stage.Stage;
import soma.Configuration;
import soma.Stream;
import soma.async.RecordStreamTask;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by pkcyr on 6/21/2016.
 */
public class ControllerStreamMusic implements ChangeListener<Number>{
    @FXML private HBox main_root;
    @FXML private Button play_pause;
    @FXML private Button stop;
    @FXML private Slider volume;

    @FXML private Label channel_name;
    @FXML private Label channel_format;

    private boolean playing = false;
    private Media media;
    private MediaPlayer player;
    private Configuration configuration;
    private File saveFile;
    private File bufferFile;
    private final SimpleObjectProperty<Stream> stream;

    private Thread thread;
    private RecordStreamTask downloadTask;

    public ControllerStreamMusic(){
        stream = new SimpleObjectProperty<>(new Stream());
        media = null;
        player = null;
        bufferFile = null;
        thread = null;
        downloadTask = null;
    }

    public void initialize(){
    }

    public void setConfiguration(Configuration configuration){
        this.configuration = configuration;
    }

    public void changeChannel(Stream channel){
        if(configuration == null)
            return;

        cleanup();

        channel_name.setText(channel.getName());
        channel_format.setText(channel.getType());

        int bufferSize = Integer.parseInt(configuration.getString(Configuration.PLAYER_BUFFER));
        String url = channel.getAlt1();
        String bufferPath = configuration.getString(Configuration.PLAYER_DIRECTORY);
        File directory = new File(bufferPath);
        directory.mkdirs();

        SimpleDateFormat df = new SimpleDateFormat(" yyyy-MM-dd HH;mm;ss.");
        Calendar now = Calendar.getInstance();
        String filename = channel.getName() + df.format(now.getTime()) +(channel.getType().substring(0,3).equals("mp3")?"mp3":"m4a");

        saveFile = new File(bufferPath+filename);
        bufferFile = new File("buffer.mp3");
        try {
            downloadTask = new RecordStreamTask(bufferFile, new URL(url), bufferSize, this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        thread = new Thread(downloadTask);
        thread.setDaemon(true);
        thread.start();
    }

    public void selectChannel(){
        FXMLLoader loader = new FXMLLoader();
        try {
            VBox streamSelect = loader.load(getClass().getResourceAsStream("/layout/layout_choose_stream.fxml"));
            ControllerChooseStream streamController = loader.getController();
            stream.bind(streamController.selectionProperty());

            Stage dialog = new Stage();
            dialog.setScene(new Scene(streamSelect, 500, 300));
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(main_root.getScene().getWindow());

            dialog.showAndWait();

            changeChannel(stream.get());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onPlayPause(ActionEvent actionEvent) {
        if (player != null) {
            if (playing) {
                player.pause();
            } else {
                player.play();
            }
        }else{
            if(stream.get().getName().equals(""))
                selectChannel();
            else
                changeChannel(stream.get());
        }
    }

    public void onStop(ActionEvent actionEvent) {
        cleanup();

        if(Boolean.parseBoolean(configuration.getString(Configuration.PLAYER_RECORD))){
            try {
                Files.copy(bufferFile.toPath(), saveFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void cleanup(){
        if(player != null){
            player.dispose();
            media = null;
            player = null;
        }
        if(downloadTask != null){
            downloadTask.progressProperty().removeListener(this);
            downloadTask.cancel();
            downloadTask = null;
        }
        if(thread != null){
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread = null;
        }

        play_pause.setText("Play");
        stop.setDisable(true);
    }

    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        System.out.println("Progress:"+newValue);
        if(newValue.doubleValue() >= 1.0){
            System.out.println(bufferFile.toURI().toString());
            media = new Media(bufferFile.toURI().toString());

            player = new MediaPlayer(media);
            player.setOnReady(()->{player.play(); stop.setDisable(false);});
            player.setOnPlaying(()->{
                play_pause.setText("Pause");
                playing=true;
            });
            player.setOnPaused(()->{
                play_pause.setText("Play");
                playing=false;
            });
            player.volumeProperty().bind(volume.valueProperty());
            observable.removeListener(this);
        }
    }
}

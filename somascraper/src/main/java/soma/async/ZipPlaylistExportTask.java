package soma.async;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import soma.export.CSVPlaylistFormatter;
import soma.data.SomaDBManager;
import soma.export.ZipPlaylistExporter;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by Peter on 6/13/2016.
 */
public class ZipPlaylistExportTask extends Task<Void> implements ProgressCallback {
    private Map<String,File> targets;
    private File archive;
    private String delimiter;
    private SomaDBManager dbManager;

    private final SimpleStringProperty subWorkName;
    private final SimpleLongProperty subWork;
    private final SimpleLongProperty subWorkTotal;
    private final SimpleDoubleProperty subWorkProgress;

    public SimpleStringProperty subWorkNameProperty(){return subWorkName;}
    public SimpleLongProperty subWorkProperty(){return subWork;}
    public SimpleLongProperty subWorkTotalProperty(){return subWorkTotal;}
    public SimpleDoubleProperty subWorkProgressProperty(){return subWorkProgress;}

    public ZipPlaylistExportTask(File archive, Map<String, File> targets, String delimiter) throws SQLException {
        subWorkName = new SimpleStringProperty("");
        subWork = new SimpleLongProperty(0);
        subWorkTotal = new SimpleLongProperty(1);
        subWorkProgress = new SimpleDoubleProperty(0);

        subWorkProgress.bind(subWork.divide(subWorkTotal));

        this.archive = archive;
        this.targets = targets;
        this.delimiter = delimiter;
        dbManager = new SomaDBManager();
    }

    @Override
    protected Void call() throws Exception {
        int i = 0;
        updateProgress(0, targets.size());
        try(ZipPlaylistExporter zpe = new ZipPlaylistExporter(new CSVPlaylistFormatter(delimiter), archive, this)) {
            for (Map.Entry<String, File> entry : targets.entrySet()) {
                subWorkName.set(entry.getKey());
                zpe.write(entry.getValue().getName(), dbManager.getPlaylist(entry.getKey()));
                updateProgress(++i, targets.size());
            }
        }
        return null;
    }

    @Override
    protected void succeeded() {
        System.out.println("Save successful!");
    }

    @Override
    public void updateCallback(long value, long max)
    {
        subWork.set(value);
        subWorkTotal.set(max);
    }

    @Override
    public void updateCallback(double value, double max) {
        updateCallback((long)value, (long)max);
    }
}

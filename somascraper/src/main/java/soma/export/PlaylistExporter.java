package soma.export;

import soma.Play;
import soma.async.ProgressCallback;

import java.io.*;
import java.util.List;

/**
 * Created by pkcyr on 6/13/2016.
 */
public class PlaylistExporter implements Closeable{

    private ProgressCallback callback;
    private PlaylistFormatter formatter;
    private BufferedWriter writer;

    public PlaylistExporter(PlaylistFormatter formatter, ProgressCallback callback) {
        this.formatter = formatter;
        this.callback = callback;
        writer = null;
    }

    public PlaylistExporter(PlaylistFormatter formatter, File target, ProgressCallback callback) throws IOException {
        this.formatter = formatter;
        this.callback = callback;
        open(target);
    }

    public void open(File file) throws IOException {
        if(writer != null)
            writer.close();

        writer = new BufferedWriter(new FileWriter(file));
    }

    public void write(List<Play> playList) throws IOException {
        if(writer == null){
            throw new IOException("No file open for writing.");
        }
        writer.write(formatter.header());
        long progress = 0;
        final long size = playList.size();
        for(Play p : playList){
            writer.write(formatter.format(p));
            if(callback != null)
                callback.updateCallback(++progress, size);
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
        writer = null;
    }
}

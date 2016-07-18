package soma.export;

import soma.Play;
import soma.async.ProgressCallback;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by pkcyr on 6/13/2016.
 */
public class ZipPlaylistExporter implements Closeable {
    private ProgressCallback callback;
    private PlaylistFormatter formatter;
    private ZipOutputStream writer;
    private StringBuilder outBuffer;

    public ZipPlaylistExporter(PlaylistFormatter formatter, ProgressCallback callback) {
        this.callback = callback;
        this.formatter = formatter;
        this.outBuffer = new StringBuilder(8192);
        writer = null;
    }

    public ZipPlaylistExporter(PlaylistFormatter formatter, File target, ProgressCallback callback) throws IOException {
        this.callback = callback;
        this.formatter = formatter;
        this.outBuffer = new StringBuilder(8192);
        open(target);
    }

    public void open(File file) throws IOException {
        if(writer != null)
            writer.close();
        writer = new ZipOutputStream(new FileOutputStream(file));
    }

    public void write(String listName, List<Play> playList) throws IOException {
        if(writer == null){
            throw new IOException("No file open for writing.");
        }
        writer.putNextEntry(new ZipEntry(listName));
        outBuffer.setLength(0);
        outBuffer.append(formatter.header());
        long work = 0;
        final long size = playList.size();
        for(Play p : playList){
            outBuffer.append(formatter.format(p));
            if(outBuffer.length() > 4096){
                writer.write(outBuffer.toString().getBytes());
                outBuffer.setLength(0);
            }
            if(callback != null){
                callback.updateCallback(++work, size);
            }
        }
        if(outBuffer.length() > 0){
            writer.write(outBuffer.toString().getBytes());
        }
        writer.closeEntry();
    }

    @Override
    public void close() throws IOException {
        writer.close();
        writer = null;
    }
}

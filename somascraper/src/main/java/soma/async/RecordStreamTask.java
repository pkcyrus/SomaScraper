package soma.async;

import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;

import java.io.*;
import java.net.URL;

/**
 * Created by pkcyr on 6/21/2016.
 */
public class RecordStreamTask extends Task<Void> {
    public static final int DEFAULT_BUFFER = 512; //512kBytes

    private File target;
    private URL stream;
    private final long diskBufferSize;

    public RecordStreamTask(File target, URL stream, int diskBufferSize, ChangeListener<Number> bufferListener){
        if(bufferListener!= null)
            progressProperty().addListener(bufferListener);
        this.target = target;
        this.stream = stream;
        this.diskBufferSize = diskBufferSize * 1024;
    }

    @Override
    protected Void call() throws Exception {
        updateProgress(0,diskBufferSize);
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(target));
             BufferedInputStream inputStream = new BufferedInputStream(stream.openStream())){
//            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(target));
//            BufferedInputStream inputStream = new BufferedInputStream(stream.openStream());

            long work = 0;
            int read = 0;
            byte[] buffer = new byte[4096];
            while ((read = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, read);
                work += read;
                updateProgress(work, diskBufferSize);
                if (isCancelled())
                    break;
            }
//            outputStream.close();
//            inputStream.close();
//            target.delete();
        }catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}

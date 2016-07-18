package soma.async;

import javafx.concurrent.Task;
import org.xml.sax.SAXException;
import soma.Channel;
import soma.Play;
import soma.Scraper;

import java.io.IOException;
import java.util.List;

/**
 * Created by Peter on 6/19/2016.
 */
public class ScrapeChannelsTask extends Task<List<Play>> implements ProgressCallback {
    private Scraper scraper;
    private Channel channel;

    public ScrapeChannelsTask(Channel channel){
        try {
            this.channel = channel;
            scraper = new Scraper(this);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected List<Play> call() throws Exception {
        List<Play> result;
        if(channel == null)
            result = scraper.scrapeAllNow();
        else
            result = scraper.scrapeChannel(channel);
        return result;
    }

    @Override
    public void updateCallback(long value, long max) {
        updateProgress(value,max);
    }

    @Override
    public void updateCallback(double value, double max) {
        updateProgress(value,max);
    }
}

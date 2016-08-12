package com.pskehagias.soma.async;

import com.pskehagias.soma.async.ProgressCallback;
import javafx.concurrent.Task;
import com.pskehagias.soma.common.Channel;
import com.pskehagias.soma.common.Play;
import com.pskehagias.soma.scraper.Scraper;

import java.util.List;

/**
 * Created by Peter on 6/19/2016.
 */
public class ScrapeChannelsTask extends Task<List<Play>> implements ProgressCallback {
    private Scraper scraper;
    private Channel channel;
    private List<Channel> channels;

    public ScrapeChannelsTask(Channel channel){
        this.channel = channel;
        scraper = new Scraper(this);
    }

    public ScrapeChannelsTask(List<Channel> channels){
        this.channels = channels;
        scraper = new Scraper(this);
    }

    @Override
    protected List<Play> call() throws Exception {
        List<Play> result = null;
        if(channels != null)
            result = scraper.scrapeAllNow(channels);
        else if(channel != null)
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

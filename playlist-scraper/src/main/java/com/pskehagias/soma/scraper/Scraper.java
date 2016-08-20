package com.pskehagias.soma.scraper;

import com.pskehagias.soma.common.Channel;
import com.pskehagias.soma.common.Play;
import com.pskehagias.soma.util.ParseTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import com.pskehagias.soma.async.ProgressCallback;

import java.io.IOException;
import java.util.*;

/**
 * Created by Peter on 6/9/2016.
 */
public class Scraper {
    private ProgressCallback callback;

    public Scraper(ProgressCallback callback){
        this.callback = callback;
    }

    public List<Play> scrapeChannel(Channel channel){
        if(callback != null){
            callback.updateCallback(0,1);
        }
        return scrapeInternal(channel);
    }

    private List<Play> scrapeInternal(Channel channel) {
        List<Play> result = new ArrayList<>(30);
        try {
            Document document = Jsoup.connect(channel.getUrl()).timeout(10000).get();
            Elements songRows = document.select("#playinc tr");
            for (int idx = 3; idx < songRows.size(); idx++) {
                Elements columns = songRows.get(idx).select("td");
                if (columns.size() < 4)
                    continue;
                result.add(new Play(ParseTime.convertTimeToMillis(columns.get(0).text()), columns.get(1).text(), columns.get(3).text(), columns.get(2).text(), channel.getName(), 0,0,0,0,0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Play> scrapeAllNow(List<Channel> channels){
        List<Play> result = new ArrayList<>(50);

        long work = 0;
        final long tWork = channels.stream().filter(Channel::getDoScrape).count();
        if(callback != null)
            callback.updateCallback(work,tWork);
        for(Channel c : channels){
            if(c.getDoScrape()){
                result.addAll(scrapeInternal(c));
                if(callback != null)
                    callback.updateCallback(++work,tWork);
            }
        }
        return result;
    }
}

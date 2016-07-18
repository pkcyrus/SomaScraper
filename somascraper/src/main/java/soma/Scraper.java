package soma;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;
import soma.async.ProgressCallback;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Peter on 6/9/2016.
 */
public class Scraper {
    private Configuration config;
    private ProgressCallback callback;

    public Scraper(ProgressCallback callback)throws IOException,SAXException{
        config = new Configuration();
        this.callback = callback;
    }

    public long convertTimeToMillis(String time) {
        Calendar now = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        try {
            Date playtime = df.parse(time);
            if(now.get(Calendar.HOUR_OF_DAY) < playtime.getHours()) {
                now.set(Calendar.DATE, now.get(Calendar.DATE)-1);
            }
            now.set(Calendar.HOUR_OF_DAY, playtime.getHours());
            now.set(Calendar.MINUTE, playtime.getMinutes());
            now.set(Calendar.SECOND, playtime.getSeconds());
            now.set(Calendar.MILLISECOND, 0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return now.getTimeInMillis();
    }

    public List<Play> scrapeChannel(String channel){
        List<Channel> channels = config.getChannels();
        List<Play> result = null;
        for(Channel c : channels){
            if(c.getName().equals(channel)){
                result = scrapeChannel(c);
            }
        }
        return result;
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
                result.add(new Play(convertTimeToMillis(columns.get(0).text()), columns.get(1).text(), columns.get(3).text(), columns.get(2).text(), channel.getName(), 0,0,0,0,0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Play> scrapeAllNow(){
        List<Play> result = new ArrayList<>(50);
        List<Channel> channels = config.getChannels();
        long work = 0;
        final long tWork = channels.stream().filter(channel -> channel.getDoScrape()).count();
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

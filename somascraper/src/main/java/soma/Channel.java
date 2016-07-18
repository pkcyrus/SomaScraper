package soma;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Created by Peter on 6/10/2016.
 */
public class Channel {
    private final SimpleBooleanProperty doScrape;
    private String name;
    private String pl_url;

    public Channel(String name, String pl_url, boolean doScrape) {
        this.name = name;
        this.pl_url = pl_url;
        this.doScrape = new SimpleBooleanProperty(doScrape);
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return pl_url;
    }

    public boolean getDoScrape() {
        return doScrape.get();
    }

    public void setDoScrape(boolean doScrape) {
        this.doScrape.set(doScrape);
    }

    public BooleanProperty doScrapeProperty(){
        return doScrape;
    }
    @Override
    public String toString() {
        return this.name;
    }
}

package com.pskehagias.soma.common;

import com.pskehagias.common.XpathConfiguration;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.xpath.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter on 6/10/2016.
 */
public class Configuration extends XpathConfiguration{
    public interface InitializerDelegate{
        void initialize(Configuration config);
    }

    public static final String FILE_DEFAULT = "/default_config.xml";
    public static final String FILE_USER = "./soma.xml";

    public static final String SCRAPER_INITIALIZED = "/scraper/initialized";
    public static final String SCRAPE_FREQUENCY = "//scrapeFrequency";
    public static final String MINIMIZE_ON_EXIT = "//minimizeOnExit";
    public static final String PLAYER_BUFFER = "//player/@buffer";
    public static final String PLAYER_DIRECTORY = "//player/@directory";
    public static final String PLAYER_RECORD = "//player/@record";
    public static final String DATABASE_DIRECTORY = "//databaseDirectory";

    public static final String CHANNELS = "//channels/channel";

    public Configuration() throws IOException, SAXException {
        this(FILE_USER);
    }

    public Configuration(String file) throws IOException, SAXException {
        super(file, FILE_DEFAULT);
    }

    public Configuration(String file, InitializerDelegate initializer) throws IOException, SAXException {
        this(file);
        if(!isInitialized()) {
            initializer.initialize(this);
        }
    }

    public boolean isInitialized(){
        boolean result = false;
        try{
            XPathExpression isInit = xPath.compile("boolean(/scraper/initialized[.=\"true\"])");
            result = (Boolean) isInit.evaluate(configDocument, XPathConstants.BOOLEAN);
        }catch(XPathExpressionException e){
            e.printStackTrace();
        }
        return result;
    }

    public void setInitialized(boolean val){
        setBoolean(SCRAPER_INITIALIZED, val);
    }

    public List<Channel> getChannels(){
        List<Channel> result = new ArrayList<>(40);
        try{
            XPathExpression channelExpr = xPath.compile(CHANNELS);
            XPathExpression doScrapeExpr = xPath.compile("boolean(./doScrape[.=\"true\"])");

            NodeList channels = (NodeList)channelExpr.evaluate(configDocument, XPathConstants.NODESET);
            for(int idx = 0; idx < channels.getLength(); idx++){
                Node current = channels.item(idx);
                NamedNodeMap attributes = current.getAttributes();
                boolean doScrape = (Boolean) doScrapeExpr.evaluate(current, XPathConstants.BOOLEAN);
                String name = attributes.getNamedItem("name").getNodeValue();
                String pl_url = attributes.getNamedItem("pl_url").getNodeValue();

                result.add(new Channel(name, pl_url, doScrape));
            }
        }catch(XPathExpressionException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Stream> getStreams(){
        List<Stream> results = new ArrayList<>(100);
        try{
            XPathExpression channelExpr = xPath.compile(CHANNELS);
            XPathExpression streamExpr = xPath.compile("./stream");

            NodeList channels = (NodeList) channelExpr.evaluate(configDocument, XPathConstants.NODESET);
            for(int idx = 0; idx < channels.getLength(); idx++){
                Node current = channels.item(idx);
                String name = current.getAttributes().getNamedItem("name").getNodeValue();
                NodeList streams = (NodeList) streamExpr.evaluate(current, XPathConstants.NODESET);
                for(int idx2 = 0; idx2 < streams.getLength(); idx2++){
                    NamedNodeMap attributes = streams.item(idx2).getAttributes();
                    String alt1 = attributes.getNamedItem("url").getNodeValue();
                    String alt2 = attributes.getNamedItem("alt").getNodeValue();
                    String type = attributes.getNamedItem("type").getNodeValue();

                    results.add(new Stream(name, type, alt1, alt2));
                }
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return results;
    }

    public String getImage(String channel){
        String result = null;
        try{
            String gc = String.format("//channel[@name=\"%s\"]/icon/@resource",channel);
            XPathExpression imageExpr = xPath.compile(gc);

            Node node = (Node)imageExpr.evaluate(configDocument, XPathConstants.NODE);
            if(node != null){
                result = node.getTextContent();
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void setChannelConfig(String name, boolean doScrape){
        try{
            String gc = String.format("//channel[@name=\"%s\"]/doScrape",name);
            XPathExpression getChannel = xPath.compile(gc);
            Node node = (Node)getChannel.evaluate(configDocument, XPathConstants.NODE);
            node.setTextContent(((Boolean)doScrape).toString());
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }
}

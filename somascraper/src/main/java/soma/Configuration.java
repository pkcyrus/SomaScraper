package soma;

import javafx.scene.image.Image;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import soma.data.SomaDBManager;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter on 6/10/2016.
 */
public class Configuration {
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

    private Document fileDoc;
    private String filename;

    private final XPath xPath;

    public Configuration() throws IOException, SAXException {
        this(FILE_USER);
    }

    public Configuration(String file) throws IOException, SAXException {
        xPath = XPathFactory.newInstance().newXPath();
        loadConfiguration(file);

        if(!isInitialized()){
            initialize();
        }
    }

    public boolean isInitialized(){
        boolean result = false;
        try{
            XPathExpression isInit = xPath.compile("boolean(/scraper/initialized[.=\"true\"])");
            result = (Boolean) isInit.evaluate(fileDoc, XPathConstants.BOOLEAN);
        }catch(XPathExpressionException e){
            e.printStackTrace();
        }
        return result;
    }

    public void initialize(){
        try {
            SomaDBManager scraper = new SomaDBManager();
            XPathExpression initializeNode = xPath.compile(SCRAPER_INITIALIZED);
            XPathExpression channelExpression = xPath.compile(CHANNELS);

            NodeList channels = (NodeList)channelExpression.evaluate(fileDoc, XPathConstants.NODESET);
            for (int idx = 0; idx < channels.getLength(); idx++) {
                NamedNodeMap attributes = channels.item(idx).getAttributes();
                String name = attributes.getNamedItem("name").getNodeValue();
                String pl_url = attributes.getNamedItem("pl_url").getNodeValue();
                scraper.addChannel(name, pl_url);
            }

            Node init = (Node)initializeNode.evaluate(fileDoc, XPathConstants.NODE);
            init.setTextContent("true");
            saveConfiguration();
        }catch(XPathExpressionException e){
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadConfiguration(String file)throws IOException, SAXException{
        this.filename = file;
        File check = new File(file);
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            if(check.exists())
                fileDoc = docBuilder.parse(file);
            else {
                fileDoc = docBuilder.parse(getClass().getResourceAsStream(FILE_DEFAULT));
                saveConfiguration();
            }
        }catch(ParserConfigurationException e){
            throw new RuntimeException("Can't load soma.xml");
        }
    }

    public void saveConfiguration(){
        saveConfiguration(filename);
    }

    public void saveConfiguration(String file){
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();

            Result output = new StreamResult(new File(file));
            Source input = new DOMSource(fileDoc);

            transformer.transform(input, output);
        }catch(TransformerException e){
            System.err.println("Failed to save configuration");
            e.printStackTrace();
        }
    }

    public List<Channel> getChannels(){
        List<Channel> result = new ArrayList<>(40);
        try{
            XPathExpression channelExpr = xPath.compile(CHANNELS);
            XPathExpression doScrapeExpr = xPath.compile("boolean(./doScrape[.=\"true\"])");

            NodeList channels = (NodeList)channelExpr.evaluate(fileDoc, XPathConstants.NODESET);
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

            NodeList channels = (NodeList) channelExpr.evaluate(fileDoc, XPathConstants.NODESET);
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

    public Image getImage(String channel){
        Image result = null;
        try{
            String gc = String.format("//channel[@name=\"%s\"]/icon/@resource",channel);
            XPathExpression imageExpr = xPath.compile(gc);

            Node node = (Node)imageExpr.evaluate(fileDoc, XPathConstants.NODE);
            if(node != null){
                result = new Image(getClass().getResourceAsStream(node.getTextContent()));
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
            Node node = (Node)getChannel.evaluate(fileDoc, XPathConstants.NODE);
            node.setTextContent(((Boolean)doScrape).toString());
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    public String getString(String xpath){
        String result = null;
        try{
            XPathExpression expression = xPath.compile(xpath);
            Node node = (Node)expression.evaluate(fileDoc, XPathConstants.NODE);
            if(node != null)
                result = node.getTextContent();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void setString(String xpath, String value){
        try{
            XPathExpression expression = xPath.compile(xpath);
            Node node = (Node)expression.evaluate(fileDoc, XPathConstants.NODE);
            if(node != null)
                node.setTextContent(value);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    public int getInteger(String xpath){
        return Integer.parseInt(getString(xpath));
    }

    public void setInteger(String xpath, Integer value){
        setString(xpath,value.toString());
    }

    public double getDouble(String xpath){
        return Double.parseDouble(getString(xpath));
    }

    public void setDouble(String xpath, Double value){
        setString(xpath, value.toString());
    }

    public boolean getBoolean(String xpath){
        return Boolean.parseBoolean(getString(xpath));
    }

    public void setBoolean(String xpath, Boolean value){
        setString(xpath, value.toString());
    }
}

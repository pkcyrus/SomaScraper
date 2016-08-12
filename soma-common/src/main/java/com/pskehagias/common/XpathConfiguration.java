package com.pskehagias.common;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by pkcyr on 8/12/2016.
 */
public class XpathConfiguration {

    protected final String defaultFile;
    protected final String userFile;

    protected final XPath xPath;
    protected final DocumentBuilder dBuilder;
    protected final Transformer transformer;

    protected Document configDocument;

    public XpathConfiguration(String userFile, String defaultFile) throws IOException, SAXException {
        this.defaultFile = defaultFile;
        this.userFile = userFile;

        xPath = XPathFactory.newInstance().newXPath();
        try {
            dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (ParserConfigurationException e) {
            IllegalStateException ex = new IllegalStateException("The default parser configuration was invalid...");
            ex.initCause(e);
            throw ex;
        } catch (TransformerConfigurationException e) {
            IllegalStateException ex = new IllegalStateException("The default transformer configuration was invalid...");
            ex.initCause(e);
            throw ex;
        }

        loadConfiguration();
    }

    private void loadConfiguration() throws IOException, SAXException {
        File check = new File(userFile);
        if(check.exists()){
            configDocument = dBuilder.parse(userFile);
        }else{
            loadDefault();
        }
    }

    public void loadDefault() throws IOException, SAXException {
        configDocument = dBuilder.parse(getClass().getResourceAsStream(defaultFile));
    }

    public void saveConfiguration() throws TransformerException {
        Result output = new StreamResult(new File(userFile));
        Source input = new DOMSource(configDocument);
        transformer.transform(input, output);
    }

    public String getString(String xpath){
        String result = null;
        try{
            XPathExpression expression = xPath.compile(xpath);
            Node node = (Node)expression.evaluate(configDocument, XPathConstants.NODE);
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
            Node node = (Node)expression.evaluate(configDocument, XPathConstants.NODE);
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

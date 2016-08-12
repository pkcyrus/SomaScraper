package com.pskehagias.soma.common;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by Peter on 6/29/2016.
 */
public class Stream {
    private final SimpleStringProperty alt1;
    private final SimpleStringProperty alt2;
    private final SimpleStringProperty name;
    private final SimpleStringProperty type;

    public Stream() {
        alt1 = new SimpleStringProperty("");
        alt2 = new SimpleStringProperty("");
        name = new SimpleStringProperty("");
        type = new SimpleStringProperty("");
    }

    public Stream(String channelName, String type, String alt1, String alt2) {
        this.alt1 = new SimpleStringProperty(alt1);
        this.alt2 = new SimpleStringProperty(alt2);
        this.name = new SimpleStringProperty(channelName);
        this.type = new SimpleStringProperty(type);
    }

    public SimpleStringProperty alt1Property() {
        return alt1;
    }

    public SimpleStringProperty alt2Property() {
        return alt2;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleStringProperty typeProperty() {
        return type;
    }

    public String getAlt1() {
        return alt1.get();
    }

    public String getAlt2() {
        return alt2.get();
    }

    public String getName() {
        return name.get();
    }

    public String getType() {
        return type.get();
    }

    public void setAlt1(String value) {
        alt1.set(value);
    }

    public void setAlt2(String value) {
        alt2.set(value);
    }

    public void setName(String value) {
        name.set(value);
    }

    public void setType(String value) {
        type.set(value);
    }

    @Override
    public String toString() {
        return name.get() + " " + type.get();
    }
}

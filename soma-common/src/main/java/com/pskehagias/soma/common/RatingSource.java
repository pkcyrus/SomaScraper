package com.pskehagias.soma.common;

import javafx.beans.property.SimpleIntegerProperty;

/**
 * Created by pkcyr on 8/20/2016.
 */
public interface RatingSource {
    SimpleIntegerProperty ratingProperty();
    int getRating();
    void setRating(int rating);
}

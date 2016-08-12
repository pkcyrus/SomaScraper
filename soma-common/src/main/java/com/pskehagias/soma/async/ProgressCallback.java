package com.pskehagias.soma.async;

/**
 * Created by pkcyr on 6/18/2016.
 *
 */
public interface ProgressCallback {
    void updateCallback(long value, long max);
    void updateCallback(double value, double max);
}

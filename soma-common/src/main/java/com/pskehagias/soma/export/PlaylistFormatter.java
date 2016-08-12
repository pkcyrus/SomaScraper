package com.pskehagias.soma.export;

import com.pskehagias.soma.common.Play;

/**
 * Created by pkcyr on 6/13/2016.
 */
public interface PlaylistFormatter {
    String header();
    String format(Play p);
}

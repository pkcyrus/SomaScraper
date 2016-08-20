package com.pskehagias.soma.ui;

import javafx.scene.control.TableCell;
import com.pskehagias.soma.common.Play;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by Peter on 7/17/2016.
 * Formats the milliseconds from epoch timestamp into a human readable format.
 */
public class TimestampCell extends TableCell<Play, Long> {
    public static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");

    @Override
    protected void updateItem(Long item, boolean empty) {
        if (!empty) {
            OffsetDateTime odt = OffsetDateTime.ofInstant(Instant.ofEpochMilli(item), OffsetTime.now().getOffset());
            setText(odt.format(dateTimeFormat));
        } else
            setText("");
    }
}

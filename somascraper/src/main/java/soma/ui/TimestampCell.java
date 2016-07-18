package soma.ui;

import javafx.scene.control.TableCell;
import soma.Play;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Peter on 7/17/2016.
 * Formats the milliseconds from epoch timestamp into a human readable format
 */
public class TimestampCell extends TableCell<Play, Long> {
    public static final SimpleDateFormat dateFormat = getDateFormat();

    public static SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    }

    @Override
    protected void updateItem(Long item, boolean empty) {
        if (!empty) {
            Date date = new Date(item);
            setText(dateFormat.format(date));
        } else
            setText("");
    }
}

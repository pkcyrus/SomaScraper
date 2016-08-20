package com.pskehagias.soma.ui;

import com.pskehagias.soma.common.RatingSource;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import com.pskehagias.soma.common.Play;

/**
 * Created by pkcyr on 7/14/2016.
 */
public class RatingCell extends TableCell<RatingSource, Integer> implements EventHandler<MouseEvent> {
    private static final Color[] barColor =
            {Color.DARKRED,
                    Color.RED,
                    Color.ORANGERED,
                    Color.DARKORANGE,
                    Color.ORANGE,
                    Color.YELLOW,
                    Color.GREENYELLOW,
                    Color.FORESTGREEN,
                    Color.GREEN,
                    Color.DARKGREEN};
    private Canvas ratingBar;
    private Integer rating;
    boolean clickMode = false;
    private RatingUpdateCallback callback;


    public RatingCell(RatingUpdateCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback can not be NULL");
        }
        this.callback = callback;

        setPadding(new Insets(5, 5, 5, 5));
        ratingBar = new Canvas(100, 20);
        setGraphic(ratingBar);

        setOnMousePressed(this);
        setOnMouseDragged(this);
        setOnMouseReleased(this);
    }

    @Override
    public void handle(MouseEvent event) {
        EventType type = event.getEventType();
        Integer mouseRating = getRatingFromMouse(event.getX());

        if (type == MouseEvent.MOUSE_PRESSED) {
            clickMode = true;
        } else if (type == MouseEvent.MOUSE_DRAGGED) {

        } else if (type == MouseEvent.MOUSE_RELEASED) {
            clickMode = false;
            rating = mouseRating;
            callback.updateRating(getTableRow().getIndex(), rating);
        } else {
            return;
        }
        drawBar(mouseRating);
    }

    private Integer getRatingFromMouse(double mouseX) {
        if (mouseX > ratingBar.getWidth())
            mouseX = ratingBar.getWidth();
        else if (mouseX < 0)
            mouseX = 0;

        return (int) Math.ceil((mouseX - 5) * 10 / ratingBar.getWidth());
    }

    @Override
    public void resize(double width, double height) {
        super.resize(width, height);
        ratingBar.resize(width - 10, height - 10);
        ratingBar.setWidth(width - 10);
        if (rating != null) {
            drawBar();
        }
    }

    private void drawBar() {
        drawBar(rating);
    }

    private void drawBar(Integer rating) {
        GraphicsContext context = ratingBar.getGraphicsContext2D();
        double width = ratingBar.getWidth();
        double height = ratingBar.getHeight();
        if (rating == 0) {
            context.setFill(Color.LIGHTGREY);
            context.fillRect(0, 0, width, height);
        } else {
            context.clearRect(0, 0, width, height);
            for (int i = 0; i < rating; i++) {
                context.setFill(barColor[i]);
                context.fillRect(i * width / 10, 0, width / 10, height);
            }
        }
    }

    private void clearBar() {
        GraphicsContext context = ratingBar.getGraphicsContext2D();
        context.clearRect(0, 0, ratingBar.getWidth(), ratingBar.getHeight());
    }

    @Override
    protected void updateItem(Integer item, boolean empty) {
        rating = item;
        if (!empty) {
            drawBar();
        } else {
            clearBar();
        }
    }

    public interface RatingUpdateCallback{
        void updateRating(int rowIndex, int value);
    }
}

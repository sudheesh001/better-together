package ac.robinson.bettertogether.plugin.base.cardgame.models;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;

import ac.robinson.bettertogether.plugin.base.cardgame.utils.MathUtils;

import static android.content.ContentValues.TAG;

/**
 * Created by t-apmehr on 4/5/2017.
 */

public abstract class Renderable {

    public static final int OVERLAP_THRESHOLD_LIMIT = 250*250;

    public int x; // X cooridnate
    public int y; // Y coordinate
    private boolean touched; // if it has been touched or picked up
    Bitmap bitmap = null;

    private String name;

    protected boolean hidden;

    protected final int scaledWidth = 300;
    protected final int scaledHeight = 375;

    protected CardDeckStatus status;

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    protected static final int MAX_DURATION = 200;

    public CardDeckStatus getStatus() {
        return status;
    }

    public void setStatus(CardDeckStatus status) {
        this.status = status;
    }

    public abstract String getName();

    public abstract void setName(String name);

    public abstract Gesture handleActionDown(int eventX, int eventY);

    public abstract void draw(Canvas canvas);

    public int getX(){
        return this.x;
    }

    public void setX(int x){
        this.x = x-scaledWidth/2;
    }

    public int getY(){
        return this.y;
    }

    public void setY(int y){
        this.y = y-scaledHeight/2;
    }

    public abstract boolean isTouched();

    public abstract void setTouched(boolean touched);

    public boolean isOverlapping(Renderable image) {
        Bitmap imBmp = image.getBitmap();

        if (imBmp == null || bitmap == null) return false;

        MathUtils.Rectangle imRect = new MathUtils.Rectangle(
                image.getX() - imBmp.getWidth()/2,
                image.getY() - imBmp.getHeight()/2,
                image.getX() + imBmp.getWidth()/2,
                image.getY() + imBmp.getHeight()/2
        );
        MathUtils.Rectangle selfRect = new MathUtils.Rectangle(
                getX() - bitmap.getWidth()/2,
                getY() - bitmap.getHeight()/2,
                getX() + bitmap.getWidth()/2,
                getY() + bitmap.getHeight()/2
        );

        int intArea = MathUtils.rectangleIntersectionArea(imRect, selfRect);
        Log.d(TAG, "isOverlapping: " + intArea);
        return intArea >= OVERLAP_THRESHOLD_LIMIT;
    }

    public abstract Card handleDoubleTap(MotionEvent event);

    public Bitmap getBitmap() {
        return bitmap;
    }
}
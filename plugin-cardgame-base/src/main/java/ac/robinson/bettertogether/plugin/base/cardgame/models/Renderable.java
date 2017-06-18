package ac.robinson.bettertogether.plugin.base.cardgame.models;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;

import java.util.List;

import ac.robinson.bettertogether.plugin.base.cardgame.utils.MathUtils;

import static android.content.ContentValues.TAG;

/**
 * Created by t-apmehr on 4/5/2017.
 */

public abstract class Renderable {

    public int x; // X cooridnate
    public int y; // Y coordinate
    private boolean touched; // if it has been touched or picked up
    Bitmap bitmap = null;

    private String name;
    public boolean safeToDelete = false; // should this card be deleted.

    protected boolean hidden;
    protected static final Paint GLOW_PAINT = new Paint();
    static {
        final int GLOW_RADIUS = 32;
        GLOW_PAINT.setColor(Color.rgb(255, 255, 255));
        GLOW_PAINT.setMaskFilter(new BlurMaskFilter(GLOW_RADIUS, BlurMaskFilter.Blur.OUTER));
    }
    public static Renderable selectedRenderableForContext = null;

    protected final int FACADE_SCALED_WIDTH = 30;
    public static final int scaledWidth = 300;
    public static final int scaledHeight = 375;
    public static final int OVERLAP_THRESHOLD_LIMIT = ((scaledWidth*5)/6) * ((scaledHeight*2)/3);

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

    public void displaceX(int x) {
        this.x += x;
    }

    public void displaceY(int y) {
        this.y += y;
    }

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

    public abstract boolean isFlinged();

    public boolean isOverlapping(Renderable image) {
        Bitmap imBmp = image.getBitmap();

        if (imBmp == null || bitmap == null) return false;

        MathUtils.Rectangle imRect = new MathUtils.Rectangle(
                image.getX(),
                image.getY(),
                image.getX() + imBmp.getWidth(),
                image.getY() + imBmp.getHeight()
        );
        MathUtils.Rectangle selfRect = new MathUtils.Rectangle(
                getX(),
                getY(),
                getX() + bitmap.getWidth(),
                getY() + bitmap.getHeight()
        );

        int intArea = MathUtils.rectangleIntersectionArea(imRect, selfRect);
        Log.d(TAG, "isOverlapping: " + intArea);
        return intArea >= OVERLAP_THRESHOLD_LIMIT;
    }

    public abstract List<Card> handleDoubleTap(MotionEvent event);

    public Bitmap getBitmap() {
        return bitmap;
    }
}
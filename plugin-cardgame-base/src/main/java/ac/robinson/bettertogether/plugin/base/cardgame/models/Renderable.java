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

public abstract class Renderable implements Cloneable{

    private int x; // X cooridnate
    private int y; // Y coordinate
    private Bitmap openBitmap = null;
    private Bitmap hiddenBitmap = null;
    public boolean safeToDelete = false;

    protected String name;
    protected boolean canBeMadeVisible = true; // this could be managed in the Magic cards.

    protected boolean hidden;
    protected static final Paint GLOW_PAINT = new Paint();
    public static final Paint SELECTED_BUTTON_PAINT = new Paint();
    static {
        final int GLOW_RADIUS = 32;
        GLOW_PAINT.setColor(Color.rgb(255, 255, 255));
        GLOW_PAINT.setMaskFilter(new BlurMaskFilter(GLOW_RADIUS, BlurMaskFilter.Blur.OUTER));

        SELECTED_BUTTON_PAINT.setColor(Color.rgb(173, 216, 230));
    }
    public static Renderable selectedRenderableForContext = null;

    protected static final int FACADE_SCALED_WIDTH = 30;
    public static final int scaledWidth = 300;
    public static final int scaledHeight = 375;
    public static final int OVERLAP_THRESHOLD_LIMIT = ((scaledWidth*5)/6) * ((scaledHeight*2)/3);

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getName() {return name;};

    public void setName(String name) {this.name = name;};

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

    public void setAbsoluteX(int x) {
        this.x = x;
    }
    public void setAbsoluteY(int y) {
        this.y = y;
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
        Bitmap bitmap = getBitmap();

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
        return hidden ? hiddenBitmap: openBitmap;
    }

    public void setBitmap(Bitmap openBitmap, Bitmap hiddenBitmap) {
        this.openBitmap = Bitmap.createScaledBitmap(openBitmap, scaledWidth, scaledHeight, true);;
        this.hiddenBitmap = Bitmap.createScaledBitmap(hiddenBitmap, scaledWidth, scaledHeight, true);;
        Bitmap bitmap = getBitmap();
        setX(getX() + (bitmap.getWidth()/2)); // Don't remember why I had to do this.
        setY(getY() + (bitmap.getHeight()/2));
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public void clampInPlayground(int screenWidth, int screenHeight) {
        this.x = Math.max(-scaledWidth/2, this.x);
        this.x = Math.min(screenWidth+scaledWidth/2 - scaledWidth, this.x);

        this.y = Math.max(-scaledHeight/2, this.y);
        this.y = Math.min(screenHeight+scaledHeight/2 - scaledHeight, this.y);
    }

    public boolean isSafeToDelete() {
        return safeToDelete;
    }
}
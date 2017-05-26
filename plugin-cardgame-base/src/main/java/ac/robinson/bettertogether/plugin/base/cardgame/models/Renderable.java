package ac.robinson.bettertogether.plugin.base.cardgame.models;

import android.graphics.Canvas;
import android.view.MotionEvent;

/**
 * Created by t-apmehr on 4/5/2017.
 */

public abstract class Renderable {

    public int x; // X cooridnate
    public int y; // Y coordinate
    private boolean touched; // if it has been touched or picked up

    private String name;

    protected final int scaledWidth = 300;
    protected final int scaledHeight = 375;

    protected static final int MAX_DURATION = 200;

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

    public abstract boolean isOverlapping(Renderable image);

    public abstract Card handleDoubleTap(MotionEvent event);

}
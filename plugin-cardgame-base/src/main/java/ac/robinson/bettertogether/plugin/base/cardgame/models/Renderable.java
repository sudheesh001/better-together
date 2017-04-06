package ac.robinson.bettertogether.plugin.base.cardgame.models;

import android.graphics.Canvas;

/**
 * Created by t-apmehr on 4/5/2017.
 */

public abstract class Renderable {

    public int x; // X cooridnate
    public int y; // Y coordinate
    private boolean touched; // if it has been touched or picked up

    protected final int scaledWidth = 300;
    protected final int scaledHeight = 375;

    public abstract void handleActionDown(int eventX, int eventY);

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

    public boolean isTouched(){
        return this.touched;
    }

    public void setTouched(boolean touched){
        this.touched = touched;
    }
}
package ac.robinson.bettertogether.plugin.base.cardgame.models;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;

import java.util.Random;

/**
 * Created by t-apmehr on 4/2/2017.
 */

public class Card extends Renderable{

    private static final String TAG = Card.class.getSimpleName();

    private Integer cardId;

    private String name;
    private CardRank rank;
    private Suits suit;

    private Bitmap bitmap;
    private Bitmap openBitmap;
    private Bitmap hiddenBitmap;

    private boolean hidden;
    private boolean touched;
    protected long startTime;

// variable for moving the view

    public Integer getCardId() {
        return cardId;
    }

    public void setCardId(Integer cardId) {
        this.cardId = cardId;
    }
    public String getName() {
        return name;
    }

    public void randomizeScreenLocation(int x, int y){
        Random rand = new Random();
        setX(x + rand.nextInt(500) + (bitmap.getWidth()/ 2)); // TODO fix that it should not go out of screen
        setY(y);
    }

    public void setName(String name) {
        this.name = name;
    }

    public CardRank getRank() {
        return rank;
    }

    public void setRank(CardRank rank) {
        this.rank = rank;
    }

    public Suits getSuit() {
        return suit;
    }

    public void setSuit(Suits suit) {
        this.suit = suit;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap openBitmap, Bitmap hiddenBitmap) {
        this.openBitmap = Bitmap.createScaledBitmap(openBitmap, scaledWidth, scaledHeight, true);;
        this.hiddenBitmap = Bitmap.createScaledBitmap(hiddenBitmap, scaledWidth, scaledHeight, true);;
        this.bitmap = this.openBitmap;
        setX(x + (bitmap.getWidth()/2));
        setY(y + (bitmap.getHeight()/2));
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public boolean isTouched() {
        return this.touched;
    }

    @Override
    public void setTouched(boolean touched) {
        this.touched = touched;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, x, y , null);
    }

    @Override
    public boolean isOverlapping(Renderable image) {
        return false;
    }

    @Override
    public Card handleDoubleTap(MotionEvent event) {
        this.toggleHidden();
        return this;
    }


    /**
     * Handles the {@link MotionEvent.ACTION_DOWN} event. If the event happens on the
     * bitmap surface then the touched state is set to <code>true</code> otherwise to <code>false</code>
     * @param eventX - the event's X coordinate
     * @param eventY - the event's Y coordinate
     */
    @SuppressWarnings("JavadocReference")
    public Gesture handleActionDown(int eventX, int eventY) {
        if (eventX >= (getX() - bitmap.getWidth() ) && (eventX <= (getX() + bitmap.getWidth()))) {
            if (eventY >= (getY() - bitmap.getHeight() ) && (eventY <= (getY() + bitmap.getHeight() ))) {
                if ((System.currentTimeMillis() - startTime <= MAX_DURATION) && isTouched()) {
                    Log.d(TAG, this.getName() + " double touched " + isTouched() + " diff = " + (System.currentTimeMillis() - startTime)  );
                    return Gesture.DOUBLE_TAP;
                }
                setTouched(true);
                startTime = System.currentTimeMillis();
                return Gesture.TOUCHED;
            } else {
                setTouched(false);
            }
        } else {
            setTouched(false);
        }
        return Gesture.NONE;

    }

    public void toggleHidden() {
        this.hidden = !this.hidden;

        if( isHidden() ){
            this.bitmap = this.hiddenBitmap;
        }else {
            this.bitmap = this.openBitmap;
        }
    }
}

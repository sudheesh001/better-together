package ac.robinson.bettertogether.plugin.base.cardgame.models;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;

/**
 * Created by t-apmehr on 4/2/2017.
 */

public class Card extends Renderable{

    private Integer cardId;

    private String name;
    private CardRank rank;
    private Suits suit;
    private Bitmap bitmap;
    private boolean hidden;
    private boolean touched;

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

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
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
                // droid touched
                setTouched(true);
                return Gesture.SINGLE_TAP;
            } else {
                setTouched(false);
            }
        } else {
            setTouched(false);
        }
        return Gesture.NONE;

    }

}

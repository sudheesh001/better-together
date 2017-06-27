package ac.robinson.bettertogether.plugin.base.cardgame.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

import java.util.List;
import java.util.Random;

/**
 * Created by t-apmehr on 4/2/2017.
 */

public class Card extends Renderable{

    private static final String TAG = Card.class.getSimpleName();

    private Context mContext;
    private Integer cardId;

    private CardRank rank;
    private Suits suit;

    private boolean touched;

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

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
        setX(x + rand.nextInt(500) + (scaledWidth)); // TODO fix that it should not go out of screen
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

    public Bitmap getBitmap(boolean forceLoad) {
        if( getBitmap() == null && forceLoad) {
            setBitmap(
                    BitmapFactory.decodeResource(mContext.getResources(),
                            mContext.getResources().getIdentifier(this.getName(), "drawable", mContext.getPackageName())),
                    BitmapFactory.decodeResource(mContext.getResources(),
                            mContext.getResources().getIdentifier("card_back", "drawable", mContext.getPackageName()))
            );
        }
        return getBitmap();
    }

    public boolean isHidden() {
        return super.hidden;
    }

    public void setHidden(boolean hidden) {
        super.hidden = hidden;
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
        Bitmap bitmapToUse = getBitmap(true);

        if (this == Renderable.selectedRenderableForContext) {
            Bitmap alpha = bitmapToUse.extractAlpha();
            canvas.drawBitmap(alpha, getX(), getY(), GLOW_PAINT);
        }
        canvas.drawBitmap(bitmapToUse, getX(), getY() , null);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);
        canvas.drawPoint(getX(), getY(), paint);


        if (isHidden()) {
            paint.setColor(Color.RED);
        } else {
            paint.setColor(Color.GREEN);
        }
        canvas.drawPoint(getX()+100, getY(), paint);
    }

    @Override
    public List<Card> handleDoubleTap(MotionEvent event) {
        this.toggleHidden();
        return null;
    }


    /**
     * Handles the {@link MotionEvent.ACTION_DOWN} event. If the event happens on the
     * bitmap surface then the touched state is set to <code>true</code> otherwise to <code>false</code>
     * @param eventX - the event's X coordinate
     * @param eventY - the event's Y coordinate
     */
    @SuppressWarnings("JavadocReference")
    public Gesture handleActionDown(int eventX, int eventY) {
        if (eventX >= (getX()) && (eventX <= (getX() + getBitmap(true).getWidth()))) {
            if (eventY >= (getY()) && (eventY <= (getY() + getBitmap(true).getHeight() ))) {
                setTouched(true);
                return Gesture.TOUCHED;
            } else {
                setTouched(false);
            }
        } else {
            setTouched(false);
        }
        return Gesture.NONE;

    }

    private void toggleHidden() {
        this.hidden = !this.hidden;
    }
}

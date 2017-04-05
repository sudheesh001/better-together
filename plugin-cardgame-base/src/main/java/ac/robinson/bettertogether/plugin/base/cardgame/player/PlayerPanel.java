package ac.robinson.bettertogether.plugin.base.cardgame.player;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

import ac.robinson.bettertogether.plugin.base.cardgame.R;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Suits;

/**
 * Created by t-sus on 4/5/2017.
 */

public class PlayerPanel extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = PlayerPanel.class.getSimpleName();

    private PlayerThread thread;
    private List<Card> mCards;
    private Context mContext;

    public PlayerPanel(Context context, List<Card> cards) {
        super(context);
        this.mContext = context;
        getHolder().addCallback(this);

        this.mCards = cards;

        thread = new PlayerThread(getHolder(), this);

        setFocusable(true);
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // at this point the surface is created and
        // we can safely start the game loop
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Surface is being destroyed");
        // tell the thread to shut down and wait for it to finish
        // this is a clean shutdown
        boolean retry = true;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // try again shutting down the thread
            }
        }
        Log.d(TAG, "Thread was shut down cleanly");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // delegating event handling to the droid
            // Hardcoding all to 0
            mCards.get(0).handleActionDown((int)event.getX(), (int)event.getY());

            // check if in the lower part of the screen we exit
            if (event.getY() > getHeight() - 50) {
                thread.setRunning(false);
                ((Activity)getContext()).finish();
            } else {
                Log.d(TAG, "Coords: x=" + event.getX() + ",y=" + event.getY());
            }
        } if (event.getAction() == MotionEvent.ACTION_MOVE) {
            // the gestures
            if (mCards.get(0).isTouched()) {
                // the droid was picked up and is being dragged
                mCards.get(0).setX((int)event.getX());
                mCards.get(0).setY((int)event.getY());
            }
        } if (event.getAction() == MotionEvent.ACTION_UP) {
            // touch was released
            if (mCards.get(0).isTouched()) {
                mCards.get(0).setTouched(false);
            }
        }
        return true;
    }

    public void render(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        if (mCards.size() == 0) {
            Card noCard = new Card();
            noCard.setBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.black_joker));
            noCard.setCardId(-1);
            noCard.setHidden(false);
            noCard.setName("Joker");
            noCard.setSuit(Suits.clubs);
            noCard.setY(2*canvas.getHeight()/3);
            noCard.draw(canvas);
        }
        else {
            mCards.get(0).draw(canvas);
        }
    }

}

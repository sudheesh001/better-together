package ac.robinson.bettertogether.plugin.base.cardgame.dealer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.GestureDetectorCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Gesture;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Renderable;

/**
 * Created by t-apmehr, t-sus on 4/5/2017.
 */

public class DealerPanel extends SurfaceView implements SurfaceHolder.Callback{

    private static final String TAG = DealerPanel.class.getSimpleName();

    private DealerThread thread;
    private List<Renderable> mCards = new ArrayList<>();

    private SurfaceView surfaceView;
    private GestureDetectorCompat mDetector;
    private Context mContext;


    public DealerPanel(Context context, List<Renderable> cards, List<CardDeck> cardDecks) {
        super(context);
        // adding the callback (this) to the surface holder to intercept events
        getHolder().addCallback(this);
        surfaceView = this;
        mContext = context;

        if(cards !=  null) {
            this.mCards.addAll(cards);
        }
        if(cardDecks != null) {
            this.mCards.addAll(cardDecks);
        }

        // create the game loop thread
        thread = new DealerThread(getHolder(), this);

        // make the Panel focusable so it can handle events
        setFocusable(true);

    }

    private void setupPanel(Canvas canvas) {

        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        int screenWidth = (metrics.widthPixels);
        int screenHeight = ((int) (metrics.heightPixels*0.9))+80;
        //  Set paint options
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);

        canvas.drawLine(0,(screenHeight/3)*2,screenWidth,(screenHeight/3)*2,paint);
        canvas.drawLine(0,(screenHeight/3),screenWidth,(screenHeight/3),paint);


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
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
            for (int i = 0; i < mCards.size(); i++) {
                Renderable r = mCards.get(i);
                if (r.handleActionDown((int) event.getX(), (int) event.getY()).equals(Gesture.SINGLE_TAP)) {
                    Log.d(TAG, r.toString() + " Single Tap " + r.getX() + "," + r.getY());
                    Collections.swap(mCards, i, mCards.size() - 1);
                }


                // check if in the lower part of the screen we exit
                if (event.getY() > getHeight() - 50) {
                    thread.setRunning(false);
                    ((Activity) getContext()).finish();
                } else {
//                Log.d(TAG, "Coords: x=" + event.getX() + ",y=" + event.getY());
                }
            }
        }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
//            Log.d(TAG, "Move: x=" + event.getX() + ",y=" + event.getY());
                // the gestures
                for (Renderable r : mCards) {
                    if (r.isTouched()) {
                        // the droid was picked up and is being dragged
                        r.setX((int) event.getX());
                        r.setY((int) event.getY());
//                    Log.d(TAG, "Moving:"+r.toString()+" x=" + event.getX() + ",y=" + event.getY());
                    }
                }
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // touch was released
//            Log.d(TAG, "Act Up Coords: x=" + event.getX() + ",y=" + event.getY());
                for (Renderable r : mCards) {
                    if (r.isTouched()) {
                        r.setTouched(false);
                        Log.d(TAG, "Setting to False " + r.toString() + "Coords: x=" + event.getX() + ",y=" + event.getY() + " " + r.isTouched());
                        for (Renderable r2 : mCards
                                ) {
                            if (r2.equals(r)) {
                                continue;
                            }
                            r2.isOverlapping(r);
                        }
                    }
                }
            }
            return true;
        }

    public void render(Canvas canvas) {

        canvas.drawColor(Color.BLACK);
        setupPanel(canvas);

        for (Renderable r: mCards) {
            r.draw(canvas);
        }

    }

}

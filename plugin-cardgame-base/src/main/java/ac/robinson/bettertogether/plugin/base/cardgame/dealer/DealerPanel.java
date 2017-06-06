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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeckStatus;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Gesture;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Renderable;

/**
 * Created by t-apmehr, t-sus on 4/5/2017.
 */

public class DealerPanel extends SurfaceView implements SurfaceHolder.Callback, GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private static final String TAG = DealerPanel.class.getSimpleName();

    private DealerThread thread;
    private List<Renderable> mCards = Collections.synchronizedList(new ArrayList<Renderable>());

    private SurfaceView surfaceView;
    private GestureDetectorCompat mDetector;
    private Context mContext;


    public DealerPanel(Context context, List<Renderable> cards, List<CardDeck> cardDecks) {
        super(context);
        // adding the callback (this) to the surface holder to intercept events
        getHolder().addCallback(this);
        surfaceView = this;
        mContext = context;

        if (cards != null) {
            this.mCards.addAll(cards);
        }
        if (cardDecks != null) {
            this.mCards.addAll(cardDecks);
        }

        // create the game loop thread
        thread = new DealerThread(getHolder(), this);

        // make the Panel focusable so it can handle events
        setFocusable(true);

        mDetector = new GestureDetectorCompat(mContext, this);

        surfaceView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // touch was released
//                    Log.d(TAG, "Act Up Coords: x=" + event.getX() + ",y=" + event.getY());

                        for (int i = 0; i<mCards.size();i++) {
                            Renderable r = mCards.get(i);
                            if (r.isTouched()) {
                                r.setTouched(false);
//                            Log.d(TAG, r.getName() + " Setting to False " + r.getName() + "Coords: x=" + event.getX() + ",y=" + event.getY() + " " + r.isTouched());
                                for (int j = 0; j< mCards.size();j++) {
                                    Renderable r2 = mCards.get(j);
                                    if (r2.equals(r)) {
                                        continue;
                                    }
                                    if (r2.isOverlapping(r)) {
                                        mergeRenderables(r, r2);
                                    }
                                }
                            }

                        }

                }

                mDetector.onTouchEvent(event);
                return true;
            }


        });
        // Set the gesture detector as the double tap
        // listener.
        mDetector.setOnDoubleTapListener(this);


    }

    private void removeCardFromList(Renderable card) {

            mCards.remove(card);

    }

    private void mergeRenderables(Renderable r1, Renderable r2) {
        if (r1.isHidden() == r2.isHidden()) {
            Card c1, c2;
            CardDeck cd1, cd2;
            c1 = r1 instanceof Card ? (Card) r1 : null;
            c2 = r2 instanceof Card ? (Card) r2 : null;
            cd1 = r1 instanceof CardDeck ? (CardDeck) r1 : null;
            cd2 = r2 instanceof CardDeck ? (CardDeck) r2 : null;

            CardDeck finalDeck;
            if (cd1 == null && cd2 == null) {
                finalDeck = new CardDeck(mContext, (r1.isHidden() ? CardDeckStatus.CLOSED : CardDeckStatus.OPEN), false);
                    mCards.add(finalDeck);
            } else {
                finalDeck = cd1 == null ? cd2: cd1;
            }

            if (c1 != null) {
                finalDeck.addCardToDeck(c1);
                removeCardFromList(c1);
            }
            if (c2 != null) {
                finalDeck.addCardToDeck(c2);
                removeCardFromList(c2);
            }
            if (cd2 != null && finalDeck != cd2) {
                for (Renderable card : cd2.getmCards()) {
                    finalDeck.addCardToDeck((Card) card);
                }
                removeCardFromList(cd2);
            }
            if (cd1 != null && finalDeck != cd1) {
                for (Renderable card : cd1.getmCards()) {
                    finalDeck.addCardToDeck((Card) card);
                }
                removeCardFromList(cd1);
            }
        }
    }

    private void setupPanel(Canvas canvas) {

        // right now just providing assisting lines on the screen to demarcate regions.

        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        int screenWidth = (metrics.widthPixels);
        int screenHeight = ((int) (metrics.heightPixels * 0.9)) + 80;
        //  Set paint options
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);

        canvas.drawLine(0, (screenHeight / 3) * 2, screenWidth, (screenHeight / 3) * 2, paint);
        canvas.drawLine(0, (screenHeight / 3), screenWidth, (screenHeight / 3), paint);


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

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            // delegating event handling to the droid
//            for (int i = 0; i < mCards.size(); i++) {
//                Renderable r = mCards.get(i);
//                if (r.handleActionDown((int) event.getX(), (int) event.getY()).equals(Gesture.TOUCHED)) {
//                    Log.d(TAG, r.getName() + " Single Tap " + r.getX() + "," + r.getY());
//                    Collections.swap(mCards, i, mCards.size() - 1);
//                }
//
//
//                // check if in the lower part of the screen we exit
//                if (event.getY() > getHeight() - 50) {
//                    thread.setRunning(false);
//                    ((Activity) getContext()).finish();
//                } else {
////                Log.d(TAG, "Coords: x=" + event.getX() + ",y=" + event.getY());
//                }
//            }
//        }
//            if (event.getAction() == MotionEvent.ACTION_MOVE) {
////            Log.d(TAG, "Move: x=" + event.getX() + ",y=" + event.getY());
//                // the gestures
//                for (Renderable r : mCards) {
//                    if (r.isTouched()) {
//                        // the image was picked up and is being dragged
//                        Log.d(TAG, "Moving " + r.getName() + " to Coords: x=" + event.getX() + ",y=" + event.getY());
//                        r.setX((int) event.getX());
//                        r.setY((int) event.getY());
////                    Log.d(TAG, "Moving:"+r.toString()+" x=" + event.getX() + ",y=" + event.getY());
//                    }
//                }
//            }
//            if (event.getAction() == MotionEvent.ACTION_UP) {
//                // touch was released
//            Log.d(TAG, "Act Up Coords: x=" + event.getX() + ",y=" + event.getY());
//                for (Renderable r : mCards) {
//                    if (r.isTouched()) {
//                        r.setTouched(false);
//                        Log.d(TAG, r.getName()+ " Setting to False " + r.getName() + "Coords: x=" + event.getX() + ",y=" + event.getY() + " " + r.isTouched());
//                        for (Renderable r2 : mCards
//                                ) {
//                            if (r2.equals(r)) {
//                                continue;
//                            }
//                            r2.isOverlapping(r);
//                        }
//                    }
//                }
//            }
//            return true;
//            this.mDetector.onTouchEvent(event);
//            // Be sure to call the superclass implementation
//            return super.onTouchEvent(event);
//
//        }


    public void render(Canvas canvas) {

        canvas.drawColor(Color.BLACK);
        setupPanel(canvas);

//        for (Renderable r: mCards) {
//            r.draw(canvas);
//        }

        for (int i = 0; i < mCards.size(); i++) {
            mCards.get(i).draw(canvas);
        }

    }

    @Override
    public boolean onDown(MotionEvent event) {
        Log.d(TAG, "onDown: " + event.toString());
        for (int i = 0; i < mCards.size(); i++) {
            Renderable r = mCards.get(i);
            if (r.handleActionDown((int) event.getX(), (int) event.getY()).equals(Gesture.TOUCHED)) {
                Log.d(TAG, r.getName() + " Single Tap " + r.getX() + "," + r.getY());
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
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        Log.d(TAG, "onFling: " + event1.toString() + event2.toString());
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        Log.d(TAG, "onLongPress: " + event.toString());
    }


    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d(TAG, "onScroll: " + e1.toString() + e2.toString());

        Log.d(TAG, "Move: x=" + e2.getX() + ",y=" + e2.getY());
//                // the gestures
        for (Renderable r : mCards) {
            if (r.isTouched()) {
                // the image was picked up and is being dragged
                Log.d(TAG, "Moving " + r.getName() + " to Coords: x=" + e2.getX() + ",y=" + e2.getY());
                r.setX((int) e2.getX());
                r.setY((int) e2.getY());

                break; // only moce the top card
            }
        }

        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        Log.d(TAG, "onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        Log.d(TAG, "onSingleTapUp: " + event.toString());
//        Log.d(TAG, "Act Up Coords: x=" + event.getX() + ",y=" + event.getY());
//        for (Renderable r : mCards) {
//            if (r.isTouched()) {
//
//                r.setTouched(false);
////                        Log.d(TAG, r.getName()+ " Setting to False " + r.getName() + "Coords: x=" + event.getX() + ",y=" + event.getY() + " " + r.isTouched());
//            }
//        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        Log.d(TAG, "onDoubleTap: " + event.toString());

        Card card = null;
        for (int i = 0; i < mCards.size(); i++) {
            Renderable r = mCards.get(i);
            if (r.handleActionDown((int) event.getX(), (int) event.getY()).equals(Gesture.TOUCHED)) {
                Collections.swap(mCards, i, mCards.size() - 1);
                // TODO toggle if its a card and open the top card if it's a deck
                card = r.handleDoubleTap(event);
                Log.d(TAG, " Double tap on card " + r.getName());
                break;
            }

        }

        if (card != null)
            mCards.add(card);

        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
//        Log.d(TAG, "onDoubleTapEvent: " + event.toString());
        return true;
    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        Log.d(TAG, "onSingleTapConfirmed: " + event.toString());

        // TODO it's N2 .. change it find the nearest cards because we know all centre and their heights and widths so can bring it down to N maybe lower
//        for (Renderable r : mCards) {
//            for (Renderable r2 : mCards) {
//                if (r2.equals(r)) {
//                    continue;
//                }
//                r2.isOverlapping(r);
//                // TODO merge the two decks is they are of similar type
//            }
//        }

        return true;
    }
}

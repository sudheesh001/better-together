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
import android.widget.Toast;

import java.util.List;

import ac.robinson.bettertogether.plugin.base.cardgame.models.Renderable;

/**
 * Created by t-apmehr, t-sus on 4/5/2017.
 */

public class DealerPanel extends SurfaceView implements SurfaceHolder.Callback{

    private static final String TAG = DealerPanel.class.getSimpleName();

    private DealerThread thread;
    private List<? extends Renderable> mCards;

    private SurfaceView surfaceView;
    private GestureDetectorCompat mDetector;
    private Context mContext;
    private long startTime;

    private static final int MAX_DURATION = 200;

    public DealerPanel(Context context, List<? extends Renderable> cards) {
        super(context);
        // adding the callback (this) to the surface holder to intercept events
        getHolder().addCallback(this);
        surfaceView = this;
        mContext = context;
//        mDetector = new GestureDetectorCompat(mContext, this);
//
//        mDetector.setIsLongpressEnabled(true);
//
//        mDetector.setOnDoubleTapListener(this);
//
//        surfaceView.setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                mDetector.onTouchEvent(event);
//                return true;
//            }
//        });

        this.mCards = cards;

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

            for( Renderable r : mCards) {
                r.handleActionDown((int) event.getX(), (int) event.getY());
            }

            // delegating event handling to the droid
            if (System.currentTimeMillis() - startTime <= MAX_DURATION) {
                Toast.makeText(getContext(), "Double Tapped Found. Checking.", Toast.LENGTH_SHORT).show();
                for( Renderable r : mCards) {
                    r.handleActionDown((int) event.getX(), (int) event.getY());
                }
            }

            // check if in the lower part of the screen we exit
            if (event.getY() > getHeight() - 50) {
                thread.setRunning(false);
                ((Activity)getContext()).finish();
            } else {
//                Log.d(TAG, "Coords: x=" + event.getX() + ",y=" + event.getY());
            }
        } if (event.getAction() == MotionEvent.ACTION_MOVE) {
//            Log.d(TAG, "Move: x=" + event.getX() + ",y=" + event.getY());
            // the gestures
            for( Renderable r : mCards) {
                if (r.isTouched()) {
                    // the droid was picked up and is being dragged
                    r.setX((int) event.getX());
                    r.setY((int) event.getY());
//                    Log.d(TAG, "Moving:"+r.toString()+" x=" + event.getX() + ",y=" + event.getY());
                }
            }
        } if (event.getAction() == MotionEvent.ACTION_UP) {
            // touch was released
            Log.d(TAG, "Act Up Coords: x=" + event.getX() + ",y=" + event.getY());
            startTime = System.currentTimeMillis();
            for( Renderable r : mCards) {
                if (r.isTouched()) {
                    r.setTouched(false);
//                    Log.d(TAG, "Setting to False "+ r.toString()+"Coords: x=" + event.getX() + ",y=" + event.getY());
                    for (Renderable r2: mCards) {
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
package ac.robinson.bettertogether.plugin.base.cardgame.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;

import java.util.List;

import ac.robinson.bettertogether.plugin.base.cardgame.R;

import static android.content.ContentValues.TAG;

/**
 * Created by darkryder on 18/6/17.
 */

public class CardContextActionPanel extends Renderable {

    String actionName;

    public static final int SHOW_SHUFFLE =      0x1;
    public static final int SHOW_TRANSFER =     0x10;
    public static final int SHOW_DISTRIBUTE =   0x100;
    public static final int SHOW_REVERSE =      0x1000;
    private static final int[] FLAGS = {SHOW_SHUFFLE, SHOW_TRANSFER, SHOW_DISTRIBUTE, SHOW_REVERSE};

    private Context mContext;
    private Renderable forRenderable;
    private int showFlags = SHOW_SHUFFLE | SHOW_TRANSFER | SHOW_DISTRIBUTE | SHOW_REVERSE;

    private static Bitmap bitmapShuffle;
    private static Bitmap bitmapTransfer;
    private static Bitmap bitmapDistrubute;
    private static Bitmap bitmapReverse;

    private static final int CONTEXT_ACTION_DIMEN = 64;
    private static final int MARGIN = 16;

    private static CardContextActionPanel instance;
    public static CardContextActionPanel getInstance(Context context) {
        if (instance == null) {
            instance = new CardContextActionPanel(context);
        }
        return instance;
    }

    private CardContextActionPanel(Context context) {
        this.mContext = context;
        if (bitmapShuffle == null) {
            bitmapShuffle = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.shuffle);
            bitmapShuffle = Bitmap.createScaledBitmap(bitmapShuffle, CONTEXT_ACTION_DIMEN, CONTEXT_ACTION_DIMEN, false);
        }
        if (bitmapTransfer == null) {
            bitmapTransfer = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.transfer);
            bitmapTransfer = Bitmap.createScaledBitmap(bitmapTransfer, CONTEXT_ACTION_DIMEN, CONTEXT_ACTION_DIMEN, false);
        }
        if (bitmapDistrubute == null) {
            bitmapDistrubute = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.distribute);
            bitmapDistrubute = Bitmap.createScaledBitmap(bitmapDistrubute, CONTEXT_ACTION_DIMEN, CONTEXT_ACTION_DIMEN, false);
        }
        if (bitmapReverse == null) {
            bitmapReverse = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.reverse);
            bitmapReverse = Bitmap.createScaledBitmap(bitmapReverse, CONTEXT_ACTION_DIMEN, CONTEXT_ACTION_DIMEN, false);
        }
    }

    @Override
    public String getName() {
        return actionName;
    }

    @Override
    public void setName(String name) {
        actionName = name;
    }

    @Override
    public Gesture handleActionDown(int eventX, int eventY) {
        if (hidden) return Gesture.NONE;

        int panelsShown = 0;
        for(int i = 0; i < FLAGS.length; i++) {
            if ((FLAGS[i] & showFlags) != 0) panelsShown++;
        }
        int panelWidth = (MARGIN+CONTEXT_ACTION_DIMEN)*panelsShown;

        if (eventX >= x && eventX <= (x+panelWidth)) {
            if (eventY >= y && eventY <= y + MARGIN + CONTEXT_ACTION_DIMEN) {

                int selectedActionIdx = (eventX-x)/(MARGIN+CONTEXT_ACTION_DIMEN);
                selectedActionIdx = Math.max(0, selectedActionIdx);
                selectedActionIdx = Math.min(FLAGS.length-1, selectedActionIdx);

                for(int i = 0, idx = 0; i < FLAGS.length; i++) {
                    if ((FLAGS[i] & showFlags) != 0) { // flag was selected.
                         if (idx == selectedActionIdx) {
                             switch (FLAGS[i]) {
                                 case SHOW_SHUFFLE:
                                     handleShuffle(eventX, eventY); break;
                                 case SHOW_TRANSFER:
                                     handleTransfer(eventX, eventY); break;
                                 case SHOW_DISTRIBUTE:
                                     handleDistribute(eventX, eventY); break;
                                 case SHOW_REVERSE:
                                     handleReverse(eventX, eventY); break;
                             }
                             return Gesture.HANDLED;
                         }
                        idx++;
                    }
                }

            }
        }
        return Gesture.NONE;
    }

    @Override
    public void draw(Canvas canvas) {
        if (hidden) return;
        if (Renderable.selectedRenderableForContext == null) {
            hidden = true;
            return;
        }

        int shownActions = 0;
        if ((showFlags & SHOW_SHUFFLE) != 0) {
            canvas.drawBitmap(bitmapShuffle, x + shownActions*(CONTEXT_ACTION_DIMEN+MARGIN), y, null);
            shownActions += 1;
        }
        if ((showFlags & SHOW_TRANSFER) != 0) {
            canvas.drawBitmap(bitmapTransfer, x + shownActions*(CONTEXT_ACTION_DIMEN+MARGIN), y, null);
            shownActions += 1;
        }
        if ((showFlags & SHOW_DISTRIBUTE) != 0) {
            canvas.drawBitmap(bitmapDistrubute, x + shownActions*(CONTEXT_ACTION_DIMEN+MARGIN), y, null);
            shownActions += 1;
        }
        if ((showFlags & SHOW_REVERSE) != 0) {
            canvas.drawBitmap(bitmapReverse, x + shownActions*(CONTEXT_ACTION_DIMEN+MARGIN), y, null);
        }
    }

    public void show(Renderable renderable, int showFlags) {
        forRenderable = renderable;
        hidden = false;
        x = renderable.getX();
        y = renderable.getY() - MARGIN - CONTEXT_ACTION_DIMEN;
        this.showFlags = showFlags;
    }

    @Override
    public boolean isTouched() {
        return false;
    }

    @Override
    public void setTouched(boolean touched) {

    }

    @Override
    public boolean isFlinged() {
        return false;
    }

    @Override
    public List<Card> handleDoubleTap(MotionEvent event) {
        return null;
    }

    private void handleShuffle(int x, int y) {
        Log.d(TAG, "handleShuffle: ");
    }
    
    private void handleTransfer(int x, int y) {
        Log.d(TAG, "handleTransfer: ");
    }
    
    private void handleDistribute(int x, int y) {
        Log.d(TAG, "handleDistribute: ");
    }
    
    private void handleReverse(int x, int y) {
        Log.d(TAG, "handleReverse: ");
    }
}

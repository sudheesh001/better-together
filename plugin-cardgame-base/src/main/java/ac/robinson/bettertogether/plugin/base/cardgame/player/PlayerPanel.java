package ac.robinson.bettertogether.plugin.base.cardgame.player;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ac.robinson.bettertogether.plugin.base.cardgame.BaseCardGameActivity;
import ac.robinson.bettertogether.plugin.base.cardgame.common.Action;
import ac.robinson.bettertogether.plugin.base.cardgame.common.BroadcastCardMessage;
import ac.robinson.bettertogether.plugin.base.cardgame.common.MessageHelper;
import ac.robinson.bettertogether.plugin.base.cardgame.common.MessageType;
import ac.robinson.bettertogether.plugin.base.cardgame.dealer.DealerPanel;
import ac.robinson.bettertogether.plugin.base.cardgame.dealer.DealerThread;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardContextActionPanel;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Gesture;
import ac.robinson.bettertogether.plugin.base.cardgame.models.MagicCard;
import ac.robinson.bettertogether.plugin.base.cardgame.models.MarketplaceItem;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Renderable;
import ac.robinson.bettertogether.plugin.base.cardgame.utils.APIClient;

/**
 * Created by t-sus on 4/5/2017.
 */

public class PlayerPanel extends SurfaceView implements SurfaceHolder.Callback, GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    // Constants
    private static final int FLING_CARD_OUTSIDE_VELOCITY_THRESHOLD = 3500;
    private static final int FLING_CARD_DISTANCE_FROM_EDGE_THRESHOLD = (Renderable.scaledHeight/2); //  mid point
    private static final String TAG = PlayerPanel.class.getSimpleName();

    protected static final int PULL_CARD_BUTTON_MARGIN = 30;
    protected static final int PULL_CARD_BUTTON_RADIUS = 60;
    public static final Paint TEXT_PAINT;
    public static final Paint DEBUG_TEXT_PAINT;
    private static final int DEBUG_TEXT_PAINT_SIZE = 24;
    static {
        TEXT_PAINT = new Paint();
        TEXT_PAINT.setColor(Color.WHITE);
        TEXT_PAINT.setTextSize(52f);
        TEXT_PAINT.setAntiAlias(true);
        TEXT_PAINT.setFakeBoldText(true);
        TEXT_PAINT.setShadowLayer(12f, 0, 0, Color.BLACK);
        TEXT_PAINT.setStyle(Paint.Style.FILL);
        TEXT_PAINT.setTextAlign(Paint.Align.LEFT);

        DEBUG_TEXT_PAINT = new Paint(TEXT_PAINT);
        DEBUG_TEXT_PAINT.setTextSize(DEBUG_TEXT_PAINT_SIZE);
    }

    private PlayerThread playerThread;
    protected HashMap<String, Card> mAllCardsRes= new HashMap<>();  // Map of card_name -> Card.
    protected List<Renderable> mRenderablesInPlay = new ArrayList<>();  // List of renderables currently on screen.
    protected Renderable mLastCardTouched = null; // for detecting the card being flung.

    protected SurfaceView surfaceView;
    protected GestureDetectorCompat mDetector;
    protected Context mContext;
    protected MessageHelper mMessageHelper;

    protected final int SCREEN_WIDTH;
    protected final int SCREEN_HEIGHT;

//    private CardPanelCallback cardPanelCallback;

    public void init() {
        playerThread = new PlayerThread(getHolder(), this);  // create the game loop thread
    }

    public void setCurrentlyPlayingCardDeck(MarketplaceItem item, boolean addToPanel, boolean addToPanelAsHidden) {
        String backgroundCardUrl = APIClient.getBaseURL().concat(item.getBackground_card());
        CardDeck cardDeck = new CardDeck(mContext, addToPanelAsHidden);

        for(String cardName: item.getCards()) {
            MagicCard card = new MagicCard();
            card.setmContext(mContext);
            card.setName(cardName);
            card.setHidden(addToPanelAsHidden);
            card.setFrontBitmapUrl(APIClient.getBaseURL().concat(cardName));
            card.setBackBitmapUrl(backgroundCardUrl);

            card.addMagicAttribute(new MagicCard.MagicAttributes(MagicCard.MAGIC_TYPE.TTL, 20, 20, null));
            card.addMagicAttribute(new MagicCard.MagicAttributes(MagicCard.MAGIC_TYPE.ACTIVATE, 5, 15, null));

            mAllCardsRes.put(card.getName(), card);

            if (addToPanel) {
                cardDeck.addCardToDeck(card);
            }
        }

        if (addToPanel) {
            mRenderablesInPlay.add(cardDeck);
        }
    }

    public PlayerPanel(Context context) {
        super(context);
        getHolder().addCallback(this);  // adding the callback (this) to the surface holder to intercept events
        surfaceView = this;
        mContext = context;
        mMessageHelper = MessageHelper.getInstance(mContext);

        setFocusable(true);  // make the Panel focusable so it can handle events
        mDetector = new GestureDetectorCompat(mContext, this);
        surfaceView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // touch was released

                    for (int i = 0; i< mRenderablesInPlay.size(); i++) {
                        Renderable r = mRenderablesInPlay.get(i);
                        if (r.isTouched()) {
                            r.setTouched(false);
                            mLastCardTouched = r;
                            for (int j = 0; j< mRenderablesInPlay.size(); j++) {
                                Renderable r2 = mRenderablesInPlay.get(j);
                                if (r2.equals(r)) {
                                    continue;
                                }
                                if (r2.isOverlapping(r)) {
                                    mergeRenderables(r, r2);
                                    break;
                                }
                            }
                            break;
                        }
                    }

                    if (BasePlayerActivity.requestingCardActively || BasePlayerActivity.isRequestCardHolder) {
                        BasePlayerActivity.requestingCardActively = false;
                        BasePlayerActivity.isRequestCardHolder = false;
                        ((BasePlayerActivity) mContext).sendRequestDrawCardMessage(MessageType.REQUEST_DRAW_CARD_WITHDRAW);
                    }
                }
                mDetector.onTouchEvent(event);
                return true;
            }
        });

        // Set the gesture detector as the double tap
        // listener.
        mDetector.setOnDoubleTapListener(this);

        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        SCREEN_WIDTH = metrics.widthPixels;
        SCREEN_HEIGHT = metrics.heightPixels;
    }

    protected void removeCardFromList(Renderable card) {
        mRenderablesInPlay.remove(card);
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
                finalDeck = new CardDeck(mContext, r1.isHidden());
                mRenderablesInPlay.add(finalDeck);
            } else {
                finalDeck = cd1 == null ? cd2: cd1;
            }

            if (c1 != null) {
                finalDeck.addCardToDeck(c1, 0);
                removeCardFromList(c1);
            }
            if (c2 != null) {
                finalDeck.addCardToDeck(c2);
                removeCardFromList(c2);
            }
            if (cd2 != null && finalDeck != cd2) {
                for (Renderable card : cd2.getmCards()) {
                    card.setHidden(r1.isHidden());
                    finalDeck.addCardToDeck((Card) card);
                }
                removeCardFromList(cd2);
            }
            if (cd1 != null && finalDeck != cd1) {
                for (Renderable card : cd1.getmCards()) {
                    card.setHidden(r1.isHidden());
                    finalDeck.addCardToDeck((Card) card);
                }
                removeCardFromList(cd1);
            }

            finalDeck.setAbsoluteX(r2.getX());
            finalDeck.setAbsoluteY(r2.getY());
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

        Map<String, MessageHelper.PlayerType> connectionMap = mMessageHelper.getConnectionMap();
        if (connectionMap != null && !connectionMap.isEmpty()) {
            List<String> players = new ArrayList<>(connectionMap.keySet());
            int numPlayers = players.size();
            for(int i = 0; i < numPlayers; i++) {
                String playerName = players.get(i);
                if (playerName == null) {
                    playerName = "NuLL--Null";
                }
                if (connectionMap.containsKey(playerName) && connectionMap.get(playerName).equals(MessageHelper.PlayerType.DEALER)) {
                    DEBUG_TEXT_PAINT.setColor(Color.RED);
                }
                canvas.drawText(playerName, PULL_CARD_BUTTON_MARGIN, screenHeight - (numPlayers-i)*(PULL_CARD_BUTTON_MARGIN + DEBUG_TEXT_PAINT_SIZE), DEBUG_TEXT_PAINT);
                DEBUG_TEXT_PAINT.setColor(Color.WHITE);
            }
        }

        if (mMessageHelper.getmUser() != null && !mMessageHelper.getmUser().isEmpty()) {
            canvas.drawText(mMessageHelper.getmUser(), screenWidth - 240, screenHeight - PULL_CARD_BUTTON_MARGIN - DEBUG_TEXT_PAINT_SIZE, DEBUG_TEXT_PAINT);
            if (connectionMap.containsKey(mMessageHelper.getmUser())) {
                String type = connectionMap.get(mMessageHelper.getmUser()).name();
                canvas.drawText(type, screenWidth - 240, screenHeight - 2*(PULL_CARD_BUTTON_MARGIN - DEBUG_TEXT_PAINT_SIZE), DEBUG_TEXT_PAINT);
            }
        }

        printFPS(canvas, screenWidth);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // at this point the surface is created and
        // we can safely start the game loop
        if (playerThread != null) {
            playerThread.setRunning(true);
            playerThread.start();
        }
    }

    protected void tryClosingThread(Thread thread) {
        boolean retry = true;
        if (thread instanceof PlayerThread) {
            ((PlayerThread) thread).setRunning(false);
        } else {
            ((DealerThread) thread).setRunning(false);
        }
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // try again shutting down the thread
            }
        }
        Log.d(TAG, "Thread " + thread + " was shut down cleanly");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Surface is being destroyed");
        // tell the thread to shut down and wait for it to finish
        // this is a clean shutdown
        tryClosingThread(playerThread);
    }


    // for printing fps
    private long fpsMLastTime = 0;
    private int fps = 0, ifps = 0;
    private void printFPS(Canvas canvas, int screenWidth) {
        canvas.drawText("FPS: " + fps + " X 2", screenWidth - 160, PULL_CARD_BUTTON_MARGIN, DEBUG_TEXT_PAINT);
        long now = System.currentTimeMillis();
        ifps++;
        if(now > (fpsMLastTime + 500)) {
            fpsMLastTime = now;
            fps = ifps;
            ifps = 0;
        }
    }


    private final Paint CLOCK_PAINT = new Paint();
    private long baseTime = 0;
    boolean usePlayerThreadForTime = false;
    private boolean clockDrawInit = false;
    protected void drawClock(Canvas canvas) {
        if (!clockDrawInit) {
            Typeface clockFontface = Typeface.create(
                    Typeface.createFromAsset(mContext.getAssets(), "digital-7.ttf"),
                    Typeface.BOLD);
            CLOCK_PAINT.setTypeface(clockFontface);
            CLOCK_PAINT.setColor(Color.RED);
            CLOCK_PAINT.setTextSize(64f);

            if (Thread.currentThread() instanceof PlayerThread) {
                baseTime = PlayerThread.CURRENT_TIME;
                usePlayerThreadForTime = true;
            } else if (Thread.currentThread() instanceof DealerThread) {
                baseTime = DealerThread.CURRENT_TIME;
                usePlayerThreadForTime = false;
            } else {
                Log.e("WTF", "draw: which thread is this??  " + Thread.currentThread().getName());
            }
            clockDrawInit = true;
        }

        long currentTimeStep = usePlayerThreadForTime ? PlayerThread.CURRENT_TIME : DealerThread.CURRENT_TIME;
        currentTimeStep -= baseTime;

        String minutes = Long.toString(currentTimeStep/60);
        String seconds = Long.toString(currentTimeStep % 60);
        if (minutes.length() == 1) minutes = "0" + minutes;
        if (seconds.length() == 1) seconds = "0" + seconds;

        String time = minutes + ":" + seconds;
        canvas.drawText(
                time,
                canvas.getWidth() - 160,
                0 + 64, // border
                CLOCK_PAINT
        );
    }

    public void render(Canvas canvas, boolean isPlayerThread) {
        canvas.drawBitmap(PANEL_BACKGROUND, 0, 0, null);

        List<Renderable> toDelete = null;
        for(int i = 0; i < mRenderablesInPlay.size(); i++) {
            Renderable renderable = mRenderablesInPlay.get(i);
            if (renderable.safeToDelete) {
                if (toDelete == null) toDelete = new ArrayList<>();
                toDelete.add(renderable);
            }
        }

        if (toDelete != null && toDelete.size() > 0) {
            mRenderablesInPlay.removeAll(toDelete);
        }

        if (BaseCardGameActivity.IS_DEBUG_MODE) {
            setupPanel(canvas);
        }
        for (int i = 0; i < mRenderablesInPlay.size(); i++) {
            Renderable renderable = mRenderablesInPlay.get(i);
            renderable.clampInPlayground(SCREEN_WIDTH, SCREEN_HEIGHT);
            mRenderablesInPlay.get(i).draw(canvas);
        }
        CardContextActionPanel.getInstance(mContext).draw(canvas);
        if (isPlayerThread) { // PlayerPanel is calling this render function
            canvas.drawCircle(
                    PULL_CARD_BUTTON_RADIUS + PULL_CARD_BUTTON_MARGIN,
                    PULL_CARD_BUTTON_RADIUS + PULL_CARD_BUTTON_MARGIN,
                    PULL_CARD_BUTTON_RADIUS,
                    Renderable.SELECTED_BUTTON_PAINT
             );
        }
    }

    public boolean drawCardFromDeck(CardDeck cardDeck, Card card){
        boolean removed = cardDeck.drawCardFromDeck(card);
        if (removed) {
            mRenderablesInPlay.add(card);
        }
        if (cardDeck.getmCards().size() == 1) {
            mRenderablesInPlay.add(cardDeck.getTopCardFromDeck());
            mRenderablesInPlay.remove(cardDeck);
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        Log.d(TAG, "onDown: " + event.toString());
        mLastCardTouched = null;

        float x = event.getX(); float y = event.getY();
        if (
                playerThread != null && // detect request card on player thread
                !BasePlayerActivity.isRequestCardHolder &&
                !BasePlayerActivity.requestingCardActively &&
                x <= (PULL_CARD_BUTTON_MARGIN+2*PULL_CARD_BUTTON_RADIUS) &&
                y <= (PULL_CARD_BUTTON_MARGIN+2*PULL_CARD_BUTTON_MARGIN)
            ) {
            BasePlayerActivity.requestingCardActively = true;
            ((BasePlayerActivity) mContext).sendRequestDrawCardMessage(MessageType.REQUEST_DRAW_CARD);
            return true;
        }

        if (CardContextActionPanel.getInstance(mContext).handleActionDown((int) x, (int) y).equals(Gesture.HANDLED)) {
            return true;
        }
        Renderable.selectedRenderableForContext = null;

        int mSize = mRenderablesInPlay.size() - 1;
        for (int i = mSize; i >= 0; i--) {
            Renderable r = mRenderablesInPlay.get(i);
            if (r.handleActionDown((int) x, (int) y).equals(Gesture.TOUCHED)) {
//                Log.d(TAG, r.getName() + " Single Tap " + r.getX() + "," + r.getY());
//                Collections.swap(mRenderablesInPlay, i, mRenderablesInPlay.size() - 1);
                break;
            }
        }
        return true;
    }

    protected void handleFling(Renderable flungRenderable) {
        Log.d(TAG, "onFling: Sending " + flungRenderable  + " to Dealer Panel");

        // Prepare broadcast message
        BroadcastCardMessage message = new BroadcastCardMessage();
        message.setCardAction(Action.play);
        if( flungRenderable instanceof Card){
            List<String> cards = new ArrayList<>();
            cards.add(flungRenderable.getName());
            message.setCards(cards);
        }else if( flungRenderable instanceof CardDeck){
            List<String> cards = new ArrayList<>();
            for (Card card: ((CardDeck) flungRenderable).getmCards()) {
                cards.add(card.getName());
            }
            message.setCards(cards);
        }
        message.setHidden(flungRenderable.isHidden());
        ((BasePlayerActivity)getContext()).prepareMessage(message);
        mRenderablesInPlay.remove(flungRenderable);
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        if (mLastCardTouched != null) {
            Log.d(TAG, "Flinged " + mLastCardTouched.getName() + " to Coords: x=" + event1.getX() + ", y=" + event1.getY());
            Log.d(TAG, "Flinged E2" + mLastCardTouched.getName() + " to Coords: x=" + event2.getX() + ", y=" + event2.getY());
            Log.d(TAG, "onFling: velocity " + velocityX + " " + velocityY);
            if ((Math.abs(velocityX) + Math.abs(velocityY) >= FLING_CARD_OUTSIDE_VELOCITY_THRESHOLD)
                    && event2.getY() <= FLING_CARD_DISTANCE_FROM_EDGE_THRESHOLD) {
                handleFling(mLastCardTouched);
                mLastCardTouched = null;
                return true;
            }

            // dealer thread can fling any card/deck to any side.
            if ((Math.abs(velocityX) + Math.abs(velocityY) >= FLING_CARD_OUTSIDE_VELOCITY_THRESHOLD)) {
                if (this instanceof DealerPanel) {
                    if (event2.getX() <= FLING_CARD_DISTANCE_FROM_EDGE_THRESHOLD ||
                            event2.getY() <= FLING_CARD_DISTANCE_FROM_EDGE_THRESHOLD ||
                            event2.getX() >= SCREEN_WIDTH - FLING_CARD_DISTANCE_FROM_EDGE_THRESHOLD ||
                            event2.getY() >= SCREEN_HEIGHT - FLING_CARD_DISTANCE_FROM_EDGE_THRESHOLD) {
                        handleFling(mLastCardTouched);
                        mLastCardTouched = null;
                        return true;
                    }
                }
            }
        }
        return true;
    }

    protected void handleLongPress(CardDeck cardDeck) {
        ((BasePlayerActivity)getContext()).inflateCardFanView(cardDeck);
    }

    @Override
    public void onLongPress(MotionEvent event) {
        Log.d(TAG, "onLongPress: " + event.toString());
        for(int i = 0; i < mRenderablesInPlay.size(); i++) {
            Renderable r = mRenderablesInPlay.get(i);
            if ((r instanceof CardDeck) && r.isTouched()) {
                // make sure it works only for deck and
                Log.d(TAG, "Long pressed " + r.getName() + " to Coords: x=" + event.getX() + ",y=" + event.getY());
                handleLongPress((CardDeck) r);
                break; // only moce the top card
            }
        }
    }


    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d(TAG, "onScroll: " + e1.toString() + e2.toString());

        Log.d(TAG, "Move: x=" + e2.getX() + ",y=" + e2.getY());
//                // the gestures
        for (Renderable r : mRenderablesInPlay) {
            if (r.isTouched()) {
                // the image was picked up and is being dragged
                Log.d(TAG, "Moving " + r.getName() + " to Coords: x=" + e2.getX() + ",y=" + e2.getY());
                r.displaceX((int) -distanceX);
                r.displaceY((int) -distanceY);

                break; // only move the top card
            }
        }

        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        Log.d(TAG, "onDoubleTap: " + event.toString());
        List<Card> cards = null;
        for (int i = 0; i < mRenderablesInPlay.size(); i++) {
            Renderable r = mRenderablesInPlay.get(i);
            if (r.handleActionDown((int) event.getX(), (int) event.getY()).equals(Gesture.TOUCHED)) {
                Collections.swap(mRenderablesInPlay, i, mRenderablesInPlay.size() - 1);
                // TODO toggle if its a card and open the top card if it's a deck
                cards = r.handleDoubleTap(event);

                if (r instanceof CardDeck && r.isSafeToDelete()) {
                    mRenderablesInPlay.remove(r);
                }

                Log.d(TAG, " Double tap on card " + r.getName());
                break;
            }
        }

        if (cards != null){
            mRenderablesInPlay.addAll(cards);
        }

        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        return true;
    }

    protected void handleSingleTapOnRenderable(Renderable r) {
        if (r instanceof Card) {
            CardContextActionPanel
                    .getInstance(mContext)
                    .show(r, CardContextActionPanel.SHOW_TRANSFER | CardContextActionPanel.SHOW_REVERSE);
        } else if (r instanceof CardDeck){
            CardContextActionPanel.getInstance(mContext).show(r,
                            CardContextActionPanel.SHOW_TRANSFER |
                            CardContextActionPanel.SHOW_REVERSE |
                            CardContextActionPanel.SHOW_SHUFFLE);
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        Log.d(TAG, "onSingleTapConfirmed: " + event.toString());
        for(Renderable r: mRenderablesInPlay) {
            if (r.handleActionDown((int) event.getX(), (int) event.getY()).equals(Gesture.TOUCHED)) {
                r.setTouched(false);
                Renderable.selectedRenderableForContext = r;
                handleSingleTapOnRenderable(r);
                break;
            }
        }
        return true;
    }

    public void onCardReceived(BroadcastCardMessage cardMessage) {
        List<String> receivedCards = cardMessage.getCards();
        if (receivedCards.size() == 0) return;
        if (receivedCards.size() == 1) {
            // TODO: check for magic card and apply attributes if still active.
            Card receivedCard;
            try {
                receivedCard = (Card) mAllCardsRes.get(receivedCards.get(0)).clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return;
            }
            receivedCard.randomizeScreenLocation(200, 200);
            receivedCard.setHidden(cardMessage.isHidden());
            mRenderablesInPlay.add(receivedCard);
        }

        else {
            CardDeck receivedCardDeck = new CardDeck(mContext, cardMessage.isHidden());
            for(String cardId: receivedCards) {
                Card card;
                try {
                    // TODO: check for magic card and apply attributes if still active.
                    card = (Card) mAllCardsRes.get(cardId).clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                    continue;
                }
                card.setHidden(cardMessage.isHidden());
                receivedCardDeck.addCardToDeck(card);
            }
            receivedCardDeck.setX(200); receivedCardDeck.setY(200);
            mRenderablesInPlay.add(receivedCardDeck);
        }

//        if (BasePlayerActivity.isRequestCardHolder) {
//            BasePlayerActivity.isRequestCardHolder = false;
//
//            // so whenever the player picks up his finger, its becomes false. Also it should be true,
//            // so a new request is not made when the figure directly slides in that area.
//            BasePlayerActivity.requestingCardActively = true;
//        }
    }

    public void discardCardsFromDeck(CardDeck cardDeck, List<Card> cards) {
        cardDeck.getmCards().removeAll(cards);
        if (cardDeck.getmCards().size() == 0) {
            mRenderablesInPlay.remove(cardDeck);
        }
        else if (cardDeck.getmCards().size() == 1) {
            mRenderablesInPlay.remove(cardDeck);
            mRenderablesInPlay.add(cardDeck.getTopCardFromDeck());
        }
    }
}

package ac.robinson.bettertogether.plugin.base.cardgame.dealer;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardContextActionPanel;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Renderable;
import ac.robinson.bettertogether.plugin.base.cardgame.player.PlayerPanel;

/**
 * Created by t-apmehr, t-sus on 4/5/2017.
 */

public class DealerPanel extends PlayerPanel {

    private static final String TAG = DealerPanel.class.getSimpleName();

    private DealerThread dealerThread;

    public DealerPanel(Context context) {
        super(context);
        mContext = context;
        dealerThread = new DealerThread(getHolder(), this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // at this point the surface is created and
        // we can safely start the game loop
        if (dealerThread != null) {
            dealerThread.setRunning(true);
            dealerThread.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Surface is being destroyed");
        // tell the thread to shut down and wait for it to finish
        // this is a clean shutdown
        tryClosingThread(dealerThread);
    }

    @Override
    protected void handleFling(Renderable flungRenderable) {
        Log.d(TAG, "handleFling: " + flungRenderable);
        if (BaseDealerActivity.requestedPlayerId != null) {
            Map<String, List<Card>> distribution = new HashMap<String, List<Card>>();
            List<Card> cards = new ArrayList<>();
            if (flungRenderable instanceof Card) {
                cards.add((Card) flungRenderable);
            } else if (flungRenderable instanceof CardDeck) {
                cards.addAll(((CardDeck)flungRenderable).getmCards());
            }
            distribution.put(BaseDealerActivity.requestedPlayerId, cards);
            ((BaseDealerActivity)mContext).handleCardDistribution(distribution, flungRenderable);
            mRenderablesInPlay.remove(flungRenderable);
        }
    }

    public void render(Canvas canvas) {
        super.render(canvas, false);
        if (BaseDealerActivity.requestedPlayerId != null) {
            canvas.drawCircle(
                    PULL_CARD_BUTTON_RADIUS + PULL_CARD_BUTTON_MARGIN,
                    PULL_CARD_BUTTON_RADIUS + PULL_CARD_BUTTON_MARGIN,
                    PULL_CARD_BUTTON_RADIUS,
                    Renderable.SELECTED_BUTTON_PAINT
            );
            canvas.drawText(
                    "Requested by: " + BaseDealerActivity.requestedPlayerId,
                    (PULL_CARD_BUTTON_MARGIN+PULL_CARD_BUTTON_RADIUS) * 2,
                    PULL_CARD_BUTTON_MARGIN+PULL_CARD_BUTTON_RADIUS,
                    TEXT_PAINT);
        }

        drawClock(canvas);
    }

    @Override
    protected void handleLongPress(CardDeck cardDeck) {
        Log.d(TAG, "handleLongPress: " + cardDeck);
        ((BaseDealerActivity)getContext()).inflateCardFanView(cardDeck);

    }

    @Override
    protected void handleSingleTapOnRenderable(Renderable r) {
//        if (r instanceof Card) {
//            CardContextActionPanel
//                    .getInstance(mContext);
//                    .show(r, CardContextActionPanel.SHOW_TRANSFER | CardContextActionPanel.SHOW_REVERSE);
//        } else
        if (r instanceof CardDeck) {
            CardContextActionPanel.getInstance(mContext).show(r,
//                    CardContextActionPanel.SHOW_TRANSFER |
                            CardContextActionPanel.SHOW_REVERSE |
                            CardContextActionPanel.SHOW_DISTRIBUTE |
                            CardContextActionPanel.SHOW_SHUFFLE);
        }
    }
}

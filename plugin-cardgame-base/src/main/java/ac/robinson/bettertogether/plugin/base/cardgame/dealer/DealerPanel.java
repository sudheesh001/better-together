package ac.robinson.bettertogether.plugin.base.cardgame.dealer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.List;

import ac.robinson.bettertogether.plugin.base.cardgame.common.BroadcastCardMessage;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardContextActionPanel;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeckStatus;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Renderable;
import ac.robinson.bettertogether.plugin.base.cardgame.player.PlayerPanel;

/**
 * Created by t-apmehr, t-sus on 4/5/2017.
 */

public class DealerPanel extends PlayerPanel {

    private static final String TAG = DealerPanel.class.getSimpleName();

    private DealerThread dealerThread;

    public DealerPanel(Context context, @NonNull CardDeck cardDeck) {
        super(context, cardDeck.getmCards());
        mContext = context;
        mRenderablesInPlay.add(cardDeck);
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
    }

    @Override
    protected void handleLongPress(CardDeck cardDeck) {
        Log.d(TAG, "handleLongPress: " + cardDeck);
    }

    @Override
    protected void handleSingleTapOnRenderable(Renderable r) {
        if (r instanceof Card) {
            CardContextActionPanel
                    .getInstance(mContext)
                    .show(r, CardContextActionPanel.SHOW_TRANSFER | CardContextActionPanel.SHOW_REVERSE);
        } else if (r instanceof CardDeck) {
            CardContextActionPanel.getInstance(mContext).show(r,
                    CardContextActionPanel.SHOW_TRANSFER |
                            CardContextActionPanel.SHOW_REVERSE |
                            CardContextActionPanel.SHOW_DISTRIBUTE |
                            CardContextActionPanel.SHOW_SHUFFLE);
        }
    }

    public void onCardReceived(BroadcastCardMessage cardMessage) {
        List<String> receivedCards = cardMessage.getCards();
        if (receivedCards.size() == 0) return;
        if (receivedCards.size() == 1) {
            Card receivedCard = mAllCardsRes.get(receivedCards.get(0));
            receivedCard.randomizeScreenLocation(200, 200);
            receivedCard.setHidden(cardMessage.isHidden());
            mRenderablesInPlay.add(receivedCard);
        }

        else {
            CardDeck receivedCardDeck = new CardDeck(mContext, cardMessage.isHidden() ? CardDeckStatus.CLOSED: CardDeckStatus.OPEN, false);
            for(String cardId: receivedCards) {
                receivedCardDeck.addCardToDeck(mAllCardsRes.get(cardId));
            }
            receivedCardDeck.setX(200); receivedCardDeck.setY(200);
            mRenderablesInPlay.add(receivedCardDeck);
        }
    }
}

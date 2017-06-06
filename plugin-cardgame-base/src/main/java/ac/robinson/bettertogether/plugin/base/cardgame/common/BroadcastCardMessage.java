package ac.robinson.bettertogether.plugin.base.cardgame.common;

import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;

/**
 * Created by t-sus on 4/8/2017.
 */

public class BroadcastCardMessage {
    private String cardFrom;
    private String cardTo;
    private Action cardAction;
    private Card mCard;

    public String getCardFrom() {
        // Get string from PlaySession AndroidID
        return cardFrom;
    }

    public void setCardFrom(String cardFrom) {
        this.cardFrom = cardFrom;
    }

    public String getCardTo() {
        // Android ID of Client/Server.
        return cardTo;
    }

    public void setCardTo(String cardTo) {
        this.cardTo = cardTo;
    }

    public Action getCardAction() {
        return cardAction;
    }

    public void setCardAction(Action cardAction) {
        this.cardAction = cardAction;
    }

    public Card getmCard() {
        return mCard;
    }

    public void setmCard(Card mCard) {
        this.mCard = mCard;
    }
}

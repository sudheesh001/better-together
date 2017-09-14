package ac.robinson.bettertogether.plugin.base.cardgame.common;

import java.util.ArrayList;
import java.util.List;

import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;
import ac.robinson.bettertogether.plugin.base.cardgame.models.MagicCard;

/**
 * Created by t-sus on 4/8/2017.
 */

public class BroadcastCardMessage {
    private String cardFrom;
    private String cardTo;
    private Action cardAction;

    private List<String> Cards;
    private List<String> currentRandomRefCard;
    private boolean isHidden;

    public BroadcastCardMessage() {
        Cards = new ArrayList<>();
        currentRandomRefCard = new ArrayList<>();
    }

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

    public List<String> getCards() {
        return Cards;
    }

    public List<String> getCurrentRandomRefCard() {
        return currentRandomRefCard;
    }

    public void addCard(Card card) {
        Cards.add(card.getName());
        if (card instanceof MagicCard &&
                ((MagicCard) card).randomCardRef != null &&
                ((MagicCard) card).randomCardRef.size() > 0) {
            Card currentCardRef = ((MagicCard) card).randomCardRef.get(((MagicCard) card).getRandomCurrIdx());
            currentRandomRefCard.add(currentCardRef.getName());
        } else {
            currentRandomRefCard.add(null);
        }
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }
}

package ac.robinson.bettertogether.plugin.base.cardgame.CardUtils;

import ac.robinson.bettertogether.plugin.base.cardgame.CardUtils.Card;
import ac.robinson.bettertogether.plugin.base.cardgame.CardUtils.CardDeck;

/**
 * Created by t-apmehr on 4/2/2017.
 */

public interface CardActions {

    public Card drawCard(Integer deckCode, boolean hidden);
    public boolean discardCard(Card card);
    public boolean showCard(Card card);

}

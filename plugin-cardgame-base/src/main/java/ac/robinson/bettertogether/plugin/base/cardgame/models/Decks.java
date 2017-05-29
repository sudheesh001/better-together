package ac.robinson.bettertogether.plugin.base.cardgame.models;

import java.util.List;

/**
 * Created by t-sus on 5/29/2017.
 */

public class Decks {
    // Private members
    private List<DeckDetail> decks;
    private Integer numberOfDecks;

    public List<DeckDetail> getDecks() {
        return decks;
    }

    public void setDecks(List<DeckDetail> decks) {
        this.decks = decks;
    }

    public Integer getNumberOfDecks() {
        return numberOfDecks;
    }

    public void setNumberOfDecks(Integer numberOfDecks) {
        this.numberOfDecks = numberOfDecks;
    }
}

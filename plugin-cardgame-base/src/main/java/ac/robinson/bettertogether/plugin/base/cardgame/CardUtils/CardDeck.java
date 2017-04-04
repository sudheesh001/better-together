/*
 * Copyright (C) 2017 The Better Together Toolkit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package ac.robinson.bettertogether.plugin.base.cardgame.CardUtils;

/**
 * Created by t-sus on 3/23/2017.
 */

import android.content.Context;

import java.lang.String;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import ac.robinson.bettertogether.plugin.base.cardgame.utils.Constants;

public class CardDeck implements CardActions{

    private Context mContext;
    // Mention the entire suite of cards.

    private List<Card> mClosedCardDeck;
    private List<Card> mOpenedCardDeck;
    private List<Card> mDiscardedCardDeck;

    private final Integer deckCount = 1; // TODO Hardcoded to a single deck for now

    // get each of the decks.
    public List<Card> getClosedCardDeck() {
        return mClosedCardDeck;
    }

    public List<Card> getOpenCardDeck() {
        return mOpenedCardDeck;
    }

    public List<Card> getDiscardedCardDeck() {
        return mDiscardedCardDeck;
    }

    // Method to add card to deck.
    public void addCardToDeck(String deck, Card mCard) {
        switch(deck) {
            case "open":
                mOpenedCardDeck.add(mCard);
                break;
            case "discarded":
                mOpenedCardDeck.add(mCard);
                break;
            case "closed":
                mDiscardedCardDeck.add(mCard);
                break;
        }
    }

    public void removeCardFromDeck(String deck, Card mCard) {
        switch(deck) {
            case "open":
                mOpenedCardDeck.remove(0);
                break;
            case "discarded":
                mDiscardedCardDeck.remove(0);
                break;
            case "closed":
                mClosedCardDeck.remove(0);
                break;
        }
    }
    
    public CardDeck(Context mContext) {

        this.mContext  = mContext;
        this.mClosedCardDeck = new ArrayList<>();
        this.mOpenedCardDeck = new ArrayList<>();
        this.mDiscardedCardDeck = new ArrayList<>();

        for (int i = 0, cardId = 1; i < deckCount ; i++) {

            for (Suits suit: Suits.values()) {

                for (CardRank rank: CardRank.values() ) {
                    Card card = new Card();
                    card.setCardId(cardId++);
                    card.setSuit(suit);
                    card.setRank(rank);
                    card.setName(rank + Constants.CONNECTOR + suit);
                    card.setHidden(true);
                    card.setBitmap(mContext.getResources().getIdentifier(card.getName(),"drawable",mContext.getPackageName()));
                    mClosedCardDeck.add(card);
                }
            }
        }

        // TODO Adding the special cards if required

    }

    // Fisher-Yates shuffle

    public String getRandomCardFromDeck() {

//        return mCardDeck.get(mCardValues.get(0)); // Sample
        return "";
    }

    public Card getTopCardFromDeck(Integer deckCode) {

        switch (deckCode) {
            case 0:
                return mClosedCardDeck.get(0);
        }
        return null;
    }

    public void shuffleCardDeck(List<Card> deck) {
        Collections.shuffle(deck);
    }

    @Override
    public Card drawCard(Integer deckCode, boolean hidden) {
        //TODO hardcoding deck code to 0. deckcode for each deck type
        deckCode = 0;

        Card drawnCard = getTopCardFromDeck(deckCode);

        drawnCard.setHidden(hidden);

        return drawnCard;
    }

    @Override
    public boolean discardCard(Card card) {
        return false;
    }

    @Override
    public boolean showCard(Card card) {
        return false;
    }
}

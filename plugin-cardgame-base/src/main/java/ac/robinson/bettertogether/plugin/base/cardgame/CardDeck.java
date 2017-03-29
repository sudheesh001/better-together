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

package ac.robinson.bettertogether.plugin.base.cardgame;

/**
 * Created by t-sus on 3/23/2017.
 */

import java.lang.String;
import java.util.List;
import java.util.Map;

public class CardDeck {

    // Mention the entire suite of cards.
    private String[] mCardSuit = {"clubs", "diamonds", "hearts", "spades"};
    private String[] mCards = {"ace", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "jack", "queen", "king"};
    private String[] mSpecialCards =  {"black_joker", "red_joker"};

    private String CONNECTOR = "_of_";
    private String EXTENSION = ".png";

    // Maintain and use these public methods
    // 1. Create an array of filename of images using private members.
    protected List<String> mCardNames;

    public Map<Integer, String> mCardDeck;
    public List<Integer> mCardValues;          // This contains the list of cards available.
    public List<Integer> mDiscardedCardValues; // Add card number to this list when the card is picked.

    // Generate the card names using mCards + '_of_' + mCardSuit + '.png' and assign
    CardDeck() {
        // Combine from ace to king of all cards for each suit and generate names.
        for (int cardCount = 0; cardCount < mCards.length; cardCount++) {
            for (int suitCount = 0; suitCount < mCardSuit.length; suitCount++) {
                String temporaryString = mCards[cardCount] + CONNECTOR + mCardSuit[suitCount] + EXTENSION;
                mCardNames.add(temporaryString);
            }
        }
        // Add special cards
        for (int specialCardCount = 0; specialCardCount < mSpecialCards.length; specialCardCount++) {
            mCardNames.add(mSpecialCards[specialCardCount]+EXTENSION);
        }
        // Contains the final card names generated as per filename.
        // Now assign them to mCardDeck to return publicly.
        int counter = 1;
        for (String cardName : mCardNames) {
            mCardDeck.put(counter++, cardName);
        }

        for (Integer cardNumber : mCardDeck.keySet()) {
            mCardValues.add(cardNumber);
        }
    }

    // Fisher-Yates shuffle

    public static <T> void shuffleList(List<T> a) {
        int n = a.size();
        for (int i = 0; i < n; i++) {
            int change = i + Random.nextInt(n - i);
            swap(a, i, change);
        }
    }

    private static <T> void swap(List<T> a, int i, int change) {
        T helper = a.get(i);
        a.set(i, a.get(change));
        a.set(change, helper);
    }

    public void cardShuffle() {
        // Shuffle the List<Integers>
        // Pick top[0]
        shuffleList(mCardValues);
    }

    public Integer getRandomCardFromDeck() {
        return 1; // Sample
    }

    public Integer getTopCardFromDeck() {
        return 1; // Sample
    }

}
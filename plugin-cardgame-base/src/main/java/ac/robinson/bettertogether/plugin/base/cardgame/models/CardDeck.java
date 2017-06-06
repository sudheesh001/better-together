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

package ac.robinson.bettertogether.plugin.base.cardgame.models;

/**
 * Created by t-sus on 3/23/2017.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import ac.robinson.bettertogether.plugin.base.cardgame.R;
import ac.robinson.bettertogether.plugin.base.cardgame.utils.Constants;

public class CardDeck extends Renderable implements CardActions{

    private static final String TAG = CardDeck.class.getSimpleName();

    private Context mContext;
    // Mention the entire suite of cards.

    private boolean hidden;

    private boolean touched;

    private List<Card> mCards;
    private Integer deckCount = 1;

    protected long startTime;

    private String name;

    public List<Card> getmCards() {
        return mCards;
    }

    // Method to add card to deck.
    public void addCardToDeck(Card mCard) {
        if (this.mCards.size() == 0) {
            this.bitmap = mCard.getBitmap(true);
        }
        this.mCards.add(mCard);
    }

    public void removeCardFromDeck(Card card) {
        if (card == mCards.get(0)) {
            // change the bitmap of the card deck to the next lower card.
            if (mCards.size() > 1) {
                this.bitmap = mCards.get(1).getBitmap(true);
            }
        }
        mCards.remove(card);
    }
    
    public CardDeck(Context mContext, CardDeckStatus deckType, boolean randomInitialize) {

        this.mContext  = mContext;
        super.setStatus(deckType);

        boolean card_hidden = false;

        switch (super.status){
            case CLOSED:
                this.bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.black_joker);
                this.bitmap = Bitmap.createScaledBitmap(this.bitmap, scaledWidth, scaledHeight, true);
                this.name = "Closed Deck";
                card_hidden = true;
                break;
            case OPEN:
                this.bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.red_joker); // TODO top card
                this.bitmap = Bitmap.createScaledBitmap(this.bitmap, scaledWidth, scaledHeight, true);
                this.name = "Open Deck";
                card_hidden = false;
                break;
            case DISCARDED:
                this.name  = "Discarded Deck";
                break;
        }
        setHidden(card_hidden);

        Random rand = new Random();
        setX(x + rand.nextInt(500) + (scaledWidth));
        setY(y + rand.nextInt(1000)+ (scaledHeight));

        this.mCards = new ArrayList<>();

        // TODO currently fixed to one but a deck can have more than one deck of cards
        if( randomInitialize ) {
            for (int i = 0, cardId = 1; i < deckCount; i++) {

                for (Suits suit : Suits.values()) {

                    for (CardRank rank : CardRank.values()) {
                        Card card = new Card();
                        card.setmContext(mContext);
                        card.setCardId(cardId++);
                        card.setSuit(suit);
                        card.setRank(rank);
                        card.setName(rank + Constants.CONNECTOR + suit);
                        card.setHidden(card_hidden);
                        card.setStatus(CardDeckStatus.NONE);
//                    card.setBitmap(BitmapFactory.decodeResource(mContext.getResources(),
//                            mContext.getResources().getIdentifier(card.getName(),"drawable",mContext.getPackageName())),
//                                   BitmapFactory.decodeResource(mContext.getResources(),
//                                            mContext.getResources().getIdentifier("card_back","drawable",mContext.getPackageName()
//                            )));
                        addCardToDeck(card);
//                        mCards.add(card);
                    }
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

        if( mCards.size() > 0) {
            return mCards.get(0);
        }

        return null;
    }

    public void shuffleCardDeck(List<Card> deck) {
        Collections.shuffle(deck);
    }


    @Override
    public boolean discardCard(Card card) {
        return false;
    }

    @Override
    public boolean showCard(Card card) {
        return false;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, x, y , null);
    }

    @Override
    public boolean isTouched() {
        return this.touched;
    }

    @Override
    public void setTouched(boolean touched) {
        this.touched = touched;
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
    public Card handleDoubleTap(MotionEvent event) {

        if( mCards.size() > 1){
            Card card =  drawCard(0, false);
            removeCardFromDeck(card);
            card.randomizeScreenLocation(this.getX(), this.getY());
            return card;
        }

        // delete self and create a new top card if number of cards left in deck is 2.
        // delete self and create a new top card if number of cards left in deck is 2.
        mCards.get(0).toggleHidden();
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Handles the {@link MotionEvent.ACTION_DOWN} event. If the event happens on the
     * bitmap surface then the touched state is set to <code>true</code> otherwise to <code>false</code>
     * @param eventX - the event's X coordinate
     * @param eventY - the event's Y coordinate
     */
    @SuppressWarnings("JavadocReference")
    public Gesture handleActionDown(int eventX, int eventY) {
        if (eventX >= (getX() - bitmap.getWidth() ) && (eventX <= (getX() + bitmap.getWidth()))) {
            if (eventY >= (getY() - bitmap.getHeight() ) && (eventY <= (getY() + bitmap.getHeight() ))) {
                setTouched(true);
                return Gesture.TOUCHED;
            } else {
                setTouched(false);
            }
        } else {
            setTouched(false);
        }
        return Gesture.NONE;

    }

}

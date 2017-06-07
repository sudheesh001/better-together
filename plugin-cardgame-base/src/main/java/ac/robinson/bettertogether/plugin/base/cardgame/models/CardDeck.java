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
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import ac.robinson.bettertogether.plugin.base.cardgame.R;
import ac.robinson.bettertogether.plugin.base.cardgame.utils.Constants;

public class CardDeck extends Renderable implements CardActions, Serializable{

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
                this.bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_back);
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

        if (facadeBitmap1 == null || facadeBitmap2 == null || facadeBitmap3 == null) {
            facadeBitmap1 = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(mContext.getResources(), R.drawable.deck_facade_1),
                    FACADE_SCALED_WIDTH, scaledHeight, true);
            facadeBitmap2 = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(mContext.getResources(), R.drawable.deck_facade_2),
                    FACADE_SCALED_WIDTH, scaledHeight, true);
            facadeBitmap3 = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(mContext.getResources(), R.drawable.deck_facade_3),
                    FACADE_SCALED_WIDTH, scaledHeight, true);
        }

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

    private static Bitmap facadeBitmap1 = null;
    private static Bitmap facadeBitmap2 = null;
    private static Bitmap facadeBitmap3 = null;

    @Override
    public boolean discardCard(Card card) {
        return false;
    }

    @Override
    public boolean showCard(Card card) {
        return false;
    }

    @Override
    public boolean drawCardFromDeck(Card card) {

        for (Card localCard: getmCards()) {
            if(localCard.getName().equals(card.getName())){ // shoudl be uuid
                removeCardFromDeck(card);
                return true;
            }
        }

        return false;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, x, y , null);
        if (this.mCards != null && this.mCards.size() > 1) {
            Bitmap facadeBitmap = facadeBitmap3;
            switch (mCards.size()) {
                case 2: facadeBitmap = facadeBitmap1; break;
                case 3: facadeBitmap = facadeBitmap2; break;
            }
            canvas.drawBitmap(facadeBitmap, x-facadeBitmap.getWidth(), y, null);
        }
        canvas.drawBitmap(bitmap, x, y , null);


        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);
        canvas.drawPoint(x, y, paint);

        if (status == CardDeckStatus.CLOSED) {
            paint.setColor(Color.RED);
        } else if (status == CardDeckStatus.OPEN) {
            paint.setColor(Color.GREEN);
        } else {
            paint.setColor(Color.BLUE);
        }

        canvas.drawPoint(x+100, y, paint);
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
    public boolean isFlinged() {
        return false;
    }

    @Override
    public Card drawTopCard(Integer deckCode, boolean hidden) {
        //TODO hardcoding deck code to 0. deckcode for each deck type
        deckCode = 0;
        Card drawnCard = getTopCardFromDeck(deckCode);
        drawnCard.setHidden(hidden);
        return drawnCard;
    }

    @Override
    public List<Card> handleDoubleTap(MotionEvent event) {

        if( mCards.size() > 2){
            Card card =  drawTopCard(0, false);
            removeCardFromDeck(card);
            card.randomizeScreenLocation(this.getX(), this.getY());
            return Collections.singletonList(card);
        }

        // delete self and create a new top card if number of cards left in deck is 2.
        this.x = -99999;
        this.y = -99999;
        this.safeToDelete = true;

//        mCards.get(0).toggleHidden();

        if (mCards.size() == 2) {
            return Arrays.asList(mCards.get(0), mCards.get(1));
        }
        return Arrays.asList(mCards.get(0));
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
        if (eventX >= (getX()) && (eventX <= (getX() + bitmap.getWidth()))) {
            if (eventY >= (getY()) && (eventY <= (getY() + bitmap.getHeight() ))) {
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

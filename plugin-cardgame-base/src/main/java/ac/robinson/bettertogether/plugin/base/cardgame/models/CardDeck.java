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
import android.util.Log;
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
    private boolean safeToDelete = false;

    private boolean touched;
    private List<Card> mCards;
    private String name;

    private static Bitmap facadeBitmap1 = null;
    private static Bitmap facadeBitmap2 = null;
    private static Bitmap facadeBitmap3 = null;


    public List<Card> getmCards() {
        return mCards;
    }

    // Method to add card to deck.
    public void addCardToDeck(Card mCard) {
        this.mCards.add(mCard);
    }

    public void removeCardFromDeck(Card card) {
        mCards.remove(card);
    }
    
    public CardDeck(Context mContext, boolean isHidden, boolean randomInitialize) {
        setHidden(isHidden);
        this.name = isHidden ? "Closed Deck" : "Open Deck";

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
        setX(getX() + rand.nextInt(500) + (scaledWidth));
        setY(getY() + rand.nextInt(1000)+ (scaledHeight));

        this.mCards = new ArrayList<>();

        // TODO currently fixed to one but a deck can have more than one deck of cards
        if( randomInitialize ) {
            final int N_DECKS = 1; // create just one deck.
            for (int i = 0, cardId = N_DECKS; i < 1; i++) {
                for (Suits suit : Suits.values()) {
                    for (CardRank rank : CardRank.values()) {
                        Card card = new Card();
                        card.setmContext(mContext);
                        card.setCardId(cardId++);
                        card.setSuit(suit);
                        card.setRank(rank);
                        card.setName(rank + Constants.CONNECTOR + suit);
                        card.setHidden(isHidden);
                        addCardToDeck(card);
                    }
                }
            }
        }
    }

    // Fisher-Yates shuffle

    public Card getTopCardFromDeck() {
        if( mCards.size() > 0) {
            Card card = mCards.get(0);
            card.setHidden(isHidden());
            return card;
        }
        return null;
    }

    void shuffleCardDeck() {
        Collections.shuffle(mCards);
    }

    @Override
    public boolean drawCardFromDeck(Card card) {
        for (Card localCard: getmCards()) {
            if(localCard.getName().equals(card.getName())) { // assuming name is uuid
                removeCardFromDeck(card);
                return true;
            }
        }
        return false;
    }

    @Override
    public Bitmap getBitmap() {
        if (mCards == null || mCards.size() == 0) {
            Log.e(TAG, "getBitmap: Trying to fetch bitmap from deck with no cards " + this);
            return null;
        }
        Card topCard = mCards.get(0);
        return topCard.getBitmap(true);
    }

    public void draw(Canvas canvas) {
        if (this.mCards == null || this.mCards.size() == 0) return;

        if (this.mCards != null && this.mCards.size() > 1) {
            Bitmap facadeBitmap = facadeBitmap3;
            switch (mCards.size()) {
                case 2: facadeBitmap = facadeBitmap1; break;
                case 3: facadeBitmap = facadeBitmap2; break;
            }
            canvas.drawBitmap(facadeBitmap, getX()-facadeBitmap.getWidth(), getY(), null);
        }

        Bitmap bitmap = getBitmap();
        canvas.drawBitmap(bitmap, getX(), getY(), null);

        if (this == Renderable.selectedRenderableForContext) {
            Bitmap alpha = bitmap.extractAlpha();
            canvas.drawBitmap(alpha, getX(), getY(), GLOW_PAINT);
        }

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);
        canvas.drawPoint(getX(), getY(), paint);

        if (isHidden()) {
            paint.setColor(Color.RED);
        } else {
            paint.setColor(Color.GREEN);
        }

        canvas.drawPoint(getX()+100, getY(), paint);
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
    public List<Card> handleDoubleTap(MotionEvent event) {

        if( mCards.size() > 2){
            Card card =  getTopCardFromDeck();
            removeCardFromDeck(card);
            card.randomizeScreenLocation(this.getX(), this.getY());
            return Collections.singletonList(card);
        }

        this.safeToDelete = true;
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
        Bitmap bitmap = getBitmap();
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

    public boolean isSafeToDelete() {
        return safeToDelete;
    }
}

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

package ac.robinson.bettertogether.plugin.base.cardgame.dealer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeckType;

public class BaseDealerActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private static final String TAG = BaseDealerActivity.class.getSimpleName();

    ImageView mDeckImage, mOpenDeckImage, mDiscardedDeckImage;

    private Context mContext;

    private GestureDetectorCompat mDetector;

    private CardDeck cardDeck, mOpenDeck,mClosedDeck,mDiscardedDeck; // FIXME harcoded to 3 but later we want any number of decks as possible in line with NUI

    List<CardDeck> mCardsDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_base_dealer);
//
        mContext = this;
//
        cardDeck = new CardDeck(mContext, CardDeckType.CLOSED);
//
//        mDeckImage = (ImageView) findViewById(R.id.deckImage);
//        mOpenDeckImage = (ImageView) findViewById(R.id.openDeckImage);
//        mDiscardedDeckImage = (ImageView) findViewById(R.id.discardedDeckImage);
//
//        mDeckImage.setImageResource(R.drawable.card_back);
//
//        mDetector = new GestureDetectorCompat(this,this);
//        // Set the gesture detector as the double tap
//        // listener.
//        mDetector.setOnDoubleTapListener(this);
//        cardDeck.shuffleCardDeck(cardDeck.getClosedCardDeck());

        mCardsDisplay = new ArrayList<>();

        mCardsDisplay.add(cardDeck);
        // requesting to turn the title OFF
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // making it full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // set our MainGamePanel as the View
        setContentView(new DealerPanel(this, mCardsDisplay));
        Log.d(TAG, "View added");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Toast.makeText(getApplicationContext(), "DOUBLE TAP",Toast.LENGTH_SHORT).show();
        Card card = cardDeck.drawCard(0,false);
        mDeckImage.setImageBitmap(card.getBitmap());
        // This opens up the card and its available. Now
        // Step 1: Move it to open deck.
        cardDeck.addCardToDeck(card);
        // Step 2: Remove card from ClosedDeck.
        cardDeck.removeCardFromDeck(card);
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {

        return false;
    }
}

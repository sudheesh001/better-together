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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeckStatus;

public class BaseDealerActivity extends AppCompatActivity {

    private static final String TAG = BaseDealerActivity.class.getSimpleName();

    ImageView mDeckImage, mOpenDeckImage, mDiscardedDeckImage;

    private Context mContext;

    private CardDeck cardDeck, mOpenDeck,mClosedDeck,mDiscardedDeck; // FIXME harcoded to 4 but later we want any number of decks as possible in line with NUI

    List<CardDeck> mCardsDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_base_dealer);
//
        mContext = this;
//
        cardDeck = new CardDeck(mContext, CardDeckStatus.CLOSED, true);
        mOpenDeck = new CardDeck(mContext, CardDeckStatus.OPEN, true);
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
        mCardsDisplay.add(mOpenDeck);
        // requesting to turn the title OFF
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // making it full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // set our MainGamePanel as the View
        setContentView(new DealerPanel(this, null, mCardsDisplay));
        Log.d(TAG, "View added");
    }
}

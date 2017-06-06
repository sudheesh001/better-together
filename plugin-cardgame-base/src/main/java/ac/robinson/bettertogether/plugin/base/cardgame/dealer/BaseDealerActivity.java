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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeckStatus;


import ac.robinson.bettertogether.api.BasePluginActivity;
import ac.robinson.bettertogether.api.messaging.BroadcastMessage;
import ac.robinson.bettertogether.plugin.base.cardgame.common.MessageHelper;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeckType;

public class BaseDealerActivity extends BasePluginActivity {

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
        // Set player type based on the activity & get player id from sharedpreferences and send discovery protocol.
        SharedPreferences prefs = getSharedPreferences("Details", MODE_PRIVATE);
        String mName = prefs.getString("Name", null);
        MessageHelper.PlayerType mPlayerType = MessageHelper.PlayerType.DEALER;
        // Now that we have name and type. Send discovery protocol
        MessageHelper m = MessageHelper.getInstance();
        sendMessage(m.Discovery(mName, mPlayerType));

        Log.d(TAG, "View added");
    }


    @Override
    protected void onMessageReceived(@NonNull BroadcastMessage message) {
        Log.d(TAG, "Message: " + message.getMessage());

        // once you get a DR ..
        // pass it to messaga helper to parse and update the connection map
        // if you get a action type then pass to MHelper to parse and do appropriate action.
        MessageHelper m = MessageHelper.getInstance();
        if (message.getType() == 999) {
            // This is the discover protocol message received.
            // 1. Update connectionMap and broadcast again.
            m.ReceivedDiscoveryMessage(message.getMessage());
            SharedPreferences prefs = getSharedPreferences("Details", MODE_PRIVATE);
            String mName = prefs.getString("Name", null);
            MessageHelper.PlayerType mPlayerType = MessageHelper.PlayerType.DEALER;
            sendMessage(m.Discovery(mName, mPlayerType));

            // TODO: Will this cause a network flood?
        }
        else {
            m.parse(message);
            m.ServerReceivedMessage();
        }
        Toast.makeText(mContext, "Player message." + message.getMessage(), Toast.LENGTH_SHORT).show();
    }
}

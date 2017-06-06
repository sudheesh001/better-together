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

package ac.robinson.bettertogether.plugin.base.cardgame.player;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.GestureDetector;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import ac.robinson.bettertogether.api.BasePluginActivity;
import ac.robinson.bettertogether.api.messaging.BroadcastMessage;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeckStatus;

public class BasePlayerActivity extends BasePluginActivity {

    private static final String TAG = BasePlayerActivity.class.getSimpleName();

    ImageView mPlayerDeck;

    private Context mContext;
    private GestureDetector mDetector;

    // Open carddeck available with the player.
    private List<Card> cardDeck;
    List<CardDeck> mCardsDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        cardDeck = new CardDeck(mContext, CardDeckStatus.OPEN, true);

        mCardsDisplay = new ArrayList<>();

        mCardsDisplay.add(new CardDeck(mContext, CardDeckStatus.OPEN, true));
        mCardsDisplay.add(new CardDeck(mContext, CardDeckStatus.CLOSED, true));

        setContentView(new PlayerPanel(this, null, mCardsDisplay));
        // setContentView(R.layout.activity_base_player);
    }

    @Override
    protected void onMessageReceived(@NonNull BroadcastMessage message) {
        // The identifier is the Card that has been selected.
        // This is the card that the user performs an action on.

    }

}

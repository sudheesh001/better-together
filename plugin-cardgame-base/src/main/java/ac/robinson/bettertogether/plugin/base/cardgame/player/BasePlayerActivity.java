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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ac.robinson.bettertogether.api.BasePluginActivity;
import ac.robinson.bettertogether.api.messaging.BroadcastMessage;
import ac.robinson.bettertogether.plugin.base.cardgame.common.BroadcastCardMessage;
import ac.robinson.bettertogether.plugin.base.cardgame.common.BroadcastCardResponses;
import ac.robinson.bettertogether.plugin.base.cardgame.common.CardPanelCallback;
import ac.robinson.bettertogether.plugin.base.cardgame.common.MessageHelper;
import ac.robinson.bettertogether.plugin.base.cardgame.common.MessageType;
import ac.robinson.bettertogether.plugin.base.cardgame.common.WheelViewFragment;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeckStatus;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Renderable;

public class BasePlayerActivity extends BasePluginActivity implements CardPanelCallback, WheelViewFragment.OnFragmentInteractionListener{


    private static final String TAG = BasePlayerActivity.class.getSimpleName();

    ImageView mPlayerDeck;

    FrameLayout parentFrame = null;
    PlayerPanel playerPanel = null;
    MainFragment mainFragment;
    WheelViewFragment wheelViewFragment = null;

    private Context mContext;
    private GestureDetector mDetector;

    // Open carddeck available with the player.
    private List<CardDeck> mCardsDisplay;

    MessageHelper messageHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        messageHelper = MessageHelper.getInstance();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        parentFrame = new FrameLayout(mContext);
        parentFrame.setId(View.generateViewId());

//        cardDeck = new CardDeck(mContext, CardDeckStatus.OPEN, true);

        mCardsDisplay = new ArrayList<>();

        mCardsDisplay.add(new CardDeck(mContext, CardDeckStatus.OPEN, true));
        mCardsDisplay.add(new CardDeck(mContext, CardDeckStatus.CLOSED, true));

        playerPanel = new PlayerPanel(this, null, mCardsDisplay);
        playerPanel.setCardPanelCallback(this);
        parentFrame.addView(playerPanel);
        //TODO add a button right now for context menu emuation

        LinearLayout linearLayout = new LinearLayout(mContext);
        Button button = new Button(mContext);
        button.setWidth(100);
        button.setText("Click Me For Wheel View");
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "Hye I was clicked");
//                        Renderable r = new CardDeck(mContext, CardDeckStatus.OPEN, true);
//                        inflateWheelView(messageHelper.getConnectionMap(), r, true);
                    }
                }
        );
        linearLayout.addView(button);

        parentFrame.addView(linearLayout);

        setContentView(parentFrame);
        // setContentView(R.layout.activity_base_player);

        // setContentView(R.layout.activity_base_player);

        MessageHelper m = MessageHelper.getInstance();
        SharedPreferences prefs = getSharedPreferences("Details", MODE_PRIVATE);
        String mName = prefs.getString("Name", null);
        MessageHelper.PlayerType mPlayerType = MessageHelper.PlayerType.PLAYER;
        sendMessage(m.Discovery(mName, mPlayerType));
    }

    @Override
    protected void onMessageReceived(@NonNull BroadcastMessage message) {
        // The identifier is the Card that has been selected.
        // This is the card that the user performs an action on.
        Log.d(TAG, "Player Gets: " + message.getMessage());
        messageHelper = MessageHelper.getInstance();

        if (message.getType() == MessageType.DISCOVER) {
            // This is the discover protocol message received.
            // 1. Update connectionMap and broadcast again.
            messageHelper.ReceivedDiscoveryMessage(message.getMessage());

            SharedPreferences prefs = getSharedPreferences("Details", MODE_PRIVATE);
            String mName = prefs.getString("Name", null);
            MessageHelper.PlayerType mPlayerType = MessageHelper.PlayerType.PLAYER;

            sendMessage(messageHelper.Discovery(mName, mPlayerType));

            // TODO: Will this cause a network flood?
        }
        else {
            messageHelper.parse(message);
            messageHelper.PlayerReceivedMessage();
        }
        Toast.makeText(mContext, "Player message." + message.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (mainFragment == null || !mainFragment.isAdded() || !mainFragment.isVisible()) {
            super.onBackPressed();
        }else if( mainFragment.isVisible()){
            getSupportFragmentManager().beginTransaction().remove(mainFragment).commit();

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    public void inflateCardFanView(CardDeck cardDeck, boolean status){

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(parentFrame.getId(), mainFragment = MainFragment.newInstance(cardDeck))
                    .commit();
    }

    public void inflateWheelView( Renderable renderable, boolean status){

        getSupportFragmentManager()
                .beginTransaction()
                .add(parentFrame.getId(), wheelViewFragment = WheelViewFragment.newInstance(renderable, messageHelper.getConnectionMap(), new WheelViewFragment.DistributionCompletedCallback() {
                    @Override
                    public void onDistributionDecided(List<String> cardDistributionPlayerSequence) {
                        getSupportFragmentManager().beginTransaction().remove(wheelViewFragment).commit();
                        wheelViewFragment = null;
                    }
                }))
                .commit();
    }

    public void getSelectedCard(CardDeck cardDeck, Card card){
        Log.d(TAG, " Got Card " + card.getName());
        playerPanel.drawCardFromDeck(cardDeck, card);
    }

    @Override
    public BroadcastCardResponses receivedAction(BroadcastCardMessage broadcastCardMessage) {
        return null;
    }


    @Override
    public void onFragmentInteraction(Uri uri) {
        return;
    }
}

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
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.Gson;

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
import ac.robinson.bettertogether.plugin.base.cardgame.utils.Constants;

public class BasePlayerActivity extends BasePluginActivity implements CardPanelCallback, WheelViewFragment.OnFragmentInteractionListener{

    private static final String TAG = BasePlayerActivity.class.getSimpleName();

    FrameLayout parentFrame = null;
    PlayerPanel playerPanel = null;
    MainFragment mainFragment;
    WheelViewFragment wheelViewFragment = null;

    private Context mContext;
    private GestureDetector mDetector;

    public static boolean requestingCardActively = false;
    public static boolean isRequestCardHolder = false;

    // Open carddeck available with the player.
//    private List<CardDeck> mCardsDisplay;

    MessageHelper messageHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        messageHelper = MessageHelper.getInstance(mContext);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        parentFrame = new FrameLayout(mContext);
        parentFrame.setId(View.generateViewId());

//        cardDeck = new CardDeck(mContext, CardDeckStatus.OPEN, true);

//        mCardsDisplay = new ArrayList<>();

//        mCardsDisplay.add(new CardDeck(mContext, CardDeckStatus.OPEN, true));
//        mCardsDisplay.add();

        playerPanel = new PlayerPanel(this, new CardDeck(mContext, true, true).getmCards());
        playerPanel.init();
//        playerPanel.setCardPanelCallback(this);
        parentFrame.addView(playerPanel);
        //TODO add a button right now for context menu emuation

        setContentView(parentFrame);
        // setContentView(R.layout.activity_base_player);

        // setContentView(R.layout.activity_base_player);

        SharedPreferences prefs = getSharedPreferences("Details", MODE_PRIVATE);
        String mName = prefs.getString(Constants.USER_ANDROID_ID, "NoNameFoundForPlayer");
        MessageHelper.PlayerType mPlayerType = MessageHelper.PlayerType.PLAYER;
        messageHelper.getConnectionMap().put(mName, mPlayerType);
        sendMessage(messageHelper.Discovery(mName, mPlayerType));
    }

    @Override
    protected void onMessageReceived(@NonNull BroadcastMessage message) {
        // The identifier is the Card that has been selected.
        // This is the card that the user performs an action on.
        Log.d(TAG, "Player Gets: " + message.getMessage());
        messageHelper = MessageHelper.getInstance(mContext);

        if (message.getType() == MessageType.DISCOVER) {
            // This is the discover protocol message received.
            // 1. Update connectionMap and broadcast again.
            boolean replyDiscoveryNeeded = messageHelper.ReceivedDiscoveryMessage(message.getMessage());

            if (replyDiscoveryNeeded) {
                SharedPreferences prefs = getSharedPreferences("Details", MODE_PRIVATE);
                String mName = prefs.getString(Constants.USER_ANDROID_ID, messageHelper.getmUser());
                MessageHelper.PlayerType mPlayerType = MessageHelper.PlayerType.PLAYER;

                sendMessage(messageHelper.Discovery(mName, mPlayerType)); // TODO: Will this cause a network flood?
            }
        } else if (message.getType() == MessageType.DEALER_TO_PLAYER) {
            if (message.getMessage() != null && !message.getMessage().isEmpty()) {
                BroadcastCardMessage receivedMessage = new Gson().fromJson(message.getMessage(), BroadcastCardMessage.class);
                if (receivedMessage.getCardTo().equals(messageHelper.getmUser())) {
                    playerPanel.onCardReceived(receivedMessage);
                }
            }
        } else if (message.getType() == MessageType.REQUEST_DRAW_CARD_ACK && BasePlayerActivity.requestingCardActively) {
            if (!messageHelper.getmUser().equals(message.getMessage())) return;
            BasePlayerActivity.requestingCardActively = false;
            BasePlayerActivity.isRequestCardHolder = true;
        } else if (message.getType() == MessageType.REQUEST_DRAW_CARD_NACK && BasePlayerActivity.requestingCardActively) {
            if (!messageHelper.getmUser().equals(message.getMessage())) return;
            BasePlayerActivity.requestingCardActively = false;
            BasePlayerActivity.isRequestCardHolder = false;
        }

        else {
            Log.e(TAG, "onMessageReceived: Ignoring message that I don't know how to handle " + message + " " + message.getMessage() + " " + message.getType());
//            Log.e(TAG, "onMessageReceived: Trying to parse extra message " + message + " " + message.getMessage() + " " + message.getType());
//            messageHelper.parse(message);
//            messageHelper.PlayerReceivedMessage();
        }
        Toast.makeText(mContext, "Player message." + message.getMessage(), Toast.LENGTH_SHORT).show();
    }

    protected void sendRequestDrawCardMessage(int messageType) {
        sendMessage(messageHelper.RequestCardMessage(messageHelper.getmUser(), messageType));
    }

    protected void prepareMessage(@NonNull BroadcastCardMessage message){
        message.setCardFrom(messageHelper.getmUser());
//        messageHelper.getConnectionMap();
        message.setCardTo(messageHelper.getDealerFromMap());
        sendMessage(messageHelper.PlayerToDealerMessage(message));
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

    public void inflateCardFanView(CardDeck cardDeck){
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(parentFrame.getId(), mainFragment = MainFragment.newInstance(cardDeck))
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

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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ac.robinson.bettertogether.api.BasePluginActivity;
import ac.robinson.bettertogether.api.messaging.BroadcastMessage;
import ac.robinson.bettertogether.plugin.base.cardgame.common.Action;
import ac.robinson.bettertogether.plugin.base.cardgame.common.BroadcastCardMessage;
import ac.robinson.bettertogether.plugin.base.cardgame.common.MessageHelper;
import ac.robinson.bettertogether.plugin.base.cardgame.common.MessageType;
import ac.robinson.bettertogether.plugin.base.cardgame.common.WheelViewFragment;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeckStatus;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Renderable;
import ac.robinson.bettertogether.plugin.base.cardgame.player.MainFragment;

public class BaseDealerActivity extends BasePluginActivity implements WheelViewFragment.OnFragmentInteractionListener {

    private static final String TAG = BaseDealerActivity.class.getSimpleName();
    FrameLayout parentFrame = null;
    MainFragment mainFragment;
    private Context mContext;
    WheelViewFragment wheelViewFragment = null;
    MessageHelper messageHelper = null;

    DealerPanel mDealerPanel;
    private CardDeck mCardDeck;
    public static String requestedPlayerId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        mCardDeck = new CardDeck(mContext, CardDeckStatus.CLOSED, true); // TODO: read this from selected card deck
        // requesting to turn the title OFF
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // making it full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        parentFrame = new FrameLayout(mContext);
        parentFrame.setId(View.generateViewId());
        mDealerPanel = new DealerPanel(this, mCardDeck);
        parentFrame.addView(mDealerPanel);
        setContentView(parentFrame);

        // Set player type based on the activity & get player id from sharedpreferences and send discovery protocol.
        SharedPreferences prefs = getSharedPreferences("Details", MODE_PRIVATE);
        String mName = prefs.getString("Name", null);
        MessageHelper.PlayerType mPlayerType = MessageHelper.PlayerType.DEALER;
        // Now that we have name and type. Send discovery protocol
        MessageHelper m = MessageHelper.getInstance(mContext);
        sendMessage(m.Discovery(mName, mPlayerType));
        messageHelper = m;

        Log.d(TAG, "View added");
    }

    public void inflateCardFanView(CardDeck cardDeck, boolean status){
        getSupportFragmentManager()
                .beginTransaction()
                .add(parentFrame.getId(), mainFragment = MainFragment.newInstance(cardDeck))
                .commit();
    }

    public void handleCardDistribution(Map<String, List<Card>> distribution, Renderable renderable) {
        for(String playerId: distribution.keySet()) {
            List<String> cardsToSend = new ArrayList<>();
            for(Card card: distribution.get(playerId)) {
                cardsToSend.add(card.getName());
            }

            if (cardsToSend.size() > 0) {
                BroadcastCardMessage message = new BroadcastCardMessage();
                message.setCardAction(Action.draw);
                message.setCards(cardsToSend);
                message.setHidden(renderable.isHidden());
                message.setCardFrom(messageHelper.getmUser());
                message.setCardTo(playerId);
                Log.d(TAG, "handleCardDistribution: Sending message " + message + " from" + messageHelper.getmUser() + " to " + playerId);
                sendMessage(messageHelper.DealerToPlayerMessage(message));
            }
        }
    }

    public void inflateWheelView(final Renderable renderable, boolean status){
        getSupportFragmentManager()
                .beginTransaction()
                .add(parentFrame.getId(), wheelViewFragment = WheelViewFragment.newInstance(renderable, messageHelper.getConnectionMap(), new WheelViewFragment.DistributionCompletedCallback() {

                    @Override
                    public void onDistributionDecided(Map<String, List<Card>> cardDistributionPlayerSequence) {
                        getSupportFragmentManager().beginTransaction().remove(wheelViewFragment).commit();
                        wheelViewFragment = null;

                        handleCardDistribution(cardDistributionPlayerSequence, renderable);
                    }
                }))
                .commit();
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
    protected void onMessageReceived(@NonNull BroadcastMessage message) {
        Log.d(TAG, "Message: " + message.getMessage());

        // once you get a DR ..
        // pass it to messaga helper to parse and update the connection map
        // if you get a action type then pass to MHelper to parse and do appropriate action.
        MessageHelper m = MessageHelper.getInstance(mContext);
        if (message.getType() == MessageType.DISCOVER) {
            // This is the discover protocol message received.
            // 1. Update connectionMap and broadcast again.
            Boolean response = m.ReceivedDiscoveryMessage(message.getMessage());
            SharedPreferences prefs = getSharedPreferences("Details", MODE_PRIVATE);
            String mName = prefs.getString("Name", null);
            MessageHelper.PlayerType mPlayerType = MessageHelper.PlayerType.DEALER;

            if( response )
                sendMessage(m.Discovery(mName, mPlayerType));

            // TODO: Will this cause a network flood?
        }
        else if (message.getType() == MessageType.PLAYER_TO_DEALER) {
            if (message.getMessage() != null && !message.getMessage().isEmpty()) {
                BroadcastCardMessage receivedMessage = new Gson().fromJson(message.getMessage(), BroadcastCardMessage.class);
                mDealerPanel.onCardReceived(receivedMessage);
            }
        } else if (message.getType() == MessageType.REQUEST_DRAW_CARD) {
            if (requestedPlayerId == null) {
                requestedPlayerId = message.getMessage();
                sendMessage(messageHelper.RequestCardMessage(requestedPlayerId, MessageType.REQUEST_DRAW_CARD_ACK));
            } else {
                sendMessage(messageHelper.RequestCardMessage(requestedPlayerId, MessageType.REQUEST_DRAW_CARD_NACK));
            }
        } else if (message.getType() == MessageType.REQUEST_DRAW_CARD_WITHDRAW) {
            if (requestedPlayerId != null && requestedPlayerId.equals(message.getMessage())) {
                requestedPlayerId = null;
            }
        }
        else {
            m.parse(message);
            m.ServerReceivedMessage();
        }
        Toast.makeText(mContext, "Player message." + message.getMessage(), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onFragmentInteraction(Uri uri) {}
}

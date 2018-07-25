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
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import ac.robinson.bettertogether.api.BasePluginActivity;
import ac.robinson.bettertogether.api.messaging.BroadcastMessage;
import ac.robinson.bettertogether.plugin.base.cardgame.common.Action;
import ac.robinson.bettertogether.plugin.base.cardgame.common.BroadcastCardMessage;
import ac.robinson.bettertogether.plugin.base.cardgame.common.MessageHelper;
import ac.robinson.bettertogether.plugin.base.cardgame.common.MessageType;
import ac.robinson.bettertogether.plugin.base.cardgame.common.WheelViewFragment;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;
import ac.robinson.bettertogether.plugin.base.cardgame.models.MarketplaceItem;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Renderable;
import ac.robinson.bettertogether.plugin.base.cardgame.player.MainFragment;
import ac.robinson.bettertogether.plugin.base.cardgame.utils.Constants;

public class BaseDealerActivity extends BasePluginActivity implements WheelViewFragment.OnFragmentInteractionListener {

    private static final String TAG = BaseDealerActivity.class.getSimpleName();
    FrameLayout parentFrame = null;
    MainFragment mainFragment;
    private Context mContext;
    WheelViewFragment wheelViewFragment = null;
    MessageHelper messageHelper = null;

    DealerPanel mDealerPanel;
    public static String requestedPlayerId = null;
    public static int SELECTED_CARD_DECK = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mContext = this;
        messageHelper = MessageHelper.getInstance(mContext);

        // requesting to turn the title OFF
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // making it full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final BaseDealerActivity that = this;

        selectCardDeckToPlay(new OnDeckSelectedCallback() {
            @Override
            public void onDeckSelected(MarketplaceItem item) {
                SELECTED_CARD_DECK = item.getId();

                parentFrame = new FrameLayout(mContext);
                parentFrame.setId(View.generateViewId());
                mDealerPanel = new DealerPanel(mContext);
                mDealerPanel.setCurrentlyPlayingCardDeck(item, true, true);
                parentFrame.addView(mDealerPanel);

                that.setContentView(parentFrame);

                // Set player type based on the activity & get player id from sharedpreferences and send discovery protocol.
                SharedPreferences prefs = that.getSharedPreferences("Details", MODE_PRIVATE);
                final String mName = prefs.getString(Constants.USER_ANDROID_ID, "NoNameFoundForDealer");
                final MessageHelper.PlayerType mPlayerType = MessageHelper.PlayerType.DEALER;
                // Now that we have name and type. Send discovery protocol
                messageHelper.getConnectionMap().put(mName, mPlayerType);
                that.sendMessage(messageHelper.Discovery(mName, mPlayerType));
                Log.d(TAG, "View added");
            }

            @Override
            public void onDismiss() {
                finish();
            }
        });
    }

    private interface OnDeckSelectedCallback {
        void onDeckSelected(MarketplaceItem item);
        void onDismiss();
    }

    private void selectCardDeckToPlay(final OnDeckSelectedCallback callback) {
        Hawk.init(this).build();
        Set<Integer> downloadedDeckIds = Hawk.get(Constants.DOWNLOADED_ITEMS_ID_KEY, new HashSet<Integer>());
        if (downloadedDeckIds.size() == 0) {
            Toast.makeText(mContext, "Please download at least 1 deck from the marketplace.", Toast.LENGTH_SHORT).show();
            callback.onDismiss();
            return;
        }
        final List<MarketplaceItem> items = new ArrayList<>();
        List<String> deckNames = new ArrayList<>();
        for(int downloadedDeckId: downloadedDeckIds) {
            MarketplaceItem item = Hawk.get(Integer.toString(downloadedDeckId), null);
            if (item != null) {
                items.add(item);
                deckNames.add(item.getName());
            }
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Select Deck to play.");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(deckNames);
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                callback.onDismiss();
            }
        });
        dialogBuilder.setCancelable(false);

        dialogBuilder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MarketplaceItem selectedCardDeck = items.get(which);
                callback.onDeckSelected(selectedCardDeck);
            }
        });
        dialogBuilder.show();
    }

    public void inflateCardFanView(CardDeck cardDeck){
        getSupportFragmentManager()
                .beginTransaction()
                .add(parentFrame.getId(), mainFragment = MainFragment.newInstance(cardDeck, null))
                .commit();
    }

    public void handleCardDistribution(Map<String, List<Card>> distribution, Renderable renderable) {
        for(String playerId: distribution.keySet()) {
            BroadcastCardMessage message = new BroadcastCardMessage();
            for(Card card: distribution.get(playerId)) {
                message.addCard(card);
            }

            if (message.getCards().size() > 0) {
                message.setCardAction(Action.draw);
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

                        if (renderable instanceof CardDeck) {
                            List<Card> cardsToDiscard = new ArrayList<Card>();
                            for(List<Card> cardsList: cardDistributionPlayerSequence.values()) {
                                for(Card card: cardsList) {
                                    cardsToDiscard.add(card);
                                }
                            }
                            mDealerPanel.discardCardsFromDeck((CardDeck) renderable, cardsToDiscard);
                        }
                    }
                }))
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (mainFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(mainFragment).commit();
            mainFragment = null;
        } else if (wheelViewFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(wheelViewFragment).commit();
            wheelViewFragment = null;
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onMessageReceived(@NonNull BroadcastMessage message) {
        Log.d(TAG, "Message: " + message.getMessage());

        // once you get a DR ..
        // pass it to messaga helper to parse and update the connection map
        // if you get a action type then pass to MHelper to parse and do appropriate action.
        final MessageHelper m = MessageHelper.getInstance(mContext);
        if (message.getType() == MessageType.DISCOVER) {
            // This is the discover protocol message received.
            // 1. Update connectionMap and broadcast again.
            Boolean response = m.ReceivedDiscoveryMessage(message.getMessage());

            if( response ){
                SharedPreferences prefs = getSharedPreferences("Details", MODE_PRIVATE);
                final String mName = prefs.getString(Constants.USER_ANDROID_ID, null);
                final MessageHelper.PlayerType mPlayerType = MessageHelper.PlayerType.DEALER;

                sendMessage(m.Discovery(mName, mPlayerType));
                sendMessage(m.UseSelectedCardDeckMessage(SELECTED_CARD_DECK));
                // TODO: Will this cause a network flood?
            }

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
            Log.e(TAG, "onMessageReceived: Ignoring message that I don't know how to handle " + message + " " + message.getMessage() + " " + message.getType());
//            Log.e(TAG, "onMessageReceived: Trying to parse extra message " + message + " " + message.getMessage() + " " + message.getType());
//            messageHelper.parse(message);
//            messageHelper.PlayerReceivedMessage();
//            m.parse(message);
//            m.ServerReceivedMessage();
//            Toast.makeText(mContext, "Player message." + message.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onFragmentInteraction(Uri uri) {}

    public void getSelectedCard(CardDeck cardDeck, Card card){
        Log.d(TAG, " Got Card " + card.getName());
        mDealerPanel.drawCardFromDeck(cardDeck, card);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // ignore orientation/keyboard change
        super.onConfigurationChanged(newConfig);
    }
}

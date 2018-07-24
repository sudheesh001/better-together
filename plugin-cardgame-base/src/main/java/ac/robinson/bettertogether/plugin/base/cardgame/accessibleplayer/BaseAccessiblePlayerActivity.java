package ac.robinson.bettertogether.plugin.base.cardgame.accessibleplayer;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ac.robinson.bettertogether.api.BasePluginActivity;
import ac.robinson.bettertogether.api.messaging.BroadcastMessage;
import ac.robinson.bettertogether.plugin.base.cardgame.R;
import ac.robinson.bettertogether.plugin.base.cardgame.common.BroadcastCardMessage;
import ac.robinson.bettertogether.plugin.base.cardgame.common.MessageHelper;
import ac.robinson.bettertogether.plugin.base.cardgame.common.MessageType;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;
import ac.robinson.bettertogether.plugin.base.cardgame.models.MarketplaceItem;
import ac.robinson.bettertogether.plugin.base.cardgame.utils.APIClient;
import ac.robinson.bettertogether.plugin.base.cardgame.utils.Constants;

public class BaseAccessiblePlayerActivity extends BasePluginActivity {

    private static final String TAG = BaseAccessiblePlayerActivity.class.getSimpleName();
    private Context mContext;

    final ActionBar actionBar = getActionBar();
    private ActionBar.TabListener actionBarTabListener;
    DeckPagerAdapter deckPagerAdapter;
    ViewPager deckPager;

    private static Integer SELECTED_CARD_DECK = null;
    static List<CardDeck> mDecks;
    private HashMap<String, Card> mAllCardsRes= new HashMap<>();  // Map of card_name -> Card.
    MessageHelper messageHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        messageHelper = MessageHelper.getInstance(mContext);

        setContentView(R.layout.activity_base_accessible_player);

        SharedPreferences prefs = getSharedPreferences("Details", MODE_PRIVATE);
        final String mName = prefs.getString(Constants.USER_ANDROID_ID, "NoNameFoundForPlayer");
        final MessageHelper.PlayerType mPlayerType = MessageHelper.PlayerType.PLAYER;
        messageHelper.getConnectionMap().put(mName, mPlayerType);

        sendMessage(messageHelper.Discovery(mName, mPlayerType));
        mDecks = new ArrayList<>();

        initDeckTabs();
        debugInfo();
    }

    void debugInfo() {
        TextView debugInfo = (TextView) findViewById(R.id.debug_info);
        Map<String, MessageHelper.PlayerType> connectionMap = messageHelper.getConnectionMap();
        if (connectionMap != null && !connectionMap.isEmpty()) {
            List<String> players = new ArrayList<>(connectionMap.keySet());
            StringBuilder playersText = new StringBuilder();

            for (String player: players) {
                playersText.append(player);
                playersText.append(' ');
            }
            debugInfo.setText(playersText);
        }
        else {
            debugInfo.setText("Empty connection map");
        }
    }

    private void initDeckTabs() {
        if (actionBar == null) return;

        deckPagerAdapter = new DeckPagerAdapter(getSupportFragmentManager());
        deckPager = (ViewPager) findViewById(R.id.pager);
        deckPager.setAdapter(deckPagerAdapter);

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBarTabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // show the given tab
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) { }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) { }
        };
    }

    void clearAllDecks() {
        mDecks.clear();
    }

    void addCardToDeck(CardDeck deck, Card card) {
        deck.addCardToDeck(card);
    }

    void addDeckToView(CardDeck cardDeck) {
        mDecks.add(cardDeck);

        if (actionBar == null) return;
        actionBar.addTab(actionBar
                        .newTab()
                        .setText("Deck " + cardDeck.getName())
                        .setTabListener(actionBarTabListener)
        );
    }

    @Override
    protected void onMessageReceived(@NonNull BroadcastMessage message) {
        // The identifier is the Card that has been selected.
        // This is the card that the user performs an action on.
        Log.d(TAG, "Player Gets: " + message.getMessage());

        if (message.getType() == MessageType.DISCOVER) {
            // This is the discover protocol message received.
            // 1. Update connectionMap and broadcast again.
            boolean replyDiscoveryNeeded = messageHelper.ReceivedDiscoveryMessage(message.getMessage());

            if (replyDiscoveryNeeded) {
                SharedPreferences prefs = getSharedPreferences("Details", MODE_PRIVATE);
                final String mName = prefs.getString(Constants.USER_ANDROID_ID, messageHelper.getmUser());
                final MessageHelper.PlayerType mPlayerType = MessageHelper.PlayerType.PLAYER;

                sendMessage(messageHelper.Discovery(mName, mPlayerType)); // TODO: Will this cause a network flood?
            }
            debugInfo();
        } else if (message.getType() == MessageType.DEALER_TO_PLAYER) {
            if (message.getMessage() != null && !message.getMessage().isEmpty()) {
                BroadcastCardMessage receivedMessage = new Gson().fromJson(message.getMessage(), BroadcastCardMessage.class);
                if (receivedMessage.getCardTo().equals(messageHelper.getmUser())) {
                    onCardReceived(receivedMessage);
                }
            }
        } else if (message.getType() == MessageType.REQUEST_DRAW_CARD_ACK) {
        } else if (message.getType() == MessageType.REQUEST_DRAW_CARD_NACK) {
        } else if (message.getType() == MessageType.USE_SELECTED_CARD_DECK && SELECTED_CARD_DECK == null) {
            SELECTED_CARD_DECK = Integer.parseInt(message.getMessage());
            Hawk.init(mContext).build();
            if (!Hawk.get(Constants.DOWNLOADED_ITEMS_ID_KEY, new HashSet<>()).contains(SELECTED_CARD_DECK)) {
                Toast.makeText(mContext, "Please download deck", Toast.LENGTH_SHORT).show();
                onBackPressed();
            } else {
                MarketplaceItem selectedDeck = Hawk.get(Integer.toString(SELECTED_CARD_DECK));
                setCurrentlyPlayingCardDeck(selectedDeck);
            }
        }

        else {
            Log.e(TAG, "onMessageReceived: Ignoring message that I don't know how to handle " + message + " " + message.getMessage() + " " + message.getType());
            Toast.makeText(mContext, "Player message." + message.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void onCardReceived(BroadcastCardMessage cardMessage) {
        List<String> receivedCards = cardMessage.getCards();
        if (receivedCards.size() == 0) return;
        if (receivedCards.size() == 1) {
            Card receivedCard;
            try {
                receivedCard = (Card) mAllCardsRes.get(receivedCards.get(0)).clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return;
            }
            addCardToDeck(mDecks.get(0), receivedCard);
            return;
        }

        CardDeck receivedCardDeck = new CardDeck(mContext, false);
        for(int i = 0; i < receivedCards.size(); i++) {
            String cardId = receivedCards.get(i);
            Card card;
            try {
                card = (Card) mAllCardsRes.get(cardId).clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                continue;
            }
            card.setHidden(false);
            receivedCardDeck.addCardToDeck(card);
        }
        receivedCardDeck.setHidden(false);
        addDeckToView(receivedCardDeck);
    }

    // NOTICE: Accessible players can only play with CardType.Normal cards.
    private void setCurrentlyPlayingCardDeck(MarketplaceItem item) {
        clearAllDecks();
        String backgroundCardUrl = APIClient.getBaseURL().concat(item.getBackground_card());
        CardDeck singleCardDecks = new CardDeck(mContext, false);

        mAllCardsRes = new HashMap<>();
        for(String cardUuid: item.getCards().keySet()) {
            MarketplaceItem.CardItem cardItem = item.getCards().get(cardUuid);
            if (cardItem.type != MarketplaceItem.CardType.NORMAL) {
                throw new RuntimeException("Accessible players can only play with CardType.Normal in this implementation.");
            }
            Card card = new Card();
            card.setmContext(mContext);
            card.setName(cardItem.uuid);
            card.setHidden(false);
            card.setFrontBitmapUrl(APIClient.getBaseURL().concat(cardItem.path));
            card.setBackBitmapUrl(backgroundCardUrl);

            mAllCardsRes.put(card.getName(), card);
            singleCardDecks.addCardToDeck(card);
            card.warmBitmapCache();
        }

        addDeckToView(singleCardDecks);
    }
}

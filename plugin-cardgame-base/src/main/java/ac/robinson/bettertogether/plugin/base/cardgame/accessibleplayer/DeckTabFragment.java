package ac.robinson.bettertogether.plugin.base.cardgame.accessibleplayer;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ac.robinson.bettertogether.plugin.base.cardgame.R;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeckTabFragment extends Fragment {

    CardDeck mDeck;
    ListView cardsListView;
    CardListAdapter cardListAdapter;

    public DeckTabFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Bundle args = getArguments();
        int deckIdx = args.getInt("deck_idx");
        mDeck = BaseAccessiblePlayerActivity.mDecks.get(deckIdx);

        View root = inflater.inflate(R.layout.fragment_deck_tab, container, false);

        cardsListView = (ListView) root.findViewById(R.id.cardsListView);
        cardListAdapter = new CardListAdapter(getContext(), R.layout.row_card_layout, mDeck.getmCards().toArray(new Card[mDeck.getmCards().size()]));
        cardsListView.setAdapter(cardListAdapter);

        cardListAdapter.setCardSendButtonPressedHandler(handler);
        return root;
    }

    CardSendButtonPressedHandler handler;
    public void setCardSendButtonPressedHandler(CardSendButtonPressedHandler handler) {
        this.handler = handler;
    }
}

class CardListAdapter extends ArrayAdapter<Card> {
    List<Card> mCards;
    Context mContext;
    CardSendButtonPressedHandler cardSendButtonPressedHandler;

    public CardListAdapter(@NonNull Context context, int resource, @NonNull Card[] objects) {
        super(context, resource, objects);
        mCards = new ArrayList<>();
        for(Card card: objects) {
            mCards.add(card);
        }
        mContext = context;
    }

    @Override
    public int getCount() {
        return mCards.size();
    }

    public List<Card> getCards() {
        return mCards;
    }

    public void changeCards(List<Card> cards) {
        mCards = cards;
        notifyDataSetChanged();
    }

    public void setCardSendButtonPressedHandler(CardSendButtonPressedHandler handler) {
        cardSendButtonPressedHandler = handler;
    }

    private Map<String, View> cachedMap = new HashMap<>();
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Card card = mCards.get(position);
//        if (cachedMap.containsKey(card.getName())) return cachedMap.get(card.getName());

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_card_layout, parent, false);

        ((TextView)rowView.findViewById(R.id.card_name)).setText(card.getTitle());
        final Button deckButton = (Button) rowView.findViewById(R.id.button_send_to_deck);
        deckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "Send To deck button pressed", Toast.LENGTH_SHORT).show();
                cardSendButtonPressedHandler.handleCardSendToDeck(card);
                mCards.remove(card);
                notifyDataSetChanged();
            }
        });
//        cachedMap.put(card.getName(), rowView);
        return rowView;
    }
}

interface CardSendButtonPressedHandler {
    void handleCardSendToDeck(Card card);
}
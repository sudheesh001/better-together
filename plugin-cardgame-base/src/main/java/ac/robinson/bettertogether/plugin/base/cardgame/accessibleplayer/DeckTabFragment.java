package ac.robinson.bettertogether.plugin.base.cardgame.accessibleplayer;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ac.robinson.bettertogether.plugin.base.cardgame.R;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeckTabFragment extends Fragment {

    CardDeck mDeck;

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

        TextView countView = (TextView) root.findViewById(R.id.count);
        countView.setText(Integer.toString(mDeck.getmCards().size()));

        return root;
    }

}

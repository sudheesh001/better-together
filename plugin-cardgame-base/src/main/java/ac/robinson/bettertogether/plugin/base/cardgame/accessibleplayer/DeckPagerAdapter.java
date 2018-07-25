package ac.robinson.bettertogether.plugin.base.cardgame.accessibleplayer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;


public class DeckPagerAdapter extends FragmentStatePagerAdapter {

    public DeckPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    CardSendButtonPressedHandler handler;
    public void setCardSendButtonPressedHandler(CardSendButtonPressedHandler handler) {
        this.handler = handler;
    }

    @Override
    public Fragment getItem(int i) {
        DeckTabFragment fragment = new DeckTabFragment();
        fragment.setCardSendButtonPressedHandler(handler);
        Bundle args = new Bundle();
        args.putInt("deck_idx", i);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return BaseAccessiblePlayerActivity.mDecks.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Deck " + BaseAccessiblePlayerActivity.mDecks.get(position).getName();
    }
}

package ac.robinson.bettertogether.plugin.base.cardgame.player;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import ac.robinson.bettertogether.plugin.base.cardgame.R;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;

/**
 * Created by t-apmehr on 6/6/2017.
 */

public class CardFanLayoutActivity extends AppCompatActivity {

    public static final String TAG = CardFanLayoutActivity.class.getSimpleName();

    MainFragment mainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_fanlayout);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.root, mainFragment = MainFragment.newInstance())
                    .commit();
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.root);
            if (fragment instanceof MainFragment) {
                mainFragment = (MainFragment) fragment;
            }
        }
    }

    public void getSelectedCard(Card card){
        Log.d(TAG, " Got Card " + card.getName());
    }

    @Override
    public void onBackPressed() {
        if (mainFragment == null || !mainFragment.isAdded() || !mainFragment.deselectIfSelected()) {
            super.onBackPressed();
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
}

package ac.robinson.bettertogether.plugin.base.cardgame.player;

import android.animation.Animator;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cleveroad.fanlayoutmanager.FanLayoutManager;
import com.cleveroad.fanlayoutmanager.FanLayoutManagerSettings;
import com.cleveroad.fanlayoutmanager.callbacks.FanChildDrawingOrderCallback;

import ac.robinson.bettertogether.plugin.base.cardgame.R;
import ac.robinson.bettertogether.plugin.base.cardgame.dealer.BaseDealerActivity;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;

/**
 * Created by t-apmehr on 6/6/2017.
 */


public class MainFragment extends Fragment {

    public static final String TAG = MainFragment.class.getSimpleName();

    private FanLayoutManager fanLayoutManager;

    private CardsAdapter adapter;
    private static CardDeck cardDeck;

    public static MainFragment newInstance(CardDeck deck) {

        cardDeck = deck;

        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get the card deck for which it is intended
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rvCards);

        FanLayoutManagerSettings fanLayoutManagerSettings = FanLayoutManagerSettings
                .newBuilder(getContext())
                .withFanRadius(true)
//                .withAngleItemBounce(10)
                .withViewHeightDp(180)
                .withViewWidthDp(125)
                .build();

        fanLayoutManager = new FanLayoutManager(getContext(), fanLayoutManagerSettings);

        recyclerView.setLayoutManager(fanLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new CardsAdapter(getContext());
        adapter.addAll(cardDeck.getmCards());

        adapter.setOnItemClickListener(new CardsAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, final View view) {
                if (fanLayoutManager.getSelectedItemPosition() != itemPosition) {
                    fanLayoutManager.switchItem(recyclerView, itemPosition);
                } else {
                    fanLayoutManager.straightenSelectedItem(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                onClick(view, fanLayoutManager.getSelectedItemPosition());
                            } else {
                                onClick(fanLayoutManager.getSelectedItemPosition());
                            }
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                }
            }
        });

        recyclerView.setAdapter(adapter);

        recyclerView.setChildDrawingOrderCallback(new FanChildDrawingOrderCallback(fanLayoutManager));
        ItemTouchHelper.SimpleCallback gestureCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        Log.d(TAG, "onMove " +  target);
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        //do things
                        Log.d(TAG, "Swipe Dir = " + direction);
                    }

                    @Override
                    public int getDragDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                        int drag =  super.getDragDirs(recyclerView, viewHolder);
                        Log.d(TAG, "Drag Dir = " + drag);
                        return drag;
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(gestureCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        (view.findViewById(R.id.logo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fanLayoutManager.collapseViews();
            }
        });
    }

    @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onClick(View view, int pos) {
//        FullInfoTabFragment fragment = FullInfoTabFragment.newInstance(adapter.getModelByPos(pos));
//
//        fragment.setSharedElementEnterTransition(new SharedTransitionSet());
//        fragment.setEnterTransition(new Fade());
//        setExitTransition(new Fade());
//        fragment.setSharedElementReturnTransition(new SharedTransitionSet());
//
//        getActivity().getSupportFragmentManager()
//                .beginTransaction()
//                .addSharedElement(view, "shared")
//                .replace(R.id.root, fragment)
//                .addToBackStack(null)
//                .commit();
        Card card = adapter.getModelByPos(pos);

        if (getActivity() instanceof BasePlayerActivity) {
            ((BasePlayerActivity)getActivity()).getSelectedCard(cardDeck, card);
        } else {
            ((BaseDealerActivity) getActivity()).getSelectedCard(cardDeck, card);
        }
        // FIXME send a local broadcast to surface view

        setExitTransition(new Fade());
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .remove(this)
                .commit();

    }

    public void onClick(int pos) {
//        FullInfoTabFragment fragment = FullInfoTabFragment.newInstance(adapter.getModelByPos(pos));
//        getActivity().getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.root, fragment)
//                .addToBackStack(null)
//                .commit();
    }

    public boolean deselectIfSelected() {
        if (fanLayoutManager.isItemSelected()) {
            fanLayoutManager.deselectItem();
            return true;
        } else {
            return false;
        }
    }
}


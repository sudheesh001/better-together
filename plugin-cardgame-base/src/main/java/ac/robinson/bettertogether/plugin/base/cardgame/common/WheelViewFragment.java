package ac.robinson.bettertogether.plugin.base.cardgame.common;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ac.robinson.bettertogether.plugin.base.cardgame.R;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Renderable;
import ac.robinson.bettertogether.plugin.base.cardgame.utils.wheelview.WheelView;
import ac.robinson.bettertogether.plugin.base.cardgame.utils.wheelview.adapter.WheelArrayAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WheelViewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WheelViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WheelViewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static Renderable renderable;
    private static Map<String , MessageHelper.PlayerType> connectionMap;
    private List<String> playerNames;
    private DistributionCompletedCallback callback;

    private OnFragmentInteractionListener mListener;

    public Map<String, List<Card>> cardDistributionSequence;

    public WheelViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param renderableObj Parameter 1.
     * @return A new instance of fragment WheelViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WheelViewFragment newInstance(Renderable renderableObj, Map<String , MessageHelper.PlayerType> coonectionMapObj, DistributionCompletedCallback callback) {
        WheelViewFragment fragment = new WheelViewFragment();
        //TODO ideally should be set through arguments
//        Bundle args = new Bundle();
//        args.putSerializable(ARG_PARAM1, (Card)renderable);
//        fragment.setArguments(args);
        renderable = renderableObj;

        connectionMap = coonectionMapObj;

        fragment.playerNames = new ArrayList<>();
        for(String playerId: connectionMap.keySet()) {
            if (connectionMap.get(playerId) == MessageHelper.PlayerType.PLAYER) {
                fragment.playerNames.add(playerId);
            }
        }
        fragment.playerNames.add(0, "OK"); // TODO: make this more elegant.

        fragment.cardDistributionSequence = new HashMap<>();
        fragment.callback = callback;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wheel_view, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        final WheelView wheelView = (WheelView) view.findViewById(R.id.wheelview);

        //create data for the adapter
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(connectionMap.size());
        for(String playerName: playerNames) {
            entries.add(WheelViewMaterialColor.random(getContext(), "\\D*_500$"));
        }

        //populate the adapter, that knows how to draw each item (as you would do with a ListAdapter)
        wheelView.setAdapter(new MaterialColorAdapter(entries, playerNames));
        wheelView.setRepeatableAdapter(false);
        wheelView.setWheelDrawableRotatable(false);
        wheelView.setWheelDrawable(renderable);

        //a listener for receiving a callback for when the item closest to the selection angle changes
        wheelView.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectListener() {
            @Override
            public void onWheelItemSelected(WheelView parent, Drawable itemDrawable, int position) {
                //get the item at this position
                Map.Entry<String, Integer> selectedEntry = ((MaterialColorAdapter) parent.getAdapter()).getItem(position);
                parent.setSelectionColor(getContrastColor(selectedEntry));
            }
        });

        wheelView.setOnWheelItemClickListener(new WheelView.OnWheelItemClickListener() {
            @Override
            public void onWheelItemClick(WheelView parent, int position, boolean isSelected) {
                if (position == 0) {
                    callback.onDistributionDecided(cardDistributionSequence);
                    return;
                }
                if (renderable == null) return;

                WheelViewDrawable drawable = ((WheelViewDrawable)((LayerDrawable)wheelView.getCacheItem(position).mDrawable).getDrawable(1));
                drawable.count++;

                Card cardToAdd;
                if (renderable instanceof CardDeck) {
                    cardToAdd = ((CardDeck) renderable).getmCards().get(0);
                } else {
                    cardToAdd = (Card) renderable;
                }
                String playerId = playerNames.get(position);
                if (!cardDistributionSequence.containsKey(playerId)) {
                    cardDistributionSequence.put(playerId, new ArrayList<Card>());
                }
                cardDistributionSequence.get(playerId).add(cardToAdd);

                wheelView.invalidate();

                if (renderable instanceof CardDeck) {
                    CardDeck deck = (CardDeck) renderable;
                    int old_x = deck.getX(); int old_y = deck.getY();
                    List<Card> discardedCards = deck.handleDoubleTap(null);
                    if (discardedCards.size() == 2) {
                        try {
                            renderable = (Card) discardedCards.get(1).clone();
                            wheelView.setWheelDrawable(renderable);
                            // since deck.x and deck.y are -99999 by the time it gets here,
                            // set the renderable x/y coordinates to the old coordinates.
                            renderable.setAbsoluteX(old_x);
                            renderable.setAbsoluteY(old_y);
                            wheelView.invalidate();
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Card card = (Card) renderable;
                    card.setX(-99999);
                    card.setY(-99999);
                    renderable = null;
                }
            }
        });

        //initialise the selection drawable with the first contrast color
//        wheelView.setSelectionColor(getContrastColor(entries.get(0)));

        /*
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //wheelView.setSelectionAngle(-wheelView.getAngleForPosition(5));
                wheelView.setMidSelected();
            }
        }, 3000); */
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private int getContrastColor(Map.Entry<String, Integer> entry) {
        String colorName = WheelViewMaterialColor.getColorName(entry);
        return WheelViewMaterialColor.getContrastColor(colorName);
    }

    static class MaterialColorAdapter extends WheelArrayAdapter<Map.Entry<String, Integer>> {

        List<String> names;
        MaterialColorAdapter(List<Map.Entry<String, Integer>> colors, List<String> names) {
            super(colors);
            this.names = names;
        }

        @Override
        public Drawable getDrawable(int position) {
            Drawable[] drawable = new Drawable[] {
                    createOvalDrawable(getItem(position).getValue()),
                    new WheelViewDrawable(names.get(position))
            };
            return new LayerDrawable(drawable);
        }

        private Drawable createOvalDrawable(int color) {
            ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
            shapeDrawable.getPaint().setColor(color);
            return shapeDrawable;
        }
    }

    public interface DistributionCompletedCallback {
        void onDistributionDecided(Map<String, List<Card>> cardDistributionPlayerSequence);
    }
}

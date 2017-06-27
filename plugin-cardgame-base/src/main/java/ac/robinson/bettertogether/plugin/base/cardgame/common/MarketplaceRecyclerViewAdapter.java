package ac.robinson.bettertogether.plugin.base.cardgame.common;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ac.robinson.bettertogether.plugin.base.cardgame.R;
import ac.robinson.bettertogether.plugin.base.cardgame.models.DeckDetail;

/**
 * Created by t-sus on 6/7/2017.
 */

public class MarketplaceRecyclerViewAdapter extends RecyclerView.Adapter<MarketplaceRecyclerViewAdapter.ViewHolder> {

    private List<DeckDetail> items;
    private Context mContext;
//    private ItemClickListener mClickListener;

    public MarketplaceRecyclerViewAdapter(Context mContext, List<DeckDetail> items){
        this.items = items;
        this.mContext = mContext;
    }



    public class ViewHolder extends RecyclerView.ViewHolder
    {

        public View layoutView;
        public TextView deckName;
        public TextView authorName;
        public ImageView deckImage;

        public ViewHolder(View v){

            super(v);
            layoutView = v;
            deckName = (TextView)v.findViewById(R.id.DeckName);
//            authorName = (TextView)v.findViewById(R.id.DeckAuthor);
            deckImage = (ImageView)v.findViewById(R.id.deckImageView);
        }

    }

    @Override
    public MarketplaceRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.card_deck_recycler_item, null);
        ViewHolder viewHolder = new ViewHolder(layoutView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position){

        holder.deckName.setText(items.get(position).getName());
//        holder.authorName.setText(items.get(position).getId().toString());
        if(items.get(position).getDeckImage() != null ){
            //TODO get image and set it as a place holder
        }
        holder.layoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TAG", "You clicked number " + position + ", which is at cell position " + items.get(position).getName());
                Intent i = new Intent(mContext, DeckHomeActivity.class);
                i.putExtra("ITEM", items.get(position));
                mContext.startActivity(i);
            }
        });
    }



    @Override
    public int getItemCount(){

        return items.size();
    }

    // convenience method for getting data at click position
    public DeckDetail getItem(int id) {
        return items.get(id);
    }

}

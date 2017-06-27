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

package ac.robinson.bettertogether.plugin.base.cardgame;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;

import com.orhanobut.hawk.Hawk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ac.robinson.bettertogether.plugin.base.cardgame.common.MarketplaceAPI;
import ac.robinson.bettertogether.plugin.base.cardgame.common.MarketplaceRecyclerViewAdapter;
import ac.robinson.bettertogether.plugin.base.cardgame.models.DeckDetail;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Decks;
import ac.robinson.bettertogether.plugin.base.cardgame.utils.APIClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaseCardGameActivity extends AppCompatActivity
//        implements MarketplaceRecyclerViewAdapter.ItemClickListener{
{

    MarketplaceAPI apiInterface; // TODO: Temporary for testing

    Map<String, String> decksInMarketplace = new HashMap<String, String>();

    private StaggeredGridLayoutManager _sGridLayoutManager;
    private RecyclerView recyclerView;
    private MarketplaceRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_card_game);

        Hawk.init(this).build();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewMarketplace);
        recyclerView.setHasFixedSize(true);

        _sGridLayoutManager = new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(_sGridLayoutManager);
        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // TODO: Uncomment this when moving the client out.
        apiInterface = APIClient.getClient().create(MarketplaceAPI.class);
        Call<Decks> call = apiInterface.getDecksFromMarketplace();

        call.enqueue(new Callback<Decks>() {
            @Override
            public void onResponse(Call<Decks> call, Response<Decks> response) {
                Decks res = response.body();
                List<DeckDetail> details = res.getDecks();

                Log.d("API", "Get Decks from Marketplace: " + response.toString());
                Log.d("Marketplace Contains", decksInMarketplace.toString());

                setupRecyclerView(details);
            }

            @Override
            public void onFailure(Call<Decks> call, Throwable t) {
                Log.d("API", "Get Decks from Marketplace Failed");
            }
        });

    }

    public void setupRecyclerView(List<DeckDetail> items){

        MarketplaceRecyclerViewAdapter rcAdapter = new MarketplaceRecyclerViewAdapter(
                this, items);
//        rcAdapter.setClickListener(this);
        recyclerView.setAdapter(rcAdapter);

    }


//    @Override
//    public void onItemClick(View view, int position) {
//        Log.i("TAG", "You clicked number " + adapter.getItem(position) + ", which is at cell position " + position);
//    }
}
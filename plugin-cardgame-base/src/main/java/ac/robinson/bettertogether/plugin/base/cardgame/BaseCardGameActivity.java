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


import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import ac.robinson.bettertogether.plugin.base.cardgame.common.MarketplaceAPI;
import ac.robinson.bettertogether.plugin.base.cardgame.utils.APIClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaseCardGameActivity extends AppCompatActivity {


    MarketplaceAPI apiInterface; // TODO: Temporary for testing

    private String mUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_card_game);

        mUser = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // TODO: Uncomment this when moving the client out.
        apiInterface = APIClient.getClient().create(MarketplaceAPI.class);
        Call call = apiInterface.getDecksFromMarketplace();

        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Log.d("API", "Get Decks from Marketplace: " + response.toString());
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d("API", "Get Decks from Marketplace Failed");
            }
        });
        // save the player to shared preferences
        SharedPreferences.Editor prefs = this.getSharedPreferences("Details", MODE_PRIVATE).edit();
        prefs.putString("Name", mUser);
        prefs.commit();

    }
}
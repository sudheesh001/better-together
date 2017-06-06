package ac.robinson.bettertogether.plugin.base.cardgame.common;

import org.json.JSONObject;

import ac.robinson.bettertogether.plugin.base.cardgame.models.Decks;
import ac.robinson.bettertogether.plugin.base.cardgame.models.MarketplaceItem;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by t-sus on 5/29/2017.
 */

public interface MarketplaceAPI {
    @GET("/api/getDecks")
    Call<Decks> getDecksFromMarketplace();

    @GET("/api/getDeck/{id}")
    Call<MarketplaceItem> getDeckById(@Path("id") String id);
}

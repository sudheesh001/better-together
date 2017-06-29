package ac.robinson.bettertogether.plugin.base.cardgame.common;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;
import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ac.robinson.bettertogether.plugin.base.cardgame.R;
import ac.robinson.bettertogether.plugin.base.cardgame.models.DeckDetail;
import ac.robinson.bettertogether.plugin.base.cardgame.models.MarketplaceItem;
import ac.robinson.bettertogether.plugin.base.cardgame.utils.APIClient;
import ac.robinson.bettertogether.plugin.base.cardgame.utils.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeckHomeActivity extends AppCompatActivity {

    private Button downloadButton;
    private TextView headerTextView;
    private ProgressBar progressBar;
    private ImageView backgroundCardImage;
    private ImageView frontCardImage;
    private Context mContext;

    private MarketplaceAPI apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_home);

        this.mContext = this;

        final DeckDetail deckDetail = (DeckDetail) getIntent().getSerializableExtra("ITEM");

        apiInterface = APIClient.getClient().create(MarketplaceAPI.class);

        progressBar = (ProgressBar)findViewById(R.id.determinateBar);
        backgroundCardImage = (ImageView)findViewById(R.id.backCardImage);
        frontCardImage = (ImageView)findViewById(R.id.firstCardImage);

        headerTextView = (TextView)findViewById(R.id.headerTextView);
        headerTextView.setText(deckDetail.getName());

        downloadButton = (Button)findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "Some Text", Toast.LENGTH_LONG).show();
                // TODO show progressing dialog till download is complete
                Call<MarketplaceItem> call = apiInterface.getDeckById(deckDetail.getId().toString());

                call.enqueue(new Callback<MarketplaceItem>() {
                    @Override
                    public void onResponse(Call<MarketplaceItem> call, Response<MarketplaceItem> response) {
                        MarketplaceItem item = response.body();

                        Set<Integer> downloadSet = Hawk.get(Constants.DOWNLOADED_ITEMS_ID_KEY, new HashSet<Integer>());
                        downloadSet.add(item.getId());
                        Hawk.put(Constants.DOWNLOADED_ITEMS_ID_KEY, downloadSet);

                        downloadImagesWithPicasso(item.getCards(), item.getBackground_card());

                        Hawk.put(item.getId().toString(),item);

                        downloadButton.setEnabled(false);
                    }

                    @Override
                    public void onFailure(Call<MarketplaceItem> call, Throwable t) {

                    }
                });

            }
        });
    }

    int mDownloadsCompleted = 0;
    public boolean downloadImagesWithPicasso(final List<String> cards, final String backgroundCard){

        progressBar.setMax(cards.size());
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);

        com.squareup.picasso.Callback counterCheckCallback = new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                mDownloadsCompleted++;
                progressBar.setProgress(mDownloadsCompleted);
                if (mDownloadsCompleted >= cards.size()) {
                    progressBar.setVisibility(View.INVISIBLE);
                    downloadButton.setText("Downloaded !!");
                }

            }

            @Override
            public void onError() {
                Log.e("Picasso", "onError: Unable to download card");
            }
        };


        for (String endUrl: cards
             ) {
            String completeUrl = APIClient.getBaseURL().concat(endUrl);

            Picasso.with(mContext).
                    load(completeUrl).
                    fetch(counterCheckCallback);

            Picasso.with(mContext).
                    load(completeUrl).
                    into(frontCardImage);

        }
        String completeUrl = APIClient.getBaseURL().concat(backgroundCard);

        Picasso.with(mContext).
                load(completeUrl).
                fetch(counterCheckCallback);


        Picasso.with(mContext).
                load(completeUrl).
                into(backgroundCardImage);


        return true;
    }


}

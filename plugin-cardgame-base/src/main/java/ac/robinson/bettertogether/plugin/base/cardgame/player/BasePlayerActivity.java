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

package ac.robinson.bettertogether.plugin.base.cardgame.player;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

import ac.robinson.bettertogether.api.BasePluginActivity;
import ac.robinson.bettertogether.api.messaging.BroadcastMessage;
import ac.robinson.bettertogether.plugin.base.cardgame.common.MessageHelper;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;
import ac.robinson.bettertogether.plugin.base.cardgame.models.CardDeck;

public class BasePlayerActivity extends BasePluginActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private static final String TAG = BasePlayerActivity.class.getSimpleName();

    ImageView mPlayerDeck;

    private Context mContext;
    private GestureDetector mDetector;

    // Open carddeck available with the player.
    private List<Card> cardDeck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(new PlayerPanel(this, cardDeck));
        // setContentView(R.layout.activity_base_player);

        MessageHelper m = MessageHelper.getInstance();
        SharedPreferences prefs = getSharedPreferences("Details", MODE_PRIVATE);
        String mName = prefs.getString("Name", null);
        MessageHelper.PlayerType mPlayerType = MessageHelper.PlayerType.PLAYER;
        sendMessage(m.Discovery(mName, mPlayerType));
    }

    @Override
    protected void onMessageReceived(@NonNull BroadcastMessage message) {
        // The identifier is the Card that has been selected.
        // This is the card that the user performs an action on.
        Log.d(TAG, "Player Gets: " + message.getMessage());
        MessageHelper m = MessageHelper.getInstance();

        if (message.getType() == 999) {
            // This is the discover protocol message received.
            // 1. Update connectionMap and broadcast again.
            m.ReceivedDiscoveryMessage(message.getMessage());
            SharedPreferences prefs = getSharedPreferences("Details", MODE_PRIVATE);
            String mName = prefs.getString("Name", null);
            MessageHelper.PlayerType mPlayerType = MessageHelper.PlayerType.PLAYER;
            sendMessage(m.Discovery(mName, mPlayerType));

            // TODO: Will this cause a network flood?
        }
        else {
            m.parse(message);
            m.PlayerReceivedMessage();
        }
        Toast.makeText(mContext, "Player message." + message.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
package ac.robinson.bettertogether.plugin.base.cardgame.common;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import ac.robinson.bettertogether.api.messaging.BroadcastMessage;
import ac.robinson.bettertogether.plugin.base.cardgame.models.Card;

/**
 * Created by t-sus on 4/8/2017.
 */

public class MessageHelper {

    private static MessageHelper mInstance = null;

    private BroadcastCardMessage message;

    private static Map<String,PlayerType> connectionMap = new HashMap<>();

    private static String mUser;

    public enum PlayerType{
        PLAYER, DEALER
    }

    public void parse(BroadcastMessage message) { // singleton
        Gson gson = new Gson();
        // Convert message.getMessage() to BroadcastCardMessage object using gson
        try {
            this.message = gson.fromJson(message.getMessage(), BroadcastCardMessage.class);
        }
        catch (NullPointerException e) {

        }
    }

    public static MessageHelper getInstance(){
        if( mInstance == null){
            mInstance = new MessageHelper();
        }

        return mInstance;
    }

    public boolean ReceivedDiscoveryMessage(String message) {
        String[] nameAndType = message.split(";");
        String name = nameAndType[0];
        PlayerType type = null;
        if(nameAndType[1] == "PLAYER") {
            type = PlayerType.PLAYER;
        }else if (nameAndType[1] == "DEALER"){
            type = PlayerType.DEALER;
        }
        if (!connectionMap.containsKey(name)) {
            connectionMap.put(name, type);
            return true;
        }
        return false;
    }

    public BroadcastMessage Discovery(String name, PlayerType type) {
        // Return Android_ID and Status
        // 1. Add the required items to the discovery for local singleton.
        if (!connectionMap.containsKey(name)) {
            // If key doesnot exist. Add it to the map
            connectionMap.put(name, type);
        }
        // 2. Trigger a broadcast
        // Discovery will use integer for discovery as 999 and will have ID;TYPE
        return new BroadcastMessage(999, name+";"+type.toString());
    }

    public void PlayerReceivedMessage() {
        Action localCardAction = this.message.getCardAction();
        String localFromUser = this.message.getCardFrom();
        String localToUser = this.message.getCardTo();
        // Now having To and From and Action in place. It's time for the player to receive this card.
        Card localCardItem = this.message.getmCard();
        // Step 1: Check if the Player is the player intended.
        // Step 2: Perform the required card action to the player list of cards.

    }

    public void ServerReceivedMessage() {
        Action localCardAction = this.message.getCardAction();
        String localFromUser = this.message.getCardFrom();
        String localToUser = this.message.getCardTo();
        // Now having To and From and Action in place. It's time for the player to receive this card.
        Card localCardItem = this.message.getmCard();
        // Step 1: Perform the required card action to the server deck of cards.

    }
}

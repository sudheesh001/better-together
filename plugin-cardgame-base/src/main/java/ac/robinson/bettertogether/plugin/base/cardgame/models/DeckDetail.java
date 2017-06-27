package ac.robinson.bettertogether.plugin.base.cardgame.models;

import java.io.Serializable;

/**
 * Created by t-sus on 5/29/2017.
 */

public class DeckDetail  implements Serializable{

    // Private members
    private String api_link;
    private Integer id;
    private String name;
    private String number_of_cards;
    private String view_link;
    private String deckImage;

    public String getDeckImage() {
        return deckImage;
    }

    public void setDeckImage(String deckImage) {
        this.deckImage = deckImage;
    }

    public String getApi_link() {
        return api_link;
    }

    public void setApi_link(String api_link) {
        this.api_link = api_link;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber_of_cards() {
        return number_of_cards;
    }

    public void setNumber_of_cards(String number_of_cards) {
        this.number_of_cards = number_of_cards;
    }

    public String getView_link() {
        return view_link;
    }

    public void setView_link(String view_link) {
        this.view_link = view_link;
    }
// Getters and Setters

}

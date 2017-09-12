package ac.robinson.bettertogether.plugin.base.cardgame.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by t-sus on 5/29/2017.
 */

public class MarketplaceItem {

    public Map<String, CardItem> getCards() {
        return cards;
    }

    public void setCards(Map<String, CardItem> cards) {
        this.cards = cards;
    }

    public static class CardItem {
        public String uuid;
        public String path;
        public CardType type;
        public HashMap<String, Object> extra;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public CardType getType() {
            return type;
        }

        public void setType(CardType type) {
            this.type = type;
        }

        public HashMap<String, Object> getExtra() {
            return extra;
        }

        public void setExtra(HashMap<String, Object> extra) {
            this.extra = extra;
        }
    }

    public enum CardType {
        NORMAL, TTL, TIMED, RANDOM
    }

    private String background_card;
    private String created_by;
    private String description;
    private String name;
    private Integer number_of_cards;
    private Integer id;
    private String rules;
    private Map<String, CardItem> cards;

    public String getBackground_card() {
        return background_card;
    }

    public void setBackground_card(String background_card) {
        this.background_card = background_card;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNumber_of_cards() {
        return number_of_cards;
    }

    public void setNumber_of_cards(Integer number_of_cards) {
        this.number_of_cards = number_of_cards;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }


}

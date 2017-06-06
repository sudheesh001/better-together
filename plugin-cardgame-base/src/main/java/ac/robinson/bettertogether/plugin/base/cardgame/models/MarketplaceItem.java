package ac.robinson.bettertogether.plugin.base.cardgame.models;

import java.util.List;

/**
 * Created by t-sus on 5/29/2017.
 */

public class MarketplaceItem {
    private String backgroundCardLink;
    private String createdBy;
    private String description;
    private String name;
    private Integer numberOfCards;
    private Integer id;
    private String rules;
    private List<String> cardLinks;

    public String getBackgroundCardLink() {
        return backgroundCardLink;
    }

    public void setBackgroundCardLink(String backgroundCardLink) {
        this.backgroundCardLink = backgroundCardLink;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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

    public Integer getNumberOfCards() {
        return numberOfCards;
    }

    public void setNumberOfCards(Integer numberOfCards) {
        this.numberOfCards = numberOfCards;
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

    public List<String> getCardLinks() {
        return cardLinks;
    }

    public void setCardLinks(List<String> cardLinks) {
        this.cardLinks = cardLinks;
    }
}

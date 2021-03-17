package com.searchpath.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ImdbResponse {

    private long total;
    private ImdbObject[] items;


    @JsonCreator
    public ImdbResponse(@JsonProperty("total") long total, @JsonProperty("items") ImdbObject[] items){
        this.total = total;
        this.items = items;
    }


    public ImdbResponse() {
    }

    public long getTotal() {
        return total;
    }

    public ImdbObject[] getItems() {
        return items;
    }
}

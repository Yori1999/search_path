package com.searchpath.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FilmResponse {

    private long total;
    private Film[] items;

    @JsonCreator
    public FilmResponse(@JsonProperty("total") long total, @JsonProperty("items") Film[] items){
        this.total = total;
        this.items = items;
    }

    public long getTotal() {
        return total;
    }

    public Film[] getItems() {
        return items;
    }
}

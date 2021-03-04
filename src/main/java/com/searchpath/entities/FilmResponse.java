package com.searchpath.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FilmResponse {

    private int total;
    private Film[] items;

    @JsonCreator
    public FilmResponse(@JsonProperty("total") int total, @JsonProperty("items") Film[] items){
        this.total = total;
        this.items = items;
    }

    public int getTotal() {
        return total;
    }

    public Film[] getItems() {
        return items;
    }
}

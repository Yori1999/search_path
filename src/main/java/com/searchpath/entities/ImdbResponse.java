package com.searchpath.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ImdbResponse {

    private long total;
    private ImdbObject[] items;
    private Map<String,Map<String,Long>> aggregations;
    private String[] suggestions;

    @JsonCreator
    public ImdbResponse(@JsonProperty("total") long total, @JsonProperty("items") ImdbObject[] items,
                        @JsonProperty("aggregations") Map<String,Map<String,Long>> aggregations,
                        @JsonProperty("suggestions") String[] suggestions){
        this.total = total;
        this.items = items;

        this.aggregations = aggregations;

        this.suggestions = suggestions;
    }

    public ImdbResponse(@JsonProperty("total") long total, @JsonProperty("items") ImdbObject[] items,
                        @JsonProperty("aggregations") Map<String,Map<String,Long>> aggregations){
        this(total, items, aggregations, null);
    }

    public ImdbResponse(){};

    public long getTotal() { return total; }

    public ImdbObject[] getItems() { return items; }

    public Map<String,Map<String,Long>> getAggregations(){ return aggregations; }

    public String[] getSuggestions(){ return suggestions; }
}
